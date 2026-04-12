package com.xiyue.app.playback

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

internal class ToneSynthesisEngine(private val sampleRate: Int) {
    
    fun createInstrumentalSamples(
        midiNotes: List<Int>,
        stepDurationMs: Long,
        renderDurationMs: Long,
        tonePreset: TonePreset,
        volumeFactor: Float,
    ): ShortArray {
        val totalFrames = ((renderDurationMs / 1000f) * sampleRate).toInt().coerceAtLeast(1)
        val stepFrames = ((stepDurationMs / 1000f) * sampleRate).toInt().coerceAtLeast(1)
        val tailFrames = (totalFrames - stepFrames).coerceAtLeast(1)
        val samples = ShortArray(totalFrames * 2)
        val safeVolume = volumeFactor.coerceIn(0.12f, 1f)
        val noteCount = midiNotes.size.coerceAtLeast(1)
        val profile = profileFor(tonePreset)
        val baseAmplitude = profile.baseAmplitude / sqrt(noteCount.toDouble())
        val chordBalance = chordBalance(noteCount, profile)

        val lowestMidi = midiNotes.minOrNull() ?: 0
        val lowestIndex = midiNotes.indexOf(lowestMidi).coerceAtLeast(0)

        for (frame in 0 until totalFrames) {
            val timeSeconds = frame.toDouble() / sampleRate
            val adsrEnvelope = envelope(frame, stepFrames, tailFrames, totalFrames, profile)
            val noiseFloor = noiseBed(frame, totalFrames, profile)
            val resonance = lowPassResonance(timeSeconds, profile)
            var left = 0.0
            var right = 0.0

            midiNotes.forEachIndexed { noteIndex, midiNote ->
                val frequency = ToneAudioMath.midiToFrequency(midiNote)
                val highDamping = highFrequencyDamping(frequency, profile)
                val onsetLagSeconds = microStaggerSeconds(noteIndex, noteCount, profile)
                val warmth = warmthContour(frequency, timeSeconds, profile)
                val mellowRollOff = mellowRollOff(frequency, profile)
                val toneColor = harmonicTone(
                    frequency, timeSeconds, noteIndex, noteCount, onsetLagSeconds, profile
                ) * resonance * highDamping * chordBalance * warmth * mellowRollOff
                val hammerTransient = hammerTransient(frequency, timeSeconds, onsetLagSeconds, profile)
                val woodPulse = woodPulse(frequency, timeSeconds, onsetLagSeconds, profile)
                val bodyResonance = bodyResonance(frequency, timeSeconds, noteIndex, profile)
                val bassBoost = if (noteIndex == lowestIndex) {
                    bassReinforcement(frequency, timeSeconds, profile)
                } else {
                    0.0
                }
                val stereo = stereoSpread(noteIndex, noteCount, profile)
                val softAttackBlend = softAttackBlend(timeSeconds, onsetLagSeconds, profile)
                val voicedTone = ((toneColor * softAttackBlend) + hammerTransient + woodPulse + bodyResonance + bassBoost) * profile.presenceSoften
                left += voicedTone * (1.0 - stereo)
                right += voicedTone * (1.0 + stereo)
            }

            val feltNoise = feltNoise(frame, totalFrames, timeSeconds, profile)
            val warmBlur = warmBlur(timeSeconds, profile)
            val airTrim = airTrim(timeSeconds, profile)
            left += noiseFloor * airTrim * warmBlur
            right += noiseFloor * airTrim * warmBlur
            left += feltNoise
            right += feltNoise * profile.feltStereoBias

            val leftSample = ToneAudioMath.softClip(left * baseAmplitude * adsrEnvelope * safeVolume)
            val rightSample = ToneAudioMath.softClip(right * baseAmplitude * adsrEnvelope * safeVolume)
            samples[frame * 2] = ToneAudioMath.toPcm16(leftSample)
            samples[frame * 2 + 1] = ToneAudioMath.toPcm16(rightSample)
        }

        return samples
    }

