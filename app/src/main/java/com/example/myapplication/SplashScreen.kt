package com.example.myapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun SplashScreenCustom(onTimeout: () -> Unit) {
    // Temporizador de 3 segundos
    LaunchedEffect(Unit) {
        delay(3000)
        onTimeout()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HospitalIcon()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Hospital General CUA",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF4169E1) // Azul Rey
            )
        }
    }
}

@Composable
fun HospitalIcon() {
    val royalBlue = Color(0xFF4169E1) // Azul Rey
    val gold = Color(0xFFFFD700)      // Dorado
    val doorColor = MaterialTheme.colorScheme.background

    Canvas(modifier = Modifier.size(120.dp)) {
        val width = size.width
        val height = size.height

        // 1. Edificio principal (Azul Rey)
        drawRoundRect(
            color = royalBlue,
            topLeft = Offset(width * 0.15f, height * 0.3f),
            size = Size(width * 0.7f, height * 0.7f),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // 2. Cruz superior del hospital (Azul Rey)
        val crossSize = width * 0.2f
        drawRect(
            color = royalBlue,
            topLeft = Offset((width - crossSize) / 2, height * 0.1f + crossSize / 3),
            size = Size(crossSize, crossSize / 3)
        )
        drawRect(
            color = royalBlue,
            topLeft = Offset(width / 2 - crossSize / 6, height * 0.1f),
            size = Size(crossSize / 3, crossSize)
        )

        // 3. Ventanas Doradas (2 filas de 3 ventanas)
        val windowWidth = width * 0.12f
        val windowHeight = height * 0.12f
        val startX = width * 0.23f
        val startY = height * 0.4f
        val spacingX = width * 0.21f

        // Fila 1
        drawRect(color = gold, topLeft = Offset(startX, startY), size = Size(windowWidth, windowHeight))
        drawRect(color = gold, topLeft = Offset(startX + spacingX, startY), size = Size(windowWidth, windowHeight))
        drawRect(color = gold, topLeft = Offset(startX + spacingX * 2, startY), size = Size(windowWidth, windowHeight))

        // Fila 2
        drawRect(color = gold, topLeft = Offset(startX, startY + height * 0.18f), size = Size(windowWidth, windowHeight))
        drawRect(color = gold, topLeft = Offset(startX + spacingX, startY + height * 0.18f), size = Size(windowWidth, windowHeight))
        drawRect(color = gold, topLeft = Offset(startX + spacingX * 2, startY + height * 0.18f), size = Size(windowWidth, windowHeight))

        // 4. Puerta principal
        drawRect(
            color = doorColor,
            topLeft = Offset(width * 0.4f, height * 0.75f),
            size = Size(width * 0.2f, height * 0.25f)
        )
    }
}