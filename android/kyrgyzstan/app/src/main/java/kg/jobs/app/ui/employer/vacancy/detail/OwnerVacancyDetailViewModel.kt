package kg.jobs.app.ui.employer.vacancy.detail

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kg.jobs.app.R
import kg.jobs.app.model.Vacancy
import kg.jobs.app.repository.VacancyRepository
import kg.jobs.app.ui.BaseViewModel
import kotlinx.coroutines.launch

class OwnerVacancyDetailViewModel(val repo: VacancyRepository, val context: Context) : BaseViewModel() {
    enum class State { LOADING, SUCCESS, FAIL }

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    private val _vacancy = MutableLiveData<Vacancy>()
    val vacancy: LiveData<Vacancy> = _vacancy

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun init(v: Vacancy) {
        launch {
            try {
                _state.postValue(State.LOADING)
                val vacancy = repo.getVacancyDetail(v)
                _state.postValue(State.SUCCESS)
                _vacancy.postValue(vacancy)
            } catch (e: Exception) {
                _error.postValue(context.getString(R.string.unknown_error))
                _state.postValue(State.FAIL)
            }
        }
    }
}