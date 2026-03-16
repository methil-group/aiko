package com.methil.aiko.ui.screens.auth

import android.widget.ImageView
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import com.methil.aiko.R
import com.methil.aiko.ui.theme.DarkPurple
import com.methil.aiko.ui.theme.LightViolet
import com.methil.aiko.ui.theme.LightestPink
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(onStartClick: () -> Unit) {
    // Cascade animation stages
    var animationStage by remember { mutableStateOf(0) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    // Dither background alpha
    val ditherAlpha: Float by animateFloatAsState(
        targetValue = if (animationStage >= 0) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "DitherAlpha"
    )
    
    // E-girl fade in animation
    val egirlAlpha: Float by animateFloatAsState(
        targetValue = if (animationStage >= 1) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "EgirlAlpha"
    )
    
    // Logo fade in
    val logoAlpha: Float by animateFloatAsState(
        targetValue = if (animationStage >= 2) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "LogoAlpha"
    )
    
    // Button slide up and scale
    val buttonOffsetY: androidx.compose.ui.unit.Dp by animateDpAsState(
        targetValue = if (animationStage >= 3) 0.dp else 100.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ButtonOffsetY"
    )
    
    val buttonScale: Float by animateFloatAsState(
        targetValue = if (animationStage >= 3) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ButtonScale"
    )

    LaunchedEffect(Unit) {
        animationStage = 0 // Dither background visible
        delay(300)
        animationStage = 1 // E-girl fades in
        delay(800)
        animationStage = 2 // Logo fades in
        delay(300)
        animationStage = 3 // Button slides up
    }

    val context = LocalContext.current
    
    // Image loader with nearest neighbor filtering for pixel art
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .crossfade(false)
            .build()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightViolet)
    ) {
        // Dither background - full screen ignoring safe area with nearest neighbor filtering
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = ditherAlpha },
            factory = { ctx ->
                ImageView(ctx).apply {
                    setImageResource(R.drawable.dither_bg_dark)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setScaleType(ImageView.ScaleType.CENTER_CROP)
                }
            },
            update = { view ->
                view.alpha = ditherAlpha
            }
        )

        // E-girl image - fade in
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = egirlAlpha }
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                factory = { ctx ->
                    ImageView(ctx).apply {
                        setImageResource(R.drawable.e_girl)
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setScaleType(ImageView.ScaleType.CENTER_CROP)
                    }
                }
            )
        }

        // Logo at bottom (Methil logo)
        if (animationStage >= 2) {
            Image(
                painter = painterResource(R.drawable.logo_methil_jap),
                contentDescription = null,
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .size(60.dp)
                    .align(Alignment.BottomCenter)
                    .graphicsLayer { alpha = logoAlpha }
            )
        }

        // Discuter Button Positioned for UX (thumb position) - on top of everything
        if (animationStage >= 3) {
            Y2kButton(
                text = stringResource(R.string.onboarding_start_button),
                onClick = onStartClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .width(220.dp)
                    .height(60.dp)
                    .offset(y = buttonOffsetY)
                    .graphicsLayer { scaleX = buttonScale; scaleY = buttonScale }
                    .zIndex(1f) // Ensure button is on top
            )
        }
    }
}

@Composable
fun Y2kButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shadowColor = DarkPurple
    val shadowOffset = 4.dp
    
    Box(modifier = modifier) {
        // Hard shadow (solid offset rectangle)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(shadowColor)
        )
        
        // Main button
        Surface(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            color = LightestPink,
            border = BorderStroke(3.dp, Color(0xFFE0B8FF))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = text,
                    color = DarkPurple,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
