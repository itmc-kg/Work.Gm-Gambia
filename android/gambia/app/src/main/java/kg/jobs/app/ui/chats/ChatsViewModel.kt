package kg.jobs.app.ui.chats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.*
import kg.jobs.app.R
import kg.jobs.app.await
import kg.jobs.app.awaitWithException
import kg.jobs.app.model.Chat
import kg.jobs.app.model.ChatInfo
import kg.jobs.app.model.Message
import kg.jobs.app.model.MsgStatus
import kg.jobs.app.ui.BaseViewModel
import kg.jobs.app.ui.messenger.ChatViewModel
import kotlinx.coroutines.launch
import java.util.*

class ChatsViewModel(val vacancyId: String,
                     val openChatId: String,
                     val uid: String,
                     val firestore: FirebaseFirestore,
                     val context: Context) : BaseViewModel() {
    private val _chat = MutableLiveData<Chat>()
    val chat: LiveData<Chat> = _chat

    val openChat = MutableLiveData<Chat>()


    private val _status = MutableLiveData<Status>()
    val status: LiveData<Status> = _status

    val emptyChatsText = MutableLiveData<String>()

    private var chatsListener: ListenerRegistration? = null

    init {
        _status.value = Status.LOADING

        if (vacancyId.isNotEmpty()) emptyChatsText.postValue(
                context.getString(R.string.chats_empty_description_for_employer))

        chatsListener = firestore.collection("chats")
                .whereArrayContains("users", uid)
                .run {
                    if (vacancyId.isNotEmpty()) whereEqualTo("vacancyId", vacancyId)
                    else this
                }
                .orderBy("updatedAt", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        _status.value = Status.ERROR
                        Log.w("Exception", "listen:error", e)
                        return@addSnapshotListener
                    }
                    if (snapshot?.documents?.isEmpty() == true) _status.value = Status.EMPTY


                    for (dc in snapshot!!.documentChanges) {

                        _status.value = Status.DATA
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                val chat = chat(dc.document)
                                Log.e("CHAT", "${chat.vacancyId} - ${chat.partner}")
                                launch {
                                    val role = firestore.collection("users")
                                            .document(chat.partner).get().awaitWithException()
                                            ?.getString("role") ?: "employee"

                                    Log.e("role", role)


                                    if (vacancyId.isNotEmpty()){
                                        _chat.value = chat
                                        if (chat.id == openChatId)
                                            openChat.postValue(chat)
                                    }else{
                                        if (role == "employer") {
                                            _chat.value = chat
                                            if (chat.id == openChatId)
                                                openChat.postValue(chat)
                                        }
                                    }
                                    _status.value = Status.CHECK_DATA
                                }

                            }
                            DocumentChange.Type.MODIFIED -> {
                                val chat = chat(dc.document)
                                launch {
                                    val role = firestore.collection("users")
                                            .document(chat.partner).get().awaitWithException()
                                            ?.getString("role") ?: "employee"

                                    Log.e("employer", role)
                                    if (vacancyId.isNotEmpty()){
                                        _chat.value = chat
                                        if (chat.id == openChatId)
                                            openChat.postValue(chat)
                                    }else{
                                        if (role == "employer") {
                                            _chat.value = chat
                                            if (chat.id == openChatId)
                                                openChat.postValue(chat)
                                        }
                                    }
                                    _status.value = Status.CHECK_DATA
                                }
                                launch {
                                    val root = firestore.collection("chats")
                                            .document(chat.id)
                                            .collection("messages")

                                    chat.lastMessage?.also { msg ->
                                        if (msg.to == uid) {
                                            val update = try {
                                                root.document(msg.id).get().awaitWithException()
                                                        ?.getString("status") != MsgStatus.READ.name
                                            } catch (e: Exception) {
                                                false
                                            }
                                            if (update)
                                                root.document(msg.id).update(mapOf("status" to MsgStatus.DELIVERED.name))
                                        }
                                    }
                                }
                            }
                            DocumentChange.Type.REMOVED -> {
                            }
                        }
                    }

                }

    }


    private fun chat(doc: DocumentSnapshot): Chat {
        val partnerId = (doc.get("users") as? List<String> ?: listOf()).find { it != uid } ?: "_"
        val chatInfo = (doc.get(partnerId) as? Map<*, *>).let {
            ChatInfo(it?.get("name") as? String ?: "", it?.get("image") as? String ?: "")
        }
        val message = (doc.get("lastMessage") as? Map<*, *>)?.run {
            Message(id = get("id") as? String ?: "",
                    content = get("content") as? String ?: "",
                    from = get("from") as? String ?: "",
                    to = get("to") as? String ?: "",
                    isMyMessage = get("from") ?: "" == uid,
                    status = MsgStatus.valueOf(get("status") as? String ?: "SENT"),
                    createdAt = get("createdAt") as? Date ?: Date())
        }
        return Chat(id = doc.id,
                me = uid,
                partner = partnerId,
                data = chatInfo,
                lastMessage = message,
                vacancyId = doc.getString("vacancyId") ?: "",
                updatedAt = doc.getDate("updatedAt") ?: Date())
    }

    override fun onCleared() {
        super.onCleared()
        chatsListener?.remove()
    }
}

enum class Status {
    LOADING,
    EMPTY,
    DATA,
    ERROR,
    CHECK_DATA,
}