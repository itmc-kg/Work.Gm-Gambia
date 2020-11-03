package kg.jobs.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kg.jobs.app.await
import kg.jobs.app.awaitWithException
import kg.jobs.app.model.Education
import kg.jobs.app.model.User
import kg.jobs.app.model.Work
import java.util.*

class ProfileRepository(private val uid: String,
                        private val firestore: FirebaseFirestore,
                        private val pref: LocalPrefData) {
    suspend fun save(user: User, works: List<Work>, edus: List<Education>): Boolean {
        return try {
            if (user.image.isEmpty()){
                firestore.collection("employees")
                        .document(uid)
                        .set(mapOf("name" to user.name), SetOptions.merge())
                        .awaitWithException()
            }else {
                firestore.collection("employees")
                        .document(uid)
                        .set(mapOf("name" to user.name,
                                "image" to user.image), SetOptions.merge())
                        .awaitWithException()
            }

            works.forEach { work ->
                firestore.collection("employees")
                        .document(uid)
                        .collection("works")
                        .run {
                            val map = mapOf(
                                    "company" to work.company,
                                    "position" to work.position,
                                    "startDate" to work.startDate,
                                    "endDate" to work.endDate
                            )
                            if (work.id.isEmpty()) add(map)
                            else document(work.id).set(map)
                        }.awaitWithException()
            }
            edus.forEach { edu ->
                firestore.collection("employees")
                        .document(uid)
                        .collection("educations")
                        .run {
                            val map = mapOf(
                                    "name" to edu.name,
                                    "speciality" to edu.speciality,
                                    "type" to edu.type,
                                    "startDate" to edu.startDate,
                                    "endDate" to edu.endDate
                            )
                            if (edu.id.isEmpty()) add(map)
                            else document(edu.id).set(map)
                        }.awaitWithException()
            }
            pref.profileCreated()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUser(id: String = uid): User {
        return firestore.collection("employees")
                .document(id)
                .get().await()
                .takeIf { it.isSuccessful }
                ?.run { result }
                ?.run {
                    User(id = id,
                            name = get("name")as? String ?: "",
                            image = get("image") as? String ?: "")
                } ?: User()
    }

    suspend fun getWorks(id: String = uid): MutableList<Work>? {
        return firestore.collection("employees")
                .document(id)
                .collection("works")
                .get().await()
                .takeIf { it.isSuccessful }
                ?.run { result?.documents }
                ?.asSequence()
                ?.map {
                    Work(id = it.id,
                            company = it.get("company") as? String ?: "",
                            position = it.get("position") as? String ?: "",
                            startDate = it.getDate("startDate"),
                            endDate = it.getDate("endDate"))
                }?.toMutableList()
                ?.apply { if (isEmpty() && id == uid) add(Work()) }
                ?: mutableListOf(Work())
    }

    suspend fun getEducations(id: String = uid): MutableList<Education>? {
        return firestore.collection("employees")
                .document(id)
                .collection("educations")
                .get().await()
                .takeIf { it.isSuccessful }
                ?.run { result?.documents }
                ?.asSequence()
                ?.map {
                    Education(id = it.id,
                            type = it.get("type") as? String ?: "",
                            name = it.get("name") as? String ?: "",
                            speciality = it.get("speciality") as? String ?: "",
                            startDate = it.getDate("startDate"),
                            endDate = it.getDate("endDate"))
                }?.toMutableList()
                ?.apply { if (isEmpty() && id == uid) add(Education()) }
                ?: mutableListOf(Education())
    }
}
