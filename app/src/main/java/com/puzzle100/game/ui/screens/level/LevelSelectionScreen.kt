package com.puzzle100.game.ui.screens.level

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.puzzle100.game.model.LevelInfo
import com.puzzle100.game.ui.theme.BlockOrange
import com.puzzle100.game.ui.theme.BlockTurquoise
import com.puzzle100.game.ui.theme.NavyBackground
import com.puzzle100.game.ui.theme.NavyCard
import com.puzzle100.game.ui.theme.NavyGridBorder
import com.puzzle100.game.ui.theme.NavySurface
import com.puzzle100.game.ui.theme.StarEmpty
import com.puzzle100.game.ui.theme.StarGold
import com.puzzle100.game.ui.theme.TextMuted
import com.puzzle100.game.ui.theme.TextPrimary
import com.puzzle100.game.ui.theme.TextSecondary

@Composable
fun LevelSelectionScreen(
    onBackClicked: () -> Unit,
    onLevelSelected: (Int) -> Unit,
    viewModel: LevelSelectionViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(context) {
        viewModel.attachContext(context)
    }

    val state by viewModel.uiState.collectAsState()

    val chapters = listOf(
        "Tân thủ (1-25)",
        "Tập sự (26-50)",
        "Chuyên gia (51-75)",
        "Bậc thầy (76-100)"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        NavyBackground,
                        Color(0xFF070B14)
                    )
                )
            )
            .padding(top = 24.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(NavySurface)
                    .border(1.dp, NavyGridBorder, CircleShape)
                    .clickable { onBackClicked() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = "CHỌN CẤP ĐỘ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Tổng số 100 thử thách",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chapter Tab Row
        ScrollableTabRow(
            selectedTabIndex = state.selectedChapter - 1,
            containerColor = NavySurface,
            contentColor = BlockTurquoise,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedChapter - 1]),
                    color = BlockTurquoise,
                    height = 3.dp
                )
            }
        ) {
            chapters.forEachIndexed { index, title ->
                val isSelected = state.selectedChapter == index + 1
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.selectChapter(index + 1) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) BlockTurquoise else TextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Levels Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.filteredLevels, key = { it.levelNumber }) { level ->
                LevelGridItem(
                    level = level,
                    onClick = {
                        if (level.isUnlocked) {
                            onLevelSelected(level.levelNumber)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LevelGridItem(
    level: LevelInfo,
    onClick: () -> Unit
) {
    val borderColor = when {
        !level.isUnlocked -> NavyGridBorder
        level.stars == 3 -> BlockTurquoise
        level.stars > 0 -> BlockOrange
        else -> NavyGridBorder.copy(alpha = 0.8f)
    }

    Box(
        modifier = Modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (level.isUnlocked) NavyCard else NavySurface.copy(alpha = 0.6f))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = level.isUnlocked) { onClick() }
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            if (level.isUnlocked) {
                Text(
                    text = "${level.levelNumber}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )

                // Stars row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { starIndex ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (starIndex < level.stars) StarGold else StarEmpty,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${level.levelNumber}",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
