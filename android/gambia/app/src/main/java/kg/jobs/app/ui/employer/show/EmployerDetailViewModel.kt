package kg.jobs.app.ui.employer.show

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kg.jobs.app.R
import kg.jobs.app.model.Employer
import kg.jobs.app.repository.EmployerRepository
import kg.jobs.app.ui.BaseViewModel
import kotlinx.coroutines.launch

class EmployerDetailViewModel(private val myId: String,
                              private val employerId: String,
                              private val repo: EmployerRepository,
                              private val context: Context) : BaseViewModel() {
    enum class State { DATA, LOADING }

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _employer = MutableLiveData<Employer>()
    val employer: LiveData<Employer> = _employer

    val editButtonEnable = MutableLiveData<Boolean>()

    fun init() {
        launch {
            _state.postValue(State.LOADING)
            if (myId == employerId) {
                editButtonEnable.postValue(true)
                _title.postValue(context.getString(R.string.profile))
                _employer.postValue(repo.getEmployer(myId))
            } else {
                editButtonEnable.postValue(false)
                _title.postValue(context.getString(R.string.about_company))
                _employer.postValue(repo.getEmployer(employerId))
            }
            _state.postValue(State.DATA)
        }
    }
}