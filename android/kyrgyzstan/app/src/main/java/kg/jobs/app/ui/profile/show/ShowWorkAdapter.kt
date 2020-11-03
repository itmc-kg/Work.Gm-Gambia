package kg.jobs.app.ui.profile.show

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kg.jobs.app.Const
import kg.jobs.app.R
import kg.jobs.app.model.Work
import kg.jobs.app.parse

class ShowWorkAdapter : RecyclerView.Adapter<ShowWorkAdapter.ViewHolder>() {
    var items = mutableListOf<Work>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_detail_work, parent, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun update(list: List<Work>?) {
        list?.also {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val company_name = view.findViewById<TextView>(R.id.work_company_name)
        val position = view.findViewById<TextView>(R.id.work_position)

        fun bind(work: Work) {
            company_name.text = "${work.company}, ${work.startDate.parse(Const.DATE_FORMAT)}-${work.endDate.parse(Const.DATE_FORMAT)}"
            position.text = work.position
        }

    }
}

