package kg.jobs.app.ui.employer


import androidx.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kg.jobs.app.*
import kg.jobs.app.model.*
import kg.jobs.app.repository.vacancy.State
import kg.jobs.app.ui.employer.show.EmployerDetailActivity
import kg.jobs.app.ui.employer.vacancy.MainVacancyActivity
import kg.jobs.app.ui.messenger.ChatActivity
import kg.jobs.app.ui.settings.SettingsActivity
import kg.jobs.app.ui.vacancy.EditVacancyActivity
import kotlinx.android.synthetic.main.activity_vacancies.*
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import java.util.*
import kotlin.math.absoluteValue

class VacanciesActivity : AppCompatActivity() {
    private val viewModel by viewModel<VacanciesViewModel>()
    private val adapter = VacancyAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vacancies)
        initViews()
        viewModel.vacancies.observe(this, Observer(adapter::submitList))
        viewModel.vacancies.observe(this, Observer{
            if (it.size == 0){
                isEmptyLayout.visibility = View.VISIBLE
            }else{
                isEmptyLayout.visibility = View.GONE
            }
        })

        viewModel.employer.observe(this, Observer {
            it?.apply {
                company_name.text = "$name,"
                city_name.text = city
                avatar.setImageURI(image)
            }
        })

        viewModel.networkState.observe(this, Observer {
            when (it) {
                State.LOADING -> progressBar.isVisible = true
                State.FAIL -> {
                    progressBar.isVisible = false
                    Snackbar.make(recyclerView, R.string.error_no_internet, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.retry) { viewModel.refresh() }
                            .show()
                }
                State.SUCCESS -> progressBar.isVisible = false
            }
        })
        adapter.onItemClick = { vacancy ->
            startActivity<MainVacancyActivity> {
                putExtra("vacancy", vacancy)
            }
        }
        adapter.onArchiveClick = {
            viewModel.archive(it)
        }
        adapter.onEditClick = {
            startActivity<EditVacancyActivity> {
                putExtra("vacancy", it)
            }
        }

        viewModel.init()
        checkNotifications(intent)
    }

    private fun initViews() {
        collapsingToolbar.title = getString(R.string.vacancies)
        appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val params = avatar.layoutParams.apply {
                width = 40.toPx + (40.toPx - (verticalOffset.absoluteValue / appBarLayout.totalScrollRange.toFloat() * 40.toPx)).toInt()
                height = 40.toPx + (40.toPx - (verticalOffset.absoluteValue / appBarLayout.totalScrollRange.toFloat() * 40.toPx)).toInt()
            }
            avatar.layoutParams = params
        })
        setSupportActionBar(toolbar)
        show_profile.setOnClickListener { startActivity<EmployerDetailActivity>() }
        add_vacancy.setOnClickListener { startActivity<EditVacancyActivity>() }
        recyclerView.adapter = adapter

        tabs.addTab(tabs.newTab().setText(getString(R.string.tab_vacancy_opened)))
        tabs.addTab(tabs.newTab().setText(getString(R.string.tab_vacancy_closed)))
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                viewModel.refresh()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.setStatus(when (tab?.position) {
                    0 -> "opened"
                    else -> "closed"
                })
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, Menu.FIRST, 0, getString(R.string.action_settings))?.apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setIcon(R.drawable.ic_settings)
            setOnMenuItemClickListener {
                startActivity<SettingsActivity>()
                true
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        viewModel.refresh()
        checkNotifications(intent)
    }


    private val firestore by inject<FirebaseFirestore>()
    private val uid by inject<String>(named("uid"))
    private fun checkNotifications(intent: Intent?) {
        if (intent?.hasExtra("chatId") == true) {
            viewModel.launch {
                try {
                    val chatId = intent.getStringExtra("chatId")!!
                    firestore.collection("chats")
                            .document(chatId)
                            .get().awaitWithException()?.also { doc ->
                                val partnerId = (doc.get("users") as? List<String>
                                        ?: listOf()).find { it != uid } ?: "_"
                                val chatInfo = (doc.get(partnerId) as? Map<*, *>).let {
                                    ChatInfo(it?.get("name")as? String
                                            ?: "", it?.get("image")as? String ?: "")
                                }
                                val chat = Chat(id = doc.id,
                                        me = uid,
                                        partner = partnerId,
                                        data = chatInfo,
                                        lastMessage = null,
                                        vacancyId = doc.getString("vacancyId") ?: "",
                                        updatedAt = doc.getDate("updatedAt") ?: Date())
                                ChatActivity.start(this@VacanciesActivity, chat)
                            }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        if (intent?.hasExtra("applicationId") == true) {
            viewModel.launch {
                try {
                    val applicationId = intent.getStringExtra("applicationId")!!
                    val vacancy = firestore.collection("applications")
                            .document(applicationId)
                            .get().awaitWithException()
                            ?.let { doc ->
                                firestore.collection("vacancies")
                                        .document(doc.getString("vacancyId")!!)
                                        .get().awaitWithException()
                            }?.let { vacancy(it) }
                    if (vacancy != null) {
                        startActivity<MainVacancyActivity> {
                            putExtra("vacancy", vacancy)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun vacancy(snapshot: DocumentSnapshot): Vacancy {
        val sphereId = snapshot.get("sphereId")as? String
        val sphereName = sphereId?.let { id ->
            firestore.collection("spheres")
                    .document(id)
                    .get().await()
                    .takeIf { it.isSuccessful }
                    ?.run { result?.getString(get(named("lang"))) }
        } ?: ""

        return Vacancy(id = snapshot.id,
                position = snapshot.get("position")as? String ?: "",
                description = snapshot.get("description") as? String ?: "",
                scheduleId = snapshot.getLong("scheduleId")?.toInt() ?: 0,
                employmentTypeId = snapshot.getLong("employmentTypeId")?.toInt() ?: 0,
                sphere = JobSphere(sphereId ?: "", sphereName),
                salary = (snapshot.get("salary")as? Map<*, *>)?.run {
                    Salary(get("id") as? String ?: "",
                            (get("start") as? Number)?.toInt() ?: 0,
                            (get("end")as? Number)?.toInt() ?: 0)
                } ?: Salary(),
                applicationsCount = snapshot.getLong("applicationsCount") ?: 0
        )
    }
}
