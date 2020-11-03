package kg.jobs.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Application(val id: String = "",
                       val sphere: JobSphere = JobSphere("", ""),
                       val position: String = "",
                       val vacancyId: String,
                       val authorId: String,
                       val author: User? = null,
                       val status: String,
                       val employerId: String,
                       val employer: Employer? = null,
                       val createdAt: Date = Date()) : Parcelable {

    private fun createdAtInDays() = createdAt.time / 86400000

    fun isCreatedSameDate(other: Application?): Boolean = createdAtInDays() == other?.createdAtInDays()
}