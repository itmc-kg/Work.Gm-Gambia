package kg.jobs.app.ui.filter

import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import kg.jobs.app.R
import kg.jobs.app.customViews.RecyclerViewDivider
import kg.jobs.app.startActivity
import kg.jobs.app.toPx
import kg.jobs.app.toast
import kg.jobs.app.ui.MainActivity
import kg.jobs.app.ui.dialogs.LoadingDialog
import kotlinx.android.synthetic.main.activity_pre_filter.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class PreFilterActivity : AppCompatActivity() {
    private val viewModel by viewModel<PreFilterViewModel>()
    private val sphereAdapter = JobSphereAdapter()
    private val loadingDialog by lazy { LoadingDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_filter)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        collapsingToolbar?.title = getString(R.string.activity_filter_title)
        toolbar.setNavigationOnClickListener { finish() }

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
                PreFilterViewModel.Event.LOADING -> loadingDialog.show()
                PreFilterViewModel.Event.SUCCESS -> loadingDialog.dismiss()
                PreFilterViewModel.Event.ERROR -> loadingDialog.dismiss()
                PreFilterViewModel.Event.CLOSE -> {
                    loadingDialog.dismiss()
                    startActivity<MainActivity>()
                    finish()
                }
            }
        })
        viewModel.error.observe(this, Observer { toast(it!!, Toast.LENGTH_SHORT) })

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

        sphereAdapter.onChecked = viewModel::sphereChecked
        next.setOnClickListener { viewModel.save() }

        viewModel.init()
    }
}
