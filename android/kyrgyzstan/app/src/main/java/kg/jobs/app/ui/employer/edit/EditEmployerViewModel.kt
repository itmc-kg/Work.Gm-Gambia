package kg.jobs.app.ui.employer.edit

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kg.jobs.app.R
import kg.jobs.app.model.Employer
import kg.jobs.app.model.Region
import kg.jobs.app.repository.EmployerRepository
import kg.jobs.app.repository.ImageRepository
import kg.jobs.app.ui.BaseViewModel
import kotlinx.coroutines.launch

class EditEmployerViewModel(private val repo: EmployerRepository,
                            private val imageRepo: ImageRepository,
                            private val context: Context) : BaseViewModel() {
    enum class Event { LOADING, ERROR, SUCCESS, CLOSE }

    private val _events = MutableLiveData<Event>()
    val events: LiveData<Event> = _events

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _regions = MutableLiveData<List<Region>>()
    val regions: LiveData<List<Region>> = _regions

    private val _selectedRegion = MutableLiveData<Region>()
    val selectedRegion: LiveData<String> = Transformations.map(_selectedRegion) { it.name }

    private val _employer = MutableLiveData<Employer>()
    val employer: LiveData<Employer> = _employer

    private var _newImage: Uri? = null

    fun init() {
        launch {
            _events.postValue(Event.LOADING)
//            repo.getEmployer()?.let(_employer.value = it)
            _employer.value = repo.getEmployer()
            val regions = repo.allRegions()
            if (regions == null) {
                _events.postValue(Event.ERROR)
                _error.postValue(context.getString(R.string.unknown_error))
            } else {
                _regions.postValue(regions)
                regions.find { it.checked }?.also { _selectedRegion.postValue(it) }
                _events.postValue(Event.SUCCESS)
            }
        }
    }

    fun selectRegion(region: Region) {
        _selectedRegion.value = region
    }

    fun newImage(image: Uri) {
        _newImage = image
    }

    fun save(name: String, about: String, city: String) {
        launch {
            if (name.trim().isEmpty()
                    || about.trim().isEmpty()
                    || city.trim().isEmpty()
                    || _selectedRegion.value == null)
                _error.value = context.getString(R.string.fill_all_fields)
            else {
                _events.value = Event.LOADING
                val image = _newImage?.let { imageRepo.upload(it) } ?: _employer.value?.image ?: ""
                if (repo.save(name.trim(), about.trim(), _selectedRegion.value!!.id, city.trim(), image))
                    _events.value = Event.CLOSE
                else {
                    _events.value = Event.ERROR
                    _error.value = context.getString(R.string.unknown_error)
                }
            }
        }
    }
}