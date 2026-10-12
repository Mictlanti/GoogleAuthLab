# GoogleAuthLab

Proyecto de aprendizaje de desarrollo Android nativo con Kotlin y Jetpack Compose. El objetivo es implementar un sistema de autenticación con Google y Firebase, aplicando una arquitectura por capas, inyección de dependencias, separación de responsabilidades y observación reactiva de la sesión.

## 1. Tecnologías utilizadas

- **Kotlin:** lenguaje principal del proyecto.
- **Jetpack Compose:** construcción de interfaces declarativas.
- **Material 3:** componentes visuales y sistema de colores.
- **Firebase Authentication:** autenticación con Google y de forma anónima.
- **Google Credential Manager:** obtención de credenciales de Google.
- **Kotlin Coroutines y Flow:** operaciones asíncronas y observación de cambios de sesión.
- **Hilt:** inyección de dependencias.
- **KSP:** procesamiento de código utilizado por las herramientas que lo requieren.
- **Navigation Compose:** navegación entre las pantallas de inicio de sesión y perfil.
- **Gradle:** configuración y construcción del proyecto.

## 2. Arquitectura del proyecto

El código se organiza en capas para separar la interfaz, la lógica de autenticación y el acceso a los servicios externos.

### Domain

Contiene los modelos y contratos que representan la lógica del dominio, procurando no depender directamente de Firebase ni de componentes de Android.

- `UserProfile`: modelo de usuario utilizado por la aplicación.
- `AuthRepository`: contrato que define las operaciones de autenticación y la observación de la sesión.
- `AuthRes`: resultado genérico que representa un éxito o un error.

### Data

Implementa el acceso a Firebase y adapta los datos externos a los modelos del dominio.

- `AuthManager`: encapsula las operaciones de Firebase Authentication y observa los cambios de sesión.
- `GoogleAuthClient`: gestiona la obtención del token de Google mediante Credential Manager.
- `AuthRepositoryImpl`: implementa el contrato `AuthRepository`.
- `FirebaseUser.toDomain()`: convierte un usuario de Firebase en un `UserProfile`.
- `AuthModule` y `RepositoryModule`: configuran las dependencias mediante Hilt.

### UI

Contiene la interfaz y la lógica específica de presentación.

- `LoginViewRoute`: conecta la pantalla de inicio de sesión con el ViewModel.
- `LoginViewModel`: coordina las operaciones de autenticación y expone el estado de la interfaz.
- `LoginUiState`: representa el estado actual de la interfaz, incluida la comprobación inicial de sesión.
- `LoginUiEvent`: representa efectos puntuales, como comunicar errores.
- `ProfileViewRoute`: conecta el estado del ViewModel con la pantalla de perfil.
- `ProfileView`: muestra la información de la cuenta y las opciones disponibles.
- `Navigation`: decide qué pantalla mostrar según el estado de autenticación.

> Los nombres de paquetes y archivos pueden variar según la organización final del proyecto.

## 3. Modelo de usuario

El modelo `UserProfile` representa los datos que necesita la aplicación sin exponer directamente el objeto `FirebaseUser`.

```kotlin
data class UserProfile(
    val id: String,
    val username: String,
    val email: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean
)
```

Sus propiedades representan lo siguiente:

- `id`: UID del usuario en Firebase.
- `username`: nombre de usuario disponible.
- `email`: correo electrónico, cuando está disponible.
- `photoUrl`: URL de la fotografía de perfil, cuando está disponible.
- `isAnonymous`: indica si la cuenta corresponde a una sesión anónima.

La conversión desde Firebase se realiza en la capa Data:

```kotlin
fun FirebaseUser.toDomain() = UserProfile(
    id = uid,
    username = displayName.orEmpty(),
    email = email,
    photoUrl = photoUrl?.toString(),
    isAnonymous = isAnonymous
)
```

De esta manera, la capa UI trabaja con `UserProfile` y no necesita depender directamente de las clases de Firebase.

## 4. Manejo de resultados de autenticación

Se utiliza una clase sellada genérica para representar los resultados de las operaciones:

```kotlin
sealed class AuthRes<out T> {
    data class Success<T>(
        val data: T
    ) : AuthRes<T>()

    data class Error(
        val errorMsg: String
    ) : AuthRes<Nothing>()
}
```

Esto permite distinguir entre una operación exitosa y una fallida, comunicando los resultados entre capas mediante un tipo explícito.

El uso de `Nothing` en `AuthRes.Error` permite que el error pueda utilizarse con cualquier tipo genérico sin contener un resultado exitoso.

## 5. Autenticación con Google

El flujo de inicio de sesión se divide en varias responsabilidades:

1. `GoogleAuthClient` utiliza Credential Manager para solicitar las credenciales de Google y obtener un ID token.
2. `LoginViewModel` entrega ese token a `AuthRepository`.
3. `AuthRepositoryImpl` delega la autenticación en `AuthManager`.
4. `AuthManager` crea una credencial mediante `GoogleAuthProvider` y autentica al usuario con Firebase Authentication.
5. El resultado se transforma en un `UserProfile` y se refleja en el estado de la interfaz.