    fun createVocalSamples(
        midiNotes: List<Int>,
        stepDurationMs: Long,
        renderDurationMs: Long,
        volumeFactor: Float,
    ): ShortArray {
        val totalFrames = ((renderDurationMs / 1000f) * sampleRate).toInt().coerceAtLeast(1)
        val stepFrames = ((stepDurationMs / 1000f) * sampleRate).toInt().coerceAtLeast(1)
        val samples = ShortArray(totalFrames * 2)
        val safeVolume = volumeFactor.coerceIn(0.12f, 1f)
        val noteCount = midiNotes.size.coerceAtLeast(1)
        val baseAmplitude = 0.35 / sqrt(noteCount.toDouble())

        for (frame in 0 until totalFrames) {
            val timeSeconds = frame.toDouble() / sampleRate
            val envelope = vocalEnvelope(frame, stepFrames, totalFrames)
            var left = 0.0
            var right = 0.0

            midiNotes.forEachIndexed { noteIndex, midiNote ->
                val frequency = ToneAudioMath.midiToFrequency(midiNote)
                val vocal = vocalFormant(frequency, timeSeconds, noteIndex, noteCount)
                val stereo = stereoSpread(noteIndex, noteCount, ToneProfile.vocal())
                left += vocal * (1.0 - stereo)
                right += vocal * (1.0 + stereo)
            }

            val leftSample = ToneAudioMath.softClip(left * baseAmplitude * envelope * safeVolume)
            val rightSample = ToneAudioMath.softClip(right * baseAmplitude * envelope * safeVolume)
            samples[frame * 2] = ToneAudioMath.toPcm16(leftSample)
            samples[frame * 2 + 1] = ToneAudioMath.toPcm16(rightSample)
        }

        return samples
    }

    /**
     * Create a simple tone sample for countdown beep and reference tones
     */
    fun createToneSamples(
        frequency: Double,
        durationMs: Int,
        profile: ToneProfile,
        volumeFactor: Float,
    ): ShortArray {
        val totalFrames = ((durationMs / 1000f) * sampleRate).toInt().coerceAtLeast(1)
        val samples = ShortArray(totalFrames * 2)
        val safeVolume = volumeFactor.coerceIn(0.12f, 1f)
        val baseAmplitude = profile.baseAmplitude

        // Simple envelope: attack -> sustain -> release
        val attackFrames = (sampleRate * 0.01).toInt().coerceAtLeast(1) // 10ms attack
        val releaseFrames = (sampleRate * 0.05).toInt().coerceAtLeast(1) // 50ms release

        for (frame in 0 until totalFrames) {
            val timeSeconds = frame.toDouble() / sampleRate
            val envelope = when {
                frame < attackFrames -> frame.toDouble() / attackFrames
                frame >= totalFrames - releaseFrames -> (totalFrames - frame).toDouble() / releaseFrames
                else -> 1.0
            }

            // Simple sine wave with slight harmonics
            val fundamental = ToneAudioMath.sinWave(frequency, timeSeconds)
            val harmonic2 = ToneAudioMath.sinWave(frequency * 2.0, timeSeconds) * profile.secondHarmonic
            val tone = fundamental + harmonic2

            val sample = ToneAudioMath.softClip(tone * baseAmplitude * envelope * safeVolume)
            val pcmValue = ToneAudioMath.toPcm16(sample)
            samples[frame * 2] = pcmValue
            samples[frame * 2 + 1] = pcmValue // Mono to stereo
        }

        return samples
    }

