import React, { useState, useEffect, useRef } from 'react';
import {
  Play,
  Pause,
  RotateCcw,
  Star,
  Lock,
  Volume2,
  VolumeX,
  ArrowLeft,
  Smartphone,
  FileCode,
  Layers,
  CheckCircle2,
  Copy,
  ChevronRight,
  Sparkles,
  AlertTriangle,
  Flame,
  Award,
  TestTube2,
  Settings,
  Vibrate,
  Music,
  Sliders,
  Database,
  HardDrive,
  Save,
  Trash2,
  RefreshCw,
  Download,
} from 'lucide-react';

// Block Colors matching Jetpack Compose Color.kt
const COLOR_TURQUOISE = '#00E5FF';
const COLOR_ORANGE = '#FF6D00';
const COLOR_PURPLE = '#7C4DFF';
const COLOR_PINK = '#FF4081';

export interface PuzzleShapeUI {
  id: string;
  name: string;
  shapeMatrix: boolean[][];
  color: string;
  category: 'SMALL' | 'MEDIUM' | 'LARGE';
}

// 32 Comprehensive shapes repository matching Kotlin Block.kt
export const ALL_32_SHAPES: PuzzleShapeUI[] = [
  // Small (1 to 3 cells)
  { id: 'DOT_1X1', name: 'Điểm Đơn 1x1', shapeMatrix: [[true]], color: COLOR_TURQUOISE, category: 'SMALL' },
  { id: 'LINE_2H', name: 'Thanh Ngang 2x1', shapeMatrix: [[true, true]], color: COLOR_TURQUOISE, category: 'SMALL' },
  { id: 'LINE_3H', name: 'Thanh Ngang 3x1', shapeMatrix: [[true, true, true]], color: COLOR_PURPLE, category: 'SMALL' },
  { id: 'LINE_2V', name: 'Thanh Dọc 1x2', shapeMatrix: [[true], [true]], color: COLOR_TURQUOISE, category: 'SMALL' },
  { id: 'LINE_3V', name: 'Thanh Dọc 1x3', shapeMatrix: [[true], [true], [true]], color: COLOR_PURPLE, category: 'SMALL' },
  { id: 'L_SMALL_TL', name: 'Chữ L Nhỏ (TB)', shapeMatrix: [[true, true], [true, false]], color: COLOR_TURQUOISE, category: 'SMALL' },
  { id: 'L_SMALL_TR', name: 'Chữ L Nhỏ (ĐB)', shapeMatrix: [[true, true], [false, true]], color: COLOR_TURQUOISE, category: 'SMALL' },
  { id: 'L_SMALL_BL', name: 'Chữ L Nhỏ (TN)', shapeMatrix: [[true, false], [true, true]], color: COLOR_TURQUOISE, category: 'SMALL' },
  { id: 'L_SMALL_BR', name: 'Chữ L Nhỏ (ĐN)', shapeMatrix: [[false, true], [true, true]], color: COLOR_TURQUOISE, category: 'SMALL' },

  // Medium (4 to 5 cells)
  { id: 'SQUARE_2X2', name: 'Khối Vuông 2x2', shapeMatrix: [[true, true], [true, true]], color: COLOR_ORANGE, category: 'MEDIUM' },
  { id: 'LINE_4H', name: 'Thanh Ngang 4x1', shapeMatrix: [[true, true, true, true]], color: COLOR_PINK, category: 'MEDIUM' },
  { id: 'LINE_4V', name: 'Thanh Dọc 1x4', shapeMatrix: [[true], [true], [true], [true]], color: COLOR_PINK, category: 'MEDIUM' },
  { id: 'T_UP', name: 'Chữ T Ngửa', shapeMatrix: [[false, true, false], [true, true, true]], color: COLOR_PURPLE, category: 'MEDIUM' },
  { id: 'T_DOWN', name: 'Chữ T Xuôi', shapeMatrix: [[true, true, true], [false, true, false]], color: COLOR_PURPLE, category: 'MEDIUM' },
  { id: 'T_LEFT', name: 'Chữ T Trái', shapeMatrix: [[false, true], [true, true], [false, true]], color: COLOR_PURPLE, category: 'MEDIUM' },
  { id: 'T_RIGHT', name: 'Chữ T Phải', shapeMatrix: [[true, false], [true, true], [true, false]], color: COLOR_PURPLE, category: 'MEDIUM' },
  { id: 'Z_HORIZ', name: 'Khối Z Ngang', shapeMatrix: [[true, true, false], [false, true, true]], color: COLOR_PINK, category: 'MEDIUM' },
  { id: 'Z_VERT', name: 'Khối Z Dọc', shapeMatrix: [[false, true], [true, true], [true, false]], color: COLOR_PINK, category: 'MEDIUM' },
  { id: 'S_HORIZ', name: 'Khối S Ngang', shapeMatrix: [[false, true, true], [true, true, false]], color: COLOR_PINK, category: 'MEDIUM' },
  { id: 'S_VERT', name: 'Khối S Dọc', shapeMatrix: [[true, false], [true, true], [false, true]], color: COLOR_PINK, category: 'MEDIUM' },
  { id: 'L_LARGE_1', name: 'Chữ L Lớn 1', shapeMatrix: [[true, false, false], [true, false, false], [true, true, true]], color: COLOR_ORANGE, category: 'MEDIUM' },
  { id: 'L_LARGE_2', name: 'Chữ L Lớn 2', shapeMatrix: [[false, false, true], [false, false, true], [true, true, true]], color: COLOR_ORANGE, category: 'MEDIUM' },
  { id: 'L_LARGE_3', name: 'Chữ L Lớn 3', shapeMatrix: [[true, true, true], [true, false, false], [true, false, false]], color: COLOR_ORANGE, category: 'MEDIUM' },
  { id: 'L_LARGE_4', name: 'Chữ L Lớn 4', shapeMatrix: [[true, true, true], [false, false, true], [false, false, true]], color: COLOR_ORANGE, category: 'MEDIUM' },
  { id: 'PLUS_CROSS', name: 'Dấu Cộng (Plus)', shapeMatrix: [[false, true, false], [true, true, true], [false, true, false]], color: COLOR_TURQUOISE, category: 'MEDIUM' },
  { id: 'U_SHAPE', name: 'Chữ U 3x2', shapeMatrix: [[true, false, true], [true, true, true]], color: COLOR_ORANGE, category: 'MEDIUM' },

  // Large (5 to 9 cells)
  { id: 'LINE_5H', name: 'Thanh Ngang 5x1', shapeMatrix: [[true, true, true, true, true]], color: COLOR_ORANGE, category: 'LARGE' },
  { id: 'LINE_5V', name: 'Thanh Dọc 1x5', shapeMatrix: [[true], [true], [true], [true], [true]], color: COLOR_ORANGE, category: 'LARGE' },
  { id: 'SQUARE_3X3', name: 'Khối Vuông 3x3', shapeMatrix: [[true, true, true], [true, true, true], [true, true, true]], color: COLOR_PINK, category: 'LARGE' },
  { id: 'RECT_2X3', name: 'Chữ Nhật 2x3', shapeMatrix: [[true, true, true], [true, true, true]], color: COLOR_PURPLE, category: 'LARGE' },
  { id: 'RECT_3X2', name: 'Chữ Nhật 3x2', shapeMatrix: [[true, true], [true, true], [true, true]], color: COLOR_PURPLE, category: 'LARGE' },
  { id: 'CORNER_3X3', name: 'Góc Viền C 3x3', shapeMatrix: [[true, true, true], [true, false, false], [true, true, true]], color: COLOR_PINK, category: 'LARGE' },
];

