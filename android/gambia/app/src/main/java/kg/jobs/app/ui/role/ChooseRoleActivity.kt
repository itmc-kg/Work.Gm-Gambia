package kg.jobs.app.ui.role

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import kg.jobs.app.LangPref
import kg.jobs.app.R
import kg.jobs.app.repository.LocalPrefData
import kg.jobs.app.startActivity
import kg.jobs.app.toast
import kg.jobs.app.ui.MainActivity
import kg.jobs.app.ui.SplashActivity
import kg.jobs.app.ui.country.ChooseCountryActivity
import kg.jobs.app.ui.country.ChooseCountryViewModel
import kg.jobs.app.ui.dialogs.LoadingDialog
import kg.jobs.app.ui.employer.VacanciesActivity
import kg.jobs.app.ui.employer.edit.EditEmployerActivity
import kg.jobs.app.ui.lang.ChooseLanguageActivity
import kg.jobs.app.ui.profile.edit.EditProfileActivity
import kotlinx.android.synthetic.main.activity_choose_country.*
import kotlinx.android.synthetic.main.activity_choose_role.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChooseRoleActivity : AppCompatActivity() {
    private val viewModel: RoleViewModel by viewModel()
    private val viewModelChooseCountry: ChooseCountryViewModel by viewModel()

    private val loadingDialog by lazy { LoadingDialog(this) }
    private val pref by inject<LocalPrefData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_role)

        viewModel.state.observe(this, Observer {
            when (it) {
                RoleViewModel.State.CHECK_NETWORK -> {
                    loadingDialog.dismiss()
                    toast(getString(R.string.unknown_error))
                    finish()
                }
                RoleViewModel.State.CREATE_EMPLOYER -> {
                    loadingDialog.dismiss()
                    startActivity<EditEmployerActivity>(Intent.FLAG_ACTIVITY_CLEAR_TOP) {
                        putExtra("start", true)
                    }
                    finish()
                }
                RoleViewModel.State.OPEN_EMPLOYER -> {
                    loadingDialog.dismiss()
                    startActivity<VacanciesActivity>(Intent.FLAG_ACTIVITY_CLEAR_TOP) {
                        if (intent.hasExtra("chatId"))
                            putExtra("chatId", intent?.getStringExtra("chatId"))
                        if (intent.hasExtra("applicationId"))
                            putExtra("applicationId", intent?.getStringExtra("applicationId"))
                    }
                    finish()
                }
                RoleViewModel.State.CREATE_EMPLOYEE -> {
                    loadingDialog.dismiss()
                    startActivity<EditProfileActivity>(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    finish()
                }
                RoleViewModel.State.OPEN_EMPLOYEE -> {
                    loadingDialog.dismiss()
                    startActivity<MainActivity>(Intent.FLAG_ACTIVITY_CLEAR_TOP) {
                        if (intent.hasExtra("chatId"))
                            putExtra("chatId", intent?.getStringExtra("chatId"))
                        if (intent.hasExtra("applicationId"))
                            putExtra("applicationId", intent?.getStringExtra("applicationId"))
                    }
                    finish()
                }
                RoleViewModel.State.LOADING -> {
                    loadingDialog.show()
                }
                RoleViewModel.State.SELECT_ROLE -> {
                    loadingDialog.dismiss()
                    group.isVisible = true
                    bg.isVisible = false
                    if (!pref.hasCountry()) {
                        viewModelChooseCountry.selectGambia()
                    }else {
                        employee.setOnClickListener { viewModel.selectedEmployee() }
                        employer.setOnClickListener { viewModel.selectedEmployer() }
                    }
                }
            }
        })

        viewModelChooseCountry.selectedCountry.observe(this, Observer {
            when (it) {
                "KG" -> {
                    lang("ru")
                    employee.setOnClickListener { viewModel.selectedEmployee() }
                    employer.setOnClickListener { viewModel.selectedEmployer() }

                }
                "GM" -> {
                    lang("en")
                    employee.setOnClickListener { viewModel.selectedEmployee() }
                    employer.setOnClickListener { viewModel.selectedEmployer() }
                }
            }
        })


    }

    fun lang(language: String) {
        if (language != null) {
            LangPref(this).setLanguage(this, language)
            LangPref(this).setLanguageStart(this)
        }
    }

}