    private fun harmonicTone(
        frequency: Double, timeSeconds: Double, noteIndex: Int, noteCount: Int,
        onsetLagSeconds: Double, profile: ToneProfile
    ): Double {
        val slightDetune = 1.0 + ((noteIndex % 3) - 1) * profile.detuneWidth
        val overtoneBalance = 1.0 / (1.0 + profile.overtoneCollapse * (noteCount - 1).coerceAtLeast(0))
        val onsetEnvelope = onsetEnvelope(timeSeconds, onsetLagSeconds, profile)
        val fundamental = ToneAudioMath.sinWave(frequency * slightDetune, timeSeconds) * onsetEnvelope
        val secondHarmonic = ToneAudioMath.sinWave(frequency * 2.0, timeSeconds) * profile.secondHarmonic * overtoneBalance * onsetEnvelope
        val thirdHarmonic = ToneAudioMath.sinWave(frequency * 3.0, timeSeconds) * profile.thirdHarmonic * overtoneBalance * onsetEnvelope
        val fifthHarmonic = ToneAudioMath.sinWave(frequency * 5.0, timeSeconds) * profile.fifthHarmonic * overtoneBalance * onsetEnvelope
        val airyOvertone = ToneAudioMath.sinWave(frequency * 1.5, timeSeconds) * profile.airyOvertone * overtoneBalance * onsetEnvelope
        val chorusBloom = chorusBloom(frequency, timeSeconds, noteIndex, onsetEnvelope, profile)
        return fundamental + secondHarmonic + thirdHarmonic + fifthHarmonic + airyOvertone + chorusBloom
    }

    private fun hammerTransient(frequency: Double, timeSeconds: Double, onsetLagSeconds: Double, profile: ToneProfile): Double {
        val localTime = (timeSeconds - onsetLagSeconds).coerceAtLeast(0.0)
        val transientEnvelope = exp(-localTime / profile.transientDecaySeconds)
        val hammer = ToneAudioMath.sinWave(frequency * profile.hammerPartial, localTime)
        val transient = ToneAudioMath.sinWave(frequency * profile.transientPartial, localTime)
        return (hammer * 0.62 + transient * 0.38) * profile.hammerLevel * transientEnvelope
    }

    private fun woodPulse(frequency: Double, timeSeconds: Double, onsetLagSeconds: Double, profile: ToneProfile): Double {
        val localTime = (timeSeconds - onsetLagSeconds).coerceAtLeast(0.0)
        val onset = exp(-localTime / profile.woodPulseDecaySeconds)
        val woodPulse = ToneAudioMath.sinWave(frequency * profile.woodPulsePartial, localTime)
        return woodPulse * profile.woodPulseLevel * onset
    }

    private fun bodyResonance(frequency: Double, timeSeconds: Double, noteIndex: Int, profile: ToneProfile): Double {
        val bodyDecay = exp(-timeSeconds / profile.bodyResonanceDecaySeconds)
        val bodyFrequency = frequency * profile.bodyResonancePartial
        val bodyPhase = timeSeconds + (noteIndex * profile.bodySpreadSeconds)
        val body = ToneAudioMath.sinWave(bodyFrequency, bodyPhase)
        val bloom = cos(timeSeconds * profile.bodyBloomRate * PI)
        return body * (profile.bodyResonanceLevel + (profile.bodyBloomLevel * bloom)) * bodyDecay
    }

    private fun envelope(frame: Int, stepFrames: Int, tailFrames: Int, totalFrames: Int, profile: ToneProfile): Double {
        val attackFrames = (sampleRate * profile.attackSeconds).toInt().coerceAtLeast(1)
        val decayFrames = (sampleRate * profile.decaySeconds).toInt().coerceAtLeast(1)
        val releaseFrames = tailFrames.coerceAtLeast((sampleRate * profile.releaseSeconds).toInt().coerceAtLeast(1))
        return when {
            frame < attackFrames -> frame.toDouble() / attackFrames
            frame < attackFrames + decayFrames -> {
                val decayProgress = (frame - attackFrames).toDouble() / decayFrames
                1.0 - ((1.0 - profile.sustainLevel) * decayProgress)
            }
            frame >= stepFrames -> {
                val releaseProgress = (totalFrames - frame).toDouble() / releaseFrames
                profile.sustainLevel * releaseCurve(releaseProgress.coerceIn(0.0, 1.0))
            }
            else -> profile.sustainLevel
        }
    }

    private fun microStaggerSeconds(noteIndex: Int, noteCount: Int, profile: ToneProfile): Double {
        if (noteCount <= 1) return 0.0
        return (noteIndex * profile.microStaggerSeconds).coerceAtMost(profile.microStaggerCapSeconds)
    }

