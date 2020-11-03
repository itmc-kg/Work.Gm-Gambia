package kg.jobs.app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Chat(val id: String,
                val me: String,
                val partner: String,
                val data: ChatInfo,
                val updatedAt: Date,
                val vacancyId: String,
                val lastMessage: Message?) : Parcelable

@Parcelize
data class ChatInfo(val name: String,
                    val image: String) : Parcelable