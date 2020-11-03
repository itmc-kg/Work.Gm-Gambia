package kg.jobs.app.ui.vacancy

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kg.jobs.app.R
import kotlinx.android.synthetic.main.item_job_sphere.view.*

class SelectAdapter(private val items: List<String>) : RecyclerView.Adapter<SelectAdapter.ViewHolder>() {

    var selectedPosition: Int = 0
        set(value) {
            field = value
            notifyItemChanged(value)
            onSelect(selectedPosition)
        }
    var onSelect: (Int) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_job_sphere, parent, false))
        holder.itemView.setOnClickListener { _ ->
            holder.adapterPosition.takeIf { it >= 0 }
                    ?.also {
                        notifyItemChanged(selectedPosition)
                        selectedPosition = it
                    }
        }
        return holder
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            title.text = items[position]
            icon.setImageResource(if (position == selectedPosition) R.drawable.sphere_selected else R.drawable.sphere_unchecked)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}