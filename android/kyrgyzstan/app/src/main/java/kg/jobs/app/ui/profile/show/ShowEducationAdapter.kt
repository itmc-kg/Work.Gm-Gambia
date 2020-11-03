package kg.jobs.app.ui.profile.show

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kg.jobs.app.Const
import kg.jobs.app.R
import kg.jobs.app.model.Education
import kg.jobs.app.parse

class ShowEducationAdapter : RecyclerView.Adapter<ShowEducationAdapter.ViewHolder>() {
    private var items = mutableListOf<Education>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_detail_edu, parent, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun update(list: List<Education>?) {
        list?.also {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.name)
        val speciality = view.findViewById<TextView>(R.id.speciality)

        fun bind(edu: Education) {
            name.text = "${edu.name}, ${edu.startDate.parse(Const.DATE_FORMAT)}-${edu.endDate.parse(Const.DATE_FORMAT)}"
            speciality.text = edu.speciality
        }
    }
}

