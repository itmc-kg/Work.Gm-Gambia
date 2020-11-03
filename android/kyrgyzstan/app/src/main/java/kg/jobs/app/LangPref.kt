package kg.jobs.app

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import java.util.*

/**
 * User data stored in system Shared Preferences
 * data like tokens
 */
class LangPref(val context: Context) {

    private val LANGUAGE = "language-job"

    fun getString(context: Context, key: String?, defValue: String?): String {
        return getPrefs(context).getString(key, defValue)
    }

    fun isLang(context: Context): Boolean {
        var lan = false
        if (getLang(context) != null && getLang(context)?.isNotEmpty() ?: return false) {
            lan = true
        }
        return lan
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(LANGUAGE, Context.MODE_PRIVATE)
    }

    private fun getEditor(context: Context): SharedPreferences.Editor {
        return context.getSharedPreferences(LANGUAGE, Context.MODE_PRIVATE).edit()
    }

    fun getLang(context: Context): String {
        return getString(context, LANGUAGE, "")
    }

    fun getLanguge(context: Context): String {
        if (isLang(context)) {
            return getString(context, LANGUAGE, "")
        }else{
            return "en"
        }
    }


    fun setLanguage(context: Context, language: String?) {
        getEditor(context).putString(LANGUAGE, language).commit()
    }

    fun setLanguageStart(context: Context) {
        getLang(context)?.let { lanConfiguration(context, it) }
    }

    fun deleteLang(context: Context) {
        setLanguage(context, "")
        val mySPrefs = context.getSharedPreferences(LANGUAGE, Context.MODE_PRIVATE)
        val editor = mySPrefs.edit()
        editor.remove(LANGUAGE)
        editor.apply()
    }

    //Lang Setting
    fun lan(context: Context): Boolean {
        if (isLang(context)) {
            when {
                getLang(context) == "ky" -> {
                    lanConfiguration(context, "ky")
                }
                getLang(context) == "ru" -> {
                    lanConfiguration(context, "ru")
                }
                else -> {
                    lanConfiguration(context, "en")
                }
            }
            return true
        }
        return false
    }

//    private fun lanConfiguration(context: Context, language: String) {
//        val locale = Locale(language)
//        Locale.setDefault(locale)
//        val configuration = Configuration()
//        configuration.locale = locale
//        context.resources?.updateConfiguration(configuration, null)
//        setLanguage(context, language)
//    }

    private fun lanConfiguration(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration
        context.createConfigurationContext(configuration)
        configuration.locale = locale
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

}