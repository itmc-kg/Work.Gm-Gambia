package kg.jobs.app.repository

import android.content.Context
import kg.jobs.app.model.Country
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class LoginRepository constructor(val context: Context) {

    suspend fun readCountries(): Map<String, Country> = coroutineScope {
        val inputStream = this@LoginRepository.context.assets.open("countries.txt")
        val read = BufferedReader(InputStreamReader(inputStream))
        val list = mutableMapOf<String, Country>()
        withContext(Dispatchers.Default) {
            read.useLines {
                it.forEach {
                    if (it.isNotEmpty()) {
                        val arr = it.split(";")
                        val code = "+${arr[1]}"
                        list[code] = Country(
                                flag = arr[0],
                                id = arr[2],
                                isoNumeric = code,
                                isoString = arr[2],
                                name = arr[3],
                                mask = if (arr.size > 4) arr[4].replace("X", "-") else "")
                    }
                }
            }
        }
        list
    }
}