package kg.jobs.app.ui.employer.vacancy.detail.applicationDetail

import android.app.Activity
import androidx.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kg.jobs.app.R
import kg.jobs.app.customViews.NestedScrollToolbarElevation
import kg.jobs.app.model.Application
import kg.jobs.app.toast
import kg.jobs.app.ui.dialogs.LoadingDialog
import kg.jobs.app.ui.messenger.ChatActivity
import kg.jobs.app.ui.profile.show.ShowEducationAdapter
import kg.jobs.app.ui.profile.show.ShowWorkAdapter
import kotlinx.android.synthetic.main.activity_application_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ApplicationDetailActivity : AppCompatActivity() {

    private val viewModel: ApplicationDetailViewModel by viewModel()

    private val loadingDialog by lazy { LoadingDialog(this) }
    private val application by lazy { intent?.getParcelableExtra<Application>("application") }
    private val eduAdapter = ShowEducationAdapter()
    private val workAdapter = ShowWorkAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application_detail)
        setSupportActionBar(toolbar)
//        supportActionBar?.title = getString(R.string.employee_title)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        nestedScrollView.setOnScrollChangeListener(NestedScrollToolbarElevation(appbar))

        edu_recyclerView.apply {
            adapter = eduAdapter
            isNestedScrollingEnabled = false
        }

        work_recyclerView.apply {
            adapter = workAdapter
            isNestedScrollingEnabled = false
        }

        viewModel.application.observe(this, Observer {
            it?.apply {
                eduAdapter.update(author?.educationHistory)
                workAdapter.update(author?.workHistory)
                name.text = author?.name
                avatar.setImageURI(author?.image)
                when (status) {
                    "created" -> {
                        left_btn.isVisible = true
                        right_btn.text = getString(R.string.accept)
                        right_btn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_done_white_24dp,
                                0, 0)
                    }
                    "accepted" -> {
                        left_btn.isVisible = true
                        right_btn.text = getString(R.string.start_chat)
                        right_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    }
                    "rejected" -> {
                        right_btn.text = getString(R.string.accept)
                        right_btn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_done_white_24dp,
                                0, 0)
                        left_btn.isVisible = false
                    }
                }
            }
        })
        viewModel.applicationState.observe(this, Observer { state ->
            when (state) {
                ApplicationDetailViewModel.State.LOADING -> {
                    loadingDialog.show()
                }
                ApplicationDetailViewModel.State.ACCEPTED -> {
                    loadingDialog.dismiss()
                    setResult(Activity.RESULT_OK, Intent().putExtra("application", application)
                            .putExtra("action", "accept"))
                }
                ApplicationDetailViewModel.State.REJECTED -> {
                    loadingDialog.dismiss()
                    setResult(Activity.RESULT_OK, Intent().putExtra("application", application)
                            .putExtra("action", "reject"))
                    finish()
                }
                ApplicationDetailViewModel.State.ERROR -> {
                    loadingDialog.dismiss()
                    toast(getString(R.string.unknown_error))
                }
                ApplicationDetailViewModel.State.SUCCESS -> {
                    loadingDialog.dismiss()
                }
                ApplicationDetailViewModel.State.CLOSE -> finish()
            }
        })

        viewModel.openChat.observe(this, Observer { ChatActivity.start(this, it) })

        right_btn.setOnClickListener { viewModel.acceptOrChat() }
        left_btn.setOnClickListener {
            AlertDialog.Builder(this)
                    .setMessage(getString(R.string.dialog_reject_text))
                    .setPositiveButton(getString(R.string.reject)) { dialog, _ ->
                        viewModel.reject()
                        dialog.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                    .show()
        }

        viewModel.init(application)
    }
}
