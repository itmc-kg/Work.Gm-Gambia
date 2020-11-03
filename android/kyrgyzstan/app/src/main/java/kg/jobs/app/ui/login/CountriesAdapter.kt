package kg.jobs.app.ui.login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kg.jobs.app.R
import kg.jobs.app.model.Country
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CountriesAdapter(private val cb: (c: Country) -> Unit) : RecyclerView.Adapter<CountriesAdapter.ViewHolder>() {

    var list: List<Country> = mutableListOf()
    var reserveList: List<Country> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_country, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val pos = viewHolder.adapterPosition
            if (pos != -1) cb(list[pos])
        }
        return viewHolder
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(list[position])

    fun setData(list: List<Country>?) {
        this.list = list!!
        notifyDataSetChanged()
    }

    fun search(query: String) {
        if (query.isEmpty()) {
            searchViewCollapsed()
            return
        }
        GlobalScope.launch(Dispatchers.Main) {
            delay(1000)
            val result = reserveList.filter {
                it.name.toLowerCase().contains(query.toLowerCase())
            }

            list = result
            notifyDataSetChanged()
        }
    }

    fun searchViewExpanded() {
        reserveList = list
    }

    fun searchViewCollapsed() {
        list = reserveList
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.findViewById<TextView>(R.id.account_name)
        private val code = view.findViewById<TextView>(R.id.code)
        fun bind(country: Country) {
            name.text = "${country.flag}  ${country.name}"
            code.text = country.isoNumeric
        }

    }

}