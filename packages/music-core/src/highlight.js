export function createHighlightEvent(pitch, startMs, durationMs, highlightType = 'active') {
  return {
    pitch,
    startMs,
    durationMs,
    highlightType,
  };
}

export function generateHighlightEvents(noteEvents, highlightType = 'active') {
  return noteEvents.map((event) =>
    createHighlightEvent(event.pitch, event.startMs, event.durationMs, highlightType)
  );
}

export function getActivePitchesAtTime(events, currentMs) {
  return events
    .filter((event) => currentMs >= event.startMs && currentMs < event.startMs + event.durationMs)
    .map((event) => event.pitch);
}
