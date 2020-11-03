package kg.jobs.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Message(val id: String,
                   val content: String,
                   val from: String,
                   val to: String,
                   val status: MsgStatus,
                   val createdAt: Date,
                   val isMyMessage: Boolean = false) : Parcelable {
    private fun createdAtInDays() = createdAt.time / 86400000

    fun isCreatedSameDate(other: Message?): Boolean = createdAtInDays() == other?.createdAtInDays()
}

enum class MsgStatus {
    CREATED,
    SENT,
    DELIVERED,
    READ
}