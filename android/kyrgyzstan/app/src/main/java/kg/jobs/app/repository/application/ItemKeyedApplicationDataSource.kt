package kg.jobs.app.repository.application

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.ItemKeyedDataSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kg.jobs.app.await
import kg.jobs.app.awaitWithException
import kg.jobs.app.model.Application
import kg.jobs.app.model.Employer
import kg.jobs.app.model.JobSphere
import kotlinx.coroutines.runBlocking
import java.util.*

class ItemKeyedApplicationDataSource(private val uid: String,
                                     private val firestore: FirebaseFirestore,
                                     private val lang: String,
                                     private val employers: MutableMap<String, Employer>) : ItemKeyedDataSource<String, Application>() {

    private val _networkState = MutableLiveData<State>()
    val networkState: LiveData<State> = _networkState

    override fun getKey(item: Application) = item.id

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<Application>) {
        runBlocking {
            _networkState.postValue(State.LOADING)
            val result = firestore.collection("applications")
                    .whereEqualTo("authorId", uid)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(params.requestedLoadSize.toLong())
                    .get().await()
            if (result.isSuccessful) {
                callback.onResult(result.result?.documents?.map { application(it) } ?: listOf())
                _networkState.postValue(State.SUCCESS)
            } else {
                _networkState.postValue(State.FAIL)
                Log.e("Firebase", result.exception?.toString())
            }
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Application>) {
        runBlocking {
            try {
                val snapshot = firestore.collection("applications")
                        .document(params.key)
                        .get().awaitWithException()
                val list = firestore.collection("applications")
                        .whereEqualTo("authorId", uid)
                        .orderBy("createdAt")
                        .startAfter(snapshot)
                        .limit(params.requestedLoadSize.toLong())
                        .get().awaitWithException()
                callback.onResult(list?.map { application(it) } ?: listOf())
            } catch (e: Exception) {
                Log.e("Firebase", e.toString())
            }
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Application>) {
    }

    suspend fun application(snapshot: DocumentSnapshot): Application {
        Log.e("APPLICATION", "AAA")

        val sphereId = snapshot.get("sphereId")as? String
        val sphereName = sphereId?.let { id ->
            firestore.collection("spheres")
                    .document(id)
                    .get().await()
                    .takeIf { it.isSuccessful }
                    ?.run { result?.getString(lang) }
        } ?: ""

        return snapshot.let {
            val sphere = (it.get("sphere") as? Map<*, *>)
                    ?.run {
                        JobSphere(id = get("id")as? String ?: "",
                                name = get("name") as? String ?: "")
                    } ?: JobSphere("", "")

            val sphereId = sphere.id
            val sphereName = sphereId?.let { id ->
                firestore.collection("spheres")
                        .document(id)
                        .get().await()
                        .takeIf { it.isSuccessful }
                        ?.run { result?.getString(lang) }
            } ?: ""

            Application(id = it.id, position = it.getString("position") ?: "",
                    vacancyId = it.getString("vacancyId") ?: "",
                    authorId = it.getString("authorId") ?: "",
                    status = it.getString("status") ?: "",
                    employerId = it.getString("employerId") ?: "",
                    createdAt = it.getDate("createdAt") ?: Date(),
                    sphere = JobSphere(sphereId, sphereName),
//                    sphere = (it.get("sphere") as? Map<*, *>)
//                            ?.run {
//                                JobSphere(id = get("id")as? String ?: "",
//                                        name = get("name") as? String ?: "")
//                            } ?: JobSphere("", ""),
                    employer = getEmployer(it.getString("employerId") ?: ""))
        }
    }

    private suspend fun getEmployer(id: String): Employer? {
        return if (employers.containsKey(id)) employers[id]
        else firestore.collection("employers")
                .document(id)
                .get().await()
                .takeIf { it.isSuccessful }
                ?.let { it.result }
                ?.let {
                    Employer(id = it.id,
                            name = it.get("name")as? String ?: "",
                            about = it.get("about")as? String ?: "",
                            image = it.get("image")as? String ?: "",
                            regionId = it.get("regionId") as?String ?: "",
                            city = it.get("city")as? String ?: "")
                }?.also { employers[id] = it }
    }

}