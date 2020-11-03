package kg.jobs.app.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kg.jobs.app.await
import kg.jobs.app.awaitWithException
import kg.jobs.app.model.*
import java.util.*

class ChatRepository(val uid: String,
                     val firestore: FirebaseFirestore,
                     val employers: MutableMap<String, Employer>) {
    suspend fun createChat(_application: Application): Chat? {
        return try {
            var application = _application

            if (application.employer == null)
                application = application.copy(employer = getEmployer(application.employerId))
            if (application.author == null)
                application = application.copy(author = getUser(application.authorId))

            if (application.author != null && application.employer != null) {
                val chatId = firestore.collection("chats")
                        .whereArrayContains("users", application.authorId)
                        .whereEqualTo("vacancyId", application.vacancyId)
                        .get().awaitWithException()
                        ?.takeIf { it.documents.isNotEmpty() }
                        ?.documents?.first()?.id
                        ?: firestore.collection("chats").document().id

                val chat = Chat(chatId, uid,
                        listOf(application.authorId, application.employerId).find { it != uid }!!,
                        ChatInfo(application.employer!!.name, application.employer!!.image),
                        Date(), application.vacancyId, null)

                firestore.collection("chats")
                        .document(chatId)
                        .set(mapOf("id" to chatId,
                                "vacancyId" to application.vacancyId,
                                "users" to listOf(application.authorId, application.employerId),
                                application.authorId to mapOf("name" to application.author!!.name,
                                        "image" to application.author!!.image),
                                application.employerId to mapOf("name" to application.employer!!.name,
                                        "image" to application.employer!!.image),
                                "updatedAt" to FieldValue.serverTimestamp()), SetOptions.merge())
                        .awaitWithException()
                chat
            } else null
        } catch (e: Exception) {
            Log.e("Error", e.toString())
            null
        }
    }

    private suspend fun getEmployer(id: String): Employer? {
        return if (employers.containsKey(id)) employers[id]
        else {
            firestore.collection("employers")
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

    private suspend fun getUser(id: String): User? {
        return firestore.collection("employees")
                .document(id)
                .get().await()
                .takeIf { it.isSuccessful }
                ?.run { result }
                ?.run {
                    User(id = uid,
                            name = get("name")as? String ?: "",
                            image = get("image") as? String ?: "")
                }
    }
}