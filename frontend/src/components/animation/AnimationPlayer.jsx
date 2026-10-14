import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { graphemes } from '../motion.js'
import { ArrowDefs, SCENES, clamp, pop, seg } from './scenes.jsx'

const osReducedMotion = () => window.matchMedia?.('(prefers-reduced-motion: reduce)').matches ?? false
const sentences = (text) => (text || '').split(/(?<=[.!?])\s+/).filter(Boolean)

/** Frames built from the lesson step itself: used for every idea, after a matching scene or on their own. */
function lessonFrames(step, withIntro) {
  const frames = []
  if (withIntro) {
    const words = step.point.split(/\s+/)
    frames.push({
      caption: 'Here is the idea.', say: step.point, duration: 600 + words.length * 350, html: true,
      draw: (t) => (
        <div className="g-frame">
          <div className="g-icon">💡</div>
          <p className="g-idea">{words.map((w, i) => (
            <span key={i} className={`g-word ${i < Math.ceil(t * words.length) ? 'in' : ''}`}>{w} </span>
          ))}</p>
        </div>
      ),
    })
    const parts = sentences(step.picture)
    if (parts.length) {
      frames.push({
        caption: "Let's picture it.", say: step.picture, duration: 1500 + parts.length * 2200, html: true,
        draw: (t, ms) => (
          <div className="g-frame">
            <div className="g-icon" style={{ transform: `translateY(${Math.sin(ms / 400) * 4}px)` }}>🎨</div>
            {parts.map((p, i) => (
              <p key={i} className={`g-line ${t * parts.length > i ? 'in' : ''}`}>{p}</p>
            ))}
          </div>
        ),
      })
    }
  }
  const ex = step.example
  if (ex) {
    const lines = (ex.visual || '').split('\n').map(graphemes)
    const total = lines.reduce((n, l) => n + l.length, 0)
    frames.push({
      caption: 'Now look at the example from your lesson.', say: `${ex.title}. ${ex.content}`, duration: 3500 + total * 60, html: true,
      draw: (t) => {
        let count = 0
        const shown = seg(t, 0.35, 1) * total
        return (
          <div className="g-frame">
            <h3 className="g-title">✏️ {ex.title}</h3>
            <p className={`g-line ${t > 0.05 ? 'in' : ''}`}>{ex.content}</p>
            {total > 0 && (
              <pre className="visual big animated g-visual">
                {lines.map((chars, li) => (
                  <span key={li}>
                    {chars.map((ch, ci) => <span key={ci} className={`v-ch ${count++ < shown ? 'in' : ''}`}>{ch}</span>)}
                    {li < lines.length - 1 && '\n'}
                  </span>
                ))}
              </pre>
            )}
          </div>
        )
      },
    })
  }
  frames.push({
    caption: 'Remember this! ⭐', say: `Remember: ${step.point}`, duration: 3000, html: true,
    draw: (t) => (
      <div className="g-frame">
        <div className="g-star" style={{ transform: `scale(${Math.max(pop(seg(t, 0, 0.4)), 0.001)}) rotate(${clamp(t * 2) * 360}deg)` }}>⭐</div>
        <p className="g-idea remember" style={{ opacity: seg(t, 0.25, 0.6) }}>{step.point}</p>
      </div>
    ),
  })
  return frames
}

/** Time since the frame started, driven by requestAnimationFrame so it also works in calm mode (no CSS motion). */
function useClock(playing, resetKey) {
  const [ms, setMs] = useState(0)
  const elapsed = useRef(0)
  useEffect(() => { elapsed.current = 0; setMs(0) }, [resetKey])
  useEffect(() => {
    if (!playing) return undefined
    let raf, last = performance.now()
    const tick = (now) => {
      elapsed.current += now - last
      last = now
      setMs(elapsed.current)
      raf = requestAnimationFrame(tick)
    }
    raf = requestAnimationFrame(tick)
    return () => cancelAnimationFrame(raf)
  }, [playing, resetKey])
  return ms
}

function speak(text) {
  if (!window.speechSynthesis || !text) return
  window.speechSynthesis.cancel()
  const u = new SpeechSynthesisUtterance(text.replace(/[*•]/g, ''))
  u.rate = 0.85
  window.speechSynthesis.speak(u)
}

/**
 * Plays an idea as a short animated, narrated story (special-needs learners only).
 * Calm voice: slower, and the learner moves on with "Next" so nothing changes unexpectedly.
 */
