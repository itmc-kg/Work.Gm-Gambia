package kg.jobs.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Employer(val id: String,
                    val name: String,
                    val about: String,
                    val regionId: String,
                    val regionName: String = "",
                    val city: String,
                    val image: String = "") : Parcelable