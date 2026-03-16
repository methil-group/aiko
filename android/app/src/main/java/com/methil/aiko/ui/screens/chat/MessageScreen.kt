package com.methil.aiko.ui.screens.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import coil.compose.AsyncImage
import com.methil.aiko.R
import com.methil.aiko.ui.components.AikoCustomKeyboard
import com.methil.aiko.ui.components.AikoHeader
import com.methil.aiko.ui.components.XpWindow
import com.methil.aiko.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.methil.aiko.ui.viewmodels.MessageUI
import com.methil.aiko.ui.viewmodels.MessageViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star

@Composable
fun MessageScreen(
    sessionToken: String = "",
    characterId: Int = 0,
    viewModel: MessageViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    LaunchedEffect(sessionToken, characterId) {
        viewModel.init(sessionToken, characterId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val messages = uiState.messages

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to bottom when messages change or current message is streaming
    val lastMessageText = messages.lastOrNull()?.text ?: ""
    LaunchedEffect(messages.size, lastMessageText) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Auto-scroll when keyboard opens
    LaunchedEffect(uiState.isKeyboardOpen) {
        if (uiState.isKeyboardOpen && messages.isNotEmpty()) {
            // Give a small delay to allow the layout to adjust
            kotlinx.coroutines.delay(300)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = LightViolet
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Chat Background
            Image(
                painter = painterResource(id = R.drawable.chat_bg),
                contentDescription = "background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Top Control Area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(140.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Window: Mood/Profile
                    XpWindow(title = stringResource(R.string.chat_mood_title), modifier = Modifier.weight(1f)) {
                        Image(
                            painter = painterResource(id = R.drawable.e_girl_emo),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillWidth
                        )
                    }

                    // Right Window: Stats
                    XpWindow(title = stringResource(R.string.chat_stats_title), modifier = Modifier.weight(1.5f)) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            StatRow(
                                "LUV",
                                "http://localhost:3845/assets/ae3b83a6522aeef54dbf03912059a8181a1e451919.svg"
                            ) // heart_fill
                            StatRow(
                                "NRG",
                                "http://localhost:3845/assets/7c85398c646e1d8c43dece198ea8c2f864130d4f.svg"
                            ) // spark_fill
                        }
                    }
                }

                // Header with Back Button
                Surface(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    color = DarkPurple.copy(alpha = 0.8f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(id = R.drawable.kb_erase), // Use erase icon as back for now
                                contentDescription = stringResource(R.string.chat_back_content_description),
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        AikoHeader(
                            text = "${uiState.characterName} ${stringResource(R.string.chat_header_suffix)}",
                            fontSize = 20.sp
                        )
                    }
                }

                // Chat Messages
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    0.1f to Color.Black
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        },
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubble(msg)
                    }
                }

                // Input Area
                Y2kInputArea(
                    text = uiState.inputText,
                    isKeyboardOpen = uiState.isKeyboardOpen,
                    onInputClick = { viewModel.toggleKeyboard() },
                    onSend = { viewModel.sendMessage() }
                )

                // Custom Keyboard
                AnimatedVisibility(
                    visible = uiState.isKeyboardOpen,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = 300)
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(durationMillis = 300)
                    )
                ) {
                    AikoCustomKeyboard(
                        onKeyClick = { viewModel.onInputTextChanged(uiState.inputText + it) },
                        onDelete = { 
                            if (uiState.inputText.isNotEmpty()) {
                                viewModel.onInputTextChanged(uiState.inputText.dropLast(1))
                            }
                        },
                        onEnter = { viewModel.sendMessage() }
                    )
                }
            }

            // Error message overlay
            uiState.errorMessage?.let { error ->
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.dismissError() }) {
                            Text(stringResource(R.string.chat_dismiss), color = Color.White)
                        }
                    },
                    modifier = Modifier.padding(16.dp).align(Alignment.BottomCenter)
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, iconUrl: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 12.sp, color = DarkPurple, modifier = Modifier.width(35.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            val icon = if (label == "LUV") Icons.Default.Favorite else Icons.Default.Star
            val color = if (label == "LUV") Color(0xFFFF4081) else Color(0xFFFFD600)
            repeat(5) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = color
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: MessageUI) {
    if (message.isSystem) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message.text,
                color = LightestPink,
                fontSize = 14.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
        return
    }

    val shadowColor = DarkPurple
    val shadowOffset = 2.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isAiko) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier.width(260.dp)
        ) {
            // Shadow - Using matchParentSize to automatically adapt to bubble content
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = shadowOffset, y = shadowOffset)
                    .background(shadowColor)
            )

            // Bubble content
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (message.isAiko) 0.dp else shadowOffset,
                        end = if (message.isAiko) shadowOffset else 0.dp,
                        bottom = shadowOffset
                    ),
                color = LightestPink,
                border = BorderStroke(2.dp, LightViolet)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.e_girl_pp),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(45.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (message.isSearching) "..." else message.text,
                        color = DarkPurple,
                        fontSize = 16.sp
                    )
                }
            }

            // Tail/Arrow - Positioned at bottom corner based on sender
            val tailResId =
                if (message.isAiko) R.drawable.msg_arrow_left else R.drawable.msg_arrow_right
            Image(
                painter = painterResource(id = tailResId),
                contentDescription = null,
                modifier = Modifier
                    .align(
                        if (message.isAiko) Alignment.BottomStart else Alignment.BottomEnd
                    )
                    .size(20.dp)
                    .offset(
                        x = if (message.isAiko) 0.dp else (4).dp,
                        y = 16.dp
                    )
            )
        }
    }
}

@Composable
fun Y2kInputArea(
    text: String,
    isKeyboardOpen: Boolean,
    onInputClick: () -> Unit,
    onSend: () -> Unit
) {
    val shadowColor = DarkPurple
    val shadowOffset = 4.dp

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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp)
    ) {
        // Hard shadow for input area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(shadowColor)
        )

        Surface(
            modifier = Modifier
                .fillMaxSize(),
            border = BorderStroke(3.dp, LightViolet),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onInputClick() }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (text.isEmpty() && !isKeyboardOpen) stringResource(R.string.chat_input_placeholder) else text,
                            color = if (text.isEmpty() && !isKeyboardOpen) Color.Gray else DarkPurple,
                            fontSize = 16.sp
                        )
                        if (isKeyboardOpen) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 2.dp)
                                    .size(width = 2.dp, height = 20.dp)
                                    .graphicsLayer { alpha = cursorAlpha }
                                    .background(DarkPurple)
                            )
                        }
                    }
                }
                IconButton(onClick = onSend) {
                    Icon(
                        painter = painterResource(id = R.drawable.send_ico),
                        contentDescription = stringResource(R.string.chat_send_content_description),
                        tint = DarkPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}