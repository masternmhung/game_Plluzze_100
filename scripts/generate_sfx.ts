import * as fs from 'fs';
import * as path from 'path';

// Helper to create a mono 16-bit PCM WAV file
function createWavBuffer(sampleRate: number, durationSeconds: number, sampleGenerator: (t: number) => number): Buffer {
  const numSamples = Math.floor(sampleRate * durationSeconds);
  const blockAlign = 2; // 16-bit mono
  const byteRate = sampleRate * blockAlign;
  const dataSize = numSamples * blockAlign;
  const buffer = Buffer.alloc(44 + dataSize);

  // RIFF identifier
  buffer.write('RIFF', 0);
  buffer.writeUInt32LE(36 + dataSize, 4);
  buffer.write('WAVE', 8);

  // fmt subchunk
  buffer.write('fmt ', 12);
  buffer.writeUInt32LE(16, 16); // SubChunk1Size (16 for PCM)
  buffer.writeUInt16LE(1, 20);  // AudioFormat (1 for PCM)
  buffer.writeUInt16LE(1, 22);  // NumChannels (1 mono)
  buffer.writeUInt32LE(sampleRate, 24);
  buffer.writeUInt32LE(byteRate, 28);
  buffer.writeUInt16LE(blockAlign, 32);
  buffer.writeUInt16LE(16, 34); // BitsPerSample (16)

  // data subchunk
  buffer.write('data', 36);
  buffer.writeUInt32LE(dataSize, 40);

  // Write samples
  for (let i = 0; i < numSamples; i++) {
    const t = i / sampleRate;
    let sample = sampleGenerator(t);
    sample = Math.max(-1, Math.min(1, sample)); // Clamp
    const intSample = Math.floor(sample * 32767);
    buffer.writeInt16LE(intSample, 44 + i * 2);
  }

  return buffer;
}

const outDir = path.resolve(process.cwd(), 'app/src/main/res/raw');
if (!fs.existsSync(outDir)) {
  fs.mkdirSync(outDir, { recursive: true });
}

const sampleRate = 22050; // Standard compact gaming sample rate

// 1. Pickup: Gentle pop/blip 440Hz -> 660Hz (0.07s)
const sfxPickup = createWavBuffer(sampleRate, 0.07, (t) => {
  const freq = 440 + t * 3000;
  const env = Math.exp(-t * 40);
  return Math.sin(2 * Math.PI * freq * t) * env * 0.7;
});
fs.writeFileSync(path.join(outDir, 'sfx_pickup.wav'), sfxPickup);

// 2. Place: Crisp snap 600Hz -> 200Hz (0.09s)
const sfxPlace = createWavBuffer(sampleRate, 0.09, (t) => {
  const freq = 600 * Math.exp(-t * 20);
  const env = Math.exp(-t * 30);
  return (Math.sin(2 * Math.PI * freq * t) + 0.3 * Math.sin(2 * Math.PI * freq * 2 * t)) * env * 0.8;
});
fs.writeFileSync(path.join(outDir, 'sfx_place.wav'), sfxPlace);

// 3. Invalid: Low double buzz (0.12s)
const sfxInvalid = createWavBuffer(sampleRate, 0.12, (t) => {
  const freq = 140;
  const pulse = Math.sin(2 * Math.PI * 25 * t) > 0 ? 1 : 0.2;
  const saw = (t * freq) % 1 > 0.5 ? 1 : -1;
  const env = Math.exp(-t * 12);
  return saw * pulse * env * 0.6;
});
fs.writeFileSync(path.join(outDir, 'sfx_invalid.wav'), sfxInvalid);

// 4. Line Clear: Shimmering ascending chime (0.3s)
const sfxLineClear = createWavBuffer(sampleRate, 0.3, (t) => {
  const f1 = 523.25 + t * 800; // C5 ascending
  const f2 = 659.25 + t * 1000; // E5
  const env = Math.exp(-t * 8);
  return (Math.sin(2 * Math.PI * f1 * t) * 0.5 + Math.sin(2 * Math.PI * f2 * t) * 0.5) * env * 0.8;
});
fs.writeFileSync(path.join(outDir, 'sfx_line_clear.wav'), sfxLineClear);

// 5. Combo: Bright energetic chord (0.4s)
const sfxCombo = createWavBuffer(sampleRate, 0.4, (t) => {
  const f1 = 659.25; // E5
  const f2 = 830.61; // G#5
  const f3 = 1046.5; // C6
  const env = Math.exp(-t * 6);
  return (
    (Math.sin(2 * Math.PI * f1 * t) * 0.4 +
      Math.sin(2 * Math.PI * f2 * t) * 0.3 +
      Math.sin(2 * Math.PI * f3 * t) * 0.3) *
    env *
    0.85
  );
});
fs.writeFileSync(path.join(outDir, 'sfx_combo.wav'), sfxCombo);

// 6. Game Over: Somber descending tone (0.5s)
const sfxGameOver = createWavBuffer(sampleRate, 0.5, (t) => {
  const freq = 350 * Math.exp(-t * 2.5);
  const env = Math.exp(-t * 4);
  return Math.sin(2 * Math.PI * freq * t) * env * 0.75;
});
fs.writeFileSync(path.join(outDir, 'sfx_game_over.wav'), sfxGameOver);

// 7. Level Complete: Celebratory fanfare (0.6s)
const sfxLevelComplete = createWavBuffer(sampleRate, 0.6, (t) => {
  const noteIdx = Math.min(3, Math.floor(t / 0.15));
  const notes = [523.25, 659.25, 783.99, 1046.5]; // C5, E5, G5, C6
  const freq = notes[noteIdx];
  const localT = t % 0.15;
  const env = Math.exp(-localT * 10);
  return Math.sin(2 * Math.PI * freq * t) * env * 0.8;
});
fs.writeFileSync(path.join(outDir, 'sfx_level_complete.wav'), sfxLevelComplete);

console.log('Successfully generated all 7 WAV sound assets in app/src/main/res/raw/:');
console.log(fs.readdirSync(outDir).join(', '));
