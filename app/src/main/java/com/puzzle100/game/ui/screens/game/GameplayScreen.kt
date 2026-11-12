package com.puzzle100.game.ui.screens.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.puzzle100.game.audio.SoundManager
import com.puzzle100.game.haptics.HapticManager
import com.puzzle100.game.model.BOARD_SIZE
import com.puzzle100.game.model.BlockCell
import com.puzzle100.game.model.BoardCellState
import com.puzzle100.game.model.GhostPreviewState
import com.puzzle100.game.model.PuzzleShape
import com.puzzle100.game.ui.theme.BlockOrange
import com.puzzle100.game.ui.theme.BlockPink
import com.puzzle100.game.ui.theme.BlockPurple
import com.puzzle100.game.ui.theme.BlockTurquoise
import com.puzzle100.game.ui.theme.NavyBackground
import com.puzzle100.game.ui.theme.NavyCard
import com.puzzle100.game.ui.theme.NavyGrid
import com.puzzle100.game.ui.theme.NavyGridBorder
import com.puzzle100.game.ui.theme.NavySurface
import com.puzzle100.game.ui.theme.StarGold
import com.puzzle100.game.ui.theme.TextMuted
import com.puzzle100.game.ui.theme.TextPrimary
import com.puzzle100.game.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun GameplayScreen(
    levelNumber: Int,
    onBackClicked: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val hapticManager = remember { HapticManager(context) }

    LaunchedEffect(context) {
        viewModel.attachContext(context)
    }

    LaunchedEffect(levelNumber) {
        viewModel.initLevel(levelNumber)
    }

    val state by viewModel.gameState.collectAsState()
    val settings by viewModel.settingsState.collectAsState()

    // Sync audio and haptic settings
    LaunchedEffect(settings.isSoundEnabled, settings.masterVolume, settings.isHapticEnabled) {
        soundManager.isSoundEnabled = settings.isSoundEnabled
        soundManager.masterVolume = settings.masterVolume
        hapticManager.isHapticEnabled = settings.isHapticEnabled
    }

    // Lifecycle clean up & save session on exit
    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveSessionOnExit()
            soundManager.release()
        }
    }

    // Single-listener event flow collection: guarantees NO sound/haptic re-trigger on recomposition!
    LaunchedEffect(Unit) {
        viewModel.effectsFlow.collect { event ->
            when (event) {
                is GameEffectEvent.Pickup -> {
                    soundManager.playPickup()
                    hapticManager.vibratePickup()
                }
                is GameEffectEvent.Place -> {
                    soundManager.playPlace()
                    hapticManager.vibratePlace()
                }
                is GameEffectEvent.InvalidMove -> {
                    soundManager.playInvalidMove()
                    hapticManager.vibrateInvalidMove()
                }
                is GameEffectEvent.LineClear -> {
                    soundManager.playLineClear()
                    hapticManager.vibrateLineClear()
                }
                is GameEffectEvent.Combo -> {
                    soundManager.playCombo(event.level)
                    hapticManager.vibrateCombo(event.level)
                }
                is GameEffectEvent.GameOver -> {
                    soundManager.playGameOver()
                    hapticManager.vibrateGameOver()
                }
                is GameEffectEvent.LevelComplete -> {
                    soundManager.playLevelComplete()
                    hapticManager.vibrateLevelComplete()
                }
            }
        }
    }

    // Board bounding rectangle in root window coordinates
    var boardBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0D1527),
                        NavyBackground,
                        Color(0xFF060913)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
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
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Level & Score Display with Combo Badge
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CẤP ĐỘ ${state.currentLevel}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = TextPrimary
                        )
                        if (state.combo > 1) {
                            Spacer(modifier = Modifier.size(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BlockOrange)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "COMBO x${state.combo}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NavyBackground
                                )
                            }
                        }
                    }
                    Text(
                        text = "Điểm: ${state.score} / ${state.targetScore}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BlockTurquoise
                    )
                }

                // Pause Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NavySurface)
                        .border(1.dp, NavyGridBorder, CircleShape)
                        .clickable { viewModel.togglePause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Score Target Progress Bar
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BlockTurquoise,
                    trackColor = NavySurface
                )
            }

            // Center 10x10 Game Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(NavyCard)
                    .border(2.dp, NavyGridBorder, RoundedCornerShape(16.dp))
                    .padding(8.dp)
                    .onGloballyPositioned { coordinates ->
                        boardBounds = coordinates.boundsInRoot()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    for (row in 0 until BOARD_SIZE) {
                        val isRowClearing = state.clearAnimation.clearedRows.contains(row)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            for (col in 0 until BOARD_SIZE) {
                                val isColClearing = state.clearAnimation.clearedCols.contains(col)
                                val cellState = state.board[row][col]

                                val ghostCell = state.ghostPreview?.previewCells?.find { it.row == row && it.col == col }
                                val isGhost = ghostCell != null
                                val isGhostValid = state.ghostPreview?.isValid ?: false

                                BoardCellItem(
                                    cellState = cellState,
                                    isGhost = isGhost,
                                    isGhostValid = isGhostValid,
                                    isClearing = isRowClearing || isColClearing,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Floating Score Popup Effect Overlay
                state.scorePopup?.let { popup ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavyBackground.copy(alpha = 0.9f))
                                .border(1.dp, BlockTurquoise, RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = popup.message,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = if (popup.combo > 1) BlockOrange else BlockTurquoise
                            )
                        }
                    }
                }
            }

            // Bottom Tray: 3 Available Pieces with Drag & Drop Gesture
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(NavySurface)
                    .border(1.dp, NavyGridBorder, RoundedCornerShape(20.dp))
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "KÉO KHỐI LÊN BÀN CỜ ĐỂ ĐẶT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.availablePieces.forEachIndexed { index, piece ->
                        val isBeingDragged = state.dragState.isDragging && state.dragState.pieceIndex == index

                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavyCard.copy(alpha = 0.5f))
                                .border(1.dp, NavyGridBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (piece != null && !isBeingDragged) {
                                DraggablePieceSlot(
                                    piece = piece,
                                    onDragStart = {
                                        viewModel.onDragStarted(index)
                                    },
                                    onDrag = { touchOffset ->
                                        boardBounds?.let { bounds ->
                                            viewModel.onDragMoved(
                                                touchX = touchOffset.x,
                                                touchY = touchOffset.y,
                                                boardLeft = bounds.left,
                                                boardTop = bounds.top,
                                                boardSizePx = bounds.width
                                            )
                                        }
                                    },
                                    onDragEnd = {
                                        viewModel.onDragEnded()
                                    },
                                    onDragCancel = {
                                        viewModel.onDragCancelled()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // FLOATING DRAGGED PIECE OVERLAY (Follows user touch)
        if (state.dragState.isDragging && state.dragState.piece != null) {
            val piece = state.dragState.piece!!
            val touchX = state.dragState.touchX
            val touchY = state.dragState.touchY

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (touchX - 60f).roundToInt(),
                            y = (touchY - 180f).roundToInt()
                        )
                    }
                    .scale(1.15f)
                    .shadow(16.dp, RoundedCornerShape(8.dp), spotColor = piece.colorType.color)
            ) {
                PiecePreview(piece = piece, cellSize = 22.dp)
            }
        }

        // GAME OVER MODAL OVERLAY
        if (state.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NavyBackground.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(NavyCard)
                        .border(2.dp, Color(0xFFEF4444), RoundedCornerShape(24.dp))
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Game Over",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "KHÔNG CÒN NƯỚC ĐI!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "Tổng điểm đạt được: ${state.score}",
                        fontSize = 14.sp,
                        color = BlockTurquoise,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BlockTurquoise)
                            .clickable { viewModel.restartGame() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "THỬ LẠI",
                            color = NavyBackground,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(NavySurface)
                            .border(1.dp, NavyGridBorder, RoundedCornerShape(14.dp))
                            .clickable { onBackClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VỀ TRANG CHỦ",
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // VICTORY MODAL OVERLAY
        if (state.isVictory) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NavyBackground.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(NavyCard)
                        .border(2.dp, StarGold, RoundedCornerShape(24.dp))
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Victory",
                        tint = StarGold,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "VƯỢT CẤP XUẤT SẮC!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "Hoàn thành mục tiêu: ${state.score} điểm",
                        fontSize = 14.sp,
                        color = BlockTurquoise,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(StarGold)
                            .clickable { viewModel.initLevel(state.currentLevel + 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TIẾP TỤC CẤP ${state.currentLevel + 1}",
                            color = NavyBackground,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // PAUSE & IN-GAME SETTINGS MODAL OVERLAY
        if (state.isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NavyBackground.copy(alpha = 0.88f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(NavyCard)
                        .border(1.5.dp, NavyGridBorder, RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TẠM DỪNG",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    // In-Game Audio & Haptic Controls
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(NavySurface)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Sound Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = if (settings.isSoundEnabled) BlockTurquoise else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Âm thanh",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Switch(
                                checked = settings.isSoundEnabled,
                                onCheckedChange = { viewModel.setSoundEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NavyBackground,
                                    checkedTrackColor = BlockTurquoise,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = NavyCard
                                )
                            )
                        }

                        // Haptic Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = if (settings.isHapticEnabled) BlockOrange else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Rung phản hồi",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Switch(
                                checked = settings.isHapticEnabled,
                                onCheckedChange = { viewModel.setHapticEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NavyBackground,
                                    checkedTrackColor = BlockOrange,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = NavyCard
                                )
                            )
                        }

                        // Master Volume Slider
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Âm lượng",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${(settings.masterVolume * 100).roundToInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BlockTurquoise
                                )
                            }
                            Slider(
                                value = settings.masterVolume,
                                onValueChange = { viewModel.setMasterVolume(it) },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = BlockTurquoise,
                                    activeTrackColor = BlockTurquoise,
                                    inactiveTrackColor = NavyCard
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BlockTurquoise)
                            .clickable { viewModel.togglePause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TIẾP TỤC",
                            color = NavyBackground,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavySurface)
                            .border(1.dp, NavyGridBorder, RoundedCornerShape(12.dp))
                            .clickable { onBackClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VỀ TRANG CHỦ",
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardCellItem(
    cellState: BoardCellState,
    isGhost: Boolean,
    isGhostValid: Boolean,
    isClearing: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isClearing -> Color.White
        isGhost && isGhostValid -> BlockTurquoise.copy(alpha = 0.55f)
        isGhost && !isGhostValid -> Color(0xFFEF4444).copy(alpha = 0.55f)
        cellState.isFilled -> cellState.colorType?.color ?: BlockTurquoise
        else -> NavyGrid
    }

    val borderColor = when {
        isClearing -> StarGold
        isGhost && isGhostValid -> BlockTurquoise
        isGhost && !isGhostValid -> Color(0xFFEF4444)
        cellState.isFilled -> Color.White.copy(alpha = 0.35f)
        else -> NavyGridBorder.copy(alpha = 0.45f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(if (isGhost) 1.5.dp else 0.5.dp, borderColor, RoundedCornerShape(4.dp))
    )
}

@Composable
private fun DraggablePieceSlot(
    piece: PuzzleShape,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    var globalPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                globalPosition = coordinates.boundsInRoot().topLeft
            }
            .pointerInput(piece.id) {
                detectDragGestures(
                    onDragStart = {
                        onDragStart()
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val currentTouch = change.position + globalPosition
                        onDrag(currentTouch)
                    },
                    onDragEnd = {
                        onDragEnd()
                    },
                    onDragCancel = {
                        onDragCancel()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        PiecePreview(piece = piece, cellSize = 15.dp)
    }
}

@Composable
private fun PiecePreview(piece: PuzzleShape, cellSize: androidx.compose.ui.unit.Dp = 14.dp) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        piece.shapeMatrix.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                row.forEach { filled ->
                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (filled) piece.colorType.color else Color.Transparent
                            )
                            .border(
                                width = 0.5.dp,
                                color = if (filled) Color.White.copy(alpha = 0.25f) else Color.Transparent,
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
            }
        }
    }
}
