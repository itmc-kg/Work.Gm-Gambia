package kg.jobs.app.ui

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import kg.jobs.app.LangPref
import kg.jobs.app.R
import kg.jobs.app.startActivity
import kg.jobs.app.ui.login.LoginActivity
import kg.jobs.app.ui.role.ChooseRoleActivity
import kotlinx.android.synthetic.main.activity_splash.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lang("en")
        setContentView(R.layout.activity_splash)

        if (LangPref(this).isLang(this)) {
            LangPref(this).setLanguageStart(this)
        }

        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity<ChooseRoleActivity> {
                if (intent.hasExtra("chatId"))
                    putExtra("chatId", intent?.getStringExtra("chatId"))
                if (intent.hasExtra("applicationId"))
                    putExtra("applicationId", intent?.getStringExtra("applicationId"))
            }
            finish()
        } else {
            next.isVisible = true
            next.setOnClickListener {
                startActivity<LoginActivity>()
                finish()
            }
        }
    }

    fun lang(language: String) {
        if (language != null) {
            LangPref(this).setLanguage(this, language)
            LangPref(this).setLanguageStart(this)
        }
    }


}
