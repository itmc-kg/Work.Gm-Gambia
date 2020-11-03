package kg.jobs.app.ui.vacancy

import android.app.Activity
import android.content.Context
import androidx.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import kg.jobs.app.*
import kg.jobs.app.model.*
import kg.jobs.app.ui.dialogs.LoadingDialog
import kg.jobs.app.ui.employer.VacanciesActivity
import kotlinx.android.synthetic.main.activity_edit_vacancy.*
import kotlinx.android.synthetic.main.activity_edit_vacancy.collapsingToolbar
import kotlinx.android.synthetic.main.activity_edit_vacancy.toolbar
import kotlinx.android.synthetic.main.activity_settings.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditVacancyActivity : AppCompatActivity() {
    private val SELECT_SPHERE = 1000
    private val SELECT_SCHEDULE = 1001
    private val SELECT_SALARY = 1002

    private val viewModel: EditVacancyViewModel by viewModel()
    private val loadingDialog by lazy { LoadingDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_vacancy)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        collapsingToolbar.title = getString(R.string.new_vacancy)

        toolbar.setNavigationOnClickListener { finish() }
        intent?.getParcelableExtra<Vacancy>("vacancy")?.also { vacancy ->
            viewModel.editMode(vacancy)
            next.setText(R.string.save)
            collapsingToolbar.title = getString(R.string.edit_vacancy)
        }
//        viewModel.title.observe(this, Observer(collapsingToolbar::setTitle))
        viewModel.sphere.observe(this, Observer {
            sphere_value.text = it?.name
        })
        viewModel.schedule.observe(this, Observer(schedule_value::setText))
        viewModel.error.observe(this, Observer {
            if (it == "fill_all"){
                toast(getString(R.string.fill_all_fields), Toast.LENGTH_SHORT)
            }else
            toast(it!!, Toast.LENGTH_SHORT)
        })
        viewModel.events.observe(this, Observer {
            when (it) {
                EditVacancyViewModel.Event.ERROR,
                EditVacancyViewModel.Event.SUCCESS -> loadingDialog.dismiss()
                EditVacancyViewModel.Event.LOADING -> loadingDialog.show()
                EditVacancyViewModel.Event.CLOSE -> {
                    startActivity<VacanciesActivity>()
                    finish()
                }
            }
        })
        viewModel.vacancy.observe(this, Observer {
            name.setText(it?.position)
            description.setText(it?.description)
        })
        viewModel.salaryText.observe(this, Observer(salary_value::setText))

        sphere_title.setOnClickListener {
            startActivityForResult<SelectSphereActivity>(SELECT_SPHERE) {
                putExtra("job_sphere", viewModel.sphere.value)
            }
        }
        schedule_title.setOnClickListener {
            startActivityForResult<SelectScheduleActivity>(SELECT_SCHEDULE) {
                putExtra("schedules", getSchedules())
                putExtra("employment_types", getEmploymentTypes())
                putExtra("default_schedule", viewModel.selectedSchedule)
                putExtra("default_employment_type", viewModel.selectedEmploymentType)
            }
        }
        salary_title.setOnClickListener { _ ->
            startActivityForResult<SelectSalaryActivity>(SELECT_SALARY) {
//                viewModel.salary.observe(this, Observer {
//                    Log.e("SALARY", it)
//                })
                viewModel.salary.value?.let { putExtra("salary", it) }
            }
        }
        next.setOnClickListener {
            viewModel.save(name.text.toString(), description.text.toString())
        }
    }

    fun getSchedules(): ArrayList<Schedule> {
        return ArrayList(resources.getStringArray(R.array.schedules)
                .mapIndexed { index, s -> Schedule(index, s) })
    }
    fun getEmploymentTypes(): ArrayList<EmploymentType> {
        val list = resources.getStringArray(R.array.employment_types)

        for (employer in list){
            Log.e("TAG", employer)
        }

        return ArrayList(resources.getStringArray(R.array.employment_types)
                .mapIndexed { index, s -> EmploymentType(index, s) })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                SELECT_SPHERE ->
                    data?.getParcelableExtra<JobSphere>("job_sphere")?.let(viewModel::setSphere)
                SELECT_SCHEDULE -> {
                    data?.apply {
                        getParcelableExtra<Schedule>("schedule")?.let(viewModel::setSchedule)
                        getParcelableExtra<EmploymentType>("employment_type")?.let(viewModel::setEmploymentType)
                    }
                }
                SELECT_SALARY -> {
                    data?.apply {
                        getParcelableExtra<Salary>("salary")?.let(viewModel::setSalary)
                    }
                }
            }
    }

//    override fun attachBaseContext(newBase: Context?) {
//        super.attachBaseContext(MyContextWrapper.wrap(newBase, LangPref.getLanguge(this)))
//    }

}
