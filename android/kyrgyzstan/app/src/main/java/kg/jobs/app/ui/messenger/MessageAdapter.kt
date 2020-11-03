package kg.jobs.app.ui.messenger

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import kg.jobs.app.R
import kg.jobs.app.humanizedDate
import kg.jobs.app.model.Message
import kg.jobs.app.model.MsgStatus
import java.text.SimpleDateFormat


class MessageAdapter : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    val items = mutableListOf<Message>()

    override fun getItemViewType(position: Int): Int {
        return if (items[position].isMyMessage) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = if (viewType > 0)
            ViewHolder.InMessageViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_in, parent, false))
        else
            ViewHolder.OutMessageViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_out, parent, false))

        return holder
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items[position].also {
            holder.bind(items[position], if (position < itemCount - 1) items[position + 1] else null)
        }
    }

    fun addMessage(message: Message) {
        val pos = items.indexOfFirst { it.id == message.id }
        if (pos >= 0) {
            items[pos] = message
            notifyItemChanged(pos)
        } else {
            items.add(0, message)
            notifyItemInserted(0)
            if (itemCount > 0) notifyItemChanged(1)
        }

    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val date = view.findViewById<TextView>(R.id.date)
        private val content = view.findViewById<TextView>(R.id.content)
        private val time = view.findViewById<TextView>(R.id.time)

        private val formatter = SimpleDateFormat("HH:mm")
        open fun bind(message: Message, nextMessage: Message?) {
            if (message.isCreatedSameDate(nextMessage))
                date.isVisible = false
            else {
                date.isVisible = true
                date.text = message.createdAt.humanizedDate(itemView.context)
            }
            content.text = message.content.format()
            time.text = formatter.format(message.createdAt)
        }

        class InMessageViewHolder(view: View) : ViewHolder(view)

        class OutMessageViewHolder(view: View) : ViewHolder(view) {
            private val status = view.findViewById<ImageView>(R.id.status)
            override fun bind(message: Message, nextMessage: Message?) {
                super.bind(message, nextMessage)
                when (message.status) {
                    MsgStatus.CREATED -> status.setImageResource(R.drawable.ic_msg_clock)
                    MsgStatus.SENT -> status.setImageResource(R.drawable.ic_msg_sent)
                    MsgStatus.DELIVERED -> status.setImageResource(R.drawable.ic_msg_delivered)
                    MsgStatus.READ -> status.setImageResource(R.drawable.ic_msg_read)
                }
            }
        }
    }
}