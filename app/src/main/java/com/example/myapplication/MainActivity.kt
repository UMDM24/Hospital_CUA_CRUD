package com.example.myapplication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import java.util.*
import com.example.myapplication.ui.theme.RoyalBlue
import com.example.myapplication.ui.theme.Gold
import com.example.myapplication.ui.theme.HospitalBackground

private lateinit var dbHelper: AppointmentDbHelper


val HospitalColorScheme = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = Color.White,
    secondary = Gold,
    onSecondary = Color.Black,
    surface = Color.White,
    background = HospitalBackground
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        dbHelper = AppointmentDbHelper(this)

        setContent {
            val navController = rememberNavController()
            var showSplash by remember { mutableStateOf(true) }


            MaterialTheme(colorScheme = HospitalColorScheme) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    if (showSplash) {
                        SplashScreenCustom(onTimeout = {
                            showSplash = false
                        })
                    } else {
                        AppNavigation(navController)
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "screen1") {

        composable("screen1") {
            Screen1_PersonalData(navController)
        }

        composable("screen2/{name}/{phone}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            Screen2_DateTime(navController, name, phone)
        }

        composable("screen3") {
            Screen3_AppointmentList(navController)
        }

        composable("screen4/{name}/{phone}/{date}/{time}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            val date = backStackEntry.arguments?.getString("date")?.replace("-", "/") ?: ""
            val time = backStackEntry.arguments?.getString("time") ?: ""
            Screen4_Summary(navController, name, phone, date, time)
        }
    }
}

// --- PANTALLA 1 ---
@Composable
fun Screen1_PersonalData(navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título en Azul Rey y Negrita
        Text(
            text = "Hospital General CUA",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Agendar Nueva Cita", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Número de Teléfono") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón principal Azul Rey
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (name.isBlank()) {
                    errorMessage = "El nombre no puede estar vacío."
                } else if (phone.length != 10 || !phone.all { it.isDigit() }) {
                    errorMessage = "El teléfono debe tener exactamente 10 dígitos."
                } else {
                    errorMessage = ""
                    navController.navigate("screen2/$name/$phone")
                }
            }
        ) {
            Text("Continuar", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón secundario Dorado
        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            onClick = { navController.navigate("screen3") }
        ) {
            Text("Ver Citas Agendadas", fontWeight = FontWeight.Bold)
        }
    }
}

