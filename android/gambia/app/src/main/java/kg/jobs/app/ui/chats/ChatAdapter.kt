package kg.jobs.app.ui.chats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kg.jobs.app.R
import kg.jobs.app.model.Chat
import kg.jobs.app.model.MsgStatus
import kotlinx.android.synthetic.main.item_chat.view.*
import java.text.SimpleDateFormat

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    private val items = mutableListOf<Chat>()
    var onClick: (Chat) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat, parent, false))
        holder.itemView.setOnClickListener { view ->
            holder.adapterPosition.takeIf { it >= 0 }
                    ?.let { items[it] }
                    ?.also(onClick)
        }
        return holder
    }

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun addChat(chat: Chat?) {
        chat?.apply {
            val pos = items.indexOfFirst { it.id == chat.id }
            if (pos >= 0) {
                items[pos] = chat
                notifyItemChanged(pos)
            } else {
                items.add(0, chat)
                notifyItemInserted(0)
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val formatter = SimpleDateFormat("HH:mm")
        fun bind(chat: Chat) {
            itemView.apply {
                avatar.setImageURI(chat.data.image)
                content.text = chat.data.name
                if (chat.lastMessage == null) {
                    chats_text.isVisible = false
                    time.isVisible = false
                    status.isVisible = false
                }
                chat.lastMessage?.also {
                    chats_text.isVisible = true
                    time.isVisible = true
                    status.isVisible = true
                    chats_text.text = it.content
                    if (it.isMyMessage)
                        status.setImageResource(when (it.status) {
                            MsgStatus.CREATED -> R.drawable.ic_msg_clock
                            MsgStatus.SENT -> R.drawable.ic_msg_sent
                            MsgStatus.DELIVERED -> R.drawable.ic_msg_delivered
                            MsgStatus.READ -> R.drawable.ic_msg_read_black
                        })
                    else
                        status.isVisible = false
                    time.text = formatter.format(it.createdAt)
                }

            }
        }
    }
}