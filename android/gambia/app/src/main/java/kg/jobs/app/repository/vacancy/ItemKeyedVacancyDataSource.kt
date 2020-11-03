package kg.jobs.app.repository.vacancy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.ItemKeyedDataSource
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kg.jobs.app.await
import kg.jobs.app.awaitWithException
import kg.jobs.app.model.JobSphere
import kg.jobs.app.model.Salary
import kg.jobs.app.model.Vacancy
import kotlinx.coroutines.runBlocking

class ItemKeyedVacancyDataSource(private val uid: String,
                                 private val firestore: FirebaseFirestore,
                                 private val lang: String,
                                 private val status: String) : ItemKeyedDataSource<String, Vacancy>() {

    private val _networkState = MutableLiveData<State>()
    val networkState: LiveData<State> = _networkState

    override fun getKey(item: Vacancy) = item.id

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<Vacancy>) {
        runBlocking {
            _networkState.postValue(State.LOADING)
            val result = firestore.collection("vacancies")
                    .whereEqualTo("status", status)
                    .whereEqualTo("creatorId", uid)
                    .orderBy("createdAt")
                    .limit(params.requestedLoadSize.toLong())
                    .get().await()
            if (result.isSuccessful) {
                callback.onResult(result.result?.documents?.map { vacancy(it) } ?: listOf())
                _networkState.postValue(State.SUCCESS)
            } else {
                _networkState.postValue(State.FAIL)
                Log.e("Firebase", result.exception?.toString())
            }
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Vacancy>) {
        runBlocking {
            try {
                val snapshot = firestore.collection("vacancies")
                        .document(params.key)
                        .get().awaitWithException()
                val list = firestore.collection("vacancies")
                        .whereEqualTo("status", status)
                        .whereEqualTo("creatorId", uid)
                        .orderBy("createdAt")
                        .startAfter(snapshot)
                        .limit(params.requestedLoadSize.toLong())
                        .get().awaitWithException()
                callback.onResult(list?.map { vacancy(it) } ?: listOf())
            } catch (e: Exception) {
                Log.e("Firebase", e.toString())
            }
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Vacancy>) {
    }

    private suspend fun vacancy(snapshot: DocumentSnapshot): Vacancy {
        val sphereId = snapshot.get("sphereId")as? String
        val sphereName = sphereId?.let { id ->
            firestore.collection("spheres")
                    .document(id)
                    .get().await()
                    .takeIf { it.isSuccessful }
                    ?.run { result?.getString(lang) }
        } ?: ""

        return Vacancy(id = snapshot.id,
                position = snapshot.get("position")as? String ?: "",
                description = snapshot.get("description") as? String ?: "",
                scheduleId = snapshot.getLong("scheduleId")?.toInt() ?: 0,
                employmentTypeId = snapshot.getLong("employmentTypeId")?.toInt() ?: 0,
                sphere = JobSphere(sphereId ?: "", sphereName),
                salary = (snapshot.get("salary")as? Map<*, *>)?.run {
                    Salary(get("id") as? String ?: "",
                            (get("start") as? Number)?.toInt() ?: 0,
                            (get("end")as? Number)?.toInt() ?: 0)
                } ?: Salary(),
                status = snapshot.getString("status") ?: status,
                applicationsCount = snapshot.getLong("applicationsCount") ?: 0
        )
    }
}