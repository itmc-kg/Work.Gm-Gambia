package kg.jobs.app.ui.employer.vacancy.detail

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kg.jobs.app.R
import kg.jobs.app.model.Application
import kg.jobs.app.model.Vacancy
import kg.jobs.app.ui.profile.show.ShowEducationAdapter
import kg.jobs.app.ui.profile.show.ShowWorkAdapter
import kotlinx.android.synthetic.main.item_application_card.view.*

class ApplicationAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<ApplicationAdapter.ViewHolder>() {
    private var items = mutableListOf<Application>()
    var onRightClick: ((Application) -> Unit)? = null
    var onLeftClick: ((Application) -> Unit)? = null
    var onClick: ((Application) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_application_card, parent, false))
        holder.itemView.setOnClickListener {
            holder.adapterPosition.takeIf { it >= 0 }
                    ?.also { onClick?.invoke(items[it]) }
        }
        holder.itemView.right_btn.setOnClickListener {
            holder.adapterPosition.takeIf { it >= 0 }
                    ?.also { onRightClick?.invoke(items[it]) }
        }
        holder.itemView.left_btn.setOnClickListener {
            holder.adapterPosition.takeIf { it >= 0 }
                    ?.also { onLeftClick?.invoke(items[it]) }
        }
        return holder
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun remove(application: Application?) {
        items.indexOfFirst { it.id == application?.id }
                .takeIf { it >= 0 }
                ?.also {
                    items.removeAt(it)
                    notifyItemRemoved(it)
                }
    }

    fun deleteItem(position: Int){
        items.removeAt(position)
        notifyDataSetChanged()
    }

    fun getItemPosition(pos: Int): Application?{
        if (items.size == 0) return null
        return items[pos]
    }

    fun getItemSize():Int{
        return this.items.count()
    }

    fun update(application: Application) {
        items.indexOfFirst { it.id == application.id }
                .takeIf { it >= 0 }
                ?.also {
                    items[it] = application
                    notifyDataSetChanged()
                }
    }

    fun add(item: Application?) {
        item?.also { application ->
            val pos = items.indexOfFirst { it.id == application.id }
            if (pos >= 0) {
                items[pos] = application
                notifyItemChanged(pos)
            } else {
                items.add(application)
                notifyItemInserted(itemCount - 1)
            }
        }
    }

    class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        private val eduAdapter = ShowEducationAdapter()
        private val workAdapter = ShowWorkAdapter()

        init {
            view.edu_recyclerView.apply {
                adapter = eduAdapter
                isNestedScrollingEnabled = false
            }

            view.work_recyclerView.apply {
                adapter = workAdapter
                isNestedScrollingEnabled = false
            }
        }

        fun bind(application: Application) {
            application.apply {
                itemView.apply {
                    eduAdapter.update(author?.educationHistory)
                    workAdapter.update(author?.workHistory)
                    name.text = author?.name
                    avatar.setImageURI(author?.image)
                    when (status) {
                        "created" -> {
                            left_btn.isVisible = true
                            right_btn.text = context.getString(R.string.accept)
                            right_btn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_done_white_24dp,
                                    0, 0)
                        }
                        "accepted" -> {
                            left_btn.isVisible = true
                            right_btn.text = context.getString(R.string.start_chat)
                            right_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                        }
                        "rejected" -> {
                            right_btn.text = context.getString(R.string.accept)
                            right_btn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_done_white_24dp,
                                    0, 0)
                            left_btn.isVisible = false
                        }
                    }
                }
            }
        }
    }
}

