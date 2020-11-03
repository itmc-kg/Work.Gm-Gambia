package kg.jobs.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Work(val id: String = "",
                val company: String = "",
                val position: String = "",
                val startDate: Date? = null,
                val endDate: Date? = null) : Parcelable {
    fun isNotFull(): Boolean {
        return company.isEmpty() || position.isEmpty()
    }
}