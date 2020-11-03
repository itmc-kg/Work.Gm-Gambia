package kg.jobs.app.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kg.jobs.app.R
import kg.jobs.app.await
import kg.jobs.app.awaitWithException
import kg.jobs.app.model.*
import kotlinx.coroutines.delay
import java.util.*

class SearchRepository(private val uid: String,
                       private val lang: String,
                       private val firestore: FirebaseFirestore,
                       private val context: Context,
                       private val employers: MutableMap<String, Employer>) {
    suspend fun getVacancy(application: Application): Vacancy? {
        return try {
            val vacancy = firestore.collection("vacancies")
                    .whereEqualTo("id", application.vacancyId)
                    .get().awaitWithException()
                    ?.takeIf { it.documents.isNotEmpty() }
                    ?.let { it.documents.first() }
                    ?.let { vacancy(it) }
            return vacancy?.copy(application = getMyApplication(vacancy))
        } catch (e: Exception) {
            Log.e("error", e.toString())
            null
        }
    }

    suspend fun hasApplication(vacancy: Vacancy): Boolean {
        return firestore.collection("applications")
                .whereEqualTo("vacancyId", vacancy.id)
                .whereEqualTo("authorId", uid)
                .get().await()
                .let { it.isSuccessful && it.result?.documents?.isNotEmpty() == true }
    }

    private suspend fun getMyApplication(vacancy: Vacancy): Application? {
        return firestore.collection("applications")
                .whereEqualTo("vacancyId", vacancy.id)
                .whereEqualTo("authorId", uid)
                .get().await()
                .takeIf { it.isSuccessful && it.result?.documents?.isNotEmpty() == true }
                ?.let { it.result?.documents?.first() }
                ?.let {
                    Application(id = it.id, position = it.getString("position") ?: "",
                            vacancyId = it.getString("vacancyId") ?: "",
                            authorId = it.getString("authorId") ?: "",
                            status = it.getString("status") ?: "",
                            employerId = it.getString("employerId") ?: "",
                            createdAt = it.getDate("createdAt") ?: Date(),
                            sphere = (it.get("sphere") as? Map<*, *>)
                                    ?.run {
                                        JobSphere(id = get("id")as? String ?: "",
                                                name = get("name") as? String ?: "")
                                    } ?: JobSphere("", ""))
                }
    }

    suspend fun apply(vacancy: Vacancy): Boolean {
        val doc = firestore.collection("applications").document()
        return try {
            firestore.collection("applications")
                    .whereEqualTo("vacancyId", vacancy.id)
                    .whereEqualTo("authorId", uid)
                    .get().awaitWithException()
                    .takeIf { it?.documents?.isEmpty() ?: false }
                    ?.run {
                        firestore.collection("applications")
                                .document(doc.id)
                                .set(mapOf("id" to doc.id,
                                        "sphere" to mapOf("id" to vacancy.sphere.id,
                                                "name" to vacancy.sphere.name),
                                        "position" to vacancy.position,
                                        "vacancyId" to vacancy.id,
                                        "authorId" to uid,
                                        "employerId" to vacancy.employer!!.id,
                                        "status" to "created",
                                        "createdAt" to FieldValue.serverTimestamp()))
                                .awaitWithException()
                    }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun decline(vacancy: Vacancy): Boolean {
        delay(200)
        return true
    }

    suspend fun getEmployer(id: String): Employer? {
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

    private fun getSchedules(): ArrayList<Schedule> {
        return ArrayList(context.resources.getStringArray(R.array.schedules)
                .mapIndexed { index, s -> Schedule(index, s) })
    }

    private fun getEmploymentTypes(): ArrayList<EmploymentType> {
        return ArrayList(context.resources.getStringArray(R.array.employment_types)
                .mapIndexed { index, s -> EmploymentType(index, s) })
    }

    suspend fun vacancy(snapshot: DocumentSnapshot): Vacancy {
        val sphereId = snapshot.get("sphereId")as? String
        val sphereName = sphereId?.let { id ->
            firestore.collection("spheres")
                    .document(id)
                    .get().await()
                    .takeIf { it.isSuccessful }
                    ?.run { result?.getString(lang) }
        } ?: ""

        val vacancy = Vacancy(id = snapshot.id,
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
                creatorId = snapshot.getString("creatorId") ?: "")
        return vacancy.copy(employer = getEmployer(vacancy.creatorId),
                schedule = getSchedules().find { it.id == vacancy.scheduleId }?.name ?: "",
                employmentType = getEmploymentTypes()
                        .find { it.id == vacancy.employmentTypeId }?.name ?: "")
    }
}