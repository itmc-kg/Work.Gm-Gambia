package kg.jobs.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EmploymentType(val id: Int,
                          val name: String) : Parcelable