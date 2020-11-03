package kg.jobs.app.ui.search.show

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kg.jobs.app.model.Application
import kg.jobs.app.model.Chat
import kg.jobs.app.model.Vacancy
import kg.jobs.app.repository.ChatRepository
import kg.jobs.app.repository.SearchRepository
import kg.jobs.app.ui.BaseViewModel
import kotlinx.coroutines.launch

class VacancyDetailViewModel(private val repo: SearchRepository,
                             private val chatRepo: ChatRepository) : BaseViewModel() {
    enum class State { LOADING, DATA, ACCEPT, DECLINE, ERROR, CLOSE }

    private val _vacancy = MutableLiveData<Vacancy>()
    val vacancy: LiveData<Vacancy> = _vacancy

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    private val _openChat = MutableLiveData<Chat>()
    val openChat: LiveData<Chat> = _openChat

    private var _application: Application? = null

    fun init(application: Application) {
        launch {
            _state.postValue(State.LOADING)
            _application = application
            init(repo.getVacancy(application))
        }
    }

    fun init(vacancy: Vacancy?) {
        launch {
            if (vacancy == null)
                _state.postValue(State.CLOSE)
            else {
                _application = vacancy.application?.copy(employer = vacancy.employer)
                _state.postValue(State.DATA)
                _vacancy.postValue(vacancy)
            }
        }
    }

    fun startChat() {
        launch {
            _application?.also {
                _state.postValue(State.LOADING)
                val chat = chatRepo.createChat(it)
                if (chat != null) {
                    _state.postValue(State.DATA)
                    _openChat.postValue(chat)
                } else {
                    _state.postValue(State.ERROR)
                }
            }
        }
    }

    fun apply() {
        launch {
            vacancy.value?.apply {
                _state.postValue(State.LOADING)
                val result = repo.apply(this)
                _state.postValue(if (result) State.ACCEPT else State.ERROR)
            }
        }
    }

    fun decline() {
        launch {
            vacancy.value?.apply {
                _state.postValue(State.LOADING)
                val result = repo.decline(this)
                _state.postValue(if (result) State.DECLINE else State.ERROR)
            }
        }
    }
}