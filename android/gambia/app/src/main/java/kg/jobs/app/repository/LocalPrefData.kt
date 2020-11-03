package kg.jobs.app.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class LocalPrefData(val context: Context) {
    private val preferences: SharedPreferences =
            context.getSharedPreferences("data", Context.MODE_PRIVATE)

    fun role(): String = preferences.getString("role", "")

    fun setRole(role: String) {
        preferences.edit {
            putString("role", role)
        }
    }

    fun setCountry(country: String) {
        preferences.edit {
            putString("country", country)
        }
    }

    fun country() = preferences.getString("country", "KG")

    fun hasCountry() = preferences.getString("country", "")!!.isNotBlank()

    fun isProfileCreated() = preferences.getBoolean("is_profile_created", false)

    fun profileCreated() = preferences.edit { putBoolean("is_profile_created", true) }

    fun clear() = preferences.edit { clear() }

    fun filterSalaryFrom() = preferences.getInt("filter_salary_from", 0)
    fun filterRegion() = preferences.getString("filter_region", if (country() == "KG") "all_kg" else "all_gm")
    fun filterSpheres() = preferences.getStringSet("filter_spheres", setOf()).toList()
    fun saveFilters(region: String, spheres: List<String>, salary: Int) {
        preferences.edit {
            putStringSet("filter_spheres", spheres.toSet())
            putString("filter_region", region)
            putInt("filter_salary_from", salary)
        }
    }

    fun clearFilters() {
        preferences.edit {
            remove("filter_spheres")
            remove("filter_region")
            remove("filter_salary_from")
        }
    }
}