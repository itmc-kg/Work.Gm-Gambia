package kg.jobs.app.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kg.jobs.app.*
import kg.jobs.app.model.Application
import kg.jobs.app.model.Employer
import kg.jobs.app.model.JobSphere
import kg.jobs.app.ui.chats.ChatsFragment
import kg.jobs.app.ui.profile.ProfileFragment
import kg.jobs.app.ui.search.SearchFragment
import kg.jobs.app.ui.search.show.VacancyDetailActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_profile -> {
                profile()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                vacancies()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_chats -> {
                chats()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun chats(chatId: String = "") {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, ChatsFragment.create(chatId = chatId))
                .commit()
    }

    private fun vacancies() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, SearchFragment())
                .commit()
    }

    private fun profile() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, ProfileFragment.create())
                .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.setOnNavigationItemReselectedListener { }
        profile()

        checkNotifications(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.hasExtra("refresh_vacancies") == true) {
            vacancies()
        }
        checkNotifications(intent)
    }

    private val firestore by inject<FirebaseFirestore>()
    private fun checkNotifications(intent: Intent?) {
        if (intent?.hasExtra("chatId") == true)
            chats(intent.getStringExtra("chatId"))
        if (intent?.hasExtra("applicationId") == true) {
            profile()
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val applicationId = intent.getStringExtra("applicationId")!!
                    val application = firestore.collection("applications")
                            .document(applicationId)
                            .get().awaitWithException()
                            ?.let { application(it) }
                    if (application != null) {
                        startActivity<VacancyDetailActivity> {
                            putExtra("application", application)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    private suspend fun application(snapshot: DocumentSnapshot): Application {
        return snapshot.let {
            Application(id = it.id, position = it.getString("position") ?: "",
                    vacancyId = it.getString("vacancyId") ?: "",
                    authorId = it.getString("authorId") ?: "",
                    status = it.getString("status") ?: "",
                    employerId = it.getString("employerId") ?: "",
                    createdAt = it.getDate("createdAt") ?: Date(),
                    sphere = (it.get("sphere") as? Map<*, *>)
                            ?.run {
                                JobSphere(id = get("id")as? String ?: "",
                                        name = get("name") as? String ?: "")
                            } ?: JobSphere("", ""),
                    employer = getEmployer(it.getString("employerId") ?: ""))
        }
    }

    private suspend fun getEmployer(id: String): Employer? {
        return firestore.collection("employers")
                .document(id)
                .get().await()
                .takeIf { it.isSuccessful }
                ?.let { it.result }
                ?.let {
                    Employer(id = it.id,
                            name = it.get("name")as? String ?: "",
                            about = it.get("about")as? String ?: "",
                            image = it.get("image")as? String ?: "",
                            regionId = it.get("regionId") as?String ?: "",
                            city = it.get("city")as? String ?: "")
                }
    }

}
