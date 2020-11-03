package kg.jobs.app.ui.profile.edit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kg.jobs.app.R
import kg.jobs.app.model.Education
import kg.jobs.app.model.User
import kg.jobs.app.model.Work
import kg.jobs.app.notifyObserver
import kg.jobs.app.repository.ImageRepository
import kg.jobs.app.repository.ProfileRepository
import kg.jobs.app.ui.BaseViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class EditProfileViewModel(private val repo: ProfileRepository,
                           private val imageRepo: ImageRepository,
                           private val context: Context) : BaseViewModel() {
    enum class State { DATA, LOADING, CLOSE }

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

    private var _newImage: Uri? = null

    fun init() {
        launch {
            _state.postValue(State.LOADING)
            val user = async { repo.getUser() }
            val edus = async { repo.getEducations() }
            val works = async { repo.getWorks() }
            _user.postValue(user.await())
            _works.postValue(works.await())
            _educations.postValue(edus.await())
            _state.postValue(State.DATA)
        }
    }

    fun addNewWork() {
        _works.value?.apply {
            if (find { it.isNotFull() } == null) {
                add(Work())
                _works.notifyObserver()
            } else _error.value = context.getString(R.string.fill_empty_fields)
        }
    }

    fun addNewEducation() {
        _educations.value?.apply {
            if (find { it.isNotFull() } == null) {
                add(Education())
                _educations.notifyObserver()
            } else _error.value = context.getString(R.string.fill_empty_fields)
        }
    }

    fun educationChanged(edu: Education) {
        _educations.value!!.indexOfFirst { it.id == edu.id }
                .takeIf { it > -1 }
                ?.also {
                    _educations.value?.set(it, edu)
                }
    }

    fun workChanged(work: Work) {
        _works.value!!.indexOfFirst { it.id == work.id }
                .takeIf { it > -1 }
                ?.also {
                    _works.value?.set(it, work)
                }
    }

    fun setName(name: String) {
        _user.value?.name = name
    }

    fun save() {
        launch {
            if (isEmptyData())
            else {
                _state.value = State.LOADING

                val imageUrl = _newImage?.let { imageRepo.upload(it) } ?: ""
                _user.value = _user.value!!.copy(image = imageUrl)

                val result = repo.save(user.value!!, works.value!!, educations.value!!)
                if (result) _state.value = State.CLOSE
                else {
                    _state.value = State.DATA
                    _error.value = context.getString(R.string.check_your_internet_connection)
                }
            }
        }
    }

    private fun isEmptyData():Boolean{
        if (_user.value?.name.isNullOrEmpty()) {
            _error.value = context.getString(R.string.input_your_name)
            return true
        }

        // if company is empty
      /*  for (work in works.value!!) {
            if (work.company.isNullOrEmpty()){
                _error.value = context.getString(R.string.input_your_name)
                return true
            }
            if (work.position.isNullOrEmpty()){
                _error.value = context.getString(R.string.input_your_name)
                return true
            }
        }*/
        return false
    }


    fun newImage(image: Uri) {
        _newImage = image
    }
}