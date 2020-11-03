package kg.jobs.app.ui.profile.edit

import android.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import kg.jobs.app.Const
import kg.jobs.app.R
import kg.jobs.app.afterTextChanged
import kg.jobs.app.model.Education
import kg.jobs.app.model.EducationType
import kg.jobs.app.parse
import java.util.*
import java.util.Calendar.YEAR


class EditEducationAdapter : RecyclerView.Adapter<EditEducationAdapter.ViewHolder>() {
    var onChanged: ((Education) -> Unit)? = null
    private var items = mutableListOf<Education>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_profile_edu_edit, parent, false))
        holder.type.setOnClickListener { view ->
            val eduTexts = view.context.resources.getStringArray(R.array.education_type)
            AlertDialog.Builder(view.context)
                    .setItems(eduTexts) { dialog, which ->
                        holder.type.setText(eduTexts[which])
                        holder.adapterPosition.takeIf { it >= 0 }
                                ?.also { position ->
                                    val education = items[position].copy(type = EducationType.values()[which]
                                            .toString()
                                            .toLowerCase())
                                    items[position] = education
                                    onChanged?.invoke(education)
                                }
                        dialog.dismiss()
                    }
                    .show()
        }
        holder.name.afterTextChanged { text ->
            holder.adapterPosition.takeIf { it >= 0 }
                    ?.also { position ->
                        val education = items[position].copy(name = text)
                        items[position] = education
                        onChanged?.invoke(education)
                    }
        }
        holder.speciality.afterTextChanged { text ->
            holder.adapterPosition.takeIf { it >= 0 }
                    ?.also { position ->
                        val education = items[position].copy(speciality = text)
                        items[position] = education
                        onChanged?.invoke(education)
                    }
        }
        holder.start_date.setOnClickListener { _ ->
            val now = Calendar.getInstance()
            items[holder.adapterPosition].startDate?.also {
                now.time = it
            }
            SpinnerDatePickerDialogBuilder()
                    .context(holder.itemView.context)
                    .callback { view, year, monthOfYear, dayOfMonth ->
                        now.set(year, monthOfYear, dayOfMonth)
                        holder.start_date.setText(now.time.parse(Const.DATE_FORMAT))
                        holder.adapterPosition.takeIf { it >= 0 }
                                ?.also { position ->
                                    val education = items[position].copy(startDate = now.time)
                                    items[position] = education
                                    onChanged?.invoke(education)
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
        holder.end_date.setOnClickListener { _ ->
            val now = Calendar.getInstance()
            items[holder.adapterPosition].endDate?.also {
                now.time = it
            }
            SpinnerDatePickerDialogBuilder()
                    .context(holder.itemView.context)
                    .callback { view, year, monthOfYear, dayOfMonth ->
                        now.set(year, monthOfYear, dayOfMonth)
                        holder.end_date.setText(now.time.parse(Const.DATE_FORMAT))
                        holder.adapterPosition.takeIf { it >= 0 }
                                ?.also { position ->
                                    val education = items[position].copy(endDate = now.time)
                                    items[position] = education
                                    onChanged?.invoke(education)
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

    fun update(list: List<Education>?) {
        list?.also {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val type = view.findViewById<EditText>(R.id.edu_type)
        val name = view.findViewById<EditText>(R.id.edu_name)
        val speciality = view.findViewById<EditText>(R.id.edu_speciality)
        val start_date = view.findViewById<EditText>(R.id.edu_start)
        val end_date = view.findViewById<EditText>(R.id.edu_end)

        fun bind(edu: Education) {
            type.setText(type.context.resources.getStringArray(R.array.education_type)[
                    EducationType.values()
                            .indexOfFirst { it == EducationType.valueOf(edu.type.toUpperCase()) }
                            .takeIf { it >= 0 } ?: 0])
            name.setText(edu.name)
            speciality.setText(edu.speciality)
            start_date.setText(edu.startDate.parse(Const.DATE_FORMAT))
            end_date.setText(edu.endDate.parse(Const.DATE_FORMAT))
        }
    }
}

