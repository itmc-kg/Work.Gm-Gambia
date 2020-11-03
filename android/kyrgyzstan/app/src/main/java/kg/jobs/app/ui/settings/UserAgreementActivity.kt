package kg.jobs.app.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Html
import kg.jobs.app.R
import kotlinx.android.synthetic.main.activity_user_agreement.*
import kotlinx.android.synthetic.main.item_job_sphere.view.*

@Suppress("DEPRECATION")
class UserAgreementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_agreement)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = ""
        title_text.text = getString(R.string.user_agreement_text)
        text.text = Html.fromHtml(getString(R.string.privacy_policy))
        toolbar.setNavigationOnClickListener { finish() }
    }
}
