package kg.jobs.app.ui.country

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kg.jobs.app.LangPref
import kg.jobs.app.R
import kg.jobs.app.startActivity
import kg.jobs.app.ui.SplashActivity
import kg.jobs.app.ui.lang.ChooseLanguageActivity
import kg.jobs.app.updateLocale
import kotlinx.android.synthetic.main.activity_choose_country.*
import kotlinx.android.synthetic.main.appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChooseCountryActivity : AppCompatActivity() {
    private val viewModel: ChooseCountryViewModel by viewModel()
    private var selected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_country)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.choose_country_text)

        viewModel.selectedCountry.observe(this, Observer {
            when (it) {
                "KG" -> {
                    gm_check.visibility = View.GONE
                    kg_check.visibility = View.VISIBLE
                    lang("ru")
                    if (selected) {
                        startActivity<ChooseLanguageActivity>(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        finishAffinity()
                    }
                }
                "GM" -> {
                    gm_check.visibility = View.VISIBLE
                    kg_check.visibility = View.GONE
                    lang("en")
                    if (selected) {
                        startActivity<SplashActivity>(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        finishAffinity()
                    }
                }
            }
        })

        gm_content.setOnClickListener {
            viewModel.selectGambia()
            selected = true
        }

        kg_content.setOnClickListener {
            viewModel.selectKyrgyzstan()
            selected = true
        }
    }

    fun lang(language: String) {
        if (language != null) {
            LangPref(this).setLanguage(this, language)
            LangPref(this).setLanguageStart(this)
        }
    }

}
