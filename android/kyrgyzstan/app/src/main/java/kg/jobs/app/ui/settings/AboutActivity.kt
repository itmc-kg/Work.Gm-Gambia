package kg.jobs.app.ui.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kg.jobs.app.R
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        collapsingToolbar.title = getString(R.string.about_text)

        supportActionBar?.title = ""
        title_text.text = getString(R.string.about_text)
        toolbar.setNavigationOnClickListener { finish() }
    }
}
