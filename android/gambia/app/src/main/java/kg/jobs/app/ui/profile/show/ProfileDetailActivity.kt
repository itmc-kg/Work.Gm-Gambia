package kg.jobs.app.ui.profile.show

import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.core.view.isVisible
import kg.jobs.app.R
import kg.jobs.app.customViews.NestedScrollToolbarElevation
import kg.jobs.app.startActivity
import kg.jobs.app.toast
import kg.jobs.app.ui.dialogs.LoadingDialog
import kg.jobs.app.ui.profile.edit.EditProfileActivity
import kotlinx.android.synthetic.main.activity_profile_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ProfileDetailActivity : AppCompatActivity() {
    private val viewModel: ProfileDetailViewModel by viewModel {
        parametersOf(intent.getStringExtra("employee_id") ?: "")
    }

    private val eduAdapter = ShowEducationAdapter()
    private val workAdapter = ShowWorkAdapter()

    private val loadingDialog by lazy { LoadingDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        nestedScrollView.setOnScrollChangeListener(NestedScrollToolbarElevation(appbar))
        supportActionBar?.title = ""

        edu_recyclerView.apply {
            adapter = eduAdapter
            isNestedScrollingEnabled = false
        }

        work_recyclerView.apply {
            adapter = workAdapter
            isNestedScrollingEnabled = false
        }

        viewModel.title.observe(this, Observer {
//            supportActionBar?.title = getString(R.string.employee_text)
            supportActionBar?.title = ""

        })
        viewModel.state.observe(this, Observer {
            when (it) {
                ProfileDetailViewModel.State.DATA -> {
                    loadingDialog.dismiss()
                }
                ProfileDetailViewModel.State.LOADING -> {
                    loadingDialog.show()
                }
            }
        })
        viewModel.user.observe(this, Observer {
            name.text = it?.name
            avatar.setImageURI(it?.image)
        })

        viewModel.educations.observe(this, Observer(eduAdapter::update))
        viewModel.works.observe(this, Observer(workAdapter::update))
        viewModel.error.observe(this, Observer { text ->
            toast(text!!, Toast.LENGTH_SHORT)
        })
        viewModel.isMyProfile.observe(this, Observer { isVisible ->
            edit_btn_container.isVisible = isVisible ?: false
        })

        edit_btn.setOnClickListener {
            startActivity<EditProfileActivity> {
                putExtra("from_profile", true)
            }
        }

        viewModel.init()

    }
}
