package com.chakrawellness.app.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.indication
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.chakrawellness.app.R
import androidx.compose.animation.*
import androidx.compose.foundation.background
import kotlinx.coroutines.delay





@Composable
fun HomeScreen(navController: NavHostController) {
    Column {
        HeaderBar(title = "Home")  // ✅ Pass "Home" as title

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // ✅ Background Image
            Image(
                painter = painterResource(id = R.drawable.home_background),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ✅ Buttons with Ripple Effect
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ImageButton(navController, "profile", R.drawable.profile_button, Modifier.weight(1f))
                        ImageButton(navController, "quizRoot", R.drawable.quiz_button, Modifier.weight(1f)) // ✅ Starts at Root Chakra
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ImageButton(navController, "quizResults", R.drawable.results_button, Modifier.weight(1f))
                        ImageButton(navController, "about", R.drawable.about_button, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ✅ Image Button with Material3 Ripple + Scale Animation
@Composable
fun ImageButton(navController: NavHostController, destination: String, imageRes: Int, modifier: Modifier = Modifier) {
    var isClicked by remember { mutableStateOf(false) }
    var isTransitioning by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(isClicked) {
        if (isClicked) {
            delay(1000)  // Let ripple animation play
            isTransitioning = true
            delay(700)  // Transition effect
            navController.navigate(destination)  // Switch pages
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)  // ✅ Ensures all buttons are perfect squares
            .clickable(interactionSource = interactionSource, indication = null) {
                isClicked = true
            }
    ) {
        // ✅ Button Image
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = destination,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // ✅ Transition Effect (Fades Out)
        AnimatedVisibility(
            visible = isTransitioning,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            )
        }
    }
}

