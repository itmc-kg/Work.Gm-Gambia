package kg.jobs.app.ui.filter

import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kg.jobs.app.R
import kg.jobs.app.customViews.RangeSeekBar
import kg.jobs.app.customViews.RecyclerViewDivider
import kg.jobs.app.startActivity
import kg.jobs.app.toPx
import kg.jobs.app.toast
import kg.jobs.app.ui.MainActivity
import kg.jobs.app.ui.dialogs.LoadingDialog
import kotlinx.android.synthetic.main.activity_filter.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class FilterActivity : AppCompatActivity() {
    private val viewModel by viewModel<FilterViewModel>()
    private val sphereAdapter = JobSphereAdapter()
    private val loadingDialog by lazy { LoadingDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        collapsingToolbar?.title = getString(R.string.activity_filter_title)
        toolbar.setNavigationOnClickListener { finish() }

        rangeSeekBar.disableRange(true)
        sphere_recyclerView?.apply {
            adapter = sphereAdapter
            addItemDecoration(RecyclerViewDivider.builder(context)
                    .leftMarginPx(56.toPx)
                    .color(R.color.mid_grey_2, R.dimen.divider)
                    .build())
            isNestedScrollingEnabled = false
        }
        viewModel.events.observe(this, Observer {
            when (it) {
                FilterViewModel.Event.LOADING -> loadingDialog.show()
                FilterViewModel.Event.SUCCESS -> loadingDialog.dismiss()
                FilterViewModel.Event.ERROR -> loadingDialog.dismiss()
                FilterViewModel.Event.CLOSE -> {
                    loadingDialog.dismiss()
                    startActivity<MainActivity> {
                        putExtra("refresh_vacancies", true)
                    }
                    finish()
                }
            }
        })
        viewModel.error.observe(this, Observer { toast(it!!, Toast.LENGTH_SHORT) })

        viewModel.salary.observe(this, Observer {
            salary_text.text = String.format(getString(R.string.range_from), it!! * 1000)
            rangeSeekBar.setMinThumbValue(it)
        })
        viewModel.jobSpheres.observe(this, Observer(sphereAdapter::update))
        viewModel.sphereCheckedCount.observe(this, Observer {
            sphere_count.text = String.format(getString(R.string.selected_sphere_count), it)
        })
        viewModel.selectedRegion.observe(this, Observer(region::setText))
        viewModel.regions.observe(this, Observer { regions ->
            val selectRegionDialog = AlertDialog.Builder(this)
                    .setItems(regions!!.map { it.name }.toTypedArray()) { dialog, which ->
                        viewModel.selectRegion(regions[which])
                        dialog.dismiss()
                    }
                    .create()
            region.setOnClickListener { selectRegionDialog.show() }
        })
        rangeSeekBar.seekBarChangeListener = object : RangeSeekBar.SeekBarChangeListener {
            override fun onStartedSeeking() {
            }

            override fun onStoppedSeeking() {
            }

            override fun onValueChanged(minThumbValue: Int, maxThumbValue: Int) {
                viewModel.setSalary(minThumbValue)
            }
        }
        sphereAdapter.onChecked = viewModel::sphereChecked

        viewModel.init()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, Menu.FIRST, 0, getString(R.string.action_done))?.apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setIcon(R.drawable.ic_done_black_24dp)
            setOnMenuItemClickListener {
                viewModel.save()
                true
            }
        }
        return super.onCreateOptionsMenu(menu)
    }
}
