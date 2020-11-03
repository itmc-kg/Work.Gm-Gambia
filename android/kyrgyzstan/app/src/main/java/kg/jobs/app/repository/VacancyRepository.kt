package kg.jobs.app.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kg.jobs.app.R
import kg.jobs.app.await
import kg.jobs.app.model.*

class VacancyRepository(private val context: Context,
                        private val uid: String,
                        private val firestore: FirebaseFirestore,
                        private val localPrefData: LocalPrefData,
                        private val employers: MutableMap<String, Employer>) {

    suspend fun getEmployer(): Employer? {
        return if (employers.containsKey(uid)) employers[uid]
        else firestore.collection("employers")
                .document(uid)
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
                }?.also { employers[uid] = it }
    }

    suspend fun getVacancyDetail(vacancy: Vacancy): Vacancy {
        return vacancy.copy(employer = getEmployer(),
                schedule = getSchedules().find { it.id == vacancy.scheduleId }?.name ?: "",
                employmentType = getEmploymentTypes()
                        .find { it.id == vacancy.employmentTypeId }?.name ?: "")
    }

    fun getSchedules(): ArrayList<Schedule> {
        return ArrayList(context.resources.getStringArray(R.array.schedules)
                .mapIndexed { index, s -> Schedule(index, s) })
    }

    fun getEmploymentTypes(): ArrayList<EmploymentType> {
        val list = context.resources.getStringArray(R.array.employment_types)

        for (employer in list){
            Log.e("TAG", employer)
        }

        return ArrayList(context.resources.getStringArray(R.array.employment_types)
                .mapIndexed { index, s -> EmploymentType(index, s) })
    }

    suspend fun create(name: String, desc: String,
                       sphereId: String,
                       scheduleId: Int,
                       employmentTypeId: Int,
                       salary: Salary): Boolean {
        val doc = firestore.collection("vacancies").document()
        return firestore.collection("vacancies")
                .document(doc.id)
                .set(mapOf("id" to doc.id,
                        "position" to name,
                        "description" to desc,
                        "sphereId" to sphereId,
                        "scheduleId" to scheduleId,
                        "employmentTypeId" to employmentTypeId,
                        "creatorId" to uid,
                        "status" to "opened",
                        "country" to localPrefData.country(),
                        "salary" to mapOf("id" to salary.id,
                                "start" to salary.start,
                                "end" to salary.end),
                        "createdAt" to FieldValue.serverTimestamp()), SetOptions.merge())
                .await()
                .apply { exception?.also { Log.e("exception", it.toString()) } }
                .isSuccessful
    }

    suspend fun save(id: String, name: String, desc: String,
                     sphereId: String,
                     scheduleId: Int,
                     employmentTypeId: Int,
                     salary: Salary): Boolean {
        return firestore.collection("vacancies")
                .document(id)
                .update(mapOf(
                        "position" to name,
                        "description" to desc,
                        "sphereId" to sphereId,
                        "scheduleId" to scheduleId,
                        "country" to localPrefData.country(),
                        "employmentTypeId" to employmentTypeId,
                        "salary" to mapOf("id" to salary.id,
                                "start" to salary.start,
                                "end" to salary.end)))
                .await()
                .apply { exception?.also { Log.e("exception", it.toString()) } }
                .isSuccessful
    }

    suspend fun open(id: String): Boolean {
        return firestore.collection("vacancies")
                .document(id)
                .update(mapOf("status" to "opened"))
                .await()
                .isSuccessful
    }

    suspend fun archive(id: String): Boolean {
        return firestore.collection("vacancies")
                .document(id)
                .update(mapOf("status" to "closed"))
                .await()
                .isSuccessful
    }

}