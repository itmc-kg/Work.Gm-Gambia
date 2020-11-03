package kg.jobs.app.repository.vacancy

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.google.firebase.firestore.FirebaseFirestore
import kg.jobs.app.model.Vacancy


class VacancyDataSourceFactory(private val uid: String,
                               private val firestore: FirebaseFirestore,
                               private val lang: String) : DataSource.Factory<String, Vacancy>() {
    val liveData = MutableLiveData<ItemKeyedVacancyDataSource>()
    var status = "opened"

    override fun create(): DataSource<String, Vacancy> {
        val itemKeyedVacancyDataSource = ItemKeyedVacancyDataSource(uid, firestore, lang, status)
        liveData.postValue(itemKeyedVacancyDataSource)
        return itemKeyedVacancyDataSource
    }
}