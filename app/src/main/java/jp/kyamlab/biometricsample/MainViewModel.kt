package jp.kyamlab.biometricsample

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _authenticateResult = MutableLiveData<String>()
    val authenticateResult: LiveData<String> = _authenticateResult

    fun setAuthenticateResult(result: String) {
        _authenticateResult.postValue(result)
    }
}