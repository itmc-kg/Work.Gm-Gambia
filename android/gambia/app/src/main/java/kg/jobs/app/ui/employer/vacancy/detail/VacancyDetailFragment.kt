package kg.jobs.app.ui.employer.vacancy.detail

import androidx.lifecycle.Observer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import kg.jobs.app.R
import kg.jobs.app.customViews.NestedScrollToolbarElevation
import kg.jobs.app.model.EmploymentType
import kg.jobs.app.model.Schedule
import kg.jobs.app.model.Vacancy
import kg.jobs.app.startActivity
import kg.jobs.app.toast
import kg.jobs.app.ui.dialogs.LoadingDialog
import kg.jobs.app.ui.vacancy.EditVacancyActivity
import kotlinx.android.synthetic.main.fragment_vacancy_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VacancyDetailFragment : Fragment() {
    companion object {
        fun create(vacancy: Vacancy): Fragment {
            return VacancyDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("vacancy", vacancy)
                }
            }
        }
    }

    private val viewModel by viewModel<OwnerVacancyDetailViewModel>()
    private val loadingDialog by lazy { LoadingDialog(activity!!) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vacancy_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationOnClickListener { finish() }
            supportActionBar?.title = ""
            nestedScrollView.setOnScrollChangeListener(NestedScrollToolbarElevation(appbar))
        }

        viewModel.vacancy.observe(this, Observer { vacancy ->
            vacancy?.apply {
                company_name.text = employer?.name
                city_name.text = employer?.city
                work_sphere.text = sphere.name
                work_position.text = position
                employment_type_name.text = getEmploymentTypes()
                        .find { it.id == vacancy.employmentTypeId }?.name ?: ""
                schdule_name.text = getSchedules().find { it.id == vacancy.scheduleId }?.name ?: ""
                vacancy_text.text = description
                about_company.text = employer?.about
                salary_text.text = salary.text(context!!)
                if (salary.id == "undefined") salary_desc.isVisible = false
                salary_desc.text = getString(R.string.after_interview)
            }
        })
        viewModel.error.observe(this, Observer { activity!!.toast(it!!, Toast.LENGTH_SHORT) })
        viewModel.state.observe(this, Observer {
            when (it) {
                OwnerVacancyDetailViewModel.State.LOADING -> loadingDialog.show()
                OwnerVacancyDetailViewModel.State.SUCCESS,
                OwnerVacancyDetailViewModel.State.FAIL -> loadingDialog.dismiss()
            }
        })

        vacancy_edit.setOnClickListener {
            viewModel.vacancy.value?.also { vacancy ->
                activity?.startActivity<EditVacancyActivity> {
                    putExtra("vacancy", vacancy)
                }
            }
        }

        viewModel.init(arguments!!.get("vacancy") as Vacancy)
    }

    private fun getSchedules(): ArrayList<Schedule> {
        return ArrayList(resources?.getStringArray(R.array.schedules)
                ?.mapIndexed { index, s -> Schedule(index, s) })
    }

    private fun getEmploymentTypes(): ArrayList<EmploymentType> {
        val list = context?.resources?.getStringArray(R.array.employment_types)

        if (list != null) {
            for (employer in list){
                Log.e("TAG", employer)
            }
        }

        return ArrayList(resources?.getStringArray(R.array.employment_types)
                ?.mapIndexed { index, s -> EmploymentType(index, s) })
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingDialog.dismiss()
    }
}