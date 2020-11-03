package kg.jobs.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class JobSphere(val id: String,
                     val name: String,
                     val checked: Boolean = false) : Parcelable