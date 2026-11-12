package com.puzzle100.game.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.puzzle100.game.ui.theme.BlockOrange
import com.puzzle100.game.ui.theme.BlockPink
import com.puzzle100.game.ui.theme.BlockPurple
import com.puzzle100.game.ui.theme.BlockTurquoise
import com.puzzle100.game.ui.theme.NavyBackground
import com.puzzle100.game.ui.theme.NavyCard
import com.puzzle100.game.ui.theme.NavyGridBorder
import com.puzzle100.game.ui.theme.NavySurface
import com.puzzle100.game.ui.theme.StarGold
import com.puzzle100.game.ui.theme.TextMuted
import com.puzzle100.game.ui.theme.TextPrimary
import com.puzzle100.game.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    onPlayClicked: () -> Unit,
    onContinueClicked: ((Int) -> Unit)? = null,
    onSelectLevelClicked: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(context) {
        viewModel.attachContext(context)
    }

    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0D1527),
                        NavyBackground,
                        Color(0xFF070B14)
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Stars & Settings Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stars Chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(NavySurface)
                        .border(1.dp, NavyGridBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Stars",
                        tint = StarGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${state.totalStars} / ${state.maxStars}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Settings Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(NavySurface)
                        .border(1.dp, NavyGridBorder, CircleShape)
                        .clickable { viewModel.setSettingsDialog(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Cài đặt",
                        tint = if (state.isSoundEnabled || state.isHapticEnabled) BlockTurquoise else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Hero Brand & Decorative Blocks
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mini 4-Block decorative icon
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DecorativeTile(BlockTurquoise)
                    DecorativeTile(BlockOrange)
                    DecorativeTile(BlockPurple)
                    DecorativeTile(BlockPink)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "PUZZLE 100",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = TextPrimary
                )

                Text(
                    text = "DARK PREMIUM EDITION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    color = BlockTurquoise
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Stats Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NavyCard)
                        .border(1.dp, NavyGridBorder, RoundedCornerShape(16.dp))
                        .padding(vertical = 16.dp, horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(label = "ĐIỂM CAO", value = state.highestScore.toString(), color = BlockTurquoise)
                    StatItem(label = "CẤP HIỆN TẠI", value = "Cấp ${state.currentLevel}", color = BlockOrange)
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // If there is an active suspended game, show CONTINUE button
                if (state.hasActiveGame) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(62.dp)
                            .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = BlockTurquoise)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(BlockTurquoise, Color(0xFF00B4D8))
                                )
                            )
                            .clickable {
                                if (onContinueClicked != null) {
                                    onContinueClicked(state.activeGameLevel)
                                } else {
                                    onPlayClicked()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Continue",
                                tint = NavyBackground,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "TIẾP TỤC VÁN CHƠI",
                                    color = NavyBackground,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Màn ${state.activeGameLevel} • ${state.activeGameScore} điểm",
                                    color = NavyBackground.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Play Now / New Game Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (state.hasActiveGame) 52.dp else 60.dp)
                        .shadow(if (state.hasActiveGame) 8.dp else 16.dp, RoundedCornerShape(16.dp), spotColor = if (state.hasActiveGame) BlockOrange else BlockTurquoise)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (state.hasActiveGame) {
                                Brush.horizontalGradient(listOf(BlockOrange, Color(0xFFFF9100)))
                            } else {
                                Brush.horizontalGradient(listOf(BlockTurquoise, Color(0xFF00B4D8)))
                            }
                        )
                        .clickable { onPlayClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = NavyBackground,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.hasActiveGame) "CHƠI VÁN MỚI" else "CHƠI NGAY",
                            color = NavyBackground,
                            fontSize = if (state.hasActiveGame) 16.sp else 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Level Select Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(NavyCard)
                        .border(1.dp, NavyGridBorder, RoundedCornerShape(16.dp))
                        .clickable { onSelectLevelClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "Levels",
                            tint = BlockPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "CHỌN CẤP ĐỘ (1 - 100)",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // SETTINGS DIALOG (DARK PREMIUM)
        if (state.isSettingsDialogOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NavyBackground.copy(alpha = 0.88f))
                    .clickable { viewModel.setSettingsDialog(false) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(NavyCard)
                        .border(1.5.dp, NavyGridBorder, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                        .clickable(enabled = false) {}, // consume clicks
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = BlockTurquoise,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CÀI ĐẶT TRÒ CHƠI",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sound Effects Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavySurface)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = if (state.isSoundEnabled) BlockTurquoise else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Hiệu ứng âm thanh",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = state.isSoundEnabled,
                            onCheckedChange = { viewModel.toggleSound() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NavyBackground,
                                checkedTrackColor = BlockTurquoise,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = NavyCard
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Haptic Feedback Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavySurface)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = null,
                                tint = if (state.isHapticEnabled) BlockOrange else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Phản hồi rung",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = state.isHapticEnabled,
                            onCheckedChange = { viewModel.toggleHaptic() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NavyBackground,
                                checkedTrackColor = BlockOrange,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = NavyCard
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Master Volume Slider
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavySurface)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Âm lượng tổng",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${(state.masterVolume * 100).roundToInt()}%",
                                color = BlockTurquoise,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = state.masterVolume,
                            onValueChange = { viewModel.setMasterVolume(it) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = BlockTurquoise,
                                activeTrackColor = BlockTurquoise,
                                inactiveTrackColor = NavyCard
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Close Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BlockTurquoise)
                            .clickable { viewModel.setSettingsDialog(false) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ĐÓNG",
                            color = NavyBackground,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DecorativeTile(color: Color) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color)
    )
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}
