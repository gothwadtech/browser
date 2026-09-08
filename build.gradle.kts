// Top-level build file where you can add configuration options common to all sub-projects/modules.

// Auto-restore debug.keystore from debug.keystore.base64 if absent on disk
val debugKeystoreFile = file("${rootDir}/debug.keystore")
val base64KeystoreFile = file("${rootDir}/debug.keystore.base64")
if (!debugKeystoreFile.exists() && base64KeystoreFile.exists()) {
    try {
        val decoded = java.util.Base64.getDecoder().decode(base64KeystoreFile.readText().trim())
        debugKeystoreFile.writeBytes(decoded)
    } catch (e: Exception) {
        logger.warn("Failed to auto-restore debug.keystore: ${e.message}")
    }
}

// Android and Kotlin plugins are applied by buildSrc convention plugins (tvbrowser.android.library / tvbrowser.android.application).
// Only declare here plugins that subprojects apply via the version catalog and are not on buildSrc classpath.
plugins {
    alias(libs.plugins.ksp) apply false
}
