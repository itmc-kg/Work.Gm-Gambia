package kg.jobs.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kg.jobs.app.await
import kg.jobs.app.awaitWithException

class RoleRepository(private val uid: String,
                     private val firestore: FirebaseFirestore,
                     private val pref: LocalPrefData) {
    suspend fun getRole(): String? {
        return try {
            var role = pref.role()
            if (role.isEmpty()) {
                role = firestore.collection("users")
                        .document(uid)
                        .get().awaitWithException()
                        .run { this?.getString("role") }
                        ?: ""
                pref.setRole(role)
            }
            role
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCountry(): String? {
        return try {
            if (!pref.hasCountry()) {
                val country = firestore.collection("users")
                        .document(uid)
                        .get().awaitWithException()
                        .run { this?.getString("country") }
                        ?: ""
                pref.setCountry(country)
                country
            } else
                pref.country()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun isProfileCreated(role: String): Boolean {
        val result = when {
            pref.isProfileCreated() -> true
            role == "employee" -> firestore.collection("employees")
                    .document(uid)
                    .get().await()
                    .takeIf { it.isSuccessful }
                    ?.run { result?.exists() } ?: false
            else -> firestore.collection("employers")
                    .document(uid)
                    .get().await()
                    .takeIf { it.isSuccessful }
                    ?.run { result?.exists() } ?: false
        }
        if (result) pref.profileCreated()
        return result
    }

    suspend fun selectEmployer(): Boolean {
        return try {
            firestore.collection("users")
                    .document(uid)
                    .set(mapOf("role" to "employer"), SetOptions.merge())
                    .awaitWithException()
            pref.setRole("employer")
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun selectEmployee(): Boolean {
        return try {
            firestore.collection("users")
                    .document(uid)
                    .set(mapOf("role" to "employee"), SetOptions.merge())
                    .awaitWithException()
            pref.setRole("employee")
            true
        } catch (e: Exception) {
            false
        }
    }

}