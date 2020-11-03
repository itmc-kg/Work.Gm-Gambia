package kg.jobs.app.ui.lang

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kg.jobs.app.LangPref
import kg.jobs.app.R
import kg.jobs.app.startActivity
import kg.jobs.app.ui.SplashActivity
import kg.jobs.app.ui.country.ChooseCountryActivity
import kotlinx.android.synthetic.main.activity_choose_language.*
import kotlinx.android.synthetic.main.appbar.*

class ChooseLanguageActivity : AppCompatActivity() {

    private var language: String? = null
    private var isSettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
//        if (!intent.getBooleanExtra("isSettings", false)) {
//            if (LangPref(this).isLang(this)) {
//                LangPref(this).setLanguageStart(this)
//                startActivity<SplashActivity>()
//                finish()
//            }
//        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_language)
        setSupportActionBar(toolbar)

        isSettings = intent.getBooleanExtra("isSettings", false)
        supportActionBar?.title = getString(R.string.choose_language_text)
        if (isSettings){
            start.text = getString(R.string.save)
        }

        language = LangPref(this).getLang(this)
        checkLang()
        eng_content.setOnClickListener {
            language = "en"
            checkLang()
        }

        kg_content.setOnClickListener {
            language = "ky"
            checkLang()
        }

        ru_content.setOnClickListener {
            language = "ru"
            checkLang()
        }

        start.setOnClickListener {
            if (language != null) {
                LangPref(this).setLanguage(this, language)
                LangPref(this).setLanguageStart(this)

                if (!isSettings){
                    startActivity<SplashActivity>(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    finishAffinity()
                }else
                startActivity<SplashActivity>(Intent.FLAG_ACTIVITY_NO_ANIMATION or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NEW_TASK)
                finish()
            }
        }

    }

    private fun checkLang(){
        when (language) {
            "en" -> {
                ru_check.visibility = View.GONE
                kg_check.visibility = View.GONE
                eng_check.visibility = View.VISIBLE
            }
            "ky" -> {
                ru_check.visibility = View.GONE
                kg_check.visibility = View.VISIBLE
                eng_check.visibility = View.GONE
            }
            "ru" -> {
                ru_check.visibility = View.VISIBLE
                kg_check.visibility = View.GONE
                eng_check.visibility = View.GONE
            }
        }
    }

}
