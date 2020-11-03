package kg.jobs.app.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kg.jobs.app.await
import kg.jobs.app.model.Employer
import kg.jobs.app.model.Region

class EmployerRepository(private val uid: String,
                         private val lang: String,
                         private val firestore: FirebaseFirestore,
                         private val pref: LocalPrefData,
                         private val employers: MutableMap<String, Employer>) {
    suspend fun allRegions(): List<Region>? {
        val region = firestore.collection("employers")
                .document(uid)
                .get().await()
                .takeIf { it.isSuccessful }
                ?.run { result?.data?.get("regionId")as? String } ?: ""

        return firestore.collection("regions")
                .whereEqualTo("country", pref.country())
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

    suspend fun save(name: String, about: String, regionId: String, city: String, image: String): Boolean {
        return firestore.collection("employers")
                .document(uid)
                .set(mapOf("name" to name,
                        "about" to about,
                        "regionId" to regionId,
                        "city" to city,
                        "image" to image), SetOptions.merge())
                .await()
                .apply { exception?.also { Log.e("exception", it.toString()) } }
                .apply {
                    if (isSuccessful) {
                        pref.profileCreated()
                        if (employers.containsKey(uid))
                            employers[uid] = employers[uid]!!.copy(image = image,
                                    name = name,
                                    about = about,
                                    city = city,
                                    regionId = regionId
                            )
                    }
                }
                .isSuccessful
    }

    suspend fun getEmployer(id: String = uid): Employer? {
        return if (employers.containsKey(id)) employers[id]
        else firestore.collection("employers")
                .document(id)
                .get().await()
                .takeIf { it.isSuccessful }
                ?.run {
                    val employer = Employer(id = id,
                            name = result?.get("name")as? String ?: "",
                            about = result?.get("about") as? String ?: "",
                            city = result?.get("city") as? String ?: "",
                            image = result?.getString("image") ?: "",
                            regionId = result?.get("regionId")as?String ?: "")
                    (employer.takeIf { it.regionId.isNotEmpty() }
                            ?.run {
                                val regionName = firestore.collection("regions")
                                        .document(regionId)
                                        .get().await()
                                        .takeIf { it.isSuccessful }
                                        ?.run { result?.get(lang) as String } ?: ""
                                copy(regionName = regionName)
                            } ?: employer)
                            .also { employers[id] = it }

                }
    }

}