    private fun onsetEnvelope(timeSeconds: Double, onsetLagSeconds: Double, profile: ToneProfile): Double {
        val localTime = (timeSeconds - onsetLagSeconds).coerceAtLeast(0.0)
        return (localTime / profile.onsetAttackSeconds).coerceIn(0.0, 1.0)
    }

    private fun releaseCurve(progress: Double): Double = (progress * progress).coerceIn(0.0, 1.0)

    private fun feltNoise(frame: Int, totalFrames: Int, timeSeconds: Double, profile: ToneProfile): Double {
        val normalized = frame.toDouble() / totalFrames.coerceAtLeast(1)
        val feltTaper = (1.0 - normalized).pow(profile.feltDamping)
        val feltBurst = exp(-timeSeconds / profile.feltDecaySeconds)
        return pseudoNoise(frame + 17) * profile.feltNoiseLevel * feltTaper * feltBurst
    }

    private fun bassReinforcement(frequency: Double, timeSeconds: Double, profile: ToneProfile): Double {
        val sub = ToneAudioMath.sinWave(frequency * 0.5, timeSeconds)
        val contour = profile.bassLevel + (profile.bassMotion * cos(timeSeconds * 2.0 * PI))
        return sub * contour
    }

    private fun chorusBloom(frequency: Double, timeSeconds: Double, noteIndex: Int, onsetEnvelope: Double, profile: ToneProfile): Double {
        val detune = 1.0 + ((noteIndex % 2) * profile.chorusDetune)
        val delayedTime = (timeSeconds - profile.chorusDelaySeconds - (noteIndex * profile.chorusSpreadSeconds)).coerceAtLeast(0.0)
        return ToneAudioMath.sinWave(frequency * detune, delayedTime) * profile.chorusLevel * onsetEnvelope
    }

    private fun mellowRollOff(frequency: Double, profile: ToneProfile): Double {
        val normalized = (frequency / profile.mellowRollOffFrequency).coerceAtLeast(0.0)
        return (1.0 / (1.0 + normalized * profile.mellowRollOffDepth)).coerceIn(profile.highDampingFloor, 1.0)
    }

    private fun softAttackBlend(timeSeconds: Double, onsetLagSeconds: Double, profile: ToneProfile): Double {
        val localTime = (timeSeconds - onsetLagSeconds).coerceAtLeast(0.0)
        val bloom = 1.0 - exp(-localTime / profile.softAttackBlendSeconds)
        return (profile.softAttackBase + (bloom * (1.0 - profile.softAttackBase))).coerceIn(0.0, 1.0)
    }

    private fun warmBlur(timeSeconds: Double, profile: ToneProfile): Double =
        (profile.warmBlurBase + (profile.warmBlurMotion * cos(timeSeconds * PI))).coerceIn(0.0, 1.0)

    private fun airTrim(timeSeconds: Double, profile: ToneProfile): Double =
        (1.0 - (profile.airTrim * exp(-timeSeconds / 0.18))).coerceIn(0.0, 1.0)

    private fun warmthContour(frequency: Double, timeSeconds: Double, profile: ToneProfile): Double {
        val lowMidBias = (1.0 - (frequency / profile.warmthCeilingFrequency)).coerceIn(profile.warmthFloor, 1.0)
        val slowGlow = profile.warmthBase + profile.warmthMotion * cos(timeSeconds * PI)
        return lowMidBias * slowGlow
    }

    private fun noiseBed(frame: Int, totalFrames: Int, profile: ToneProfile): Double {
        val normalized = frame.toDouble() / totalFrames.coerceAtLeast(1)
        val taper = (1.0 - normalized).coerceIn(0.0, 1.0)
        return (pseudoNoise(frame) * profile.noiseLevel) * taper
    }

    private fun pseudoNoise(frame: Int): Double {
        val phase = frame.toDouble() * 12.9898
        val value = sin(phase) * 43758.5453
        return (value - kotlin.math.floor(value)) * 2.0 - 1.0
    }

    private fun lowPassResonance(timeSeconds: Double, profile: ToneProfile): Double {
        val timeFactor = timeSeconds.coerceAtLeast(0.0)
        val sweep = exp(-timeFactor * profile.resonanceSweep)
        val body = profile.resonanceBase + profile.resonanceLift * sweep
        val breath = profile.resonanceBreath * cos(timeSeconds * 2.5 * PI)
        return (body + breath).coerceIn(profile.resonanceFloor, 1.0)
    }

