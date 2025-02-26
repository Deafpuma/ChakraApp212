package com.chakrawellness.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chakrawellness.app.R
import androidx.compose.ui.graphics.Color

@Composable
fun HeaderBar(title: String) {
    Column {
        // ✅ Main Header Image
        Image(
            painter = painterResource(id = R.drawable.header_background),
            contentDescription = "Header",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )

        // ✅ Background Header Bar with Proper Text Overlay
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.header_background_bar),
                contentDescription = "Background Header Bar",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )

            // ✅ Correctly Centered Title Inside the Bar
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 10.dp)
            )
        }
    }
}
