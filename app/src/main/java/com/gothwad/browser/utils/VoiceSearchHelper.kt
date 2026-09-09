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
import android.net.Uri
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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.gothwad.browser.R
import com.gothwad.browser.databinding.DialogVoiceSearchBinding

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

        // 2. Dual engine check:
        // If in-app SpeechRecognizer is available, present the interactive in-app dialog
        // Otherwise, directly launch system voice search intent (e.g. Katniss on Android TV)
        val isRecognizerAvailable = try {
            SpeechRecognizer.isRecognitionAvailable(activity)
        } catch (e: Exception) {
            false
        }

        if (isRecognizerAvailable) {
            startInAppSpeechRecognition()
        } else {
            launchSystemVoiceSearch(languageModel)
        }
    }

    private fun startInAppSpeechRecognition() {
        if (isActivityDestroyed()) return

        try {
            cleanupRecognizer()
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
            // In case of binding or initialization failure, cleanly fall back to system intent
            dismissDialog()
            launchSystemVoiceSearch(activeLanguageModel)
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
            if (!isListening) {
                restartListening()
            }
        }

        binding.btnVoiceSearch.setOnClickListener {
            val text = lastRecognizedText.trim()
            if (text.isNotEmpty()) {
                dismissDialog()
                activeCallback?.onResult(text)
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
            startInAppSpeechRecognition()
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
                // Speech recognition client/service binding failure (common on certain TV boxes).
                // Seamlessly fall back to native system voice search!
                dismissDialog()
                launchSystemVoiceSearch(activeLanguageModel)
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
                showInstallVoiceEnginePrompt(activity)
            }
        } else {
            showInstallVoiceEnginePrompt(activity)
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

    private fun showInstallVoiceEnginePrompt(activity: Activity) {
        if (isActivityDestroyed()) return

        val dialogBuilder = AlertDialog.Builder(activity)
            .setTitle(R.string.app_name)
            .setMessage(R.string.voice_search_not_found)
            .setNeutralButton(android.R.string.ok) { _, _ -> }

        val appPackageName = if (Utils.isTV(activity)) {
            "com.google.android.katniss"
        } else {
            "com.google.android.googlequicksearchbox"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
        val activities = activity.packageManager.queryIntentActivities(intent, 0)
        if (activities.isNotEmpty()) {
            dialogBuilder.setPositiveButton(R.string.find_in_apps_store) { _, _ ->
                try {
                    activity.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
        try {
            dialogBuilder.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dismissDialog() {
        stopPulseAnimation()
        try {
            if (activeDialog?.isShowing == true) {
                activeDialog?.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            activeDialog = null
            dialogBinding = null
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
        activeCallback = null
    }

    private fun isActivityDestroyed(): Boolean {
        return activity.isFinishing || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed)
    }
}