    private fun stereoSpread(noteIndex: Int, noteCount: Int, profile: ToneProfile): Double {
        if (noteCount <= 1) return 0.0
        val centerOffset = noteIndex - ((noteCount - 1) / 2.0)
        return (centerOffset / noteCount.toDouble()) * profile.stereoWidth
    }

    private fun highFrequencyDamping(frequency: Double, profile: ToneProfile): Double {
        val normalized = (frequency / profile.dampingReferenceFrequency).coerceAtLeast(0.0)
        val damping = 1.0 / (1.0 + normalized * normalized)
        return damping.coerceIn(profile.highDampingFloor, 1.0)
    }

    private fun chordBalance(noteCount: Int, profile: ToneProfile): Double {
        val balance = 1.0 / (1.0 + profile.chordPenalty * (noteCount - 1).coerceAtLeast(0))
        return balance.coerceIn(profile.chordBalanceFloor, 1.0)
    }

    private fun vocalEnvelope(frame: Int, stepFrames: Int, totalFrames: Int): Double {
        val attackFrames = (sampleRate * 0.035).toInt()
        val releaseFrames = (sampleRate * 0.08).toInt()
        return when {
            frame < attackFrames -> {
                val progress = frame.toDouble() / attackFrames
                progress * progress
            }
            frame < stepFrames -> 1.0
            frame >= totalFrames - releaseFrames -> {
                val progress = (totalFrames - frame).toDouble() / releaseFrames
                progress * progress
            }
            else -> 1.0
        }
    }

    private fun vocalFormant(frequency: Double, timeSeconds: Double, noteIndex: Int, noteCount: Int): Double {
        val slightDetune = 1.0 + ((noteIndex % 3) - 1) * 0.0008
        val fundamental = ToneAudioMath.sinWave(frequency * slightDetune, timeSeconds)
        val harmonic2 = ToneAudioMath.sinWave(frequency * 2 * slightDetune, timeSeconds) * 0.5
        val harmonic3 = ToneAudioMath.sinWave(frequency * 3 * slightDetune, timeSeconds) * 0.3
        val harmonic4 = ToneAudioMath.sinWave(frequency * 4 * slightDetune, timeSeconds) * 0.15
        val harmonic5 = ToneAudioMath.sinWave(frequency * 5 * slightDetune, timeSeconds) * 0.08
        val formant1 = vocalResonance(frequency, 650.0, 80.0) * 0.8
        val formant2 = vocalResonance(frequency, 1080.0, 100.0) * 0.6
        val formant3 = vocalResonance(frequency, 2550.0, 120.0) * 0.25
        val formant4 = vocalResonance(frequency, 3200.0, 150.0) * 0.12
        val breathiness = pseudoNoise((timeSeconds * sampleRate).toInt()) * 0.04 * (1.0 - timeSeconds * 0.3).coerceAtLeast(0.0)
        val vibrato = 1.0 + 0.01 * sin(2 * PI * 5.2 * timeSeconds) * timeSeconds.coerceAtMost(0.3)
        val formantBlend = (formant1 + formant2 + formant3 + formant4) * 1.5
        val harmonics = fundamental + harmonic2 + harmonic3 + harmonic4 + harmonic5
        return (harmonics + breathiness) * formantBlend * vibrato
    }

    private fun vocalResonance(frequency: Double, formantFreq: Double, bandwidth: Double): Double {
        val diff = frequency - formantFreq
        val response = 1.0 / (1.0 + (diff * diff) / (bandwidth * bandwidth))
        return response
    }

    private fun profileFor(tonePreset: TonePreset): ToneProfile = when (tonePreset) {
        TonePreset.WARM_PRACTICE -> TonePresets.warmPractice()
        TonePreset.SOFT_PIANO -> TonePresets.softPiano()
        TonePreset.CLEAR_WOOD -> TonePresets.clearWood()
    }
}
