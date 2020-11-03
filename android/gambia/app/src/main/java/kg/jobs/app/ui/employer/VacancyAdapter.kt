package kg.jobs.app.ui.employer

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kg.jobs.app.R
import kg.jobs.app.model.Vacancy
import kotlinx.android.synthetic.main.item_vacancy_list.view.*

class VacancyAdapter : PagedListAdapter<Vacancy, VacancyAdapter.ViewHolder>(DIFF_CALLBACK) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Vacancy>() {
            override fun areItemsTheSame(oldItem: Vacancy, newItem: Vacancy): Boolean {
                return oldItem?.id == newItem?.id
            }

            override fun areContentsTheSame(oldItem: Vacancy, newItem: Vacancy): Boolean {
                return oldItem == newItem
            }
        }
    }

    var onItemClick: ((Vacancy) -> Unit)? = null
    var onEditClick: ((Vacancy) -> Unit) = {}
    var onArchiveClick: ((Vacancy) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vacancy_list, parent, false))
        holder.itemView.setOnClickListener {
            holder.adapterPosition.takeIf { it >= 0 }
                    ?.also {pos->
                        getItem(pos)?.also {
                            onItemClick?.invoke(it)
                            it.applicationsCount = 0
                            notifyItemChanged(pos)
                        }
                    }
        }
        holder.itemView.imageButton.setOnClickListener { view ->
            holder.adapterPosition.takeIf { it >= 0 }
                    ?.also { position ->
                        PopupMenu(parent.context, view).apply {
                            menu.add(0, 1, 0, view.context.getString(R.string.action_edit_vacancy))
                            menu.add(0, 2, 1,
                                    if (getItem(position)?.status == "opened")
                                        parent.context.getString(R.string.action_close_vacancy)
                                    else
                                        parent.context.getString(R.string.action_open_vacancy))
                            show()
                        }.setOnMenuItemClickListener {
                            when (it.itemId) {
                                1 -> onEditClick(getItem(position)!!)
                                2 -> onArchiveClick(getItem(position)!!)
                            }
                            true
                        }
                    }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(vacancy: Vacancy?) {
            itemView?.apply {
                vacancy?.also {
                    position.text = it.position
                    sphere_name.text = it.sphere.name
                    notification_count.text = it.applicationsCount.toString()
                    notification_count.isVisible = it.applicationsCount > 0
                }
            }
        }

    }
}

