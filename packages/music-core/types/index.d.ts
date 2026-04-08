// Type definitions for @xiyue/music-core

export interface Pitch {
  noteName: string;
  octave: number;
  midiNumber: number;
  frequencyHz: number;
}

export interface PlaybackItem {
  id: string;
  kind: 'scale' | 'chord';
  root: string;
  type: string;
  octave: number;
  displayName: string;
  pitches: Pitch[];
}

export interface NoteEvent {
  pitch: Pitch;
  startMs: number;
  durationMs: number;
  velocity: number;
}

export interface HighlightEvent {
  pitch: Pitch;
  startMs: number;
  durationMs: number;
  highlightType: 'active' | 'preview';
}

export interface LibraryItem {
  id: string;
  kind: 'scale' | 'chord';
  type: string;
  label: string;
  searchText: string;
}

export interface PlaybackModeInfo {
  label: string;
  description: string;
  supports: string[];
}

// Note names
export const NOTE_NAMES: readonly string[];

// Theory functions
export function noteNameToIndex(noteName: string): number;
export function createPitch(noteName: string, octave: number): Pitch;
export function formatPitch(pitch: Pitch): string;
export function createPitchFromMidiNumber(midiNumber: number): Pitch;

// Pattern functions
export function listScaleTypes(): string[];
export function listChordTypes(): string[];
export function generateScalePitches(rootNoteName: string, scaleType: string, octave?: number): Pitch[];
export function generateChordPitches(rootNoteName: string, chordType: string, octave?: number): Pitch[];
export function generateChordArpeggioUpPitches(rootNoteName: string, chordType: string, octave?: number): Pitch[];

// Playback functions
export function generateScaleAscendingEvents(rootNoteName: string, scaleType: string, octave?: number, bpm?: number): NoteEvent[];
export function generateChordBlockEvents(rootNoteName: string, chordType: string, octave?: number, bpm?: number): NoteEvent[];
export function generateChordArpeggioUpEvents(rootNoteName: string, chordType: string, octave?: number, bpm?: number): NoteEvent[];
export function createPlaybackItem(kind: 'scale' | 'chord', root: string, type: string, octave?: number): PlaybackItem;
export function getSupportedPlaybackModes(item: PlaybackItem): string[];
export function getDefaultPlaybackMode(item: PlaybackItem): string;
export function generatePlaybackEvents(item: PlaybackItem, mode?: string, bpm?: number): NoteEvent[];
export function getPlaybackModeInfo(): Record<string, PlaybackModeInfo>;
export function registerPlaybackMode(mode: string, generator: Function, supports: string[], label: string, description?: string): void;

// Library functions
export function listLibraryItems(): LibraryItem[];
export function searchLibraryItems(query?: string, options?: { kind?: 'all' | 'scale' | 'chord' }): LibraryItem[];

// Highlight functions
export function createHighlightEvent(pitch: Pitch, startMs: number, durationMs: number, highlightType?: 'active' | 'preview'): HighlightEvent;
export function generateHighlightEvents(noteEvents: NoteEvent[], highlightType?: 'active' | 'preview'): HighlightEvent[];
export function getActivePitchesAtTime(events: HighlightEvent[], currentMs: number): Pitch[];