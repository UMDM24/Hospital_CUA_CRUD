# Hospital General CUA - Gestión de Citas Médicas

## Descripción General
Aplicación móvil desarrollada nativamente para Android enfocada en la gestión de citas médicas. Permite a los pacientes agendar, consultar, modificar y cancelar sus citas, almacenando toda la información de manera segura y local mediante SQLite. Cuenta con una identidad visual propia (Azul Rey y Dorado) y reglas estrictas de validación de negocio.

---

## Tecnologías Usadas
* ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)**Lenguaje:** Kotlin
* ![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=flat&logo=android&logoColor=white)**Interfaz de Usuario (UI):** Jetpack Compose (Material Design 3)
* ![SQLite](https://img.shields.io/badge/SQLite-003B57?style=flat&logo=sqlite&logoColor=white)**Base de Datos:** SQLite (`SQLiteOpenHelper`)
* ![Navigation Compose](https://img.shields.io/badge/Navigation_Compose-073042?style=flat&logo=android&logoColor=white) **Navegación:** Navigation Compose (`NavHost`)
* ![Compose Canvas](https://img.shields.io/badge/Compose_Canvas-3DDC84?style=flat&logo=android&logoColor=white) **Gráficos:** Compose Canvas (para dibujo vectorial nativo)
* ![Android Studio](https://img.shields.io/badge/Android_Studio-3DDC84?style=flat&logo=androidstudio&logoColor=white)**Entorno de Desarrollo:** Android Studio

---

## Arquitectura y Explicación por Archivo

El proyecto está diseñado de forma modular para separar la lógica de base de datos, la interfaz gráfica y los recursos visuales.

### 1. `MainActivity.kt`
Es el núcleo de la interfaz de usuario. Gestiona toda la lógica de presentación y el flujo de pantallas.
* **Navigation (`NavHost`):** Controla el enrutamiento entre las 4 pantallas principales de la aplicación (Datos personales, Selección de Fecha/Hora, Lista de Citas y Resumen).
* **Pantallas (Composables):** Contiene la lógica visual para los formularios de entrada, validación de estado en tiempo real, e integración nativa de `DatePickerDialog` y `TimePickerDialog`.
* **Modales:** Implementa un `AlertDialog` dinámico para la edición de datos directamente desde la lista.

### 2. `AppointmentDbHelper.kt`
Gestiona la persistencia de datos de la aplicación. Hereda de `SQLiteOpenHelper`.
* Crea y versiona la base de datos `appointments_db.db`.
* Define la tabla `appointments` con las columnas: `id`, `name`, `phone`, `date`, y `time`.
* Encapsula los métodos CRUD para que la interfaz interactúe con los datos de forma segura mediante bloques `try-catch` y gestión de cursores.

### 3. `SplashScreen.kt`
Pantalla de carga inicial (Splash Screen) personalizada.
* Utiliza una corrutina (`LaunchedEffect` y `delay(3000)`) para mostrar la pantalla durante exactamente 3 segundos.
* Dibuja el logotipo del **Hospital General CUA** de forma nativa utilizando **Compose Canvas** (`drawRoundRect`, `drawRect`), garantizando que la imagen nunca pierda resolución sin importar el tamaño del dispositivo.

### 4. `Color.kt`
Centraliza la paleta de colores de la aplicación, separando la identidad visual de la lógica del código.
* Define los colores corporativos: **Azul Rey** (`#4169E1`) y **Dorado** (`#FFD700`).
* Define los colores de fondo temáticos (`HospitalBackground`, `HospitalCardBackground`) que luego se inyectan en el `HospitalColorScheme` para dar coherencia a toda la aplicación.

### 5. `build.gradle.kts (Module :app)`
Archivo de configuración de Gradle del módulo. Fue editado para incluir las dependencias modernas necesarias para la arquitectura de la app:
* Se integró la librería `androidx.navigation:navigation-compose`, indispensable para gestionar el enrutamiento de pantallas (`NavController` y `NavHost`) sin usar Fragmentos tradicionales.

---

## Explicación del CRUD

La aplicación es un sistema CRUD (Crear, Leer, Actualizar, Eliminar) completo sobre una base de datos local SQLite:

* **C (Create) - Crear Cita:** El usuario llena su información y elige fecha/hora. Al confirmar, el método `insertAppointment` usa un `ContentValues` para almacenar el nuevo registro en SQLite.
* **R (Read) - Consultar Citas:** La pantalla de lista llama a `getAllAppointments()`, el cual ejecuta una consulta (`db.query`) que recorre un cursor y mapea los resultados a una lista de variables que Jetpack Compose renderiza dinámicamente usando `LazyColumn`.
* **U (Update) - Actualizar Cita:** Al hacer clic en "Editar" en una tarjeta de cita, se abre un modal precargado con los datos del paciente. Al guardar, se ejecuta `updateAppointment`, actualizando la fila correspondiente basándose en su `ID` único.
* **D (Delete) - Eliminar Cita:** El botón rojo "Eliminar" invoca el método `deleteAppointment`, el cual ejecuta una sentencia SQL de borrado filtrando por el `ID` de la cita, refrescando instantáneamente la vista.

---

## Funciones Relevantes y Reglas de Negocio (Punto Destacado)

Uno de los mayores valores técnicos de esta aplicación es su robusta capa de validación de datos (Reglas de Negocio). La app no solo guarda datos, sino que evita activamente el error humano:

* **Restricción de Anticipación (7 Días):** El calendario nativo (`DatePickerDialog`) inyecta una fecha mínima (`minDate`) sumando 7 días a la fecha actual del sistema, impidiendo agendar citas de urgencia no permitidas por el hospital.
* **Validación de Jornada Laboral:** El reloj (`TimePickerDialog`) intercepta la selección de hora. Si el paciente selecciona un horario fuera del rango de atención **(06:00 a 18:00 hrs)**, el sistema borra la selección y emite un mensaje de error explicativo.
* **Protección de Edición:** Las mismas reglas estrictas de creación (jornada laboral, días mínimos, teléfono de exactamente 10 dígitos) se aplican de nuevo en el modal de **Actualización**. Un usuario no puede corromper los datos durante una edición.
* **Manejo de Estados con Compose:** El uso avanzado de `by remember { mutableStateOf() }` asegura que los mensajes de error desaparezcan al instante en cuanto el usuario empiece a corregir su error en los campos de texto.
