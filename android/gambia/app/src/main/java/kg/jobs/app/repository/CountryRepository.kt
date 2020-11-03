package kg.jobs.app.repository

import android.content.Context
import kg.jobs.app.model.Country
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class CountryRepository constructor(val context: Context) {
    suspend fun getCountries(): List<Country> {
        val inputStream = this@CountryRepository.context.assets.open("countries.txt")
        val read = BufferedReader(InputStreamReader(inputStream))
        val list = mutableListOf<Country>()
        withContext(Dispatchers.Default) {
            read.useLines {
                it.forEach {
                    if (it.isNotEmpty()) {
                        val arr = it.split(";")
                        list.add(Country(
                                flag = arr[0],
                                isoNumeric = "+${arr[1]}",
                                id = arr[2],
                                isoString = arr[2],
                                name = arr[3],
                                mask = if (arr.size > 4) arr[4].replace("X", "-") else ""))
                    }
                }
            }
        }
        list.sortBy { it.name }
        return list
    }
}