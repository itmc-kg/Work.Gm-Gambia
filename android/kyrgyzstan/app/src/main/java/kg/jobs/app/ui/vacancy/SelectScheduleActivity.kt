package kg.jobs.app.ui.vacancy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kg.jobs.app.LangPref
import kg.jobs.app.MyContextWrapper
import kg.jobs.app.R
import kg.jobs.app.model.EmploymentType
import kg.jobs.app.model.Schedule
import kotlinx.android.synthetic.main.activity_select_schedule.*

class SelectScheduleActivity : AppCompatActivity() {

    private lateinit var scheduleAdapter: SelectAdapter
    private lateinit var employmentAdapter: SelectAdapter

    private lateinit var schedules: List<Schedule>

    private lateinit var employmentTypes: List<EmploymentType>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_schedule)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        collapsingToolbar.title = getString(R.string.employment_type_title)
        toolbar.setNavigationOnClickListener { finish() }

        Log.e("RESSS", resources.getStringArray(R.array.schedules)[1])

        schedules = intent?.getParcelableArrayListExtra("schedules") ?: listOf()
        employmentTypes = intent?.getParcelableArrayListExtra("employment_types") ?: listOf()

        employmentAdapter = SelectAdapter(employmentTypes.map { it.name })
        employment_recyclerView.adapter = employmentAdapter
        employmentTypes.indexOfFirst {
            it.id == intent?.getParcelableExtra<EmploymentType>("default_employment_type")?.id
        }.takeIf { it >= 0 }?.let { employmentAdapter.selectedPosition = it }

        scheduleAdapter = SelectAdapter(schedules.map { it.name })
        schedule_recyclerView.adapter = scheduleAdapter
        schedules.indexOfFirst {
            it.id == intent?.getParcelableExtra<Schedule>("default_schedule")?.id
        }.takeIf { it >= 0 }?.let { scheduleAdapter.selectedPosition = it }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, Menu.FIRST, 0, getString(R.string.action_done))?.apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setIcon(R.drawable.ic_done_black_24dp)
            setOnMenuItemClickListener {
                setResult(Activity.RESULT_OK, Intent()
                        .putExtra("schedule", schedules[scheduleAdapter.selectedPosition])
                        .putExtra("employment_type", employmentTypes[employmentAdapter.selectedPosition])
                )
                finish()
                true
            }
        }
        return super.onCreateOptionsMenu(menu)
    }


}