El ID de cliente web configurado en los recursos de Android se utiliza para solicitar el token. No debe confundirse con el ID token obtenido durante el inicio de sesión.

## 6. Autenticación anónima

La aplicación también incluye una operación para iniciar sesión de forma anónima mediante Firebase Authentication.

Esta operación permite crear una sesión con un UID de Firebase sin exigir que el usuario se registre con Google ni proporcione un correo electrónico.

La operación se encuentra encapsulada en `AuthManager` y se expone a través de `AuthRepository`.

El modelo `UserProfile` incluye la propiedad `isAnonymous`, lo que permite distinguir las cuentas anónimas de las cuentas autenticadas con Google.

La implementación está contemplada en el código. La validación funcional debe considerarse pendiente hasta que se pruebe expresamente en la aplicación.

## 7. Inyección de dependencias con Hilt

Hilt construye las dependencias necesarias para que los componentes no tengan que instanciar manualmente todos los objetos que utilizan.

### AuthModule

Proporciona las instancias de servicios externos:

- `FirebaseAuth`, mediante `FirebaseAuth.getInstance()`.
- `GoogleAuthClient`, configurado con el ID de cliente web.

### RepositoryModule

Utiliza `@Binds` para asociar la implementación concreta con su contrato:

`AuthRepositoryImpl` → `AuthRepository`

### Flujo de dependencias

```text
LoginViewModel
   |                  \
   v                   v
AuthRepository    GoogleAuthClient
   |
   v
AuthRepositoryImpl
   |
   v
AuthManager
   |
   v
FirebaseAuth
```

Para iniciar sesión con Google, `GoogleAuthClient` obtiene el token. Posteriormente, el repositorio y `AuthManager` realizan la autenticación con Firebase.

## 8. Sesión reactiva

La aplicación observa los cambios de autenticación de Firebase en lugar de consultar al usuario actual una sola vez.

### Flujo de datos

```text
FirebaseAuth
    |
    | AuthStateListener
    v
AuthManager
    |
    | Flow<FirebaseUser?>
    v
AuthRepositoryImpl
    |
    | Flow<UserProfile?>
    v
LoginViewModel
    |
    | StateFlow<LoginUiState>
    v
Compose / Navigation
```

### Responsabilidades

- **`AuthManager`:** registra un `FirebaseAuth.AuthStateListener` y expone los cambios mediante `callbackFlow`. Cuando se cancela la colección, elimina el listener con `awaitClose`.
- **`AuthRepositoryImpl`:** transforma cada `FirebaseUser?` en un `UserProfile?` mediante `FirebaseUser.toDomain()`.
- **`LoginViewModel`:** recoge el flujo del repositorio y actualiza `LoginUiState`.
- **`Navigation`:** observa el estado para decidir si debe mostrar Login o Profile.

El flujo debe emitir también el estado inicial de la sesión. De esta manera, la aplicación puede continuar tanto si existe un usuario autenticado como si el valor actual es `null`.

### Comprobación inicial de sesión

`LoginUiState` incluye la propiedad `isCheckingSession`, cuyo valor inicial es `true`.

Cuando el ViewModel recibe la primera emisión del estado de autenticación, actualiza esta propiedad a `false`, independientemente de que exista un usuario.

Esta propiedad pertenece al estado y no a los eventos, porque representa una condición persistente que determina qué interfaz debe mostrarse mientras se comprueba la sesión.

## 9. Estado y eventos en Jetpack Compose

La interfaz utiliza mecanismos diferentes para comunicarse con el ViewModel.

### LoginUiState

Representa información que la interfaz necesita observar, como:

- `isCheckingSession`: indica si todavía se está comprobando la sesión inicial.
- `isLoading`: indica si hay una operación de autenticación en curso.
- `user`: contiene el perfil autenticado o `null` cuando no hay sesión.
- `errorMessage`: almacena un mensaje de error cuando corresponde.

El estado se expone mediante `StateFlow` y se observa desde Compose, preferentemente con `collectAsStateWithLifecycle()` en las pantallas Android.

### LoginUiEvent

Representa efectos puntuales que no deben tratarse como estado persistente.

Entre los eventos utilizados se encuentran:

- `ShowError`: comunica un error que puede mostrarse mediante un Snackbar u otro componente visual.

Si se conserva un evento de navegación en el código, este puede utilizarse para acciones puntuales de navegación que no dependan directamente del estado de sesión.

La distinción principal es que el estado describe cómo se encuentra la interfaz, mientras que un evento comunica una acción o un efecto que debe procesarse puntualmente.

En este proyecto, la navegación principal se basa en el estado reactivo de autenticación, lo que permite que la restauración de sesión y el cierre de sesión sigan la misma regla.

## 10. Navegación

El componente `Navigation` observa el estado del `LoginViewModel` mediante `collectAsStateWithLifecycle()`.

