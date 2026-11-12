package com.puzzle100.game.ui.screens.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.puzzle100.game.ui.theme.BlockOrange
import com.puzzle100.game.ui.theme.BlockPink
import com.puzzle100.game.ui.theme.BlockPurple
import com.puzzle100.game.ui.theme.BlockTurquoise
import com.puzzle100.game.ui.theme.NavyBackground
import com.puzzle100.game.ui.theme.TextMuted
import com.puzzle100.game.ui.theme.TextPrimary
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.navigateToHome.collectLatest {
            onNavigateToHome()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "SplashPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        NavyBackground,
                        Color(0xFF060913)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterVertically,
            verticalArrangement = Arrangement.Center
        ) {
            // 4-Block Signature Logo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(pulseScale)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SplashBlock(color = BlockTurquoise)
                        SplashBlock(color = BlockOrange)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SplashBlock(color = BlockPurple)
                        SplashBlock(color = BlockPink)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "PUZZLE 100",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "100 CẤP ĐỘ KHỐI KHÔNG GIAN",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = BlockTurquoise
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Đang tải dữ liệu...",
                fontSize = 13.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun SplashBlock(color: Color) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(12.dp), spotColor = color)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(color, color.copy(alpha = 0.75f))
                )
            )
    )
}