// --- PANTALLA 2 ---
@Composable
fun Screen2_DateTime(navController: NavHostController, name: String, phone: String) {
    val context = LocalContext.current
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            date = "$dayOfMonth/${month + 1}/$year"
            errorMessage = ""
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val minDateCalendar = Calendar.getInstance()
    minDateCalendar.add(Calendar.DAY_OF_YEAR, 7)
    datePickerDialog.datePicker.minDate = minDateCalendar.timeInMillis

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val isWithinWorkingHours = (hourOfDay in 6..17) || (hourOfDay == 18 && minute == 0)

            if (isWithinWorkingHours) {
                time = String.format("%02d:%02d", hourOfDay, minute)
                errorMessage = ""
            } else {
                time = ""
                errorMessage = "Por favor selecciona una hora en jornada laboral (06:00 a 18:00)."
            }
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título en Azul Rey y Negrita
        Text(
            text = "Selección de Fecha y Hora",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta con sutil fondo dorado para instrucciones
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Indicaciones:\n• Anticipación mínima de 7 días.\n• Horario de atención: 06:00 a 18:00 hrs.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(if (date.isEmpty()) "Seleccionar Fecha" else "Fecha: $date", color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = { timePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(if (time.isEmpty()) "Seleccionar Hora" else "Hora: $time", color = MaterialTheme.colorScheme.primary)
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (date.isEmpty() || time.isEmpty()) {
                    errorMessage = "Debes seleccionar fecha y hora válidas."
                } else {
                    val isInserted = dbHelper.insertAppointment(name, phone, date, time)
                    if (isInserted) {
                        val safeDate = date.replace("/", "-")
                        navController.navigate("screen4/$name/$phone/$safeDate/$time") {
                            popUpTo("screen1") { inclusive = false }
                        }
                    } else {
                        Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        ) {
            Text("Confirmar Cita", fontWeight = FontWeight.Bold)
        }
    }
}

// --- PANTALLA 3 ---
@Composable
fun Screen3_AppointmentList(navController: NavHostController) {
    var appointments by remember { mutableStateOf(dbHelper.getAllAppointments()) }
    var editingAppt by remember { mutableStateOf<Map<String, Any>?>(null) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(modifier = Modifier.height(32.dp))
        // Título en Azul Rey y Negrita
        Text(
            text = "Lista de Citas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (appointments.isEmpty()) {
            Text("No hay citas registradas.")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(appointments) { appt ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Nombre resaltado en negrita
                            Text("Paciente: ${appt["name"]}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Teléfono: ${appt["phone"]}")
                            Text("Fecha: ${appt["date"]} - Hora: ${appt["time"]}")

                            Spacer(modifier = Modifier.height(12.dp))

                            Row {
                                // Botón Editar en Dorado
                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    ),
                                    onClick = { editingAppt = appt }
                                ) {
                                    Text("Editar", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                // Botón Eliminar (Rojo)
                                Button(
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    onClick = {
                                        if (dbHelper.deleteAppointment(appt["id"] as Int)) {
                                            appointments = dbHelper.getAllAppointments()
                                            Toast.makeText(context, "Cita eliminada", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal para editar (Update) con Validaciones
    editingAppt?.let { appt ->
        var eName by remember { mutableStateOf(appt["name"] as String) }
        var ePhone by remember { mutableStateOf(appt["phone"] as String) }
        var eDate by remember { mutableStateOf(appt["date"] as String) }
        var eTime by remember { mutableStateOf(appt["time"] as String) }
        var editError by remember { mutableStateOf("") }

        val calendar = Calendar.getInstance()


        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                eDate = "$dayOfMonth/${month + 1}/$year"
                editError = ""
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        val minDateCalendar = Calendar.getInstance()
        minDateCalendar.add(Calendar.DAY_OF_YEAR, 7)
        datePickerDialog.datePicker.minDate = minDateCalendar.timeInMillis

        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val isWithinWorkingHours = (hourOfDay in 6..17) || (hourOfDay == 18 && minute == 0)
                if (isWithinWorkingHours) {
                    eTime = String.format("%02d:%02d", hourOfDay, minute)
                    editError = ""
                } else {
                    editError = "La hora debe estar entre las 06:00 y las 18:00 hrs."
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )

        AlertDialog(
            onDismissRequest = { editingAppt = null },
            title = { Text("Editar Cita", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = eName,
                        onValueChange = {
                            eName = it
                            editError = ""
                        },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ePhone,
                        onValueChange = {
                            ePhone = it
                            editError = ""
                        },
                        label = { Text("Teléfono") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))


                    OutlinedButton(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Fecha: $eDate", color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { timePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Hora: $eTime", color = MaterialTheme.colorScheme.primary)
                    }


                    if (editError.isNotEmpty()) {
                        Text(
                            text = editError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {

                    if (eName.isBlank()) {
                        editError = "El nombre no puede estar vacío."
                    } else if (ePhone.length != 10 || !ePhone.all { it.isDigit() }) {
                        editError = "El teléfono debe tener 10 dígitos."
                    } else if (eDate.isBlank() || eTime.isBlank()) {
                        editError = "Fecha y hora son obligatorias."
                    } else {
                        editError = ""
                        dbHelper.updateAppointment(appt["id"] as Int, eName, ePhone, eDate, eTime)
                        appointments = dbHelper.getAllAppointments()
                        editingAppt = null
                        Toast.makeText(context, "Cita actualizada", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { editingAppt = null }) { Text("Cancelar") }
            }
        )
    }
}

// --- PANTALLA 4 ---
@Composable
fun Screen4_Summary(navController: NavHostController, name: String, phone: String, date: String, time: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono Calendario en Dorado
        Icon(
            Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Título de Confirmación en Azul Rey y Negrita
        Text(
            text = "¡Cita Confirmada!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Detalles del Paciente", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.secondary)
                Text("Nombre: $name", modifier = Modifier.padding(vertical = 4.dp))
                Text("Teléfono: $phone", modifier = Modifier.padding(vertical = 4.dp))
                Text("Fecha: $date", modifier = Modifier.padding(vertical = 4.dp))
                Text("Hora: $time", modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate("screen1") { popUpTo(0) } }
        ) {
            Text("Volver al Inicio", fontWeight = FontWeight.Bold)
        }
    }
}