package kg.jobs.app.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kg.jobs.app.model.Application
import kg.jobs.app.model.User
import kg.jobs.app.repository.ProfileRepository
import kg.jobs.app.repository.application.ApplicationDataSourceFactory
import kg.jobs.app.repository.application.State
import kg.jobs.app.ui.BaseViewModel
import kotlinx.coroutines.launch

class ProfileViewModel(private val repo: ProfileRepository,
                       private val dataSourceFactory: ApplicationDataSourceFactory) : BaseViewModel() {
    val applications: LiveData<PagedList<Application?>>
    val networkState: LiveData<State>

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    init {
        val config = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(10)
                .build()
        networkState = Transformations.switchMap(dataSourceFactory.liveData) { it.networkState }
        applications = LivePagedListBuilder<String, Application>(dataSourceFactory, config).build()
    }

    fun init() {
        launch {
            _user.postValue(repo.getUser())
        }
    }

    fun refresh() {
        dataSourceFactory.liveData.value?.invalidate()
    }
}