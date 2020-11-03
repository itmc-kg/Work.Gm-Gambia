package kg.jobs.app.ui.profile.edit

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import kg.jobs.app.Const
import kg.jobs.app.R
import kg.jobs.app.afterTextChanged
import kg.jobs.app.model.Work
import kg.jobs.app.parse
import java.util.*

class EditWorkAdapter : RecyclerView.Adapter<EditWorkAdapter.ViewHolder>() {
    var onChanged: ((Work) -> Unit)? = null
    var items = mutableListOf<Work>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_profile_work_edit, parent, false))
        holder.company_name.afterTextChanged { text ->
            holder.adapterPosition.takeIf { it >= 0 }
                    ?.also { position ->
                        val work = items[position].copy(company = text)
                        items[position] = work
                        onChanged?.invoke(work)
                    }
        }
        holder.position.afterTextChanged { text ->
            holder.adapterPosition.takeIf { it >= 0 }
                    ?.also { position ->
                        val work = items[position].copy(position = text)
                        items[position] = work
                        onChanged?.invoke(work)
                    }
        }
        holder.start_date.setOnClickListener { _ ->
            val calendar = Calendar.getInstance()
            items[holder.adapterPosition].startDate?.also {
                calendar.time = it
            }
            SpinnerDatePickerDialogBuilder()
                    .context(holder.itemView.context)
                    .callback { view, year, monthOfYear, dayOfMonth ->
                        calendar.clear()
                        calendar.set(year, monthOfYear, dayOfMonth)
                        holder.start_date.setText(calendar.time.parse(Const.DATE_FORMAT))
                        holder.adapterPosition.takeIf { it >= 0 }
                                ?.also { position ->
                                    val work = items[position].copy(startDate = calendar.time)
                                    items[position] = work
                                    onChanged?.invoke(work)
                                }
                    }
                    .showTitle(true)
                    .showDaySpinner(true)
                    .defaultDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    .maxDate(calendar.get(Calendar.YEAR) + 5, 11, 31)
                    .minDate(1950, 0, 1)
                    .build()
                    .show()
        }
        holder.end_date.setOnClickListener { _ ->
            val now = Calendar.getInstance()
            items[holder.adapterPosition].endDate?.also {
                now.time = it
            }
            SpinnerDatePickerDialogBuilder()
                    .context(holder.itemView.context)
                    .callback { view, year, monthOfYear, dayOfMonth ->
                        now.clear()
                        now.set(year, monthOfYear, dayOfMonth)
                        holder.end_date.setText(now.time.parse(Const.DATE_FORMAT))
                        holder.adapterPosition.takeIf { it >= 0 }
                                ?.also { position ->
                                    val work = items[position].copy(endDate = now.time)
                                    items[position] = work
                                    onChanged?.invoke(work)
                                }
                    }
                    .showTitle(true)
                    .showDaySpinner(true)
                    .defaultDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
                    .maxDate(now.get(Calendar.YEAR) + 5, 11, 31)
                    .minDate(1950, 0, 1)
                    .build()
                    .show()
        }
        return holder
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
        val company_name = view.findViewById<EditText>(R.id.work_company_name)
        val position = view.findViewById<EditText>(R.id.work_position)
        val start_date = view.findViewById<EditText>(R.id.work_start)
        val end_date = view.findViewById<EditText>(R.id.work_end)

        fun bind(work: Work) {
            company_name.setText(work.company)
            position.setText(work.position)
            start_date.setText(work.startDate.parse(Const.DATE_FORMAT))
            end_date.setText(work.endDate.parse(Const.DATE_FORMAT))
        }

    }
}

