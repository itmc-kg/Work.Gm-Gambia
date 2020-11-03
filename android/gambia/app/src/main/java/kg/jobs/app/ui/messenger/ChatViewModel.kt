package kg.jobs.app.ui.messenger

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.firestore.*
import kg.jobs.app.await
import kg.jobs.app.awaitWithException
import kg.jobs.app.model.Chat
import kg.jobs.app.model.ChatInfo
import kg.jobs.app.model.Message
import kg.jobs.app.model.MsgStatus
import kg.jobs.app.ui.BaseViewModel
import kg.jobs.app.ui.role.RoleViewModel
import kotlinx.coroutines.launch
import java.util.*


class ChatViewModel(val uid: String,
                    val firestore: FirebaseFirestore,
                    val chat: Chat) : BaseViewModel() {
    enum class State { LOADING, DISMISS }

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    private val root = firestore.collection("chats")
            .document(chat.id)
            .collection("messages")

    private val _newMessage = MutableLiveData<Message>()
    val newMessage: LiveData<Message> = _newMessage

    private val _chatInfo = MutableLiveData<ChatInfo>()
    val chatInfo: LiveData<ChatInfo> = _chatInfo
    val vacancy = MutableLiveData<String>()

    var messagesListener: ListenerRegistration? = null

    val openEmployer = MutableLiveData<String>()
    val openEmployee = MutableLiveData<String>()

    init {
        _chatInfo.value = chat.data
        launch {
            vacancy.postValue(firestore.collection("vacancies")
                    .document(chat.vacancyId)
                    .get().await()
                    .takeIf { it.isSuccessful }
                    ?.let { it.result?.getString("position") } ?: "")
        }
        messagesListener = root
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("Exception", "listen:error", e)
                        return@addSnapshotListener
                    }

                    for (dc in snapshot!!.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                val message = message(dc.document)
                                _newMessage.value = message
                                if (message.from != uid)
                                    root.document(message.id)
                                            .update(mapOf("status" to MsgStatus.READ.name))
                            }
                            DocumentChange.Type.MODIFIED -> {
                                _newMessage.value = message(dc.document)
                            }
                            DocumentChange.Type.REMOVED -> {
                            }
                        }
                    }

                }
    }

    fun sendMessage(text: String) {
        launch {
            if (text.trim().isNotEmpty()) {
                val doc = root.document()
                _newMessage.postValue(Message(doc.id, text, uid, chat.partner, MsgStatus.CREATED, Date(), true))
                root.document(doc.id)
                        .set(mapOf("id" to doc.id,
                                "content" to text,
                                "from" to uid,
                                "to" to chat.partner,
                                "status" to MsgStatus.SENT.name,
                                "createdAt" to FieldValue.serverTimestamp()))
                        .await()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        messagesListener?.remove()
    }

    private fun message(snapshot: DocumentSnapshot): Message {
        return Message(id = snapshot.id,
                content = snapshot.getString("content") ?: "",
                from = snapshot.getString("from") ?: "",
                to = snapshot.getString("to") ?: "",
                isMyMessage = snapshot.getString("from") ?: "" == uid,
                status = MsgStatus.valueOf(snapshot.getString("status") ?: "SENT"),
                createdAt = snapshot.getDate("createdAt") ?: Date())
    }

    fun openChatInfo() {
        launch {
            try {
                _state.postValue(State.LOADING)
                val role = firestore.collection("users")
                        .document(uid).get().awaitWithException()
                        ?.getString("role") ?: "employee"

//                val role = firestore.collection("users")
//                        .document(uid)
//                        .get().awaitWithException()
//                        .run { this?.getString("role") }
//                        ?: ""

                Log.e("employer", role)
                if (role == "employer") {
                    openEmployee.postValue(chat.partner)
                } else {
                    openEmployer.postValue(chat.partner)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _state.postValue(State.DISMISS)
            }
        }
    }
}