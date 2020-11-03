package kg.jobs.app.repository.application

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.google.firebase.firestore.FirebaseFirestore
import kg.jobs.app.model.Application
import kg.jobs.app.model.Employer


class ApplicationDataSourceFactory(private val uid: String,
                                   private val firestore: FirebaseFirestore,
                                   private val lang: String,
                                   private val employers: MutableMap<String, Employer>) : DataSource.Factory<String, Application>() {
    val liveData = MutableLiveData<ItemKeyedApplicationDataSource>()
    lateinit var itemKeyedApplicationDataSource: ItemKeyedApplicationDataSource

    override fun create(): DataSource<String, Application> {
        itemKeyedApplicationDataSource = ItemKeyedApplicationDataSource(uid, firestore, lang, employers)
        liveData.postValue(itemKeyedApplicationDataSource)
        return itemKeyedApplicationDataSource
    }
}