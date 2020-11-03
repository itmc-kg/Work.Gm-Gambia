package kg.jobs.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Vacancy(val id: String = "",
                   val description: String = "",
                   val position: String = "",
                   val sphere: JobSphere,
                   val scheduleId: Int = 0,
                   val schedule: String = "",
                   val employmentTypeId: Int = 0,
                   val employmentType: String = "",
                   val salary: Salary = Salary(),
                   val creatorId: String = "",
                   val employer: Employer? = null,
                   val application: Application? = null,
                   val status: String = "opened",
                   val accepted: Boolean = false,
                   val declined: Boolean = false,
                   var applicationsCount: Long = 0) : Parcelable