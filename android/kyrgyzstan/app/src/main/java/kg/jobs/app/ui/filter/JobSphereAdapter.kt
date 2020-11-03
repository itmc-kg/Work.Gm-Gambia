package kg.jobs.app.ui.filter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kg.jobs.app.R
import kg.jobs.app.model.JobSphere
import kotlinx.android.synthetic.main.item_job_sphere.view.*

class JobSphereAdapter : RecyclerView.Adapter<JobSphereAdapter.ViewHolder>() {
    private val items = mutableListOf<JobSphere>()
    var onChecked: ((JobSphere) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_job_sphere, parent, false))
        holder.itemView.setOnClickListener { _ ->
            holder.adapterPosition.takeIf { it >= 0 }
                    ?.also {
                        val jobSphere = items[it].copy(checked = !items[it].checked)
                        items[it] = jobSphere
                        notifyItemChanged(it)
                        onChecked?.invoke(jobSphere)
                    }
        }
        return holder
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun update(list: List<JobSphere>?) {
        list?.also {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title = view.title
        private val icon = view.icon

        fun bind(sphere: JobSphere) {
            itemView.tag = sphere
            title.text = sphere.name
            icon.setImageResource(if (sphere.checked) R.drawable.sphere_checked else R.drawable.sphere_unchecked)
        }
    }

}