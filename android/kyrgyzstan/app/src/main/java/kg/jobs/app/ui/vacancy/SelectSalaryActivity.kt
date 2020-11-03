package kg.jobs.app.ui.vacancy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isVisible
import kg.jobs.app.R
import kg.jobs.app.customViews.RangeSeekBar
import kg.jobs.app.model.Salary
import kotlinx.android.synthetic.main.activity_select_salary.*

class SelectSalaryActivity : AppCompatActivity() {

    private lateinit var ids: List<String>

    private var salary = Salary()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_salary)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.title = ""

        ids = resources.getStringArray(R.array.salary_type_ids).toList()
        salary = intent?.getParcelableExtra("salary") ?: Salary()
        rangeSeekBar.setMinThumbValue(salary.start)
        rangeSeekBar.setMaxThumbValue(salary.end)

        val adapter = SelectAdapter(resources.getStringArray(R.array.salary_types).toList())
        type_recyclerView.adapter = adapter
        adapter.onSelect = { position ->
            salary = salary.copy(id = ids[position])
            showRangeText()
            when (position) {
                0 -> {
                    group.isVisible = true
                    rangeSeekBar.disableRange(true)
                }
                1 -> {
                    group.isVisible = true
                    rangeSeekBar.disableRange(false)
                }
                2 -> group.isVisible = false
            }
        }
        adapter.selectedPosition = ids.indexOfFirst { it == salary.id }
        showRangeText()

        rangeSeekBar.seekBarChangeListener = object : RangeSeekBar.SeekBarChangeListener {
            override fun onStartedSeeking() {
            }

            override fun onStoppedSeeking() {
            }

            override fun onValueChanged(minThumbValue: Int, maxThumbValue: Int) {
                salary = salary.copy(start = minThumbValue, end = maxThumbValue)
                showRangeText()
            }
        }
    }

    fun showRangeText() {
        salary_text.text = if (salary.id == ids[0]) "от ${salary.start * 1000}"
        else "${salary.start * 1000} - ${salary.end * 1000}"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, Menu.FIRST, 0, getString(R.string.action_done))?.apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setIcon(R.drawable.ic_done_black_24dp)
            setOnMenuItemClickListener {
                setResult(Activity.RESULT_OK, Intent().putExtra("salary", salary))
                finish()
                true
            }
        }
        return super.onCreateOptionsMenu(menu)
    }
}
