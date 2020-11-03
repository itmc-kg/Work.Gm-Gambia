package kg.jobs.app.ui.vacancy

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import kg.jobs.app.R
import kg.jobs.app.model.*
import kg.jobs.app.repository.VacancyRepository
import kg.jobs.app.ui.BaseViewModel
import kotlinx.coroutines.launch

class EditVacancyViewModel(private val repo: VacancyRepository,
                           private val context: Context) : BaseViewModel() {
    enum class Event { LOADING, SUCCESS, ERROR, CLOSE }

    private val _events = MutableLiveData<Event>()
    val events: LiveData<Event> = _events

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _sphere = MutableLiveData<JobSphere>()
    val sphere: LiveData<JobSphere> = _sphere

    private val _schedule = MutableLiveData<Pair<Schedule?, EmploymentType?>>()
    val selectedSchedule
        get() = _schedule.value?.first
    val selectedEmploymentType
        get() = _schedule.value?.second
    val schedule: LiveData<String> = Transformations.map(_schedule) { (schedule, employmentType) ->
        "${employmentType?.name}, ${schedule?.name}"
    }

    private val _salary = MutableLiveData<Salary>()
    val salary: LiveData<Salary> = _salary
    val salaryText: LiveData<String> = Transformations.map(_salary) { it.text(context) }

    private val _vacancy = MutableLiveData<Vacancy>()
    val vacancy: LiveData<Vacancy> = _vacancy

    init {
        _title.value = context.getString(R.string.new_vacancy)
    }   

    fun editMode(vacancy: Vacancy) {
        _title.value = context.getString(R.string.edit_vacancy)
        _vacancy.value = vacancy
        _schedule.value = Pair(Schedule(vacancy.scheduleId, vacancy.schedule),
                EmploymentType(vacancy.employmentTypeId, vacancy.employmentType))
        _sphere.value = vacancy.sphere
        _salary.value = vacancy.salary
    }

    fun setSphere(sphere: JobSphere) {
        _sphere.postValue(sphere)
    }

    fun setSchedule(schedule: Schedule) {
        _schedule.value = Pair(schedule, _schedule.value?.second)
    }

    fun setEmploymentType(employmentType: EmploymentType) {
        _schedule.value = Pair(_schedule.value?.first, employmentType)
    }

    fun setSalary(salary: Salary) {
        _salary.postValue(salary)
    }

    fun schedules(): ArrayList<Schedule> = repo.getSchedules()

    fun employmentTypes(): ArrayList<EmploymentType> = repo.getEmploymentTypes()

    fun save(name: String, desc: String) {
        launch {
            if (name.trim().isEmpty() || desc.isEmpty() ||
                    _sphere.value == null || selectedSchedule == null ||
                    selectedEmploymentType == null || _salary.value == null) {
                _events.postValue(Event.ERROR)
                _error.postValue("fill_all")
            } else {
                _events.postValue(Event.LOADING)
                Log.e("SHEDULE", "${selectedSchedule!!.id} -  ${ selectedEmploymentType!!.id}" )

                if (if (vacancy.value == null)
                            repo.create(name, desc,
                                    sphere.value!!.id,
                                    selectedSchedule!!.id,
                                    selectedEmploymentType!!.id,
                                    salary.value!!)
                        else
                            repo.save(vacancy.value!!.id, name, desc,
                                    sphere.value!!.id,
                                    selectedSchedule!!.id,
                                    selectedEmploymentType!!.id,
                                    salary.value!!)) {
                    _events.postValue(Event.CLOSE)
                } else {
                    _events.postValue(Event.ERROR)
                    _error.postValue(context.getString(R.string.unknown_error))
                }
            }
        }
    }
}