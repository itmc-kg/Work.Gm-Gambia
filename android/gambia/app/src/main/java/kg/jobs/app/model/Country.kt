package kg.jobs.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Country(val id: String = "",
                   val isoNumeric: String = "",
                   val isoString: String = "",
                   val mask: String = "",
                   val code: String = "",
                   val name: String = "",
                   val flag: String = "\uD83C\uDFF3️️") : Parcelable {

    companion object {
        const val EXTRA: String = "extra"
        const val UNKNOWN_FLAG = "\uD83C\uDFF3️️"
    }
}
