package kg.jobs.app.ui.profile.show

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kg.jobs.app.R
import kg.jobs.app.model.Education
import kg.jobs.app.model.User
import kg.jobs.app.model.Work
import kg.jobs.app.repository.ProfileRepository
import kg.jobs.app.ui.BaseViewModel
import kotlinx.coroutines.launch

class ProfileDetailViewModel(val uid: String,
                             val employeeId: String,
                             val repo: ProfileRepository,
                             val context: Context) : BaseViewModel() {
    enum class State { DATA, LOADING }

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    private val _educations = MutableLiveData<MutableList<Education>>()
    val educations: LiveData<MutableList<Education>> = _educations

    private val _works = MutableLiveData<MutableList<Work>>()
    val works: LiveData<MutableList<Work>> = _works

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isMyProfile = MutableLiveData<Boolean>()
    val isMyProfile: LiveData<Boolean> = _isMyProfile

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    fun init() {
        launch {
            _state.postValue(State.LOADING)
            if (employeeId == uid) {
                _title.postValue(context.getString(R.string.profile))
                _isMyProfile.postValue(true)
                _user.postValue(repo.getUser())
                _educations.postValue(repo.getEducations())
                _works.postValue(repo.getWorks())

            } else {
                _title.postValue(context.getString(R.string.employee_text))
                _isMyProfile.postValue(false)
                _user.postValue(repo.getUser(employeeId))
                _educations.postValue(repo.getEducations(employeeId))
                _works.postValue(repo.getWorks(employeeId))
            }
            _state.postValue(State.DATA)
        }
    }
}