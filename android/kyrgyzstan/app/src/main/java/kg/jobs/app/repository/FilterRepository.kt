package kg.jobs.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import kg.jobs.app.await
import kg.jobs.app.model.JobSphere
import kg.jobs.app.model.Region

class FilterRepository(private val lang: String,
                       private val firestore: FirebaseFirestore,
                       private val localPrefData: LocalPrefData) {
    suspend fun jobSpheres(): MutableList<JobSphere>? {
        val selected = localPrefData.filterSpheres()
        return firestore.collection("spheres")
                .get().await()
                .takeIf { it.isSuccessful }
                ?.run { result?.documents }
                ?.asSequence()
                ?.map { snapshot ->
                    JobSphere(id = snapshot.id,
                            name = snapshot.get(lang)as? String ?: "",
                            checked = selected.find { it == snapshot.id } != null)
                }?.toMutableList()
    }

    fun salary() = localPrefData.filterSalaryFrom()

    suspend fun allRegions(): List<Region>? {
        val region = localPrefData.filterRegion()
        return firestore.collection("regions")
                .whereEqualTo("country", localPrefData.country())
                .get().await()
                .takeIf { it.isSuccessful }
                ?.run { result?.documents }
                ?.asSequence()
                ?.map {
                    Region(id = it.id,
                            name = it.get(lang)as? String ?: "",
                            checked = it.id == region)
                }?.toMutableList()
    }

    fun saveFilters(region: String,
                    spheres: List<String>,
                    salary: Int = 0): Boolean {
        localPrefData.saveFilters(region, spheres, salary)
        return true
    }
}