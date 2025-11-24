package com.tab.tablon.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.tab.tablon.data.secure.CredentialsManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tab.tablon.ui.household.HouseholdActivity // <-- IMPORTANTE: Importar la nueva Activity
import com.tab.tablon.ui.list.ListActivity
import com.tab.tablon.ui.theme.TablonTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Fingerprint
class AuthActivity : FragmentActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(application)
    }

    //private val Icons.Filled.Fingerprint: ImageVector
    //private val credentialsManager by lazy { CredentialsManager(applicationContext) }
    private lateinit var credentialsManager: CredentialsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        credentialsManager = CredentialsManager(applicationContext)
        setContent {
            TablonTheme {
                val authState by authViewModel.authState.collectAsState()
                val context = LocalContext.current
                val canAuthenticate = credentialsManager.hasCredentials()

                // --- CAMBIO CLAVE: Lógica de Navegación ---
                // Usamos LaunchedEffect para manejar la navegación como un evento de una sola vez.
                LaunchedEffect(key1 = authState.navigation) {
                    when (authState.navigation) {
                        AuthNavigation.GO_TO_HOME_SCREEN -> {
                            Toast.makeText(context, "¡Bienvenido/a de nuevo!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(context, ListActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            context.startActivity(intent)
                        }
                        AuthNavigation.GO_TO_ONBOARDING -> {
                            Toast.makeText(context, "¡Registro exitoso! Configura tu hogar.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(context, HouseholdActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            context.startActivity(intent)
                        }
                        null -> {
                            // No hacer nada
                        }
                    }
                    // Reseteamos el evento de navegación para que no se dispare de nuevo
                    authState.navigation?.let { authViewModel.onNavigationHandled() }
                }

                AuthScreen(
                    authState = authState,
                    canAuthenticateWithBiometrics = canAuthenticate, // Le decimos a la UI si mostrar el botón
                    onLogin = { email, password ->
                        authViewModel.login(email, password)
                    },
                    onRegister = { email, password ->
                        authViewModel.register(email, password)
                    },
                    onBiometricLoginRequested = {
                        showBiometricPrompt() // Lanzamos el diálogo de huella
                    }
                )
            }
        }
    }
    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Inicio de Sesión Biométrico")
            .setSubtitle("Usa tu huella para iniciar sesión en Tablón")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // ¡Éxito! La huella es correcta. Ahora iniciamos sesión en segundo plano.
                    val email = credentialsManager.getEmail()
                    val password = credentialsManager.getPassword()
                    if (email != null && password != null) {
                        authViewModel.login(email, password)
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Error de autenticación: $errString", Toast.LENGTH_SHORT).show()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }
}

// Enum para controlar el modo de la pantalla
enum class AuthMode {
    LOGIN, REGISTER
}

@Composable
fun AuthScreen(
    authState: AuthState,
    canAuthenticateWithBiometrics: Boolean, // <-- NUEVO
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onBiometricLoginRequested: () -> Unit // <-- NUEVO
) {
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Mostramos un Toast si hay un error
    LaunchedEffect(authState.error) {
        authState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }
    LaunchedEffect(canAuthenticateWithBiometrics) {
        if (canAuthenticateWithBiometrics) {
            onBiometricLoginRequested()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val title = if (authMode == AuthMode.LOGIN) "Iniciar Sesión" else "Crear Cuenta"
            Text(text = title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))

            // Campo de Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Botón de Acción Principal (Login o Registrarse)
            Button(
                onClick = {
                    if (authMode == AuthMode.LOGIN) {
                        onLogin(email, password)
                    } else {
                        onRegister(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !authState.isLoading // Deshabilitado mientras carga
            ) {
                Text(text = title)
            }
            // --- NUEVO BOTÓN BIOMÉTRICO ---
            if (canAuthenticateWithBiometrics) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onBiometricLoginRequested,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // --- ¡NUEVO ICONO AÑADIDO! ---
                    Icon(
                        // --- USAMOS UN ICONO BÁSICO Y FIABLE ---
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Icono de Prueba", // Cambiamos la descripción
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing)) // Espacio estándar
                    Text("Iniciar Sesión con Huella")
                }
            }
            //Spacer(modifier = Modifier.height(8.dp))
            // --- CAMBIO: Hemos movido el toggle aquí arriba ---
            Spacer(modifier = Modifier.height(24.dp))
            AuthModeToggle(
                authMode = authMode,
                onToggle = { newMode -> authMode = newMode }
            )

            // Indicador de Carga
            if (authState.isLoading) {
                // Añadimos un espacio para que no se superponga con el toggle
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            //Spacer(modifier = Modifier.weight(1f)) // Empuja el siguiente texto hacia abajo

            // Botón para cambiar de modo
            /*key(authMode) {
                AuthModeToggle(
                    authMode = authMode,
                    onToggle = { newMode -> authMode = newMode }
                )
            }*/
        }
    }
}

@Composable
fun AuthModeToggle(authMode: AuthMode, onToggle: (AuthMode) -> Unit) {
    val (text, actionText) = if (authMode == AuthMode.LOGIN) {
        "¿No tienes una cuenta?" to "Regístrate"
    } else {
        "¿Ya tienes una cuenta?" to "Inicia Sesión"
    }

    Row {
        Text(text)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = actionText,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable {
                onToggle(if (authMode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    TablonTheme {
        AuthScreen(
            authState = AuthState(isLoading = false, error = null),

            canAuthenticateWithBiometrics = true, // Simulamos que sí puede usar la huella
            onBiometricLoginRequested = {}, // En la preview, no hace nada
            onLogin = { _, _ -> },
            onRegister = { _, _ -> }
        )
    }
}