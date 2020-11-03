package kg.jobs.app.ui.employer.show

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import kg.jobs.app.R
import kg.jobs.app.customViews.NestedScrollToolbarElevation
import kg.jobs.app.startActivity
import kg.jobs.app.ui.dialogs.LoadingDialog
import kg.jobs.app.ui.employer.edit.EditEmployerActivity
import kotlinx.android.synthetic.main.activity_employer_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class EmployerDetailActivity : AppCompatActivity() {
    private val viewModel by viewModel<EmployerDetailViewModel> {
        parametersOf(intent.getStringExtra("employer_id") ?: "")
    }
    private val loadingDialog by lazy { LoadingDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employer_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        toolbar.setNavigationOnClickListener { finish() }
        nestedScrollView.setOnScrollChangeListener(NestedScrollToolbarElevation(appbar))

        viewModel.title.observe(this, Observer {
            supportActionBar?.title = ""
        })

        viewModel.state.observe(this, Observer {
            when (it) {
                EmployerDetailViewModel.State.DATA -> loadingDialog.dismiss()
                EmployerDetailViewModel.State.LOADING -> loadingDialog.show()
            }
        })

        viewModel.employer.observe(this, Observer {
            it?.apply {
                city_name.text = city
                company_name.text = name
                about_company.text = about
                avatar.setImageURI(image)
            }
        })

        viewModel.editButtonEnable.observe(this, Observer {
            edit_btn_container.isVisible = it ?: false
            edit_btn.setOnClickListener { startActivity<EditEmployerActivity>() }
        })

        viewModel.init()
    }
}
