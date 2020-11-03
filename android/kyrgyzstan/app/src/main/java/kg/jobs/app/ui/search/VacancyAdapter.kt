package kg.jobs.app.ui.search

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import kg.jobs.app.R
import kg.jobs.app.model.EmploymentType
import kg.jobs.app.model.Schedule
import kg.jobs.app.model.Vacancy
import kg.jobs.app.startActivity
import kg.jobs.app.ui.employer.show.EmployerDetailActivity
import kotlinx.android.synthetic.main.item_vacancy.view.*

class VacancyAdapter : RecyclerView.Adapter<VacancyAdapter.ViewHolder>() {
    private var items = mutableListOf<Vacancy>()
    var onAccept: ((Vacancy) -> Unit)? = null
    var onDecline: ((Vacancy) -> Unit)? = null
    var onClick: ((Vacancy) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vacancy, parent, false))
        holder.itemView.setOnClickListener {
            holder.adapterPosition.takeIf { it >= 0 }
                    ?.also { onClick?.invoke(items[it]) }
        }
        holder.itemView.vacancy_ok.setOnClickListener {
            it.isEnabled = false
            holder.adapterPosition.takeIf { it >= 0 }
                    ?.also { onAccept?.invoke(items[it]) }
        }
        holder.itemView.vacancy_next.setOnClickListener {
            //            it.isEnabled = false
            holder.adapterPosition.takeIf { it >= 0 }
                    ?.also { onDecline?.invoke(items[it]) }
        }

        holder.itemView.avatar.setOnClickListener {
            holder.adapterPosition.takeIf { it >= 0 }
                    ?.also {
                        holder.itemView.context.startActivity<EmployerDetailActivity> {
                            putExtra("employer_id", items[it].employer?.id)
                        }
                    }
        }
        return holder
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun add(item: Vacancy?) {
        item?.also { vacancy ->
            val pos = items.indexOfFirst { it.id == vacancy.id }
            if (pos >= 0) {
                items[pos] = vacancy
                notifyItemChanged(pos)
            } else {
                items.add(vacancy)
                notifyItemInserted(itemCount - 1)
            }
        }
    }

    fun returnData(item: Vacancy?) {
        item?.let { items.add(0, it) }
        notifyItemInserted(0)
    }

    fun getItemPosition(pos: Int): Vacancy? {
        if (items.size == 0) return null
        return items[pos]
    }

    fun getItemSize(): Int {
        return this.items.count()
    }

    fun remove(vacancy: Vacancy?) {
        if (vacancy == null) return
        items.indexOfFirst { it.id == vacancy?.id }
                .takeIf { it >= 0 }
                ?.also {
                    items.removeAt(it)
                    notifyItemRemoved(it)
                }
    }

    fun removeAtIndex(pos: Int) {
        items.removeAt(pos)
//        notifyItemRemoved(pos)
    }

    fun accepted(topPosition: Int) {
        items[topPosition] = items[topPosition].copy(accepted = true)
        notifyDataSetChanged()
    }

    fun declined(topPosition: Int) {
        Log.e("ERR", "$topPosition - ${items.size}")
        if (topPosition < 1 && topPosition < items.size) {
            items[topPosition] = items[topPosition].copy(declined = true)
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(vacancy: Vacancy) {
            itemView.apply {
                vacancy_ok.isEnabled = true
                vacancy_next.isEnabled = true
                vacancy.apply {
                    avatar.setImageURI(employer?.image)
                    company_name.text = employer?.name
                    city_name.text = employer?.city
                    work_sphere.text = sphere.name
                    work_position.text = position
                    employment_type_name.text = getEmploymentTypes(context)
                            .find { it.id == vacancy.employmentTypeId }?.name ?: ""
                    schdule_name.text = getSchedules(context).find { it.id == vacancy.scheduleId }?.name
                            ?: ""
                    vacancy_text.text = description
                    salary_text.text = salary.text(itemView.context)
                    salary_desc.text = itemView.context.getString(R.string.after_interview)
                }
                when {
                    vacancy.accepted -> {
                        card_bg.setBackgroundResource(R.drawable.vacancy_apply_bg_ok)
                        status_icon.isVisible = true
                        status_icon.setImageResource(R.drawable.vacancy_ok_icon)
                    }
                    vacancy.declined -> {
                        card_bg.setBackgroundResource(R.drawable.vacancy_apply_bg_next)
                        status_icon.isVisible = true
                        status_icon.setImageResource(R.drawable.vacancy_next_icon)
                    }
                    else -> {
                        card_bg.isVisible = false
                        status_icon.isVisible = false
                    }
                }
            }
        }

        fun getSchedules(context: Context): ArrayList<Schedule> {
            return ArrayList(context.resources?.getStringArray(R.array.schedules)
                    ?.mapIndexed { index, s -> Schedule(index, s) })
        }

        private fun getEmploymentTypes(context: Context): ArrayList<EmploymentType> {
            val list = context?.resources?.getStringArray(R.array.employment_types)

            if (list != null) {
                for (employer in list) {
                    Log.e("TAG", employer)
                }
            }

            return ArrayList(context.resources?.getStringArray(R.array.employment_types)
                    ?.mapIndexed { index, s -> EmploymentType(index, s) })
        }
    }


}

