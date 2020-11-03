package kg.jobs.app.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.StorageReference
import kg.jobs.app.awaitWithException

class ImageRepository(val storageReference: StorageReference) {
    suspend fun upload(uri: Uri): String? {
        return try {
            val ref = storageReference.child("images/${uri.lastPathSegment}")
            ref.putFile(uri).awaitWithException()
            ref.downloadUrl.awaitWithException().toString()
        } catch (e: Exception) {
            Log.e("Error", e.toString())
            null
        }
    }
}