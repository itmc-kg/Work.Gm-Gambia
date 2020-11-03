package kg.jobs.app.ui.login

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kg.jobs.app.model.Country
import kg.jobs.app.model.VerificationSms
import kg.jobs.app.repository.LoginRepository
import kg.jobs.app.ui.BaseViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class LoginViewModel(private val repository: LoginRepository) : BaseViewModel() {
    companion object {
        const val EMPTY_LIST = "empty_list"
        const val FB_DB_ERROR = "firebase db error"
        const val LOADING = "LOADING"
        const val AUTH_ERROR = "login_activity_error"
        const val OK = "successfully"
    }

    var phone: String = ""
    var events = MutableLiveData<Pair<String, Any>>()

    init {
        launch {
            val countries = repository.readCountries()
            _countries.value = countries
        }
    }

    private var success: Boolean = false
    private var smsVerification: VerificationSms? = null
    private val _countries = MutableLiveData<Map<String, Country>>()
    val countries: LiveData<Map<String, Country>> = _countries

    fun auth(phone: String, activity: Activity) {
        this.phone = phone
        PhoneAuthProvider.getInstance()
                .verifyPhoneNumber(this.phone, 60, TimeUnit.SECONDS,
                        activity, onVerificationStateChangedCallbacks)
        events.value = Pair(LOADING, "loading")
    }

    fun verification(codeSms: String) {
        val credential: PhoneAuthCredential = PhoneAuthProvider
                .getCredential(smsVerification!!.id, codeSms)
        signInWithPhoneAuthCredential(credential)
    }

    fun resentCode(activity: Activity) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phone, 60,
                TimeUnit.SECONDS, activity, onVerificationStateChangedCallbacks, smsVerification!!.token)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance()
                .signInWithCredential(credential)
                .addOnCompleteListener {
                    success = it.isSuccessful
                    if (it.isSuccessful) {
                        events.value = Pair(OK, "ok")
                    } else {
                        val message = it.exception?.localizedMessage ?: "Error occurred"
                        events.value = Pair(AUTH_ERROR, message)
                    }
                }
    }

    private var onVerificationStateChangedCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationFailed(exception: FirebaseException) {
            exception.printStackTrace()
            events.value = Pair(AUTH_ERROR, exception.localizedMessage)
        }

        override fun onVerificationCompleted(credential: PhoneAuthCredential?) {
            if (!success) {
                signInWithPhoneAuthCredential(credential!!)
            } else {
                events.value = Pair("changed", smsVerification!!)
            }
        }

        override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
            smsVerification = VerificationSms(id, token)
            events.value = Pair("showSmsDialog", smsVerification!!)
        }
    }
}
