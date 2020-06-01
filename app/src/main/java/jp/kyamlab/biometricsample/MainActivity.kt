package jp.kyamlab.biometricsample

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isBiometricManagerAvailable()) {
            Toast.makeText(
                applicationContext, "Authentication failed",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        button_authenticate.setOnClickListener {
            showBiometricPrompt()
        }

        viewModel.authenticateResult.observe(this) {
            textView.text = it
        }
    }

    // デバイスが生体認証をサポートしているか
    // true:サポート
    // false:サポートしていない
    private fun isBiometricManagerAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "App can authenticate using biometrics.")
                true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e(TAG, "No biometric features available on this device.")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.e(TAG, "Biometric features are currently unavailable.")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.e(
                    TAG,
                    "The user hasn't associated any biometric credentials with their account."
                )
                false
            }
            else -> throw RuntimeException()
        }
    }

    private fun showBiometricPrompt() {
        val executor = Executors.newSingleThreadExecutor()

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        val biometricPrompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // 指紋認証に連続で失敗してロックされた時にも呼ばれる
                    Log.e(
                        TAG,
                        ERROR_OCCURRED
                    )
                    viewModel.setAuthenticateResult(ERROR_OCCURRED)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val authenticatedCryptoObject: BiometricPrompt.CryptoObject? =
                        result.cryptoObject
                    Log.d(
                        TAG,
                        AUTHENTICATED
                    )
                    viewModel.setAuthenticateResult(AUTHENTICATED)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.e(
                        TAG,
                        AUTHENTICATE_FAILED
                    )
                    viewModel.setAuthenticateResult(AUTHENTICATE_FAILED)
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }

    companion object {
        // ログ表示用のタグ
        private const val TAG = "MainActivity"
        private const val ERROR_OCCURRED = "エラーが発生しました"
        private const val AUTHENTICATED = "認証に成功しました"
        private const val AUTHENTICATE_FAILED = "認証に失敗しました"
    }
}