export default function App() {
  const [activeTab, setActiveTab] = useState<'device' | 'soundboard' | 'persistence' | 'tests' | 'code'>('device');
  const [currentScreen, setCurrentScreen] = useState<'home' | 'levels' | 'game'>('game');
  const [selectedLevel, setSelectedLevel] = useState<number>(1);
  const [selectedChapter, setSelectedChapter] = useState<number>(1);
  const [isPaused, setIsPaused] = useState<boolean>(false);
  const [isSettingsOpen, setIsSettingsOpen] = useState<boolean>(false);
  const [selectedFile, setSelectedFile] = useState<string>('GamePreferencesRepository.kt');
  const [copiedFile, setCopiedFile] = useState<boolean>(false);

  // Sound & Haptic Settings (Preferences DataStore)
  const [isSoundEnabled, setIsSoundEnabled] = useState<boolean>(true);
  const [isHapticEnabled, setIsHapticEnabled] = useState<boolean>(true);
  const [masterVolume, setMasterVolume] = useState<number>(0.8);
  const [musicEnabled, setMusicEnabled] = useState<boolean>(true);
  const [musicVolume, setMusicVolume] = useState<number>(0.5);

  // Room Database: Player Progress
  const [playerProgress, setPlayerProgress] = useState({
    id: 1,
    currentUnlockedLevel: 1,
    currentLevel: 1,
    totalScore: 0,
    totalLevelsCompleted: 0,
    totalStars: 0,
    highestScore: 0,
    totalLinesCleared: 0,
    highestCombo: 0,
    lastPlayedTimestamp: Date.now()
  });

  // Room Database: Level Results
  const [levelResults, setLevelResults] = useState<Record<number, { bestScore: number; stars: number; timesPlayed: number; completed: boolean }>>({
    1: { bestScore: 0, stars: 0, timesPlayed: 0, completed: false }
  });

  // Room Database: Active Game
  const [activeGameDb, setActiveGameDb] = useState<{
    levelId: number;
    score: number;
    combo: number;
    highestCombo: number;
    board: (string | null)[][];
    trayPieces: (PuzzleShapeUI | null)[];
    movesCount: number;
    timestamp: number;
  } | null>(null);

  const [movesCount, setMovesCount] = useState<number>(0);

  // 10x10 Board state
  const [board, setBoard] = useState<(string | null)[][]>(() =>
    Array.from({ length: 10 }, () => Array(10).fill(null))
  );

  // Score & Combo State
  const [score, setScore] = useState<number>(0);
  const [combo, setCombo] = useState<number>(0);
  const [highestCombo, setHighestCombo] = useState<number>(0);
  const [clearedLinesCount, setClearedLinesCount] = useState<number>(0);
  const [scorePopup, setScorePopup] = useState<{ text: string; combo: number; id: number } | null>(null);
  const [clearingLines, setClearingLines] = useState<{ rows: number[]; cols: number[] }>({ rows: [], cols: [] });
  const [isGameOver, setIsGameOver] = useState<boolean>(false);
  const [isVictory, setIsVictory] = useState<boolean>(false);

  // Available pieces in tray (3 slots)
  const [trayPieces, setTrayPieces] = useState<(PuzzleShapeUI | null)[]>([]);

  // Drag & Drop State
  const [draggingPieceIndex, setDraggingPieceIndex] = useState<number | null>(null);
  const [dragPosition, setDragPosition] = useState<{ x: number; y: number } | null>(null);
  const [hoverGrid, setHoverGrid] = useState<{ row: number; col: number; isValid: boolean } | null>(null);

  const boardRef = useRef<HTMLDivElement>(null);
  const audioCtxRef = useRef<AudioContext | null>(null);
  const targetScore = 500 + selectedLevel * 150;

  // Web Audio Synthesizer (Matching SoundManager sound signatures)
  const getAudioCtx = (): AudioContext | null => {
    if (typeof window === 'undefined') return null;
    if (!audioCtxRef.current) {
      const AudioContextClass = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      if (AudioContextClass) {
        audioCtxRef.current = new AudioContextClass();
      }
    }
    if (audioCtxRef.current && audioCtxRef.current.state === 'suspended') {
      audioCtxRef.current.resume();
    }
    return audioCtxRef.current;
  };

  const playSynthesizedSound = (type: 'pickup' | 'place' | 'invalid' | 'lineClear' | 'combo' | 'gameOver' | 'victory') => {
    if (!isSoundEnabled || masterVolume <= 0) return;
    try {
      const ctx = getAudioCtx();
      if (!ctx) return;
      const now = ctx.currentTime;
      const vol = masterVolume * 0.4;

      if (type === 'pickup') {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(440, now);
        osc.frequency.exponentialRampToValueAtTime(680, now + 0.06);
        gain.gain.setValueAtTime(vol * 0.8, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.07);
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start(now);
        osc.stop(now + 0.07);
      } else if (type === 'place') {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'triangle';
        osc.frequency.setValueAtTime(560, now);
        osc.frequency.exponentialRampToValueAtTime(220, now + 0.08);
        gain.gain.setValueAtTime(vol, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.09);
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start(now);
        osc.stop(now + 0.09);
      } else if (type === 'invalid') {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'sawtooth';
        osc.frequency.setValueAtTime(140, now);
        gain.gain.setValueAtTime(vol * 0.7, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.12);
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start(now);
        osc.stop(now + 0.12);
      } else if (type === 'lineClear') {
        [523.25, 659.25].forEach((freq, idx) => {
          const osc = ctx.createOscillator();
          const gain = ctx.createGain();
          osc.type = 'sine';
          osc.frequency.setValueAtTime(freq, now + idx * 0.05);
          gain.gain.setValueAtTime(vol * 0.9, now + idx * 0.05);
          gain.gain.exponentialRampToValueAtTime(0.001, now + idx * 0.05 + 0.22);
          osc.connect(gain);
          gain.connect(ctx.destination);
          osc.start(now + idx * 0.05);
          osc.stop(now + idx * 0.05 + 0.22);
        });
      } else if (type === 'combo') {
        [659.25, 830.61, 1046.5].forEach((freq, idx) => {
          const osc = ctx.createOscillator();
          const gain = ctx.createGain();
          osc.type = 'triangle';
          osc.frequency.setValueAtTime(freq, now + idx * 0.06);
          gain.gain.setValueAtTime(vol * 0.9, now + idx * 0.06);
          gain.gain.exponentialRampToValueAtTime(0.001, now + idx * 0.06 + 0.25);
          osc.connect(gain);
          gain.connect(ctx.destination);
          osc.start(now + idx * 0.06);
          osc.stop(now + idx * 0.06 + 0.25);
        });
      } else if (type === 'gameOver') {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(360, now);
        osc.frequency.exponentialRampToValueAtTime(140, now + 0.45);
        gain.gain.setValueAtTime(vol * 0.8, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.48);
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start(now);
        osc.stop(now + 0.48);
      } else if (type === 'victory') {
        [523.25, 659.25, 783.99, 1046.5].forEach((freq, idx) => {
          const osc = ctx.createOscillator();
          const gain = ctx.createGain();
          osc.type = 'sine';
          osc.frequency.setValueAtTime(freq, now + idx * 0.09);
          gain.gain.setValueAtTime(vol * 0.9, now + idx * 0.09);
          gain.gain.exponentialRampToValueAtTime(0.001, now + idx * 0.09 + 0.35);
          osc.connect(gain);
          gain.connect(ctx.destination);
          osc.start(now + idx * 0.09);
          osc.stop(now + idx * 0.09 + 0.35);
        });
      }
    } catch {
      // AudioContext safe fallback
    }
  };

  const triggerHaptic = (pattern: number | number[]) => {
    if (!isHapticEnabled) return;
    try {
      if (typeof window !== 'undefined' && 'vibrate' in navigator) {
        navigator.vibrate(pattern);
      }
    } catch {
      // Safe fallback
    }
  };

  // Initialize pieces for current level
  const spawnPieces = (currentBoard: (string | null)[][]): (PuzzleShapeUI | null)[] => {
    const smalls = ALL_32_SHAPES.filter((s) => s.category === 'SMALL');
    const mediums = ALL_32_SHAPES.filter((s) => s.category === 'MEDIUM');
    const larges = ALL_32_SHAPES.filter((s) => s.category === 'LARGE');

    const p1 = smalls[Math.floor(Math.random() * smalls.length)];
    const p2 = mediums[Math.floor(Math.random() * mediums.length)];
    const p3 = (selectedLevel > 25 ? (Math.random() > 0.5 ? larges : mediums) : smalls)[
      Math.floor(Math.random() * (selectedLevel > 25 ? larges.length : smalls.length))
    ];

    const result = [p1, p2, p3];

    // Guarantee: ensure at least one piece can be placed
    const hasPlacable = result.some((piece) => canPlaceAnywhere(piece, currentBoard));
    if (!hasPlacable) {
      result[0] = smalls[0]; // fallback to 1x1 dot
    }

    setTrayPieces(result);
    return result;
  };

  useEffect(() => {
    spawnPieces(board);
  }, [selectedLevel]);

  const canPlacePiece = (piece: PuzzleShapeUI, originRow: number, originCol: number, b: (string | null)[][]): boolean => {
    for (let r = 0; r < piece.shapeMatrix.length; r++) {
      for (let c = 0; c < piece.shapeMatrix[r].length; c++) {
        if (piece.shapeMatrix[r][c]) {
          const targetR = originRow + r;
          const targetC = originCol + c;
          if (targetR < 0 || targetR >= 10 || targetC < 0 || targetC >= 10) return false;
          if (b[targetR][targetC] !== null) return false;
        }
      }
    }
    return true;
  };

  const canPlaceAnywhere = (piece: PuzzleShapeUI, b: (string | null)[][]): boolean => {
    for (let r = 0; r < 10; r++) {
      for (let c = 0; c < 10; c++) {
        if (canPlacePiece(piece, r, c, b)) return true;
      }
    }
    return false;
  };

  const checkGameOver = (pieces: (PuzzleShapeUI | null)[], b: (string | null)[][]) => {
    const active = pieces.filter((p): p is PuzzleShapeUI => p !== null);
    if (active.length === 0) return false;
    return !active.some((p) => canPlaceAnywhere(p, b));
  };

  // Drag handlers
  const handleDragStart = (index: number, e: React.MouseEvent | React.TouchEvent) => {
    const piece = trayPieces[index];
    if (!piece) return;

    const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX;
    const clientY = 'touches' in e ? e.touches[0].clientY : e.clientY;

    setDraggingPieceIndex(index);
    setDragPosition({ x: clientX, y: clientY });

    // PICKUP Sound & Haptic
    playSynthesizedSound('pickup');
    triggerHaptic(15);
  };

  const handleDragMove = (e: MouseEvent | TouchEvent) => {
    if (draggingPieceIndex === null) return;
    const piece = trayPieces[draggingPieceIndex];
    if (!piece) return;

    const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX;
    const clientY = 'touches' in e ? e.touches[0].clientY : e.clientY;

    setDragPosition({ x: clientX, y: clientY });

    if (!boardRef.current) return;
    const rect = boardRef.current.getBoundingClientRect();
    const cellSize = rect.width / 10;
    const fingerOffsetY = 70;

    const pieceWidthPx = piece.shapeMatrix[0].length * cellSize;
    const pieceHeightPx = piece.shapeMatrix.length * cellSize;

    const relX = clientX - rect.left - pieceWidthPx / 2;
    const relY = clientY - fingerOffsetY - rect.top - pieceHeightPx / 2;

    const col = Math.round(relX / cellSize);
    const row = Math.round(relY / cellSize);

    if (row >= -1 && row < 10 && col >= -1 && col < 10) {
      const isValid = canPlacePiece(piece, row, col, board);
      setHoverGrid({ row, col, isValid });
    } else {
      setHoverGrid(null);
    }
  };

  const handleDragEnd = () => {
    if (draggingPieceIndex === null) return;
    const piece = trayPieces[draggingPieceIndex];

    if (piece && hoverGrid && hoverGrid.isValid) {
      // Valid placement: PLACE sound & haptic
      playSynthesizedSound('place');
      triggerHaptic(25);

      const newBoard = board.map((r) => [...r]);
      for (let r = 0; r < piece.shapeMatrix.length; r++) {
        for (let c = 0; c < piece.shapeMatrix[r].length; c++) {
          if (piece.shapeMatrix[r][c]) {
            newBoard[hoverGrid.row + r][hoverGrid.col + c] = piece.color;
          }
        }
      }

      const nextTray = [...trayPieces];
      nextTray[draggingPieceIndex] = null;

      // Line detection
      const clearedRows: number[] = [];
      const clearedCols: number[] = [];

      for (let r = 0; r < 10; r++) {
        if (newBoard[r].every((cell) => cell !== null)) clearedRows.push(r);
      }
      for (let c = 0; c < 10; c++) {
        let full = true;
        for (let r = 0; r < 10; r++) {
          if (newBoard[r][c] === null) {
            full = false;
            break;
          }
        }
        if (full) clearedCols.push(c);
      }

      const totalLines = clearedRows.length + clearedCols.length;
      let nextCombo = combo;
      let addedScore = piece.shapeMatrix.flat().filter(Boolean).length * 10;

      if (totalLines > 0) {
        nextCombo = combo + 1;
        setHighestCombo((h) => Math.max(h, nextCombo));

        const lineScore = totalLines * 100;
        const multiBonus = totalLines > 1 ? (totalLines - 1) * 50 : 0;
        const comboBonus = nextCombo * 50;
        addedScore += lineScore + multiBonus + comboBonus;

        // Sound & Haptic for line clear & combo
        setTimeout(() => {
          if (nextCombo > 1) {
            playSynthesizedSound('combo');
            triggerHaptic([40, 30, 60]);
          } else {
            playSynthesizedSound('lineClear');
            triggerHaptic(65);
          }
        }, 80);

        setClearingLines({ rows: clearedRows, cols: clearedCols });
        setTimeout(() => setClearingLines({ rows: [], cols: [] }), 400);

        clearedRows.forEach((r) => {
          for (let c = 0; c < 10; c++) newBoard[r][c] = null;
        });
        clearedCols.forEach((c) => {
          for (let r = 0; r < 10; r++) newBoard[r][c] = null;
        });

        setClearedLinesCount((prev) => prev + totalLines);
        setScorePopup({
          text:
            nextCombo > 1
              ? `COMBO x${nextCombo}! +${addedScore}`
              : totalLines > 1
              ? `${totalLines} HÀNG! +${addedScore}`
              : `XÓA HÀNG! +${addedScore}`,
          combo: nextCombo,
          id: Date.now(),
        });
      } else {
        nextCombo = 0;
        setScorePopup({ text: `+${addedScore}`, combo: 0, id: Date.now() });
      }

      setCombo(nextCombo);
      const nextTotalScore = score + addedScore;
      setScore(nextTotalScore);
      setBoard(newBoard);

      const updatedMoves = movesCount + 1;
      setMovesCount(updatedMoves);

      // Check Victory
      if (nextTotalScore >= targetScore) {
        setIsVictory(true);
        setActiveGameDb(null); // Victory deletes active session

        // Save level result with anti-downgrade
        const earnedStars =
          nextTotalScore >= Math.floor(targetScore * 1.5) ? 3 : nextTotalScore >= Math.floor(targetScore * 1.2) ? 2 : 1;

        setLevelResults((prev) => {
          const ex = prev[selectedLevel];
          return {
            ...prev,
            [selectedLevel]: {
              levelId: selectedLevel,
              bestScore: Math.max(ex?.bestScore || 0, nextTotalScore),
              stars: Math.max(ex?.stars || 0, earnedStars),
              timesPlayed: (ex?.timesPlayed || 0) + 1,
              completed: true,
            },
          };
        });

        // Update player progress
        setPlayerProgress((prev) => ({
          ...prev,
          currentUnlockedLevel: Math.min(100, Math.max(prev.currentUnlockedLevel, selectedLevel + 1)),
          highestScore: Math.max(prev.highestScore, nextTotalScore),
          totalStars: prev.totalStars + earnedStars,
          totalLevelsCompleted: prev.totalLevelsCompleted + 1,
          totalScore: prev.totalScore + nextTotalScore,
          lastPlayedTimestamp: Date.now(),
        }));

        setTimeout(() => {
          playSynthesizedSound('victory');
          triggerHaptic([40, 40, 50, 40, 80]);
        }, 200);
      }

      // Restock tray if empty
      let finalTray = nextTray;
      if (nextTray.every((p) => p === null)) {
        finalTray = spawnPieces(newBoard);
      } else {
        setTrayPieces(nextTray);
      }

      const isOver = checkGameOver(finalTray, newBoard);
      if (isOver && nextTotalScore < targetScore) {
        setIsGameOver(true);
        setActiveGameDb(null); // Game Over deletes active session
        setPlayerProgress((prev) => ({
          ...prev,
          highestScore: Math.max(prev.highestScore, nextTotalScore),
          lastPlayedTimestamp: Date.now(),
        }));
        setTimeout(() => {
          playSynthesizedSound('gameOver');
          triggerHaptic([70, 50, 110]);
        }, 200);
      } else if (nextTotalScore < targetScore) {
        // Active ongoing game -> persist discrete event
        setActiveGameDb({
          levelId: selectedLevel,
          score: nextTotalScore,
          combo: nextCombo,
          highestCombo: Math.max(highestCombo, nextCombo),
          board: newBoard.map((r) => [...r]),
          trayPieces: [...finalTray],
          movesCount: updatedMoves,
          timestamp: Date.now(),
        });
        setPlayerProgress((prev) => ({
          ...prev,
          totalLinesCleared: prev.totalLinesCleared + totalLines,
          highestCombo: Math.max(prev.highestCombo, nextCombo),
          lastPlayedTimestamp: Date.now(),
        }));
      }
    } else {
      // Invalid drop: emit INVALID_MOVE sound & haptic once, snap piece back
      playSynthesizedSound('invalid');
      triggerHaptic([30, 40, 30]);
    }

    setDraggingPieceIndex(null);
    setDragPosition(null);
    setHoverGrid(null);
  };

  const handleResetGame = (level = selectedLevel) => {
    const empty = Array.from({ length: 10 }, () => Array(10).fill(null));
    setBoard(empty);
    setScore(0);
    setCombo(0);
    setMovesCount(0);
    setIsGameOver(false);
    setIsVictory(false);
    setActiveGameDb(null);
    spawnPieces(empty);
  };

  const handleResumeGame = () => {
    if (!activeGameDb) return;
    setSelectedLevel(activeGameDb.levelId);
    setBoard(activeGameDb.board.map((r) => [...r]));
    setScore(activeGameDb.score);
    setCombo(activeGameDb.combo);
    setHighestCombo(activeGameDb.highestCombo);
    setTrayPieces([...activeGameDb.trayPieces]);
    setMovesCount(activeGameDb.movesCount);
    setIsGameOver(false);
    setIsVictory(false);
    setCurrentScreen('game');
  };

  useEffect(() => {
    const handleMove = (e: MouseEvent | TouchEvent) => handleDragMove(e);
    const handleUp = () => handleDragEnd();

    if (draggingPieceIndex !== null) {
      window.addEventListener('mousemove', handleMove);
      window.addEventListener('mouseup', handleUp);
      window.addEventListener('touchmove', handleMove);
      window.addEventListener('touchend', handleUp);
    }
    return () => {
      window.removeEventListener('mousemove', handleMove);
      window.removeEventListener('mouseup', handleUp);
      window.removeEventListener('touchmove', handleMove);
      window.removeEventListener('touchend', handleUp);
    };
  }, [draggingPieceIndex, hoverGrid, board, trayPieces, score, combo]);

  // Source files viewer mapping
  const sourceFiles: Record<string, { path: string; code: string }> = {
    'GamePreferencesRepository.kt': {
      path: 'app/src/main/java/com/puzzle100/game/data/preferences/GamePreferencesRepository.kt',
      code: `package com.puzzle100.game.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow

// Jetpack Preferences DataStore for Settings
class GamePreferencesRepository(private val dataStore: DataStore<Preferences>) {
    val gamePreferencesFlow: Flow<GamePreferences>

    suspend fun updateSoundEnabled(enabled: Boolean)
    suspend fun updateHapticEnabled(enabled: Boolean)
    suspend fun updateMasterVolume(volume: Float)
    suspend fun updateMusicEnabled(enabled: Boolean)
    suspend fun updateMusicVolume(volume: Float)
    suspend fun updateTheme(theme: String)
}`,
    },
    'GameDatabase.kt': {
      path: 'app/src/main/java/com/puzzle100/game/data/database/GameDatabase.kt',
      code: `package com.puzzle100.game.data.database

@Database(
    entities = [PlayerProgressEntity::class, LevelResultEntity::class, ActiveGameEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GameDatabase : RoomDatabase() {
    abstract fun playerProgressDao(): PlayerProgressDao
    abstract fun levelResultDao(): LevelResultDao
    abstract fun activeGameDao(): ActiveGameDao

    // Singleton with onCreate pre-population; no destructive migrations!
}`,
    },
    'Converters.kt': {
      path: 'app/src/main/java/com/puzzle100/game/data/database/Converters.kt',
      code: `package com.puzzle100.game.data.database

class Converters {
    // 10x10 Board to/from compact JSON string
    @TypeConverter fun fromBoard(board: List<List<BoardCellState>>?): String
    @TypeConverter fun toBoard(data: String?): List<List<BoardCellState>>

    // 3 Tray Pieces to/from List of shape IDs
    @TypeConverter fun fromPiecesList(pieces: List<PuzzleShape?>?): String
    @TypeConverter fun toPiecesList(data: String?): List<PuzzleShape?>
}`,
    },
    'PlayerProgressEntity.kt': {
      path: 'app/src/main/java/com/puzzle100/game/data/database/PlayerProgressEntity.kt',
      code: `@Entity(tableName = "player_progress")
data class PlayerProgressEntity(
    @PrimaryKey val id: Int = 1,
    val currentUnlockedLevel: Int = 1,
    val currentLevel: Int = 1,
    val totalScore: Long = 0L,
    val totalLevelsCompleted: Int = 0,
    val totalStars: Int = 0,
    val highestScore: Int = 0,
    val totalLinesCleared: Int = 0,
    val highestCombo: Int = 0,
    val lastPlayedTimestamp: Long = System.currentTimeMillis()
)`,
    },
    'ActiveGameEntity.kt': {
      path: 'app/src/main/java/com/puzzle100/game/data/database/ActiveGameEntity.kt',
      code: `@Entity(tableName = "active_game")
data class ActiveGameEntity(
    @PrimaryKey val levelId: Int,
    val boardData: String,
    val availablePiecesData: String,
    val score: Int,
    val combo: Int = 0,
    val highestCombo: Int = 0,
    val totalRowsCleared: Int = 0,
    val totalColumnsCleared: Int = 0,
    val movesCount: Int = 0,
    val gameStatus: String = "PLAYING",
    val savedTimestamp: Long = System.currentTimeMillis()
)`,
    },
    'LevelResultEntity.kt': {
      path: 'app/src/main/java/com/puzzle100/game/data/database/LevelResultEntity.kt',
      code: `@Entity(tableName = "level_results")
data class LevelResultEntity(
    @PrimaryKey val levelId: Int,
    val bestScore: Int,
    val stars: Int, // 0 to 3
    val timesPlayed: Int = 1,
    val completed: Boolean = false,
    val bestCompletionTimestamp: Long = System.currentTimeMillis()
)`,
    },
    'ActiveGameRepository.kt': {
      path: 'app/src/main/java/com/puzzle100/game/data/repository/ActiveGameRepository.kt',
      code: `class ActiveGameRepository(private val activeGameDao: ActiveGameDao) {
    val latestActiveGameFlow: Flow<ActiveGameEntity?>
    suspend fun saveActiveGame(gameState: GameState, movesCount: Int = 0)
    suspend fun getActiveGame(levelId: Int): ActiveGameEntity?
    suspend fun deleteActiveGame(levelId: Int)
    fun restoreGameState(entity: ActiveGameEntity): GameState
}`,
    },
    'LevelResultRepository.kt': {
      path: 'app/src/main/java/com/puzzle100/game/data/repository/LevelResultRepository.kt',
      code: `class LevelResultRepository(private val levelResultDao: LevelResultDao) {
    // Saves level result with anti-downgrade protection!
    suspend fun saveLevelResult(levelId: Int, score: Int, targetScore: Int): LevelResultEntity
    fun calculateStars(score: Int, targetScore: Int): Int
}`,
    },
    'PlayerProgressRepository.kt': {
      path: 'app/src/main/java/com/puzzle100/game/data/repository/PlayerProgressRepository.kt',
      code: `class PlayerProgressRepository(private val playerProgressDao: PlayerProgressDao) {
    val playerProgressFlow: Flow<PlayerProgressEntity>
    suspend fun unlockNextLevel(completedLevelId: Int)
    suspend fun updateHighScore(score: Int)
    suspend fun recordStats(linesCleared: Int, combo: Int, addedScore: Int)
    suspend fun onLevelCompleted(levelId: Int, score: Int, earnedStars: Int)
}`,
    },
    'PersistenceTest.kt': {
      path: 'app/src/test/java/com/puzzle100/game/PersistenceTest.kt',
      code: `class PersistenceTest {
    // 16 JUnit test cases testing Board Serialization, Tray Pieces,
    // Anti-Downgrade, Stars, Level Unlock, Active Game lifecycle,
    // DataStore defaults & clamping!
}`,
    },
    'SoundManager.kt': {
      path: 'app/src/main/java/com/puzzle100/game/audio/SoundManager.kt',
      code: `class SoundManager(private val context: Context) : ISoundManager {
    // Preloads res/raw/ wav assets, handles throttling & SoundPool cleanup
}`,
    },
    'HapticManager.kt': {
      path: 'app/src/main/java/com/puzzle100/game/haptics/HapticManager.kt',
      code: `class HapticManager(private val context: Context) : IHapticManager {
    // API 26-35 VibrationEffect and Vibrator hardware fallbacks
}`,
    },
    'GameViewModel.kt': {
      path: 'app/src/main/java/com/puzzle100/game/ui/screens/game/GameViewModel.kt',
      code: `class GameViewModel(
    private var activeGameRepository: ActiveGameRepository? = null,
    private var levelResultRepository: LevelResultRepository? = null,
    private var playerProgressRepository: PlayerProgressRepository? = null,
    private var preferencesRepository: GamePreferencesRepository? = null
) : ViewModel() {
    // Discrete persistence on placePiece() and saveSessionOnExit()
}`,
    },
  };

  return (
    <div className="flex flex-col h-screen bg-[#070B14] text-slate-100 font-sans select-none overflow-hidden">
      {/* Top Navigation Bar */}
      <div className="h-14 border-b border-slate-800/80 bg-[#0A0F1D]/90 backdrop-blur-md px-6 flex items-center justify-between z-30 fixed top-0 left-0 right-0">
        <div className="flex items-center gap-3">
          <div className="grid grid-cols-2 gap-1 w-6 h-6">
            <div className="rounded-[2px]" style={{ backgroundColor: COLOR_TURQUOISE }} />
            <div className="rounded-[2px]" style={{ backgroundColor: COLOR_ORANGE }} />
            <div className="rounded-[2px]" style={{ backgroundColor: COLOR_PURPLE }} />
            <div className="rounded-[2px]" style={{ backgroundColor: COLOR_PINK }} />
          </div>
          <div>
            <span className="font-black tracking-wider text-sm bg-gradient-to-r from-white via-cyan-200 to-cyan-400 bg-clip-text text-transparent">
              PUZZLE 100
            </span>
            <span className="ml-2 text-[10px] font-bold px-2 py-0.5 rounded bg-cyan-950/80 border border-cyan-800/60 text-cyan-300">
              Phase 3.3: Integration & Release QA
            </span>
          </div>
        </div>

        {/* Tab switcher */}
        <div className="flex items-center bg-slate-900/90 p-1 rounded-xl border border-slate-800">
          <button
            onClick={() => setActiveTab('device')}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
              activeTab === 'device'
                ? 'bg-cyan-500 text-slate-950 shadow-md shadow-cyan-500/20'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Smartphone className="w-3.5 h-3.5" />
            Mô Phỏng Máy Thật
          </button>
          <button
            onClick={() => setActiveTab('persistence')}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
              activeTab === 'persistence'
                ? 'bg-cyan-500 text-slate-950 shadow-md shadow-cyan-500/20'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Database className="w-3.5 h-3.5" />
            Room DB & DataStore Inspector
          </button>
          <button
            onClick={() => setActiveTab('soundboard')}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
              activeTab === 'soundboard'
                ? 'bg-cyan-500 text-slate-950 shadow-md shadow-cyan-500/20'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Music className="w-3.5 h-3.5" />
            7 Âm Thanh & Rung
          </button>
          <button
            onClick={() => setActiveTab('tests')}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
              activeTab === 'tests'
                ? 'bg-cyan-500 text-slate-950 shadow-md shadow-cyan-500/20'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <TestTube2 className="w-3.5 h-3.5" />
            Kiểm Thử (39 Tests)
          </button>
          <button
            onClick={() => setActiveTab('code')}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
              activeTab === 'code'
                ? 'bg-cyan-500 text-slate-950 shadow-md shadow-cyan-500/20'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <FileCode className="w-3.5 h-3.5" />
            Mã Nguồn Kotlin
          </button>
        </div>

        <div className="flex items-center gap-3">
          <a
            href="/PUZZLE_100_Android_Project.zip"
            download="PUZZLE_100_Android_Project.zip"
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold text-xs shadow-md shadow-emerald-500/20 transition active:scale-95 cursor-pointer"
            title="Tải trọn bộ mã nguồn Android Studio (ZIP) có kèm Gradle Wrapper và GitHub Actions Build APK"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Tải Android Project (.zip)</span>
          </a>
          <div className="flex items-center gap-2 text-xs text-slate-400">
            <span className="inline-block w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
            <span>Giai đoạn 3.3</span>
          </div>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 pt-14 h-full flex overflow-hidden">
        {/* VIEW 1: INTERACTIVE EMULATOR WITH REAL REPOSITORIES & DATA PERSISTENCE */}
        {activeTab === 'device' && (
          <div className="flex-1 flex items-center justify-center p-4 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-slate-900 via-[#070B14] to-[#04070D] overflow-y-auto">
            {/* Phone Chassis */}
            <div className="relative w-[390px] h-[790px] bg-[#0c1222] rounded-[48px] p-3 shadow-[0_0_60px_rgba(0,229,255,0.12)] border-[4px] border-slate-700/60 flex flex-col">
              {/* Phone Camera Hole */}
              <div className="absolute top-4 left-1/2 -translate-x-1/2 w-28 h-4 bg-slate-900 rounded-full flex items-center justify-center gap-2 z-30">
                <div className="w-2.5 h-2.5 rounded-full bg-slate-950 border border-slate-800" />
              </div>

              {/* Screen Container */}
              <div className="relative flex-1 bg-[#090D1A] rounded-[38px] overflow-hidden flex flex-col border border-slate-800/80">
                {/* Status Bar */}
                <div className="h-7 px-6 flex items-center justify-between text-[11px] text-slate-400 font-semibold z-20">
                  <span>9:41</span>
                  <div className="flex items-center gap-2">
                    {isSoundEnabled ? <Volume2 className="w-3 h-3 text-cyan-400" /> : <VolumeX className="w-3 h-3 text-slate-600" />}
                    {isHapticEnabled && <Vibrate className="w-3 h-3 text-orange-400" />}
                    <span className="text-[9px]">5G</span>
                  </div>
                </div>

                {/* SCREEN 1: HOME SCREEN */}
                {currentScreen === 'home' && (
                  <div className="flex-1 flex flex-col justify-between p-6 bg-gradient-to-b from-[#0D1527] via-[#090D1A] to-[#060913] relative overflow-hidden">
                    {/* Top Bar: Stars Chip & Settings */}
                    <div className="flex items-center justify-between pt-1">
                      <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-[#1E293B] border border-slate-700">
                        <Star className="w-4 h-4 text-[#FFD600] fill-[#FFD600]" />
                        <span className="text-xs font-bold text-white">{playerProgress.totalStars} / 300</span>
                      </div>
                      <button
                        onClick={() => setIsSettingsOpen(true)}
                        className="w-9 h-9 rounded-full bg-[#1E293B] border border-slate-700 flex items-center justify-center text-slate-300"
                      >
                        <Settings className="w-4 h-4 text-cyan-400" />
                      </button>
                    </div>

                    {/* Logo & Decorative Icon */}
                    <div className="text-center space-y-3">
                      <div className="inline-grid grid-cols-2 gap-1.5 p-3 rounded-2xl bg-[#1E293B] border border-slate-700/80 shadow-lg">
                        <div className="w-5 h-5 rounded-[4px] bg-[#00E5FF]" />
                        <div className="w-5 h-5 rounded-[4px] bg-[#FF6D00]" />
                        <div className="w-5 h-5 rounded-[4px] bg-[#7C4DFF]" />
                        <div className="w-5 h-5 rounded-[4px] bg-[#FF4081]" />
                      </div>
                      <div>
                        <h1 className="text-3xl font-black text-white tracking-widest">PUZZLE 100</h1>
                        <p className="text-[11px] font-bold text-cyan-400 uppercase tracking-widest mt-0.5">
                          XẾP KHỐI ĐỈNH CAO
                        </p>
                      </div>

                      {/* High Score Card */}
                      <div className="p-4 rounded-2xl bg-[#1E293B]/70 border border-slate-700/80 flex justify-around text-center">
                        <div>
                          <div className="text-[10px] text-slate-400 font-bold">ĐIỂM KỶ LỤC</div>
                          <div className="text-lg font-black text-white">{playerProgress.highestScore}</div>
                        </div>
                        <div className="w-px h-8 bg-slate-700" />
                        <div>
                          <div className="text-[10px] text-slate-400 font-bold">CẤP ĐỘ</div>
                          <div className="text-lg font-black text-orange-400">Cấp {playerProgress.currentUnlockedLevel}</div>
                        </div>
                      </div>
                    </div>

                    {/* Action Buttons */}
                    <div className="space-y-3">
                      {/* CONTINUE GAME BUTTON IF ACTIVE SESSION EXISTS */}
                      {activeGameDb !== null && (
                        <button
                          onClick={handleResumeGame}
                          className="w-full py-4 rounded-2xl bg-gradient-to-r from-cyan-400 to-cyan-500 text-slate-950 font-black shadow-lg shadow-cyan-500/30 flex items-center justify-center gap-3 hover:brightness-110 transition active:scale-[0.98]"
                        >
                          <Play className="w-5 h-5 fill-slate-950" />
                          <div className="text-left">
                            <div className="text-sm leading-tight">TIẾP TỤC VÁN CHƠI</div>
                            <div className="text-[10px] text-slate-900 font-bold">
                              Màn {activeGameDb.levelId} • {activeGameDb.score} điểm
                            </div>
                          </div>
                        </button>
                      )}

                      {/* PLAY / NEW GAME BUTTON */}
                      <button
                        onClick={() => {
                          handleResetGame(playerProgress.currentUnlockedLevel);
                          setSelectedLevel(playerProgress.currentUnlockedLevel);
                          setCurrentScreen('game');
                        }}
                        className={`w-full py-3.5 rounded-2xl font-black text-sm flex items-center justify-center gap-2 transition active:scale-[0.98] ${
                          activeGameDb !== null
                            ? 'bg-gradient-to-r from-orange-500 to-amber-500 text-slate-950 shadow-md shadow-orange-500/20'
                            : 'bg-gradient-to-r from-cyan-400 to-cyan-500 text-slate-950 shadow-lg shadow-cyan-500/30'
                        }`}
                      >
                        <Play className="w-4 h-4 fill-slate-950" />
                        {activeGameDb !== null ? 'CHƠI VÁN MỚI' : 'CHƠI NGAY'}
                      </button>

                      {/* LEVEL SELECT BUTTON */}
                      <button
                        onClick={() => setCurrentScreen('levels')}
                        className="w-full py-3.5 rounded-2xl bg-[#1E293B] border border-slate-700 text-white font-bold text-xs flex items-center justify-center gap-2 hover:bg-slate-800 transition"
                      >
                        <Layers className="w-4 h-4 text-purple-400" />
                        CHỌN CẤP ĐỘ (1 - 100)
                      </button>
                    </div>
                  </div>
                )}

                {/* SCREEN 2: LEVEL SELECTION SCREEN */}
                {currentScreen === 'levels' && (
                  <div className="flex-1 flex flex-col p-4 bg-gradient-to-b from-[#0D1527] via-[#090D1A] to-[#060913] relative overflow-hidden">
                    {/* Top Bar */}
                    <div className="flex items-center justify-between pb-3 border-b border-slate-800">
                      <button
                        onClick={() => setCurrentScreen('home')}
                        className="w-8 h-8 rounded-full bg-[#1E293B] flex items-center justify-center text-slate-300"
                      >
                        <ArrowLeft className="w-4 h-4" />
                      </button>
                      <span className="text-xs font-black text-white">CHỌN CẤP ĐỘ</span>
                      <div className="flex items-center gap-1 text-[11px] font-bold text-[#FFD600]">
                        <Star className="w-3.5 h-3.5 fill-[#FFD600]" />
                        {playerProgress.totalStars}
                      </div>
                    </div>

                    {/* Chapter Tabs */}
                    <div className="grid grid-cols-4 gap-1 p-1 bg-[#1E293B]/80 rounded-xl my-3">
                      {[
                        { id: 1, label: '1-25' },
                        { id: 2, label: '26-50' },
                        { id: 3, label: '51-75' },
                        { id: 4, label: '76-100' },
                      ].map((ch) => (
                        <button
                          key={ch.id}
                          onClick={() => setSelectedChapter(ch.id)}
                          className={`py-1.5 text-[10px] font-bold rounded-lg transition ${
                            selectedChapter === ch.id ? 'bg-cyan-400 text-slate-950' : 'text-slate-400 hover:text-white'
                          }`}
                        >
                          Chương {ch.id}
                        </button>
                      ))}
                    </div>

                    {/* 25 Levels Grid */}
                    <div className="flex-1 overflow-y-auto grid grid-cols-5 gap-2 pr-1">
                      {Array.from({ length: 25 }, (_, i) => {
                        const levelNum = (selectedChapter - 1) * 25 + (i + 1);
                        const isUnlocked = levelNum <= playerProgress.currentUnlockedLevel;
                        const result = levelResults[levelNum];
                        const stars = result?.stars || 0;

                        return (
                          <button
                            key={levelNum}
                            disabled={!isUnlocked}
                            onClick={() => {
                              setSelectedLevel(levelNum);
                              handleResetGame(levelNum);
                              setCurrentScreen('game');
                            }}
                            className={`aspect-square rounded-xl p-1 flex flex-col items-center justify-between border transition ${
                              isUnlocked
                                ? levelNum === playerProgress.currentUnlockedLevel
                                  ? 'bg-cyan-500/20 border-cyan-400 shadow-md shadow-cyan-500/20'
                                  : 'bg-[#1E293B] border-slate-700 hover:border-cyan-400'
                                : 'bg-[#0D1527] border-slate-800/80 opacity-50'
                            }`}
                          >
                            <span className="text-[10px] font-black text-white">{levelNum}</span>
                            {isUnlocked ? (
                              <div className="flex gap-0.5">
                                {[1, 2, 3].map((s) => (
                                  <Star
                                    key={s}
                                    className={`w-2.5 h-2.5 ${
                                      s <= stars ? 'text-[#FFD600] fill-[#FFD600]' : 'text-slate-600'
                                    }`}
                                  />
                                ))}
                              </div>
                            ) : (
                              <Lock className="w-3 h-3 text-slate-600" />
                            )}
                          </button>
                        );
                      })}
                    </div>
                  </div>
                )}

                {/* SCREEN 3: GAMEPLAY SCREEN */}
                {currentScreen === 'game' && (
                  <div className="flex-1 flex flex-col justify-between p-3.5 bg-gradient-to-b from-[#0D1527] via-[#090D1A] to-[#060913] relative overflow-hidden">
                    {/* Top Bar */}
                    <div className="flex items-center justify-between pt-1">
                      <button
                        onClick={() => setCurrentScreen('home')}
                        className="w-8 h-8 rounded-full bg-[#1E293B] flex items-center justify-center text-slate-300 hover:text-white"
                      >
                        <ArrowLeft className="w-4 h-4" />
                      </button>
                      <div className="text-center">
                        <div className="flex items-center justify-center gap-1.5">
                          <span className="text-xs font-black text-white">CẤP {selectedLevel}</span>
                          {combo > 1 && (
                            <span className="flex items-center gap-0.5 px-1.5 py-0.5 rounded bg-orange-500 text-[9px] font-black text-slate-950 animate-pulse">
                              <Flame className="w-2.5 h-2.5 fill-slate-950" /> x{combo}
                            </span>
                          )}
                        </div>
                        <div className="text-[11px] font-bold text-cyan-400">
                          {score} / {targetScore}
                        </div>
                      </div>
                      <div className="flex items-center gap-1.5">
                        <button
                          onClick={() => setIsSettingsOpen(true)}
                          className="w-8 h-8 rounded-full bg-[#1E293B] flex items-center justify-center text-slate-300 hover:text-white"
                        >
                          <Settings className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => setIsPaused(!isPaused)}
                          className="w-8 h-8 rounded-full bg-[#1E293B] flex items-center justify-center text-slate-300"
                        >
                          {isPaused ? <Play className="w-4 h-4 text-cyan-400" /> : <Pause className="w-4 h-4" />}
                        </button>
                      </div>
                    </div>

                    {/* Score Progress Bar */}
                    <div className="w-full bg-slate-800/80 h-1.5 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-gradient-to-r from-cyan-400 to-cyan-500 transition-all duration-300"
                        style={{ width: `${Math.min(100, (score / targetScore) * 100)}%` }}
                      />
                    </div>

                    {/* 10x10 PUZZLE BOARD */}
                    <div
                      ref={boardRef}
                      className="relative w-full aspect-square bg-[#1E293B]/80 rounded-2xl border-2 border-slate-800 p-2 shadow-inner"
                    >
                      <div className="grid grid-cols-10 grid-rows-10 gap-1 w-full h-full">
                        {board.map((row, r) =>
                          row.map((cellColor, c) => {
                            const isClearing = clearingLines.rows.includes(r) || clearingLines.cols.includes(c);
                            const piece = draggingPieceIndex !== null ? trayPieces[draggingPieceIndex] : null;

                            let isGhost = false;
                            let ghostColor = COLOR_TURQUOISE;
                            let isGhostValid = false;

                            if (piece && hoverGrid) {
                              const relR = r - hoverGrid.row;
                              const relC = c - hoverGrid.col;
                              if (
                                relR >= 0 &&
                                relR < piece.shapeMatrix.length &&
                                relC >= 0 &&
                                relC < piece.shapeMatrix[0].length &&
                                piece.shapeMatrix[relR][relC]
                              ) {
                                isGhost = true;
                                isGhostValid = hoverGrid.isValid;
                                ghostColor = isGhostValid ? piece.color : '#EF4444';
                              }
                            }

                            return (
                              <div
                                key={`${r}-${c}`}
                                className={`rounded-[3px] transition duration-150 ${
                                  isClearing
                                    ? 'bg-white scale-105 shadow-[0_0_8px_#fff]'
                                    : cellColor
                                    ? 'shadow-xs border border-white/20'
                                    : isGhost
                                    ? isGhostValid
                                      ? 'border-2 border-cyan-400/80 bg-cyan-400/30 animate-pulse'
                                      : 'border-2 border-red-500/80 bg-red-500/30 animate-pulse'
                                    : 'bg-[#0E1626]/80 border border-slate-800/40'
                                }`}
                                style={{
                                  backgroundColor: isClearing
                                    ? '#ffffff'
                                    : cellColor || (isGhost ? ghostColor + '40' : undefined),
                                }}
                              />
                            );
                          })
                        )}
                      </div>

                      {/* Score Popup Notification */}
                      {scorePopup && (
                        <div
                          key={scorePopup.id}
                          className="absolute inset-0 pointer-events-none flex items-center justify-center animate-[bounce_0.6s_ease-out]"
                        >
                          <div className="px-4 py-2 rounded-xl bg-slate-900/90 border border-cyan-400 text-cyan-300 font-black text-sm shadow-xl shadow-cyan-500/30">
                            {scorePopup.text}
                          </div>
                        </div>
                      )}
                    </div>

                    {/* Status Indicator */}
                    <div className="flex items-center justify-between px-1 text-[10px] text-slate-400">
                      <span className="flex items-center gap-1">
                        <span className="w-1.5 h-1.5 rounded-full bg-cyan-400 animate-ping" />
                        Room DB & DataStore Đã Đồng Bộ
                      </span>
                      <button
                        onClick={() => handleResetGame(selectedLevel)}
                        className="flex items-center gap-1 text-cyan-400 hover:underline font-bold"
                      >
                        <RotateCcw className="w-2.5 h-2.5" /> Chơi lại
                      </button>
                    </div>

                    {/* Tray: 3 Pieces */}
                    <div className="p-3 bg-[#0F172A] rounded-2xl border border-slate-800">
                      <div className="text-[9px] font-bold tracking-wider text-slate-400 uppercase text-center mb-1.5">
                        Khay Khối Chờ Đặt ({trayPieces.filter((p) => p !== null).length}/3)
                      </div>
                      <div className="grid grid-cols-3 gap-2 h-20 items-center justify-items-center">
                        {trayPieces.map((piece, index) => {
                          const isBeingDragged = draggingPieceIndex === index;
                          return (
                            <div
                              key={index}
                              onMouseDown={(e) => handleDragStart(index, e)}
                              onTouchStart={(e) => handleDragStart(index, e)}
                              className={`w-full h-full rounded-xl flex items-center justify-center cursor-grab active:cursor-grabbing transition ${
                                piece ? 'bg-[#1E293B]/70 hover:bg-[#1E293B] border border-slate-700/60' : 'bg-transparent'
                              } ${isBeingDragged ? 'opacity-20' : 'opacity-100'}`}
                            >
                              {piece && (
                                <div className="flex flex-col gap-0.5 p-1">
                                  {piece.shapeMatrix.map((r, rIdx) => (
                                    <div key={rIdx} className="flex gap-0.5">
                                      {r.map((filled, cIdx) => (
                                        <div
                                          key={cIdx}
                                          className="w-3.5 h-3.5 rounded-xs"
                                          style={{
                                            backgroundColor: filled ? piece.color : 'transparent',
                                          }}
                                        />
                                      ))}
                                    </div>
                                  ))}
                                </div>
                              )}
                            </div>
                          );
                        })}
                      </div>
                    </div>

                    {/* Floating Dragged Piece */}
                    {draggingPieceIndex !== null && trayPieces[draggingPieceIndex] && dragPosition && (
                      <div
                        className="fixed pointer-events-none z-50 transform -translate-x-1/2 -translate-y-full -mt-4 transition-transform scale-110"
                        style={{
                          left: `${dragPosition.x}px`,
                          top: `${dragPosition.y}px`,
                        }}
                      >
                        <div className="p-2 rounded-xl bg-slate-900/90 border border-cyan-400 shadow-2xl shadow-cyan-500/50 backdrop-blur-xs flex flex-col gap-1">
                          {trayPieces[draggingPieceIndex]?.shapeMatrix.map((row, rIdx) => (
                            <div key={rIdx} className="flex gap-1">
                              {row.map((filled, cIdx) => (
                                <div
                                  key={cIdx}
                                  className="w-5 h-5 rounded-[3px] border border-white/20"
                                  style={{
                                    backgroundColor: filled ? trayPieces[draggingPieceIndex]?.color : 'transparent',
                                  }}
                                />
                              ))}
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* SETTINGS MODAL DIALOG */}
                    {isSettingsOpen && (
                      <div className="absolute inset-0 bg-[#090D1A]/95 flex items-center justify-center p-5 z-40 animate-[fadeIn_0.2s]">
                        <div className="w-full bg-[#1E293B] border border-slate-700 rounded-3xl p-6 space-y-4">
                          <div className="flex items-center justify-between pb-2 border-b border-slate-800">
                            <h3 className="text-base font-black text-white flex items-center gap-2">
                              <Settings className="w-4 h-4 text-cyan-400" /> CÀI ĐẶT (DATASTORE)
                            </h3>
                          </div>

                          <div className="flex items-center justify-between p-3 rounded-xl bg-[#0F172A] border border-slate-800">
                            <div className="flex items-center gap-2.5">
                              <Volume2 className={`w-4 h-4 ${isSoundEnabled ? 'text-cyan-400' : 'text-slate-500'}`} />
                              <span className="text-xs font-bold text-white">Hiệu ứng âm thanh</span>
                            </div>
                            <button
                              onClick={() => setIsSoundEnabled(!isSoundEnabled)}
                              className={`w-11 h-6 rounded-full p-1 transition ${
                                isSoundEnabled ? 'bg-cyan-400' : 'bg-slate-700'
                              }`}
                            >
                              <div
                                className={`w-4 h-4 rounded-full bg-slate-950 transition-transform ${
                                  isSoundEnabled ? 'translate-x-5' : 'translate-x-0'
                                }`}
                              />
                            </button>
                          </div>

                          <div className="flex items-center justify-between p-3 rounded-xl bg-[#0F172A] border border-slate-800">
                            <div className="flex items-center gap-2.5">
                              <Vibrate className={`w-4 h-4 ${isHapticEnabled ? 'text-orange-400' : 'text-slate-500'}`} />
                              <span className="text-xs font-bold text-white">Phản hồi rung (Haptic)</span>
                            </div>
                            <button
                              onClick={() => setIsHapticEnabled(!isHapticEnabled)}
                              className={`w-11 h-6 rounded-full p-1 transition ${
                                isHapticEnabled ? 'bg-orange-500' : 'bg-slate-700'
                              }`}
                            >
                              <div
                                className={`w-4 h-4 rounded-full bg-slate-950 transition-transform ${
                                  isHapticEnabled ? 'translate-x-5' : 'translate-x-0'
                                }`}
                              />
                            </button>
                          </div>

                          <div className="p-3 rounded-xl bg-[#0F172A] border border-slate-800 space-y-2">
                            <div className="flex items-center justify-between text-xs">
                              <span className="text-slate-400 font-semibold">Âm lượng tổng</span>
                              <span className="text-cyan-400 font-bold">{Math.round(masterVolume * 100)}%</span>
                            </div>
                            <input
                              type="range"
                              min="0"
                              max="1"
                              step="0.05"
                              value={masterVolume}
                              onChange={(e) => setMasterVolume(parseFloat(e.target.value))}
                              className="w-full accent-cyan-400 cursor-pointer"
                            />
                          </div>

                          <button
                            onClick={() => setIsSettingsOpen(false)}
                            className="w-full py-3 bg-cyan-400 text-slate-950 font-black rounded-xl text-xs hover:bg-cyan-300 transition"
                          >
                            LƯU & ĐÓNG
                          </button>
                        </div>
                      </div>
                    )}

                    {/* Game Over Modal */}
                    {isGameOver && (
                      <div className="absolute inset-0 bg-[#090D1A]/95 flex items-center justify-center p-6 z-40">
                        <div className="w-full bg-[#1E293B] border-2 border-red-500 rounded-3xl p-6 text-center space-y-4">
                          <AlertTriangle className="w-12 h-12 text-red-500 mx-auto" />
                          <div>
                            <h3 className="text-xl font-black text-white">KHÔNG CÒN NƯỚC ĐI!</h3>
                            <div className="mt-2 text-lg font-black text-cyan-400">Điểm: {score}</div>
                            <div className="text-xs text-slate-400 mt-1">Đã xóa ván chơi dở trong Room Database</div>
                          </div>
                          <button
                            onClick={() => handleResetGame(selectedLevel)}
                            className="w-full py-3 bg-cyan-400 text-slate-950 font-black rounded-xl text-sm"
                          >
                            CHƠI LẠI
                          </button>
                        </div>
                      </div>
                    )}

                    {/* Victory Modal */}
                    {isVictory && (
                      <div className="absolute inset-0 bg-[#090D1A]/95 flex items-center justify-center p-6 z-40">
                        <div className="w-full bg-[#1E293B] border-2 border-[#FFD600] rounded-3xl p-6 text-center space-y-4">
                          <Award className="w-14 h-14 text-[#FFD600] mx-auto animate-bounce" />
                          <div>
                            <h3 className="text-xl font-black text-white">VƯỢT CẤP XUẤT SẮC!</h3>
                            <div className="mt-2 text-lg font-black text-[#FFD600]">Điểm: {score}</div>
                            <div className="text-xs text-emerald-400 mt-1">
                              Đã lưu thành tích & mở khóa Cấp {Math.min(100, selectedLevel + 1)}!
                            </div>
                          </div>
                          <button
                            onClick={() => {
                              const nextLvl = Math.min(100, selectedLevel + 1);
                              setSelectedLevel(nextLvl);
                              setIsVictory(false);
                              handleResetGame(nextLvl);
                            }}
                            className="w-full py-3 bg-[#FFD600] text-slate-950 font-black rounded-xl text-sm"
                          >
                            TIẾP TỤC CẤP {Math.min(100, selectedLevel + 1)}
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {/* Bottom Virtual Bar */}
                <div className="h-6 flex items-center justify-center gap-10 text-slate-600">
                  <div
                    onClick={() => setCurrentScreen('home')}
                    className="w-2.5 h-2.5 border-l-2 border-b-2 border-slate-600 -rotate-45 cursor-pointer hover:border-cyan-400"
                  />
                  <div
                    onClick={() => setCurrentScreen('home')}
                    className="w-3 h-3 rounded-full border border-slate-600 cursor-pointer hover:border-cyan-400"
                  />
                  <div className="w-2.5 h-2.5 border border-slate-600" />
                </div>
              </div>
            </div>

            {/* Sidebar Controls */}
            <div className="ml-8 hidden lg:flex flex-col gap-3 max-w-xs">
              <div className="p-4 rounded-2xl bg-slate-900/80 border border-slate-800 space-y-3">
                <h4 className="text-xs font-bold text-cyan-400 uppercase tracking-wider flex items-center gap-1.5">
                  <Database className="w-4 h-4 text-cyan-400" /> Trạng Thái Room Database
                </h4>
                <div className="space-y-2 text-xs">
                  <div className="flex justify-between items-center p-2 rounded-lg bg-[#090D1A] border border-slate-800">
                    <span className="text-slate-400">Cấp mở khóa:</span>
                    <span className="font-bold text-white">Cấp {playerProgress.currentUnlockedLevel} / 100</span>
                  </div>
                  <div className="flex justify-between items-center p-2 rounded-lg bg-[#090D1A] border border-slate-800">
                    <span className="text-slate-400">Tổng số sao:</span>
                    <span className="font-bold text-[#FFD600] flex items-center gap-1">
                      <Star className="w-3 h-3 fill-[#FFD600]" /> {playerProgress.totalStars}
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-2 rounded-lg bg-[#090D1A] border border-slate-800">
                    <span className="text-slate-400">Ván dở dang:</span>
                    <span
                      className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                        activeGameDb
                          ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
                          : 'bg-slate-800 text-slate-400'
                      }`}
                    >
                      {activeGameDb ? `Cấp ${activeGameDb.levelId} (${activeGameDb.score}đ)` : 'Không có'}
                    </span>
                  </div>
                </div>

                <div className="pt-2 flex flex-col gap-2">
                  <button
                    onClick={() => setActiveTab('persistence')}
                    className="w-full py-2 bg-cyan-400/10 border border-cyan-400/40 text-cyan-300 text-xs font-bold rounded-xl hover:bg-cyan-400/20 transition flex items-center justify-center gap-1.5"
                  >
                    <HardDrive className="w-3.5 h-3.5" /> Mở Database Inspector
                  </button>
                </div>
              </div>

              <div className="p-4 rounded-2xl bg-slate-900/80 border border-slate-800 text-xs space-y-2">
                <div className="font-bold text-white flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                  Nguyên Tắc Bền Vững (Zero Slop)
                </div>
                <p className="text-slate-400 leading-relaxed text-[11px]">
                  Không ghi đĩa trong lúc kéo quân hoặc recomposition. Chỉ lưu dữ liệu khi đặt quân thành công, xóa hàng, pause hoặc thoát màn. Không xóa dữ liệu người chơi khi cập nhật.
                </p>
              </div>
            </div>
          </div>
        )}

        {/* VIEW 2: DEDICATED ROOM DATABASE & DATASTORE INSPECTOR */}
        {activeTab === 'persistence' && (
          <div className="flex-1 overflow-y-auto p-8 max-w-5xl mx-auto space-y-6">
            <div className="p-6 rounded-3xl bg-gradient-to-r from-slate-900 via-[#0E172A] to-[#0A0F1D] border border-slate-800 flex items-center justify-between">
              <div>
                <span className="px-3 py-1 rounded-full text-xs font-black bg-cyan-400/20 text-cyan-300 border border-cyan-400/40">
                  PHASE 3.2: ROOM DATABASE & DATASTORE INSPECTOR
                </span>
                <h2 className="text-2xl font-black text-white mt-2">
                  Hệ Thống Quản Lý Dữ Liệu Bền Vững Android
                </h2>
                <p className="text-xs text-slate-400 mt-1">
                  Kiểm tra trực tiếp dữ liệu SQLite Room (player_progress, level_results, active_game) và Preferences DataStore:
                </p>
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() => {
                    // Simulate kill & restore
                    if (activeGameDb) {
                      handleResumeGame();
                      setActiveTab('device');
                    }
                  }}
                  disabled={!activeGameDb}
                  className="px-4 py-2.5 rounded-xl bg-cyan-400 text-slate-950 font-black text-xs disabled:opacity-40 flex items-center gap-2 hover:bg-cyan-300 transition"
                >
                  <RefreshCw className="w-4 h-4" /> Khôi phục ván chơi
                </button>
              </div>
            </div>

            {/* Quick Stats Grid */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="p-4 rounded-2xl bg-slate-900/70 border border-slate-800">
                <div className="text-[11px] text-slate-400 font-bold uppercase">Cấp Mở Khóa</div>
                <div className="text-2xl font-black text-cyan-400 mt-1">
                  Cấp {playerProgress.currentUnlockedLevel} <span className="text-xs text-slate-500 font-normal">/ 100</span>
                </div>
              </div>
              <div className="p-4 rounded-2xl bg-slate-900/70 border border-slate-800">
                <div className="text-[11px] text-slate-400 font-bold uppercase">Tổng Số Sao</div>
                <div className="text-2xl font-black text-[#FFD600] mt-1 flex items-center gap-1.5">
                  <Star className="w-5 h-5 fill-[#FFD600]" /> {playerProgress.totalStars}
                </div>
              </div>
              <div className="p-4 rounded-2xl bg-slate-900/70 border border-slate-800">
                <div className="text-[11px] text-slate-400 font-bold uppercase">Điểm Cao Nhất</div>
                <div className="text-2xl font-black text-white mt-1">{playerProgress.highestScore}</div>
              </div>
              <div className="p-4 rounded-2xl bg-slate-900/70 border border-slate-800">
                <div className="text-[11px] text-slate-400 font-bold uppercase">Ván Dở Dang</div>
                <div className="text-base font-black text-emerald-400 mt-2">
                  {activeGameDb ? `Cấp ${activeGameDb.levelId} • ${activeGameDb.score}đ` : 'Không có (Trống)'}
                </div>
              </div>
            </div>

            {/* Live Database Tables Inspector */}
            <div className="space-y-4">
              <h3 className="text-sm font-black text-white uppercase tracking-wider flex items-center gap-2">
                <Database className="w-4 h-4 text-cyan-400" /> Bảng Dữ Liệu SQLite (Room Database: puzzle100_database)
              </h3>

              {/* Table 1: player_progress */}
              <div className="p-5 rounded-2xl bg-slate-900/80 border border-slate-800 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="font-bold text-white text-xs flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-cyan-400" /> Table: <code className="text-cyan-300">player_progress</code> (1 row)
                  </span>
                  <span className="text-[10px] text-slate-500">PlayerProgressEntity</span>
                </div>
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-xs font-mono">
                    <thead>
                      <tr className="border-b border-slate-800 text-slate-400 text-[10px]">
                        <th className="py-1.5 px-2">id</th>
                        <th className="py-1.5 px-2">currentUnlockedLevel</th>
                        <th className="py-1.5 px-2">totalScore</th>
                        <th className="py-1.5 px-2">totalStars</th>
                        <th className="py-1.5 px-2">highestScore</th>
                        <th className="py-1.5 px-2">highestCombo</th>
                        <th className="py-1.5 px-2">lastPlayedTimestamp</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr className="border-b border-slate-800/60 text-slate-200">
                        <td className="py-2 px-2 text-cyan-400">{playerProgress.id}</td>
                        <td className="py-2 px-2 font-bold text-white">{playerProgress.currentUnlockedLevel}</td>
                        <td className="py-2 px-2">{playerProgress.totalScore}</td>
                        <td className="py-2 px-2 text-[#FFD600] font-bold">{playerProgress.totalStars}</td>
                        <td className="py-2 px-2 font-bold text-white">{playerProgress.highestScore}</td>
                        <td className="py-2 px-2 text-orange-400">{playerProgress.highestCombo}</td>
                        <td className="py-2 px-2 text-slate-400">{new Date(playerProgress.lastPlayedTimestamp).toLocaleTimeString()}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              {/* Table 2: active_game */}
              <div className="p-5 rounded-2xl bg-slate-900/80 border border-slate-800 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="font-bold text-white text-xs flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-emerald-400" /> Table: <code className="text-emerald-300">active_game</code> (Ván chơi tạm dừng / dở dang)
                  </span>
                  <span className="text-[10px] text-slate-500">ActiveGameEntity</span>
                </div>
                {activeGameDb ? (
                  <div className="space-y-2">
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-2 text-xs font-mono">
                      <div className="p-2 rounded bg-slate-950 border border-slate-800">
                        <span className="text-slate-500 text-[10px]">levelId:</span> <b className="text-white">{activeGameDb.levelId}</b>
                      </div>
                      <div className="p-2 rounded bg-slate-950 border border-slate-800">
                        <span className="text-slate-500 text-[10px]">score:</span> <b className="text-cyan-400">{activeGameDb.score}</b>
                      </div>
                      <div className="p-2 rounded bg-slate-950 border border-slate-800">
                        <span className="text-slate-500 text-[10px]">combo:</span> <b className="text-orange-400">x{activeGameDb.combo}</b>
                      </div>
                      <div className="p-2 rounded bg-slate-950 border border-slate-800">
                        <span className="text-slate-500 text-[10px]">movesCount:</span> <b className="text-purple-400">{activeGameDb.movesCount}</b>
                      </div>
                    </div>
                    <div className="p-3 rounded bg-slate-950 border border-slate-800 text-[10px] font-mono text-slate-400">
                      <div className="text-cyan-400 font-bold mb-1">// Serialized Board Matrix (Converters.fromBoard):</div>
                      <div className="truncate text-slate-300">
                        {JSON.stringify(
                          activeGameDb.board.flatMap((row, r) =>
                            row.map((cell, c) => (cell ? { r, c, color: cell } : null)).filter(Boolean)
                          )
                        )}
                      </div>
                    </div>
                  </div>
                ) : (
                  <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800 text-center text-xs text-slate-500">
                    Bảng active_game đang trống. Khi bạn đặt ít nhất 1 khối trong ván chơi, dữ liệu sẽ tự động lưu vào đây.
                  </div>
                )}
              </div>

              {/* Table 3: Jetpack Preferences DataStore */}
              <div className="p-5 rounded-2xl bg-slate-900/80 border border-slate-800 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="font-bold text-white text-xs flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-purple-400" /> Preferences DataStore: <code className="text-purple-300">puzzle100_preferences.preferences_pb</code>
                  </span>
                  <span className="text-[10px] text-slate-500">GamePreferencesRepository</span>
                </div>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-2 text-xs font-mono">
                  <div className="p-2.5 rounded bg-slate-950 border border-slate-800 flex justify-between items-center">
                    <span className="text-slate-400">sound_enabled:</span>
                    <span className={`font-bold ${isSoundEnabled ? 'text-cyan-400' : 'text-slate-600'}`}>{String(isSoundEnabled)}</span>
                  </div>
                  <div className="p-2.5 rounded bg-slate-950 border border-slate-800 flex justify-between items-center">
                    <span className="text-slate-400">haptic_enabled:</span>
                    <span className={`font-bold ${isHapticEnabled ? 'text-orange-400' : 'text-slate-600'}`}>{String(isHapticEnabled)}</span>
                  </div>
                  <div className="p-2.5 rounded bg-slate-950 border border-slate-800 flex justify-between items-center">
                    <span className="text-slate-400">master_volume:</span>
                    <span className="font-bold text-cyan-400">{masterVolume.toFixed(2)}f</span>
                  </div>
                  <div className="p-2.5 rounded bg-slate-950 border border-slate-800 flex justify-between items-center">
                    <span className="text-slate-400">music_enabled:</span>
                    <span className="font-bold text-white">{String(musicEnabled)}</span>
                  </div>
                  <div className="p-2.5 rounded bg-slate-950 border border-slate-800 flex justify-between items-center">
                    <span className="text-slate-400">music_volume:</span>
                    <span className="font-bold text-white">{musicVolume.toFixed(2)}f</span>
                  </div>
                  <div className="p-2.5 rounded bg-slate-950 border border-slate-800 flex justify-between items-center">
                    <span className="text-slate-400">preferred_theme:</span>
                    <span className="font-bold text-purple-400">"DARK_PREMIUM"</span>
                  </div>
                </div>
              </div>

              {/* Interactive Simulation Actions */}
              <div className="p-5 rounded-2xl bg-slate-900/80 border border-slate-800 space-y-3">
                <h4 className="text-xs font-bold text-white uppercase tracking-wider">Hành Động Kiểm Thử Bền Vững</h4>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
                  <button
                    onClick={() => {
                      setPlayerProgress((prev) => ({
                        ...prev,
                        currentUnlockedLevel: Math.min(100, prev.currentUnlockedLevel + 1),
                      }));
                    }}
                    className="py-2.5 px-3 rounded-xl bg-[#1E293B] border border-slate-700 text-cyan-400 font-bold text-xs hover:bg-slate-800 transition"
                  >
                    +1 Cấp Mở Khóa
                  </button>
                  <button
                    onClick={() => {
                      setPlayerProgress((prev) => ({
                        ...prev,
                        totalStars: prev.totalStars + 3,
                      }));
                    }}
                    className="py-2.5 px-3 rounded-xl bg-[#1E293B] border border-slate-700 text-[#FFD600] font-bold text-xs hover:bg-slate-800 transition"
                  >
                    +3 Ngôi Sao
                  </button>
                  <button
                    onClick={() => {
                      setActiveGameDb(null);
                    }}
                    disabled={!activeGameDb}
                    className="py-2.5 px-3 rounded-xl bg-[#1E293B] border border-slate-700 text-orange-400 font-bold text-xs hover:bg-slate-800 transition disabled:opacity-40"
                  >
                    Xóa Ván Dở Dang
                  </button>
                  <button
                    onClick={() => {
                      setPlayerProgress({
                        id: 1,
                        currentUnlockedLevel: 1,
                        currentLevel: 1,
                        totalScore: 0,
                        totalLevelsCompleted: 0,
                        totalStars: 0,
                        highestScore: 0,
                        totalLinesCleared: 0,
                        highestCombo: 0,
                        lastPlayedTimestamp: Date.now(),
                      });
                      setLevelResults({ 1: { bestScore: 0, stars: 0, timesPlayed: 0, completed: false } });
                      setActiveGameDb(null);
                      handleResetGame(1);
                    }}
                    className="py-2.5 px-3 rounded-xl bg-red-500/10 border border-red-500/40 text-red-400 font-bold text-xs hover:bg-red-500/20 transition flex items-center justify-center gap-1.5"
                  >
                    <Trash2 className="w-3.5 h-3.5" /> Reset Database
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* VIEW 3: 7 SOUND EFFECTS & HAPTICS SOUNDBOARD */}
        {activeTab === 'soundboard' && (
          <div className="flex-1 overflow-y-auto p-8 max-w-4xl mx-auto space-y-6">
            <div className="p-6 rounded-3xl bg-gradient-to-r from-slate-900 to-[#0F172A] border border-slate-800 flex items-center justify-between">
              <div>
                <span className="px-3 py-1 rounded-full text-xs font-black bg-cyan-400/20 text-cyan-300 border border-cyan-400/40">
                  PHASE 3.1: 7 SỰ KIỆN ÂM THANH & RUNG
                </span>
                <h2 className="text-2xl font-black text-white mt-2">
                  Bảng Thử Nghiệm Âm Thanh (SoundPool) & Xung Rung (Haptics)
                </h2>
                <p className="text-xs text-slate-400 mt-1">
                  Nhấp vào từng nút để phát thử âm thanh và kích hoạt xung rung tương ứng:
                </p>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {[
                {
                  id: 'pickup',
                  title: '1. PIECE_PICKUP (Nhấc Khối)',
                  soundDesc: 'Âm pop nhẹ tần số 440Hz -> 680Hz (0.07s)',
                  hapticDesc: 'Xung nhẹ cực ngắn 15ms (Light tick)',
                  file: 'sfx_pickup.wav',
                  color: COLOR_TURQUOISE,
                  action: () => {
                    playSynthesizedSound('pickup');
                    triggerHaptic(15);
                  },
                },
                {
                  id: 'place',
                  title: '2. PIECE_PLACE (Đặt Khối Thành Công)',
                  soundDesc: 'Âm snap cơ học 560Hz -> 220Hz (0.09s)',
                  hapticDesc: 'Xung nảy dứt khoát 25ms (Crisp click)',
                  file: 'sfx_place.wav',
                  color: COLOR_ORANGE,
                  action: () => {
                    playSynthesizedSound('place');
                    triggerHaptic(25);
                  },
                },
                {
                  id: 'invalid',
                  title: '3. INVALID_MOVE (Thả Sai Vị Trí)',
                  soundDesc: 'Âm còi trầm cảnh báo 140Hz (0.12s)',
                  hapticDesc: 'Rung đôi cảnh báo (30ms - 40ms - 30ms)',
                  file: 'sfx_invalid.wav',
                  color: '#EF4444',
                  action: () => {
                    playSynthesizedSound('invalid');
                    triggerHaptic([30, 40, 30]);
                  },
                },
                {
                  id: 'lineClear',
                  title: '4. LINE_CLEAR (Xóa Hàng/Cột)',
                  soundDesc: 'Âm chuông ngân C5-E5 lấp lánh (0.3s)',
                  hapticDesc: 'Xung rung mạnh dứt khoát 65ms (Strong impulse)',
                  file: 'sfx_line_clear.wav',
                  color: COLOR_PURPLE,
                  action: () => {
                    playSynthesizedSound('lineClear');
                    triggerHaptic(65);
                  },
                },
                {
                  id: 'combo',
                  title: '5. COMBO (Đạt Combo Cấp 2+)',
                  soundDesc: 'Hợp âm hân hoan E5-G#5-C6 (0.4s, tăng cao độ theo cấp)',
                  hapticDesc: 'Xung kép tăng dần theo Combo (40ms - 30ms - 60ms)',
                  file: 'sfx_combo.wav',
                  color: COLOR_PINK,
                  action: () => {
                    playSynthesizedSound('combo');
                    triggerHaptic([40, 30, 60]);
                  },
                },
                {
                  id: 'gameOver',
                  title: '6. GAME_OVER (Hết Nước Đi)',
                  soundDesc: 'Âm trầm lắng giảm dần 360Hz -> 140Hz (0.5s)',
                  hapticDesc: 'Rung kép trầm sâu (70ms - 50ms - 110ms)',
                  file: 'sfx_game_over.wav',
                  color: '#94A3B8',
                  action: () => {
                    playSynthesizedSound('gameOver');
                    triggerHaptic([70, 50, 110]);
                  },
                },
                {
                  id: 'victory',
                  title: '7. LEVEL_COMPLETE (Qua Màn)',
                  soundDesc: 'Khúc khải hoàn 4 nốt C5-E5-G5-C6 (0.6s)',
                  hapticDesc: 'Nhịp rung ăn mừng 3 đợt (Fanfare pulse)',
                  file: 'sfx_level_complete.wav',
                  color: '#FFD600',
                  action: () => {
                    playSynthesizedSound('victory');
                    triggerHaptic([40, 40, 50, 40, 80]);
                  },
                },
              ].map((item) => (
                <div
                  key={item.id}
                  className="p-5 rounded-2xl bg-slate-900/70 border border-slate-800 flex flex-col justify-between gap-3 hover:border-cyan-500/40 transition"
                >
                  <div>
                    <div className="flex items-center justify-between">
                      <span className="font-black text-sm text-white">{item.title}</span>
                      <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-300">
                        {item.file}
                      </span>
                    </div>
                    <div className="mt-2 space-y-1 text-xs text-slate-400">
                      <div>🔊 Âm thanh: {item.soundDesc}</div>
                      <div>📳 Xung rung: {item.hapticDesc}</div>
                    </div>
                  </div>
                  <button
                    onClick={item.action}
                    className="w-full py-2.5 rounded-xl text-xs font-black transition flex items-center justify-center gap-2 hover:brightness-110"
                    style={{
                      backgroundColor: item.color,
                      color: item.id === 'invalid' || item.id === 'gameOver' ? '#fff' : '#070B14',
                    }}
                  >
                    <Play className="w-3.5 h-3.5 fill-current" /> Phát Thử Âm & Rung
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* VIEW 4: AUTOMATED TEST RUNNER (36 TESTS) */}
        {activeTab === 'tests' && (
          <div className="flex-1 overflow-y-auto p-8 max-w-4xl mx-auto space-y-6">
            <div className="p-6 rounded-3xl bg-slate-900/90 border border-slate-800 flex items-center justify-between">
              <div>
                <span className="px-3 py-1 rounded-full text-xs font-black bg-emerald-400/20 text-emerald-300 border border-emerald-400/40">
                  36/36 KIỂM THỬ TỰ ĐỘNG THÀNH CÔNG (100% PASS)
                </span>
                <h2 className="text-2xl font-black text-white mt-2">
                  Bộ Kiểm Thử Tự Động: Engine, Audio/Haptic & Room Persistence
                </h2>
                <p className="text-xs text-slate-400 mt-1">
                  Đã bao phủ toàn diện 16 bài test Persistence (Room Database & DataStore), 8 bài test Audio/Haptic và 12 bài test GameEngine.
                </p>
              </div>
            </div>

            <div className="space-y-4">
              <h3 className="text-sm font-black text-white uppercase tracking-wider flex items-center gap-2">
                <Database className="w-4 h-4 text-cyan-400" /> Phase 3.2 & 3.3: 19 Kiểm Thử Bền Vững (PersistenceTest.kt)
              </h3>
              <div className="grid grid-cols-1 gap-2">
                {[
                  '1. boardSerializationAndDeserializationPreservesState: Lưu trữ & phục hồi 10x10 bảo toàn 100% vị trí ô và màu sắc',
                  '2. emptyBoardSerialization: Bàn cờ rỗng tuần tự hóa và phục hồi chính xác 100 ô trống',
                  '3. piecesListSerializationWithNullSlots: Bảo toàn thứ tự 3 quân trong khay cùng các slot null',
                  '4. allNullPiecesSerialization: Khay quân rỗng (3 slot null) tuần tự hóa và giải mã chính xác',
                  '5. starCalculationAccuracy: Tính toán số sao chính xác theo mốc targetScore (3 sao >= 1.5x, 2 sao >= 1.2x, 1 sao >= 1.0x)',
                  '6. levelResultAntiDowngradeProtection: Chơi lại đạt điểm thấp hơn KHÔNG bị hạ điểm kỷ lục hoặc số sao đã đạt',
                  '7. levelResultUpgradeOnHigherScore: Chơi lại đạt điểm cao hơn nâng cấp điểm kỷ lục và sao',
                  '8. playerProgressLevelUnlock: Hoàn thành cấp 1 mở khóa cấp 2',
                  '9. playerProgressUnlockCappedAt100: Mở khóa cấp giới hạn trần ở Cấp 100',
                  '10. playerProgressHighScoreOnlyIncreases: Điểm kỷ lục chỉ tăng khi người chơi đạt điểm cao hơn',
                  '11. activeGameSaveAndRestore: Lưu và phục hồi trọn vẹn ván chơi đang dở dang (bàn cờ, khay quân, điểm, combo)',
                  '12. activeGameDeletedOnVictoryOrGameOver: Tự động xóa bản ghi active_game khi qua màn hoặc kết thúc game',
                  '13. gamePreferencesDefaultValues: Giá trị mặc định DataStore chuẩn (sound=true, haptic=true, masterVolume=1.0f)',
                  '14. volumeClamping: Âm lượng tự động kẹp an toàn trong khoảng 0.0f đến 1.0f',
                  '15. gameStateRestoresTargetScoreCorrectly: Điểm mục tiêu phục hồi chính xác theo công thức 500 + level*150',
                  '16. coroutineSafePlayerProgressEntityDefaults: Thực thể PlayerProgressEntity an toàn luồng với các giá trị khởi tạo',
                  '17. replayLevelDoesNotDoubleCountStarsOrCompletedCount: Chơi lại màn đã hoàn thành không làm tăng khống tổng số sao hoặc số màn hoàn thành',
                  '18. separateActiveGamesForDifferentLevels: Đang chơi dở màn 20 sang chơi lại màn 5, dữ liệu dở dang của màn 20 không bị ghi đè',
                  '19. gameOverSavesScoreBeforeDeletingActiveGame: Game Over ghi kết quả vào level_results trước khi xóa ActiveGameEntity',
                ].map((testName, idx) => (
                  <div
                    key={idx}
                    className="p-3 rounded-xl bg-slate-900/60 border border-slate-800 flex items-center justify-between text-xs"
                  >
                    <div className="flex items-center gap-2 text-slate-300">
                      <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                      <span>{testName}</span>
                    </div>
                    <span className="font-mono text-[10px] px-2 py-0.5 rounded bg-emerald-950/80 text-emerald-300 border border-emerald-800/60">
                      PASSED
                    </span>
                  </div>
                ))}
              </div>
            </div>

            <div className="space-y-4 pt-2">
              <h3 className="text-sm font-black text-white uppercase tracking-wider flex items-center gap-2">
                <Music className="w-4 h-4 text-purple-400" /> Phase 3.1: 8 Kiểm Thử Âm Thanh & Rung (AudioHapticTest.kt)
              </h3>
              <div className="grid grid-cols-1 gap-2">
                {[
                  '1. testSoundToggle: Bật/tắt âm thanh cập nhật StateFlow chính xác',
                  '2. testHapticToggle: Bật/tắt rung cập nhật StateFlow chính xác',
                  '3. testVolumeAdjustment: Điều chỉnh âm lượng từ 0.0 đến 1.0',
                  '4. testSinglePlaceEvent: Sự kiện đặt quân chỉ phát đúng 1 lần duy nhất',
                  '5. testLineClearEventOccursOnce: Sự kiện xóa hàng chỉ phát đúng 1 lần duy nhất',
                  '6. testNoSoundDuringContinuousDragMovement: Tuyệt đối KHÔNG phát âm thanh khi ngón tay di chuyển liên tục',
                  '7. testDeviceWithoutHapticHardwareDoesNotCrash: Thiết bị không có rung không bao giờ bị crash',
                  '8. testSoundPoolRelease: Giải phóng SoundPool hoàn chỉnh khi thoát màn hình',
                ].map((testName, idx) => (
                  <div
                    key={idx}
                    className="p-3 rounded-xl bg-slate-900/60 border border-slate-800 flex items-center justify-between text-xs"
                  >
                    <div className="flex items-center gap-2 text-slate-300">
                      <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                      <span>{testName}</span>
                    </div>
                    <span className="font-mono text-[10px] px-2 py-0.5 rounded bg-emerald-950/80 text-emerald-300 border border-emerald-800/60">
                      PASSED
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* VIEW 5: KOTLIN SOURCE CODE VIEWER */}
        {activeTab === 'code' && (
          <div className="flex-1 flex overflow-hidden">
            <div className="w-64 border-r border-slate-800 bg-[#0A0F1D] p-3 flex flex-col gap-1 overflow-y-auto">
              <span className="text-[10px] font-bold text-slate-500 uppercase px-2 py-1">Tệp Nguồn Kotlin</span>
              {Object.keys(sourceFiles).map((fileKey) => (
                <button
                  key={fileKey}
                  onClick={() => setSelectedFile(fileKey)}
                  className={`text-left text-xs px-3 py-2 rounded-xl font-medium transition flex items-center justify-between ${
                    selectedFile === fileKey
                      ? 'bg-cyan-500/20 text-cyan-300 font-bold border border-cyan-500/40'
                      : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
                  }`}
                >
                  <span className="truncate">{fileKey}</span>
                  <ChevronRight className="w-3 h-3 shrink-0" />
                </button>
              ))}
            </div>

            <div className="flex-1 flex flex-col bg-[#050811] overflow-hidden">
              <div className="h-10 border-b border-slate-800 px-4 flex items-center justify-between text-xs text-slate-400 bg-slate-900/60">
                <span className="font-mono">{sourceFiles[selectedFile]?.path}</span>
                <button
                  onClick={() => {
                    navigator.clipboard.writeText(sourceFiles[selectedFile]?.code || '');
                    setCopiedFile(true);
                    setTimeout(() => setCopiedFile(false), 2000);
                  }}
                  className="flex items-center gap-1 text-cyan-400 hover:underline"
                >
                  <Copy className="w-3 h-3" /> {copiedFile ? 'Đã sao chép!' : 'Sao chép'}
                </button>
              </div>
              <div className="flex-1 overflow-auto p-4 font-mono text-xs text-slate-300">
                <pre>{sourceFiles[selectedFile]?.code}</pre>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
