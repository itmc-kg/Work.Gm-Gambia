package kg.jobs.app.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kg.jobs.app.await
import kg.jobs.app.awaitWithException
import kg.jobs.app.model.*
import java.util.*

class VacancyApplicationsRepository(private val uid: String,
                                    private val firestore: FirebaseFirestore) {

    suspend fun accept(application: Application): Boolean {
        return try {
            firestore.collection("applications")
                    .document(application.id)
                    .update(mapOf("status" to "accepted",
                            "updatedAt" to FieldValue.serverTimestamp()))
                    .awaitWithException()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun reject(application: Application): Boolean {
        return try {
            firestore.collection("applications")
                    .document(application.id)
                    .update(mapOf("status" to "rejected",
                            "updatedAt" to FieldValue.serverTimestamp()))
                    .awaitWithException()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun application(snapshot: DocumentSnapshot): Application {
        val application = snapshot.let {
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
                            } ?: JobSphere("", "")
            )
        }
        val author = firestore.collection("employees")
                .document(application.authorId)
                .get().await()
                .takeIf { it.isSuccessful }
                ?.run { result }
                ?.run {
                    User(id = application.authorId,
                            name = get("name")as? String ?: "",
                            image = get("image") as? String ?: "",
                            workHistory = getWorks(application.authorId),
                            educationHistory = getEducations(application.authorId))
                } ?: User()

        return application.copy(author = author)
    }

    private suspend fun getWorks(emplyeeId: String): List<Work> {
        return firestore.collection("employees")
                .document(emplyeeId)
                .collection("works")
                .get().await().takeIf { it.isSuccessful }
                ?.run { result?.documents }
                ?.map {
                    Work(id = it.id,
                            company = it.get("company") as? String ?: "",
                            position = it.get("position") as? String ?: "",
                            startDate = it.getDate("startDate"),
                            endDate = it.getDate("endDate"))
                }?.toList()
                ?: listOf()
    }

    private suspend fun getEducations(emplyeeId: String): List<Education> {
        return firestore.collection("employees")
                .document(emplyeeId)
                .collection("educations")
                .get().await().takeIf { it.isSuccessful }
                ?.run { result?.documents }
                ?.map {
                    Education(id = it.id,
                            type = it.get("type") as? String ?: "",
                            name = it.get("name") as? String ?: "",
                            speciality = it.get("speciality") as? String ?: "",
                            startDate = it.getDate("startDate"),
                            endDate = it.getDate("endDate"))
                }?.toList()
                ?: listOf()
    }
}