package kg.jobs.app.ui.search.show

import android.app.Activity
import android.content.Context
import androidx.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kg.jobs.app.R
import kg.jobs.app.customViews.NestedScrollToolbarElevation
import kg.jobs.app.model.Application
import kg.jobs.app.model.EmploymentType
import kg.jobs.app.model.Schedule
import kg.jobs.app.model.Vacancy
import kg.jobs.app.setBackgroundColor
import kg.jobs.app.startActivity
import kg.jobs.app.toast
import kg.jobs.app.ui.dialogs.LoadingDialog
import kg.jobs.app.ui.employer.show.EmployerDetailActivity
import kg.jobs.app.ui.messenger.ChatActivity
import kotlinx.android.synthetic.main.activity_vacancy_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VacancyDetailActivity : AppCompatActivity() {

    private val viewModel: VacancyDetailViewModel by viewModel()

    private val loadingDialog by lazy { LoadingDialog(this) }
    private val vacancy by lazy { intent?.getParcelableExtra<Vacancy>("vacancy") }
    private val application by lazy { intent?.getParcelableExtra<Application>("application") }
    var vacancyInfo: Vacancy? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vacancy_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        nestedScrollView.setOnScrollChangeListener(NestedScrollToolbarElevation(appbar))

        viewModel.vacancy.observe(this, Observer {
            vacancyInfo = it
            it?.apply {
                company_name.text = employer?.name
                avatar.setImageURI(employer?.image)
                city_name.text = employer?.city
                about_company.text = employer?.about
                work_sphere.text = sphere.name
                work_position.text = position
                employment_type_name.text = getEmploymentTypes(this@VacancyDetailActivity)
                        .find { it.id == vacancyInfo?.employmentTypeId }?.name ?: ""
                schdule_name.text = getSchedules(this@VacancyDetailActivity).find { it.id == vacancyInfo?.scheduleId }?.name ?: ""
                vacancy_text.text = description
                salary_text.text = salary.text(this@VacancyDetailActivity)
                salary_desc.text = getString(R.string.after_interview)
                if (application != null) {
                    vacancy_apply.isVisible = false
                    vacancy_decline.isVisible = false
                    start_chat.isVisible = true
                    when (application.status) {
                        "accepted" -> {
                            start_chat.isEnabled = true
                            start_chat.setBackgroundColor("#46D17E")
                            start_chat.setText(R.string.start_chat)
                            start_chat.setOnClickListener { viewModel.startChat() }
                        }
                        "rejected" -> {
                            start_chat.isEnabled = false
                            start_chat.setBackgroundColor("#FC5474")
                            start_chat.setText(R.string.rejected)
                        }
                        "created" -> {
                            start_chat.isEnabled = false
                            start_chat.setText(R.string.waiting)
                            start_chat.setBackgroundColor("#DCDFE6")
                        }
                    }
                } else {
                    vacancy_apply.isVisible = true
                    vacancy_decline.isVisible = true
                    start_chat.isVisible = false
                }
            }
        })
        viewModel.state.observe(this, Observer { state ->
            when (state) {
                VacancyDetailViewModel.State.LOADING -> {
                    loadingDialog.show()
                }
                VacancyDetailViewModel.State.DATA -> loadingDialog.dismiss()
                VacancyDetailViewModel.State.ACCEPT -> {
                    loadingDialog.dismiss()
                    setResult(Activity.RESULT_OK, Intent().putExtra("vacancy", vacancy)
                            .putExtra("action", "accept"))
                    finish()
                }
                VacancyDetailViewModel.State.DECLINE -> {
                    loadingDialog.dismiss()
                    setResult(Activity.RESULT_OK, Intent().putExtra("vacancy", vacancy)
                            .putExtra("action", "decline"))
                    finish()
                }
                VacancyDetailViewModel.State.ERROR -> {
                    loadingDialog.dismiss()
                    toast(getString(R.string.unknown_error))
                }
                VacancyDetailViewModel.State.CLOSE -> finish()
            }
        })
        viewModel.openChat.observe(this, Observer { ChatActivity.start(this, it) })

        vacancy_apply.setOnClickListener { viewModel.apply() }

        avatar.setOnClickListener {
            startActivity<EmployerDetailActivity> {
                putExtra("employer_id", vacancyInfo?.employer?.id)
            }
        }

        vacancy_decline.setOnClickListener { viewModel.decline() }
        if (application != null) viewModel.init(application!!)
        else viewModel.init(vacancy)
    }

    fun getSchedules(context: Context): ArrayList<Schedule> {
        return ArrayList(context.resources?.getStringArray(R.array.schedules)
                ?.mapIndexed { index, s -> Schedule(index, s) })
    }

    private fun getEmploymentTypes(context: Context): ArrayList<EmploymentType> {
        val list = context?.resources?.getStringArray(R.array.employment_types)

        if (list != null) {
            for (employer in list){
                Log.e("TAG", employer)
            }
        }

        return ArrayList(context.resources?.getStringArray(R.array.employment_types)
                ?.mapIndexed { index, s -> EmploymentType(index, s) })
    }
}
