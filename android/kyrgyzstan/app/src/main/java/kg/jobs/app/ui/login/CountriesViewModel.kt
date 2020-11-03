package kg.jobs.app.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kg.jobs.app.model.Country
import kg.jobs.app.repository.CountryRepository
import kg.jobs.app.ui.BaseViewModel
import kotlinx.coroutines.launch

class CountriesViewModel constructor(private val repo: CountryRepository) : BaseViewModel() {
    private val _countriesList: MutableLiveData<List<Country>> = MutableLiveData()
    val countriesList: LiveData<List<Country>> = _countriesList

    init {
        launch {
            val list = repo.getCountries()
            _countriesList.postValue(list)
        }
    }
}