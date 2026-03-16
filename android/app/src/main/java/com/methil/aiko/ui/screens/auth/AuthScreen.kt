package com.methil.aiko.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.*
import com.methil.aiko.R
import com.methil.aiko.ui.components.AikoCustomKeyboard
import com.methil.aiko.ui.components.XpWindow
import com.methil.aiko.ui.theme.DarkPurple
import com.methil.aiko.ui.theme.LightViolet
import com.methil.aiko.ui.theme.LightestPink
import com.methil.aiko.domain.LoginRequest
import com.methil.aiko.domain.RegisterRequest
import androidx.compose.foundation.BorderStroke
import com.methil.aiko.service.AuthService
import com.methil.aiko.bridge.AikoConfig
import com.methil.aiko.data.TokenManager
import kotlinx.coroutines.launch
import android.util.Log

@Composable
fun AuthScreen(
    onAuthSuccess: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var isLogin by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") } // Pseudo pour l'inscription
    var password by remember { mutableStateOf("") }
    var activeField by remember { mutableStateOf(0) } // 0: username, 1: password, 2: name
    var isKeyboardOpen by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val authService = remember { AuthService(AikoConfig.BASE_URL) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "CursorTransition")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0.7f at 500
                1f at 501
                1f at 1000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "CursorAlpha"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = LightViolet
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding() // Avoid system IME if any, though we use custom
        ) {
            // Chat Background
            Image(
                painter = painterResource(id = R.drawable.chat_bg),
                contentDescription = "background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            Box(modifier = Modifier.fillMaxSize()) {
                XpWindow(
                    title = if (isLogin) stringResource(R.string.login_title) else stringResource(R.string.register_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                        .align(Alignment.Center)
                        .offset(y = (-20).dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (isLogin) stringResource(R.string.welcome_back) else stringResource(R.string.welcome_new),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkPurple
                        )

                        // Input Fields
                        AuthInputField(
                            label = stringResource(R.string.username_label),
                            value = username,
                            isActive = activeField == 0 && isKeyboardOpen,
                            cursorAlpha = cursorAlpha,
                            isRequired = true,
                            onClick = { 
                                activeField = 0
                                isKeyboardOpen = true
                            }
                        )

                        if (!isLogin) {
                            AuthInputField(
                                label = stringResource(R.string.name_label),
                                value = name,
                                isActive = activeField == 2 && isKeyboardOpen,
                                cursorAlpha = cursorAlpha,
                                isRequired = true,
                                onClick = { 
                                    activeField = 2
                                    isKeyboardOpen = true
                                }
                            )
                        }

                        AuthInputField(
                            label = stringResource(R.string.password_label),
                            value = password,
                            isActive = activeField == 1 && isKeyboardOpen,
                            cursorAlpha = cursorAlpha,
                            isPassword = true,
                            isRequired = true,
                            onClick = { 
                                activeField = 1
                                isKeyboardOpen = true
                            }
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action Button
                        Button(
                            onClick = {
                                if (username.isBlank() || password.isBlank()) return@Button
                                if (password.length < 8) {
                                    errorMessage = context.getString(R.string.error_password_too_short)
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    if (isLogin) {
                                        val result = authService.login(LoginRequest(username, password))
                                        result.onSuccess {
                                            tokenManager.saveToken(it.token)
                                            onAuthSuccess(it.token)
                                        }.onFailure {
                                            Log.e("AuthScreen", "Login failed", it)
                                            errorMessage = context.getString(R.string.error_login_failed, it.message ?: "")
                                        }
                                    } else {
                                        val result = authService.register(RegisterRequest(username, name, password))
                                        result.onSuccess {
                                            val loginResult = authService.login(LoginRequest(username, password))
                                            loginResult.onSuccess { authRes ->
                                                tokenManager.saveToken(authRes.token)
                                                onAuthSuccess(authRes.token)
                                            }.onFailure { loginErr ->
                                                Log.e("AuthScreen", "Login after registration failed", loginErr)
                                                errorMessage = context.getString(R.string.error_login_failed, loginErr.message ?: "")
                                            }
                                        }.onFailure {
                                            Log.e("AuthScreen", "Registration failed", it)
                                            errorMessage = context.getString(R.string.error_registration_failed, it.message ?: "")
                                        }
                                    }
                                    isLoading = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = !isLoading && username.isNotBlank() && password.isNotBlank() && (isLogin || name.isNotBlank()),
                            colors = ButtonDefaults.buttonColors(containerColor = LightestPink),
                            shape = RectangleShape,
                            border = BorderStroke(2.dp, DarkPurple)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DarkPurple)
                            } else {
                                Text(
                                    text = if (isLogin) stringResource(R.string.login_button) else stringResource(R.string.register_button),
                                    color = DarkPurple,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = if (isLogin) stringResource(R.string.no_account_link) else stringResource(R.string.has_account_link),
                            modifier = Modifier.clickable { 
                                isLogin = !isLogin
                                errorMessage = null
                            },
                            fontSize = 14.sp,
                            color = DarkPurple.copy(alpha = 0.7f)
                        )
                    }
                }

                // Keyboard
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    AnimatedVisibility(
                        visible = isKeyboardOpen,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        AikoCustomKeyboard(
                            onKeyClick = { key ->
                                when (activeField) {
                                    0 -> username += key
                                    1 -> password += key
                                    2 -> name += key
                                }
                            },
                            onDelete = {
                                when (activeField) {
                                    0 -> if (username.isNotEmpty()) username = username.dropLast(1)
                                    1 -> if (password.isNotEmpty()) password = password.dropLast(1)
                                    2 -> if (name.isNotEmpty()) name = name.dropLast(1)
                                }
                            },
                            onEnter = {
                                when (activeField) {
                                    0 -> activeField = if (isLogin) 1 else 2
                                    2 -> activeField = 1
                                    1 -> isKeyboardOpen = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuthInputField(
    label: String,
    value: String,
    isActive: Boolean,
    cursorAlpha: Float,
    isPassword: Boolean = false,
    isRequired: Boolean = false,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = DarkPurple,
                fontFamily = FontFamily.Monospace
            )
            if (isRequired) {
                Text(
                    text = "*",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(Color.Black)
                .border(2.dp, if (isActive) DarkPurple else LightViolet)
                .clip(RectangleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayText = if (isPassword) "•".repeat(value.length) else value
                Text(
                    text = displayText,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .size(width = 2.dp, height = 20.dp)
                            .background(Color.White)
                            .graphicsLayer { alpha = cursorAlpha }
                    )
                }
            }
        }
    }
}
