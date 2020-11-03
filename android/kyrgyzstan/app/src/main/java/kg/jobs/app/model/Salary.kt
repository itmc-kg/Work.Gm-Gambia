package kg.jobs.app.model

import android.content.Context
import android.os.Parcelable
import kg.jobs.app.R
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Salary(val id: String = "undefined",
                  val start: Int = 0,
                  val end: Int = 100) : Parcelable {
    fun text(context: Context): String {
        return when (id) {
            "fixed" -> String.format(context.getString(R.string.range_from), start * 1000)
            "range" -> "${start * 1000} - ${end * 1000}"
            else -> context.getString(R.string.range_not_specified)
        }
    }
}