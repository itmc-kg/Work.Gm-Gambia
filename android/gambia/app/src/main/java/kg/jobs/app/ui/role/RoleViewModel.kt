package kg.jobs.app.ui.role

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceId
import kg.jobs.app.await
import kg.jobs.app.repository.LocalPrefData
import kg.jobs.app.repository.RoleRepository
import kg.jobs.app.ui.BaseViewModel
import kotlinx.coroutines.launch

class RoleViewModel(val uid: String,
                    val repo: RoleRepository,
                    val firestore: FirebaseFirestore) : BaseViewModel() {
    enum class State { LOADING, SELECT_ROLE, OPEN_EMPLOYER, OPEN_EMPLOYEE, CREATE_EMPLOYER, CREATE_EMPLOYEE, CHECK_NETWORK }

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    init {
        launch {
            _state.postValue(State.LOADING)
            repo.getCountry()
            val token = FirebaseInstanceId.getInstance().instanceId.await()
                    .takeIf { it.isSuccessful }
                    ?.let { it.result?.token }
            if (token != null) {
                firestore.collection("users")
                        .document(uid)
                        .collection("devices")
                        .document(token)
                        .set(mapOf("type" to "android"), SetOptions.merge())
            }

            when (repo.getRole()) {
                "employee" -> {
                    if (repo.isProfileCreated("employee"))
                        _state.postValue(State.OPEN_EMPLOYEE)
                    else
                        _state.postValue(State.CREATE_EMPLOYEE)
                }
                "employer" -> {
                    if (repo.isProfileCreated("employer"))
                        _state.postValue(State.OPEN_EMPLOYER)
                    else
                        _state.postValue(State.CREATE_EMPLOYER)

                }
                null -> _state.postValue(State.CHECK_NETWORK)
                else -> {
                    _state.postValue(State.SELECT_ROLE)
                }
            }
        }
    }

    fun selectedEmployer() {
        launch {
            _state.postValue(State.LOADING)
            if (repo.selectEmployer())
                _state.postValue(State.CREATE_EMPLOYER)
            else
                _state.postValue(State.CHECK_NETWORK)
        }
    }

    fun selectedEmployee() {
        launch {
            _state.postValue(State.LOADING)
            if (repo.selectEmployee())
                _state.postValue(State.CREATE_EMPLOYEE)
            else
                _state.postValue(State.CHECK_NETWORK)
        }
    }

}
