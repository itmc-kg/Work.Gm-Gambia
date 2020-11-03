package kg.jobs.app.ui.employer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kg.jobs.app.model.Employer
import kg.jobs.app.model.Vacancy
import kg.jobs.app.repository.VacancyRepository
import kg.jobs.app.repository.vacancy.State
import kg.jobs.app.repository.vacancy.VacancyDataSourceFactory
import kg.jobs.app.ui.BaseViewModel
import kotlinx.coroutines.launch

class VacanciesViewModel(private val repo: VacancyRepository,
                         private val vacancyDataSourceFactory: VacancyDataSourceFactory) : BaseViewModel() {
    val vacancies: LiveData<PagedList<Vacancy?>>
    val networkState: LiveData<State>

    val status = MutableLiveData<String>()

    private val _employer = MutableLiveData<Employer>()
    val employer: LiveData<Employer> = _employer

    init {
        val config = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(10)
                .build()
        networkState = Transformations.switchMap(vacancyDataSourceFactory.liveData) { it.networkState }
        vacancies = LivePagedListBuilder<String, Vacancy>(vacancyDataSourceFactory, config).build()
    }

    fun init() {
        launch {
            _employer.postValue(repo.getEmployer())
        }
    }

    fun refresh() {
        launch { _employer.postValue(repo.getEmployer()) }
        vacancyDataSourceFactory.liveData.value?.invalidate()
    }

    fun setStatus(status: String) {
        this.status.postValue(status)
        vacancyDataSourceFactory.status = status
        refresh()
    }

    fun archive(vacancy: Vacancy) {
        launch {
            val result = if (vacancy.status == "opened") repo.archive(vacancy.id)
            else repo.open(vacancy.id)
            if (result) refresh()
        }
    }
}