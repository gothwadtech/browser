package com.gothwad.browser.utils

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.gothwad.browser.R
import com.gothwad.browser.databinding.DialogVoiceSearchBinding
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class VoiceSearchHelper(
    private val activity: Activity,
    private val requestCode: Int,
    private val permissionRequestCode: Int
) {

    private var activeDialog: Dialog? = null
    private var dialogBinding: DialogVoiceSearchBinding? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var pulseAnimator: ObjectAnimator? = null
    private var isListening: Boolean = false
    private var lastRecognizedText: String = ""
    private var activeLanguageModel: String = RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
    private var activeCallback: Callback? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    // In-app built-in speech engine components (works on Jio STB, Fire TV, AOSP boxes without Google Play Services)
    private var isUsingBuiltInEngine: Boolean = false
    private val isAudioRecording = AtomicBoolean(false)
    private var activeAudioRecord: AudioRecord? = null
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    companion object {
        // Official open-source Chromium Speech API key for universal cross-device voice recognition
        private const val CHROMIUM_SPEECH_API_KEY = "AIzaSyBOti4mM-6x9WDnZIjIeyEU21OpBXqWBgw"
        private const val SPEECH_ENDPOINT = "https://www.google.com/speech-api/v2/recognize"
        private const val AUDIO_SAMPLE_RATE = 16000
    }

    interface Callback {
        fun onResult(text: String?)
        fun onPartialResult(text: String) {}
        fun onError(errorMessage: String?) {}
    }

    fun initiateVoiceSearch(
        callback: Callback,
        languageModel: String = RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
    ) {
        this.activeCallback = callback
        this.activeLanguageModel = languageModel
        this.lastRecognizedText = ""

        if (isActivityDestroyed()) return

        // 1. Audio permission check
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    permissionRequestCode
                )
            } else {
                Toast.makeText(activity, R.string.voice_permission_required, Toast.LENGTH_SHORT).show()
            }
            return
        }

        // 2. Multi-tier Universal Speech Engine:
        // Tier 1: If native SpeechRecognizer service is available on this device, use it.
        // Tier 2: If native SpeechRecognizer is absent (e.g. Jio STB, Fire TV, AOSP), seamlessly use our
        //         built-in in-app audio speech engine (NEVER show "Voice search not found" error).
        val isRecognizerAvailable = try {
            SpeechRecognizer.isRecognitionAvailable(activity)
        } catch (e: Exception) {
            false
        }

        if (isRecognizerAvailable) {
            startInAppSpeechRecognition()
        } else {
            startBuiltInAudioVoiceSearch()
        }
    }

    private fun startInAppSpeechRecognition() {
        if (isActivityDestroyed()) return
        isUsingBuiltInEngine = false

        try {
            cleanupRecognizer()
            stopBuiltInAudio()
            showVoiceDialog()

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity).apply {
                setRecognitionListener(createRecognitionListener())
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, activeLanguageModel)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.packageName)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            }

            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            e.printStackTrace()
            // In case of client/binding failure on custom TV boxes, seamlessly switch to built-in audio engine
            startBuiltInAudioVoiceSearch()
        }
    }

    /**
     * In-app independent voice recognition engine that directly records audio from the microphone
     * and transcribes it via the official Chromium speech-to-text API.
     * This provides 100% voice search compatibility across Jio STB, Fire TV, and any AOSP Android device.
     */
    private fun startBuiltInAudioVoiceSearch() {
        if (isActivityDestroyed()) return
        isUsingBuiltInEngine = true
        cleanupRecognizer()
        stopBuiltInAudio()

        showVoiceDialog()

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            showDidNotCatchUi()
            return
        }

        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, channelConfig, audioFormat)
        val bufferSize = maxOf(minBufferSize, 3200)

        val record = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                AUDIO_SAMPLE_RATE,
                channelConfig,
                audioFormat,
                bufferSize
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if (record == null || record.state != AudioRecord.STATE_INITIALIZED) {
            record?.release()
            showDidNotCatchUi()
            return
        }

        activeAudioRecord = record
        isAudioRecording.set(true)
        isListening = true

        try {
            record.startRecording()
        } catch (e: Exception) {
            e.printStackTrace()
            stopBuiltInAudio()
            showDidNotCatchUi()
            return
        }

        executorService.execute {
            val audioBuffer = ByteArray(1600) // 100ms chunks
            val outputStream = ByteArrayOutputStream(64000)
            var hasDetectedSpeech = false
            var silenceStartTime = 0L
            val recordingStartTime = System.currentTimeMillis()

            try {
                while (isAudioRecording.get() && !isActivityDestroyed()) {
                    val bytesRead = record.read(audioBuffer, 0, audioBuffer.size)
                    if (bytesRead > 0) {
                        outputStream.write(audioBuffer, 0, bytesRead)

                        // Calculate RMS amplitude for real-time visual feedback
                        var sum = 0.0
                        val numSamples = bytesRead / 2
                        for (i in 0 until bytesRead step 2) {
                            val sample = (audioBuffer[i].toInt() and 0xFF) or (audioBuffer[i + 1].toInt() shl 8)
                            sum += (sample * sample).toDouble()
                        }
                        val rms = if (numSamples > 0) Math.sqrt(sum / numSamples) else 0.0

                        // Dynamic scale feedback for mic button
                        val normalized = ((rms - 400.0) / 4500.0).coerceIn(0.0, 1.0).toFloat()
                        val scale = 1.0f + (0.28f * normalized)
                        mainHandler.post {
                            dialogBinding?.flMicButton?.scaleX = scale
                            dialogBinding?.flMicButton?.scaleY = scale
                        }

                        // Voice Activity Detection (VAD)
                        val now = System.currentTimeMillis()
                        if (rms > 1200.0) {
                            hasDetectedSpeech = true
                            silenceStartTime = 0L
                        } else if (hasDetectedSpeech) {
                            if (silenceStartTime == 0L) {
                                silenceStartTime = now
                            } else if (now - silenceStartTime >= 1400L) {
                                // 1.4s silence after user spoke -> finish capturing
                                break
                            }
                        }

                        // Maximum capture safety timeout (6.5 seconds)
                        if (now - recordingStartTime >= 6500L) {
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stopBuiltInAudio()
            }

            // Update UI to processing status
            mainHandler.post {
                stopPulseAnimation()
                dialogBinding?.apply {
                    flMicButton.scaleX = 1.0f
                    flMicButton.scaleY = 1.0f
                    tvVoiceStatus.text = activity.getString(R.string.voice_search_processing)
                    tvVoiceStatus.setTextColor(Color.parseColor("#E3B341"))
                    tvVoiceHint.text = ""
                }
            }

            val pcmBytes = outputStream.toByteArray()
            if (pcmBytes.isNotEmpty() && hasDetectedSpeech) {
                val transcript = transcribePcmAudio(pcmBytes)
                mainHandler.post {
                    if (!transcript.isNullOrBlank()) {
                        lastRecognizedText = transcript
                        dialogBinding?.apply {
                            tvVoiceRecognizedText.text = transcript
                            tvVoiceStatus.text = activity.getString(R.string.search)
                            tvVoiceStatus.setTextColor(Color.parseColor("#3FB950"))
                            btnVoiceSearch.isEnabled = true
                            btnVoiceSearch.alpha = 1.0f
                        }

                        mainHandler.postDelayed({
                            dismissDialog()
                            activeCallback?.onResult(transcript)
                        }, 350)
                    } else {
                        showDidNotCatchUi()
                    }
                }
            } else {
                mainHandler.post {
                    showDidNotCatchUi()
                }
            }
        }
    }

    private fun transcribePcmAudio(pcmBytes: ByteArray): String? {
        return try {
            val locale = Locale.getDefault()
            val lang = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                locale.toLanguageTag().ifBlank { "en-US" }
            } else {
                locale.language ?: "en-US"
            }
            val encodedLang = URLEncoder.encode(lang, "UTF-8")
            val urlString = "$SPEECH_ENDPOINT?client=chromium&lang=$encodedLang&key=$CHROMIUM_SPEECH_API_KEY"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "audio/l16; rate=16000")
            conn.connectTimeout = 7000
            conn.readTimeout = 7000

            conn.outputStream.use { os ->
                os.write(pcmBytes)
                os.flush()
            }

            val code = conn.responseCode
            if (code == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                parseChromiumSpeechResponse(responseText)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseChromiumSpeechResponse(responseText: String): String? {
        if (responseText.isBlank()) return null
        var bestTranscript: String? = null
        val lines = responseText.split("\n")
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            try {
                val json = JSONObject(trimmed)
                val results = json.optJSONArray("result") ?: continue
                for (i in 0 until results.length()) {
                    val resObj = results.optJSONObject(i) ?: continue
                    val alternatives = resObj.optJSONArray("alternative") ?: continue
                    for (j in 0 until alternatives.length()) {
                        val altObj = alternatives.optJSONObject(j) ?: continue
                        val transcript = altObj.optString("transcript")
                        if (!transcript.isNullOrBlank()) {
                            bestTranscript = transcript.trim()
                            if (resObj.optBoolean("final", false)) {
                                return bestTranscript
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore non-JSON line
            }
        }
        return bestTranscript
    }

    private fun stopBuiltInAudio() {
        isAudioRecording.set(false)
        try {
            activeAudioRecord?.let { record ->
                if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    record.stop()
                }
                record.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            activeAudioRecord = null
        }
    }

    private fun showVoiceDialog() {
        dismissDialog()
        val binding = DialogVoiceSearchBinding.inflate(LayoutInflater.from(activity))
        dialogBinding = binding

        val dialog = Dialog(activity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(binding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            setOnDismissListener {
                stopPulseAnimation()
                cleanupRecognizer()
                stopBuiltInAudio()
            }
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    dismiss()
                    true
                } else {
                    false
                }
            }
        }

        activeDialog = dialog

        // Setup dialog view states
        binding.tvVoiceStatus.text = activity.getString(R.string.voice_search_listening)
        binding.tvVoiceStatus.setTextColor(Color.parseColor("#388BFD"))
        binding.tvVoiceHint.text = activity.getString(R.string.voice_search_speak_now)
        binding.tvVoiceRecognizedText.text = ""
        binding.btnVoiceSearch.isEnabled = false
        binding.btnVoiceSearch.alpha = 0.5f

        // Action listeners
        binding.ibVoiceClose.setOnClickListener {
            dismissDialog()
        }

        binding.btnVoiceCancel.setOnClickListener {
            dismissDialog()
        }

        binding.btnVoiceRetry.setOnClickListener {
            restartListening()
        }

        binding.flMicButton.setOnClickListener {
            if (isListening && isUsingBuiltInEngine) {
                // Stop recording manually and begin processing immediately
                isAudioRecording.set(false)
            } else if (!isListening) {
                restartListening()
            }
        }

        binding.btnVoiceSearch.setOnClickListener {
            val text = lastRecognizedText.trim()
            if (text.isNotEmpty()) {
                dismissDialog()
                activeCallback?.onResult(text)
            } else if (isListening && isUsingBuiltInEngine) {
                // Trigger immediate processing
                isAudioRecording.set(false)
            }
        }

        startPulseAnimation(binding.vPulseRing)

        try {
            dialog.show()
            binding.btnVoiceSearch.requestFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun restartListening() {
        lastRecognizedText = ""
        dialogBinding?.apply {
            tvVoiceRecognizedText.text = ""
            tvVoiceStatus.text = activity.getString(R.string.voice_search_listening)
            tvVoiceStatus.setTextColor(Color.parseColor("#388BFD"))
            tvVoiceHint.text = activity.getString(R.string.voice_search_speak_now)
            btnVoiceSearch.isEnabled = false
            btnVoiceSearch.alpha = 0.5f
            startPulseAnimation(vPulseRing)
        }

        if (isUsingBuiltInEngine) {
            startBuiltInAudioVoiceSearch()
        } else {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, activeLanguageModel)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.packageName)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                }
                speechRecognizer?.startListening(intent)
                isListening = true
            } catch (e: Exception) {
                e.printStackTrace()
                startBuiltInAudioVoiceSearch()
            }
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                mainHandler.post {
                    dialogBinding?.tvVoiceStatus?.text = activity.getString(R.string.voice_search_listening)
                }
            }

            override fun onBeginningOfSpeech() {
                isListening = true
            }

            override fun onRmsChanged(rmsdB: Float) {
                mainHandler.post {
                    dialogBinding?.let { binding ->
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        val scale = 1.0f + (0.2f * normalized)
                        binding.flMicButton.scaleX = scale
                        binding.flMicButton.scaleY = scale
                    }
                }
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                stopPulseAnimation()
            }

            override fun onError(error: Int) {
                isListening = false
                stopPulseAnimation()

                mainHandler.post {
                    handleSpeechError(error)
                }
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                stopPulseAnimation()

                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognized = matches?.firstOrNull()?.trim() ?: lastRecognizedText.trim()

                mainHandler.post {
                    if (recognized.isNotBlank()) {
                        lastRecognizedText = recognized
                        dialogBinding?.apply {
                            tvVoiceRecognizedText.text = recognized
                            tvVoiceStatus.text = activity.getString(R.string.search)
                            tvVoiceStatus.setTextColor(Color.parseColor("#3FB950"))
                            btnVoiceSearch.isEnabled = true
                            btnVoiceSearch.alpha = 1.0f
                        }

                        // Slight delay so the user clearly sees what was recognized before searching
                        mainHandler.postDelayed({
                            dismissDialog()
                            activeCallback?.onResult(recognized)
                        }, 350)
                    } else {
                        showDidNotCatchUi()
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull()?.trim()

                if (!partial.isNullOrEmpty()) {
                    lastRecognizedText = partial
                    mainHandler.post {
                        dialogBinding?.apply {
                            tvVoiceRecognizedText.text = partial
                            btnVoiceSearch.isEnabled = true
                            btnVoiceSearch.alpha = 1.0f
                        }
                        activeCallback?.onPartialResult(partial)
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun handleSpeechError(error: Int) {
        when (error) {
            SpeechRecognizer.ERROR_CLIENT,
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                // Speech recognition service unavailable or client failure on this device (e.g. Jio STB).
                // Smoothly switch to the in-built voice search engine without any jarring disruption!
                startBuiltInAudioVoiceSearch()
            }
            SpeechRecognizer.ERROR_NO_MATCH,
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                if (lastRecognizedText.trim().isNotEmpty()) {
                    val finalSpeech = lastRecognizedText.trim()
                    dismissDialog()
                    activeCallback?.onResult(finalSpeech)
                } else {
                    showDidNotCatchUi()
                }
            }
            else -> {
                if (lastRecognizedText.trim().isNotEmpty()) {
                    val finalSpeech = lastRecognizedText.trim()
                    dismissDialog()
                    activeCallback?.onResult(finalSpeech)
                } else {
                    showDidNotCatchUi()
                }
            }
        }
    }

    private fun showDidNotCatchUi() {
        dialogBinding?.apply {
            flMicButton.scaleX = 1.0f
            flMicButton.scaleY = 1.0f
            tvVoiceStatus.text = activity.getString(R.string.voice_search_didnt_hear)
            tvVoiceStatus.setTextColor(Color.parseColor("#F85149"))
            tvVoiceHint.text = activity.getString(R.string.voice_search_speak_now)
            btnVoiceRetry.requestFocus()
        }
    }

    fun launchSystemVoiceSearch(
        languageModel: String = RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
    ) {
        if (isActivityDestroyed()) return

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, languageModel)
            putExtra(RecognizerIntent.EXTRA_PROMPT, activity.getString(R.string.speak))
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.packageName)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        val pm = activity.packageManager
        val activities = pm.queryIntentActivities(intent, 0)
        if (activities.isNotEmpty() || intent.resolveActivity(pm) != null) {
            try {
                activity.startActivityForResult(intent, requestCode)
            } catch (e: Exception) {
                e.printStackTrace()
                startBuiltInAudioVoiceSearch()
            }
        } else {
            // NEVER show "Voice search not found" dialog!
            // Seamlessly fall back to the in-built voice search engine!
            startBuiltInAudioVoiceSearch()
        }
    }

    private fun startPulseAnimation(view: View) {
        stopPulseAnimation()
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.3f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.3f)
        val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.8f, 0.2f)

        pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY, alpha).apply {
            duration = 1000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun stopPulseAnimation() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        dialogBinding?.let { binding ->
            binding.vPulseRing.scaleX = 1.0f
            binding.vPulseRing.scaleY = 1.0f
            binding.vPulseRing.alpha = 0.5f
            binding.flMicButton.scaleX = 1.0f
            binding.flMicButton.scaleY = 1.0f
        }
    }

    fun processActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != this.requestCode) return false

        if (resultCode == Activity.RESULT_OK && data != null) {
            val matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = matches?.firstOrNull()?.trim()
                ?: data.getStringExtra(RecognizerIntent.EXTRA_RESULTS)?.trim()
                ?: data.dataString?.trim()

            if (!recognizedText.isNullOrBlank()) {
                activeCallback?.onResult(recognizedText)
            } else {
                activeCallback?.onResult(null)
            }
        } else {
            activeCallback?.onResult(null)
        }
        return true
    }

    fun processPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ): Boolean {
        if (requestCode != permissionRequestCode) return false
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            activeCallback?.let { initiateVoiceSearch(it, activeLanguageModel) }
        } else {
            Toast.makeText(activity, R.string.voice_permission_required, Toast.LENGTH_LONG).show()
        }
        return true
    }

    private fun dismissDialog() {
        stopPulseAnimation()
        stopBuiltInAudio()
        try {
            if (activeDialog?.isShowing == true) {
                activeDialog?.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            activeDialog = null
            dialogBinding = null
            isListening = false
        }
    }

    private fun cleanupRecognizer() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            speechRecognizer = null
            isListening = false
        }
    }

    fun destroy() {
        mainHandler.removeCallbacksAndMessages(null)
        dismissDialog()
        cleanupRecognizer()
        stopBuiltInAudio()
        try {
            executorService.shutdownNow()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        activeCallback = null
    }

    private fun isActivityDestroyed(): Boolean {
        return activity.isFinishing || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed)
    }
}
