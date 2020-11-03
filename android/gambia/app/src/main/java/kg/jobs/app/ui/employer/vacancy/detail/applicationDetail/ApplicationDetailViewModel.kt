package kg.jobs.app.ui.employer.vacancy.detail.applicationDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kg.jobs.app.model.Application
import kg.jobs.app.model.Chat
import kg.jobs.app.repository.ChatRepository
import kg.jobs.app.repository.VacancyApplicationsRepository
import kg.jobs.app.ui.BaseViewModel
import kotlinx.coroutines.launch

class ApplicationDetailViewModel(val repo: VacancyApplicationsRepository,
                                 val chatRepo: ChatRepository) : BaseViewModel() {
    enum class State { LOADING, ACCEPTED, REJECTED, ERROR, CLOSE, SUCCESS }

    private val _applicationState = MutableLiveData<State>()
    val applicationState: LiveData<State> = _applicationState

    private val _application = MutableLiveData<Application>()
    val application: LiveData<Application> = _application

    private val _openChat = MutableLiveData<Chat>()
    val openChat: LiveData<Chat> = _openChat

    fun init(application: Application?) {
        launch {
            if (application == null)
                _applicationState.postValue(State.CLOSE)
            else
                _application.postValue(application)
        }
    }

    fun acceptOrChat() {
        launch {
            _applicationState.postValue(State.LOADING)
            if (application.value!!.status != "accepted") {
                val result = repo.accept(application.value!!)
                if (result) {
                    _applicationState.postValue(State.ACCEPTED)
                    _application.postValue(application.value!!.copy(status = "accepted"))
                } else
                    _applicationState.postValue(State.ERROR)

            } else {
                val chat = _application.value?.run { chatRepo.createChat(this) }
                if (chat == null)
                    _applicationState.postValue(State.ERROR)
                else {
                    _applicationState.postValue(State.SUCCESS)
                    _openChat.postValue(chat)
                }
            }
        }
    }

    fun reject() {
        launch {
            _applicationState.postValue(State.LOADING)
            val result = repo.reject(application.value!!)
            if (result) {
                _applicationState.postValue(State.REJECTED)
                _application.postValue(application.value!!.copy(status = "rejected"))
            } else
                _applicationState.postValue(State.ERROR)
        }
    }
}