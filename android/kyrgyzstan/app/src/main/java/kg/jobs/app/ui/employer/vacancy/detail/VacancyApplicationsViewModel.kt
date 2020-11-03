package kg.jobs.app.ui.employer.vacancy.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kg.jobs.app.model.Application
import kg.jobs.app.model.Chat
import kg.jobs.app.model.Vacancy
import kg.jobs.app.repository.ChatRepository
import kg.jobs.app.repository.VacancyApplicationsRepository
import kg.jobs.app.ui.BaseViewModel
import kotlinx.coroutines.launch

class VacancyApplicationsViewModel(val repo: VacancyApplicationsRepository,
                                   val chatRepo: ChatRepository,
                                   val firestore: FirebaseFirestore) : BaseViewModel() {
    enum class State { LOADING, ACCEPTED, REJECTED, ERROR, SUCCESS }

    private val _application = MutableLiveData<Application>()
    val application: LiveData<Application> = _application

    private val _applicationState = MutableLiveData<Pair<State, Application>>()
    val applicationState: LiveData<Pair<State, Application>> = _applicationState

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    private val _openChat = MutableLiveData<Chat>()
    val openChat: LiveData<Chat> = _openChat

    private val _loadData = MutableLiveData<Boolean>()
    val loadData: LiveData<Boolean> = _loadData

    private var listener: ListenerRegistration? = null
    fun init(vacancy: Vacancy) {
        _state.postValue(State.LOADING)
        listener = firestore.collection("applications")
                .whereEqualTo("vacancyId", vacancy.id)
                .addSnapshotListener { snapshot, e ->
                    launch {
                        if (e != null) {
                            _state.postValue(State.ERROR)
                            Log.w("Exception", "listen:error", e)
                        } else {
                            if (snapshot?.documents?.isEmpty() == true) _state.postValue(State.SUCCESS)
                            for (dc in snapshot!!.documentChanges) {
                                _loadData.postValue(true)
                                when (dc.type) {
                                    DocumentChange.Type.ADDED,
                                    DocumentChange.Type.MODIFIED -> {
                                        val application = repo.application(dc.document)
                                        _application.value = application
                                        _state.postValue(State.SUCCESS)
                                    }
                                }
                            }
                            _loadData.postValue(false)
                        }
                    }
                }
        firestore.collection("vacancies")
                .document(vacancy.id)
                .update(mapOf("applicationsCount" to 0))
    }

    fun rightClick(application: Application) {
        launch {
            if (application.status != "accepted") {
                _applicationState.postValue(Pair(State.LOADING, application))
                val result = repo.accept(application)
                _applicationState.postValue(Pair(if (result) State.ACCEPTED else State.ERROR,
                        if (result) application.copy(status = "accepted") else application))
            } else {
                _applicationState.postValue(Pair(State.LOADING, application))
                val chat = chatRepo.createChat(application)
                if (chat == null)
                    _applicationState.postValue(Pair(State.ERROR, application))
                else {
                    _applicationState.postValue(Pair(State.SUCCESS, application))
                    _openChat.postValue(chat)
                }
            }
        }
    }

    fun reject(application: Application) {
        launch {
            _applicationState.postValue(Pair(State.LOADING, application))
            val result = repo.reject(application)
            _applicationState.postValue(Pair(if (result) State.REJECTED else State.ERROR,
                    if (result) application.copy(status = "rejected") else application))
        }
    }

    override fun onCleared() {
        listener?.remove()
        super.onCleared()
    }
}