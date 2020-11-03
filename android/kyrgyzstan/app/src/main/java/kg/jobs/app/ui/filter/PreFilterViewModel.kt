package kg.jobs.app.ui.filter

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import kg.jobs.app.R
import kg.jobs.app.model.JobSphere
import kg.jobs.app.model.Region
import kg.jobs.app.repository.FilterRepository
import kg.jobs.app.ui.BaseViewModel
import kotlinx.coroutines.launch

class PreFilterViewModel(private val repo: FilterRepository,
                         private val context: Context) : BaseViewModel() {
    enum class Event { LOADING, SUCCESS, ERROR, CLOSE }

    private val _events = MutableLiveData<Event>()
    val events: LiveData<Event> = _events

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _jobSpheres = MutableLiveData<MutableList<JobSphere>>()
    val jobSpheres: LiveData<MutableList<JobSphere>> = _jobSpheres

    private val _sphereCheckedCount = MutableLiveData<Int>()
    val sphereCheckedCount: LiveData<Int> = _sphereCheckedCount

    private val _regions = MutableLiveData<List<Region>>()
    val regions: LiveData<List<Region>> = _regions

    private val _selectedRegion = MutableLiveData<Region>()
    val selectedRegion: LiveData<String> = Transformations.map(_selectedRegion) { it.name }

    fun sphereChecked(sphere: JobSphere) {
        _jobSpheres.value!!.indexOfFirst { it.id == sphere.id }
                .takeIf { it > -1 }
                ?.also {
                    _jobSpheres.value?.removeAt(it)
                    _jobSpheres.value?.add(it, sphere)
                    _sphereCheckedCount.value =
                            if (sphere.checked) _sphereCheckedCount.value!! + 1
                            else _sphereCheckedCount.value!! - 1
                }
    }

    fun selectRegion(region: Region) {
        _selectedRegion.value = region
    }

    fun save() {
        _selectedRegion.value?.also { region ->
            _jobSpheres.value?.filter { it.checked }?.also { spheres ->
                val selected = spheres.map { it.id }
                repo.saveFilters(region.id, selected)
                _events.postValue(Event.CLOSE)
            }
        }
    }

    fun init() {
        launch {
            _events.postValue(Event.LOADING)
            _sphereCheckedCount.postValue(0)
            val regions = repo.allRegions()
            val spheres = repo.jobSpheres()
            if (regions == null || spheres == null) {
                _events.postValue(Event.ERROR)
                _error.postValue(context.getString(R.string.unknown_error))
            } else {
                _regions.postValue(regions)
                regions.find { it.checked }?.also { _selectedRegion.postValue(it) }
                _jobSpheres.postValue(spheres)
                spheres.filter { it.checked }.run { _sphereCheckedCount.postValue(size) }
                _events.postValue(Event.SUCCESS)
            }
        }
    }
}