Antes de crear el `NavHost`, espera a que termine la comprobación inicial de sesión.

- Mientras `isCheckingSession` sea `true`, muestra un `CircularProgressIndicator`.
- Si `user` no es `null`, muestra `Profile`.
- Si `user` es `null`, muestra `Login`.

El comportamiento esperado es el siguiente:

| Situación | Destino |
|---|---|
| La aplicación inicia y existe una sesión | Profile |
| La aplicación inicia y no existe una sesión | Login |
| El usuario inicia sesión correctamente | Profile |
| El usuario cierra sesión | Login |

La implementación actual reinicia el estado de navegación cuando cambia la presencia de una sesión, evitando regresar a la pantalla anterior mediante el botón Atrás.

Este enfoque es adecuado para el laboratorio. En una aplicación con navegación más compleja, se podría conservar un único `NavHost` y gestionar los destinos de autenticación mediante una estrategia de navegación centralizada.

## 11. Pantalla de inicio de sesión

La interfaz de inicio de sesión se construye con Jetpack Compose y componentes de Material 3.

El botón de Google invoca el método de autenticación del ViewModel. La pantalla utiliza `LocalContext.current` para proporcionar el contexto necesario al flujo de Credential Manager.

El estado de carga permite deshabilitar temporalmente el botón y mostrar un indicador visual mientras se procesa la solicitud.

Los errores pueden comunicarse mediante `LoginUiEvent` y presentarse en la interfaz.

**Estado actual:** la vista de login está implementada y el inicio de sesión con Google se ha probado satisfactoriamente.

## 12. Pantalla de perfil

`ProfileViewRoute` recoge el estado del ViewModel y lo entrega a `ProfileView`, que se encarga de representar los datos del usuario.

La pantalla muestra:

- Fotografía de perfil, cuando está disponible.
- Nombre de usuario.
- Correo electrónico, cuando está disponible.
- Proveedor de acceso, diferenciando las cuentas anónimas de las cuentas de Google.
- Estado de la cuenta.
- Una sección de preferencias con la opción de personalización.
- Un botón para cerrar sesión.

La interfaz utiliza una `LazyColumn` y componentes reutilizables, como `UserData`, para organizar la información.

Los valores opcionales se manejan para evitar asumir que todos los usuarios tienen nombre, correo electrónico o fotografía.

### Cierre de sesión

El botón de cierre de sesión invoca al ViewModel, que delega la operación en `AuthRepository`.

El flujo esperado es:

1. El ViewModel solicita cerrar sesión.
2. El repositorio delega la operación en Firebase Authentication.
3. Firebase notifica el cambio de estado de autenticación.
4. El listener emite el nuevo estado y el repositorio lo transforma en `null`.
5. El ViewModel actualiza `LoginUiState`.
6. `Navigation` vuelve a mostrar Login.

Por ello, el botón no necesita navegar manualmente: la sesión es la fuente de verdad que determina el destino.

## 13. Estado actual y próximos pasos

### Implementado

- [x] Autenticación con Google mediante Credential Manager y Firebase Authentication.
- [x] Operación de autenticación anónima implementada en el repositorio.
- [x] Arquitectura por capas con modelos de dominio y contrato de repositorio.
- [x] Inyección de dependencias con Hilt.
- [x] Estado de interfaz mediante `StateFlow`.
- [x] Observación reactiva de la sesión de Firebase.
- [x] Comprobación de sesión al abrir la aplicación.
- [x] Navegación entre Login y Profile según el estado de autenticación.
- [x] Pantalla de perfil con datos de cuenta.
- [x] Identificación de cuentas anónimas y cuentas de Google.
- [x] Cierre de sesión conectado al ViewModel y a Firebase.

### Pendiente o ampliable

- [ ] Validar expresamente el flujo de autenticación anónima en ejecución.
- [ ] Mejorar las transiciones entre pantallas sin reiniciar todo el estado de navegación.
- [ ] Ampliar el manejo visual de errores de red y autenticación.
- [ ] Implementar la personalización que aparece en la sección Preferences.
- [ ] Ampliar la identificación de proveedores si se incorporan otros métodos de autenticación.
- [ ] Añadir pruebas unitarias para el repositorio y el ViewModel.

## 14. Objetivo de aprendizaje

Este proyecto sirve para practicar una implementación de autenticación Android con responsabilidades separadas:

- **Firebase y la capa Data** gestionan los detalles externos de autenticación.
- **Domain** define los modelos y contratos que utiliza la aplicación.
- **ViewModel** coordina las operaciones y expone el estado observable.
- **Compose** representa la interfaz a partir del estado.
- **Navigation** responde a la sesión activa para mostrar la pantalla apropiada.

El objetivo no es únicamente lograr que el login funcione, sino comprender cómo se comunican las capas, cómo mantener una interfaz coherente con el estado real de autenticación y cómo organizar un proyecto Android para facilitar su mantenimiento y evolución.
