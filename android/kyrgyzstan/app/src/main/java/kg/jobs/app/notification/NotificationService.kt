package kg.jobs.app.notification

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kg.jobs.app.R
import kg.jobs.app.model.MsgStatus
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named

class NotificationService : FirebaseMessagingService() {

    private val firestore by inject<FirebaseFirestore>()
    private val notification = get<NotificationCenter>()

    override fun onMessageReceived(message: RemoteMessage?) {
        if (FirebaseAuth.getInstance().currentUser == null) return

        message?.data?.apply {
            val type = this["notificationType"]
            when (type) {
                "new/message" -> {
                    val title = this["fromName"] ?: ""
                    val content = this["content"] ?: ""
                    val chatId = this["chatId"] ?: ""
                    val messageId = this["messageId"] ?: " "
                    notification.showMessageNotification(title, content, chatId)
                    firestore.collection("chats")
                            .document(chatId)
                            .collection("messages")
                            .document(messageId)
                            .update(mapOf("status" to MsgStatus.DELIVERED.name))
                }
                "app/accepted" -> {
                    val applicationId = this["applicationId"] ?: ""
                    val employerName = this["employerName"] ?: ""
                    notification.showApplicationNotification(getString(R.string.notif_app_accepted_title),
                            String.format(getString(R.string.notif_app_accepted_text), employerName), applicationId)
                }
                "new/app" -> {
                    val applicationId = this["applicationId"] ?: ""
                    val employeeName = this["employeeName"] ?: ""
                    notification.showApplicationNotification(getString(R.string.notif_app_created_title),
                            String.format(getString(R.string.notif_app_created_text), employeeName), applicationId)
                }

                else -> {
                    notification.showOtherNotification(message.notification?.title
                            ?: "", message.notification?.body ?: "")
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = get<String>(named("uid"))
        if (uid.isNotEmpty())
            firestore.collection("users")
                    .document(uid)
                    .collection("devices")
                    .document(token)
                    .set(mapOf("type" to "android"), SetOptions.merge())
    }
}