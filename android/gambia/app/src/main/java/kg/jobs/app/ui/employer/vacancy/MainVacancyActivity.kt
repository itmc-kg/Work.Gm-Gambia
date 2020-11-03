package kg.jobs.app.ui.employer.vacancy

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import kg.jobs.app.R
import kg.jobs.app.model.Vacancy
import kg.jobs.app.ui.chats.ChatsFragment
import kg.jobs.app.ui.employer.vacancy.detail.VacancyApplicationsFragment
import kg.jobs.app.ui.employer.vacancy.detail.VacancyDetailFragment
import kotlinx.android.synthetic.main.activity_main_vacancy.*

class MainVacancyActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        return@OnNavigationItemSelectedListener when (item.itemId) {
            R.id.navigation_vacancy -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment, VacancyDetailFragment.create(vacancy!!))
                        .commit()
                true
            }
            R.id.navigation_search -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment, VacancyApplicationsFragment.create(vacancy!!))
                        .commit()
                true
            }
            R.id.navigation_chats -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment, ChatsFragment.create(vacancy!!))
                        .commit()
                true
            }
            else -> false
        }
    }

    private var vacancy: Vacancy? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_vacancy)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.setOnNavigationItemReselectedListener { }

        vacancy = intent?.getParcelableExtra("vacancy")
        if (vacancy == null) finish()
        else navigation.selectedItemId = R.id.navigation_search
    }
}