export default function AnimationPlayer({ step, index, total, profile, voice, onClose }) {
  const scene = SCENES[step.scene]
  const frames = useMemo(() => [...(scene?.frames ?? []), ...lessonFrames(step, !scene)], [step, scene])
  const manual = voice === 'calm'
  const reduced = osReducedMotion()
  const [f, setF] = useState(0)
  const [playing, setPlaying] = useState(true)
  const [slow, setSlow] = useState(manual || profile.timeMultiplier >= 1.5)
  const [ended, setEnded] = useState(false)
  const [run, setRun] = useState(0)
  const dialogRef = useRef(null)
  const close = useRef(onClose)
  close.current = onClose

  const frame = frames[f]
  const speed = slow ? 1.6 : 1
  const duration = frame.duration * speed
  const hold = (1800 + frame.caption.split(/\s+/).length * 320) * speed
  const ms = useClock(playing && !ended, `${run}-${f}`)
  const t = reduced ? 1 : clamp(ms / duration)
  const drawMs = reduced ? 0 : ms / speed

  const go = useCallback((n) => {
    if (n >= frames.length) { setEnded(true); return }
    setEnded(false)
    setF(Math.max(0, n))
  }, [frames.length])

  // Move on by itself once the animation and a little reading time are over (not for the calm voice).
  useEffect(() => {
    if (!manual && playing && !ended && ms >= duration + hold) go(f + 1)
  }, [ms, manual, playing, ended, duration, hold, f, go])

  useEffect(() => {
    if (profile.readAloud && !ended) speak(frame.say ?? frame.caption)
  }, [f, run, ended]) // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    dialogRef.current?.focus()
    const onKey = (e) => { if (e.key === 'Escape') close.current() }
    window.addEventListener('keydown', onKey)
    return () => { window.removeEventListener('keydown', onKey); window.speechSynthesis?.cancel() }
  }, [])

  const replay = () => { setEnded(false); setF(0); setPlaying(true); setRun(run + 1) }

  return (
    <div className="anim-backdrop" onClick={(e) => { if (e.target === e.currentTarget) onClose() }}>
      <div className={`anim-dialog voice-${voice}`} role="dialog" aria-modal="true" aria-labelledby="anim-title" tabIndex={-1} ref={dialogRef}>
        <header className="anim-head">
          <h2 id="anim-title">🎬 Idea {index + 1} of {total}{scene ? `: ${scene.title}` : ''}</h2>
          <button className="anim-close" onClick={() => onClose()} aria-label="Close animation">✕</button>
        </header>

        {ended ? (
          <div className="anim-end">
            <div className="big-emoji" aria-hidden>🎉</div>
            <h3>That's the end! Did that help?</h3>
            <div className="anim-end-actions">
              <button className="btn-primary" onClick={() => onClose('helped')}>👍 Yes, I get it now!</button>
              <button className="btn-secondary" onClick={replay}>🔁 Watch again</button>
              <button className="btn-ghost" onClick={() => onClose('confused')}>🤔 I'm still confused</button>
            </div>
          </div>
        ) : (
          <>
            <div className="anim-stage">
              {frame.html
                ? frame.draw(t, drawMs)
                : (
                  <svg viewBox="0 0 400 260" role="img" aria-label={frame.caption}>
                    <ArrowDefs />
                    {frame.draw(t, drawMs)}
                  </svg>
                )}
            </div>
            <p className="anim-caption" aria-live="polite">{frame.caption}</p>
            <div className="anim-progress" aria-hidden>
              {frames.map((_, k) => (
                <span key={k} className="anim-seg"><span style={{ width: `${k < f ? 100 : k === f ? t * 100 : 0}%` }} /></span>
              ))}
            </div>
            <div className="anim-controls">
              <button className="btn-ghost" onClick={() => go(f - 1)} disabled={f === 0}>⏮ Back</button>
              <button className="btn-secondary" onClick={() => setPlaying(!playing)} aria-pressed={!playing}>
                {playing ? '⏸ Pause' : '▶ Play'}
              </button>
              <button className="btn-primary" onClick={() => go(f + 1)}>{f === frames.length - 1 ? 'Finish ✓' : 'Next ⏭'}</button>
              <button className="btn-ghost" onClick={() => setSlow(!slow)} aria-pressed={slow}>🐢 {slow ? 'Slow: on' : 'Slow: off'}</button>
              <button className="btn-ghost" onClick={replay}>🔁 Replay</button>
              {!profile.readAloud && <button className="btn-ghost" onClick={() => speak(frame.say ?? frame.caption)}>🔊 Read</button>}
            </div>
            {manual && <p className="muted small anim-hint">Step {f + 1} of {frames.length}. Tap "Next" when you are ready.</p>}
          </>
        )}
      </div>
    </div>
  )
}
