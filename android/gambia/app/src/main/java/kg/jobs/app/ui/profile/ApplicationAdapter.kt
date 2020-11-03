package kg.jobs.app.ui.profile

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kg.jobs.app.R
import kg.jobs.app.humanizedDate
import kg.jobs.app.model.Application
import kotlinx.android.synthetic.main.item_application.view.*

class ApplicationAdapter : PagedListAdapter<Application, ApplicationAdapter.ViewHolder>(DIFF_CALLBACK) {
    var onClick: (Application) -> Unit = {}

    companion object {
        val DIFF_CALLBACK = object : ItemCallback<Application>() {
            override fun areItemsTheSame(oldItem: Application, newItem: Application) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Application, newItem: Application) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_application, parent, false))
        holder.itemView.setOnClickListener { view ->
            holder.adapterPosition.takeIf { it >= 0 }
                    ?.let { getItem(it) }
                    ?.also { onClick(it) }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), (position - 1).takeIf { it >= 0 }?.let { getItem(it) })
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(application: Application?, prevItem: Application?) {
            itemView.apply {
                application?.apply {
                    if (isCreatedSameDate(prevItem)) date.isVisible = false
                    else {
                        date.isVisible = true
                        date.text = createdAt.humanizedDate(itemView.context)
                    }
                    sphere_name.text = sphere.name
                    vacancy_position.text = position
                    avatar.setImageURI(employer?.image)
                }
            }
        }

    }
}

