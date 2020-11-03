package kg.jobs.app.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceId
import kg.jobs.app.*
import kg.jobs.app.repository.LocalPrefData
import kg.jobs.app.ui.MainActivity
import kg.jobs.app.ui.SplashActivity
import kg.jobs.app.ui.country.ChooseCountryActivity
import kg.jobs.app.ui.employer.VacanciesActivity
import kg.jobs.app.ui.employer.edit.EditEmployerActivity
import kg.jobs.app.ui.lang.ChooseLanguageActivity
import kg.jobs.app.ui.profile.edit.EditProfileActivity
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named


class SettingsActivity : AppCompatActivity() {

    private val pref: LocalPrefData by inject()
    private val firestore by inject<FirebaseFirestore>()
    private val uid by inject<String>(named("uid"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        collapsingToolbar.title = getString(R.string.action_settings)
        toolbar.setNavigationOnClickListener { finish() }

        GlobalScope.launch(Dispatchers.Main) {
            if (uid.isNotEmpty()) {
                try {
                    val doc = firestore.collection("users")
                            .document(uid)
                            .get().awaitWithException()
                    message_switch.isChecked = doc?.getBoolean("message_notifications") ?: true
                    request_switch.isChecked = doc?.getBoolean("application_notifications") ?: true
                    textView2.text = if (doc?.getString("role") == "employee")
                        getString(R.string.request_switch_description)
                    else
                        getString(R.string.request_switch_description_for_employer)

                    if (doc?.getString("role") == "employee") {
                        change_role.setOnClickListener { setChangeRole(doc.getString("role").toString()) }
                        change_role.text = getString(R.string.become_employer)

                    } else {
                        change_role.text = getString(R.string.become_employee)
                        change_role.setOnClickListener { setChangeRole(doc?.getString("role").toString()) }
                    }

                    message_switch.setOnCheckedChangeListener { _, isChecked ->
                        firestore.collection("users")
                                .document(uid)
                                .update(mapOf("message_notifications" to isChecked))
                    }

                    request_switch.setOnCheckedChangeListener { _, isChecked ->
                        firestore.collection("users")
                                .document(uid)
                                .update(mapOf("application_notifications" to isChecked))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        select_country.setOnClickListener { startActivity<ChooseCountryActivity> {} }
        user_agreement.setOnClickListener { startActivity<UserAgreementActivity> {} }


        change_lang.setOnClickListener {
            val intent = Intent(this, ChooseLanguageActivity::class.java)
            intent.putExtra("isSettings", true)
            startActivity(intent)
        }

        about.setOnClickListener { startActivity<AboutActivity> {} }
        exit.setOnClickListener {
            AlertDialog.Builder(this)
                    .setMessage(getString(R.string.dialog_logout_msg))
                    .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                        FirebaseAuth.getInstance().signOut()
                        FirebaseAuth.getInstance().addAuthStateListener { auth ->
                            GlobalScope.launch(Dispatchers.Main) {
                                if (auth.currentUser == null) {
                                    pref.clear()
                                    if (uid.isNotEmpty()) {
                                        FirebaseInstanceId.getInstance().instanceId.await()
                                                .takeIf { it.isSuccessful }
                                                ?.run { result?.token }
                                                ?.also { token ->
                                                    firestore.collection("users")
                                                            .document(uid)
                                                            .collection("devices")
                                                            .document(token).delete()
                                                }
                                    }
                                    startActivity<SplashActivity>(Intent.FLAG_ACTIVITY_NO_ANIMATION or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                            Intent.FLAG_ACTIVITY_NEW_TASK)
                                    dialog.dismiss()
                                }
                            }
                        }
                    }
                    .show()
        }

        when (LocalPrefData(this).country()) {
            "KG" -> {
                change_lang_view.visibility = View.VISIBLE
                change_lang.visibility = View.VISIBLE
            }
            else ->{
                change_lang_view.visibility = View.GONE
                change_lang.visibility = View.GONE
            }
        }

    }

    private fun setChangeRole(role: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val result = when (role) {
                "employee" -> firestore.collection("employers")
                        .document(uid)
                        .get().await()
                        .takeIf { it.isSuccessful }
                        ?.run { result?.exists() } ?: false
                else -> firestore.collection("employees")
                        .document(uid)
                        .get().await()
                        .takeIf { it.isSuccessful }
                        ?.run { result?.exists() } ?: false
            }

            if (role=="employee") {
                firestore.collection("users")
                        .document(uid)
                        .set(mapOf("role" to "employer"), SetOptions.merge())
                        .awaitWithException()
                pref.setRole("employer")
            }else{
                firestore.collection("users")
                        .document(uid)
                        .set(mapOf("role" to "employee"), SetOptions.merge())
                        .awaitWithException()
                pref.setRole("employee")
            }

            if (!result) {
              if (role == "employee"){
                  startActivity<EditEmployerActivity>(Intent.FLAG_ACTIVITY_CLEAR_TOP) {
                      putExtra("start", true)
                      finish()
                  }
              }else{
                  startActivity<EditProfileActivity>(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                  finish()
              }
            }else{
                if (role=="employee") {
                    startActivity(Intent(this@SettingsActivity,VacanciesActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    finish()
                }else{
                    startActivity(Intent(this@SettingsActivity,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    finish()
                }
            }
        }
    }

}
