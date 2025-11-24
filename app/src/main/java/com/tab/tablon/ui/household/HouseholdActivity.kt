package com.tab.tablon.ui.household

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tab.tablon.ui.list.ListActivity
import com.tab.tablon.ui.theme.TablonTheme

class HouseholdActivity : ComponentActivity() {

    private val householdViewModel: HouseholdViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TablonTheme {
                val householdState by householdViewModel.householdState.collectAsState()
                val context = LocalContext.current

                // Navegamos a la lista cuando la operación sea exitosa
                if (householdState.isSuccess) {
                    // Usamos LaunchedEffect para que la navegación ocurra una sola vez
                    LaunchedEffect(Unit) {
                        Toast.makeText(context, "¡Configuración completada!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, ListActivity::class.java).apply {
                            // Limpiamos el stack de actividades para que el usuario no pueda volver aquí
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    }
                }

                // Mostramos errores
                householdState.error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    householdViewModel.onErrorShown() // Limpiamos el error
                }

                HouseholdScreen(
                    isLoading = householdState.isLoading,
                    onCreateHousehold = { householdViewModel.createHousehold() },
                    onJoinHousehold = { code -> householdViewModel.joinHousehold(code) }
                )
            }
        }
    }
}
@Composable
fun HouseholdScreen(
    isLoading: Boolean,
    onCreateHousehold: () -> Unit,
    onJoinHousehold: (String) -> Unit
) {
    var invitationCode by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Únete a un Hogar", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Para empezar, crea un nuevo hogar para tu familia o únete a uno existente usando un código de invitación.",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))

            // Sección para Unirse a un Hogar
            TextField(
                value = invitationCode,
                onValueChange = { newCode: String -> invitationCode = newCode },
                label = { Text("Código de Invitación",
                    color = MaterialTheme.colorScheme.primary ) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true // Buena práctica para códigos de invitación
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onJoinHousehold(invitationCode) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Unirse al Hogar")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            // Sección para Crear un Hogar
            Text("¿No tienes un código?")
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onCreateHousehold,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear un Nuevo Hogar")
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}