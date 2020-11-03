package kg.jobs.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Education(val id: String = "",
                     val name: String = "",
                     val speciality: String = "",
                     val type: String = EducationType.HIGH_EDU.toString().toLowerCase(),
                     val startDate: Date? = null,
                     val endDate: Date? = null) : Parcelable {
    fun isNotFull(): Boolean {
        return name.isEmpty() || speciality.isEmpty() || type.isEmpty()
    }
}

enum class EducationType {
    HIGH_EDU,
    NF_HIGHT_EDU,
    SPEC_EDU,
    SEC_EDU,
    LOW_SEC_EDU
}