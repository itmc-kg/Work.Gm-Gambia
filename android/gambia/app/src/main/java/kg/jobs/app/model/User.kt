package kg.jobs.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(val id: String = "",
                var name: String = "",
                val image: String = "",
                val workHistory: List<Work> = listOf(),
                val educationHistory: List<Education> = listOf()) : Parcelable