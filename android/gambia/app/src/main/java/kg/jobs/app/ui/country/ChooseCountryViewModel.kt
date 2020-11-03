package kg.jobs.app.ui.country

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kg.jobs.app.awaitWithException
import kg.jobs.app.repository.LocalPrefData
import kg.jobs.app.ui.BaseViewModel
import kotlinx.coroutines.launch

class ChooseCountryViewModel(val uid: String,
                             val firestore: FirebaseFirestore,
                             val localPrefData: LocalPrefData) : BaseViewModel() {
    private val _selectedCountry = MutableLiveData<String>()
    val selectedCountry: LiveData<String> = _selectedCountry

    init {
        launch {
            runCatching {
                val country = firestore.collection("users")
                        .document(uid)
                        .get()
                        .awaitWithException()
                        ?.getString("country") ?: ""
                _selectedCountry.postValue(country)
            }
        }
    }

    fun selectKyrgyzstan() {
        launch {
            runCatching {
                firestore.collection("users")
                        .document(uid)
                        .set(mapOf("country" to "KG"), SetOptions.merge())
                        .awaitWithException()
                _selectedCountry.postValue("KG")
                localPrefData.setCountry("KG")
                localPrefData.clearFilters()

            }
        }
    }

    fun selectGambia() {
        launch {
            runCatching {
                firestore.collection("users")
                        .document(uid)
                        .set(mapOf("country" to "GM"), SetOptions.merge())
                        .awaitWithException()
                localPrefData.setCountry("GM")
                localPrefData.clearFilters()
                _selectedCountry.postValue("GM")
            }
        }
    }

}
