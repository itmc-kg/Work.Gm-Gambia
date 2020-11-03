package kg.jobs.app.ui.login

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import androidx.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import kg.jobs.app.R
import kg.jobs.app.hasInternet
import kg.jobs.app.model.Country
import kg.jobs.app.model.VerificationSms
import kg.jobs.app.ui.role.ChooseRoleActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModel()
    private var smsVerificationDialog: SmsVerificationDialog? = null
    private var countries: Map<String, Country> = mutableMapOf()
    private var country: Country? = null
    private var ignoreTextChange = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()
    }

    private fun init() {
        viewModel.events.observe(this, Observer {
            val (key, value) = it!!
            when (key) {
                LoginViewModel.OK -> {
                    startRoleActivity()
                    smsVerificationDialog?.showProgressbar(false)
                }

                LoginViewModel.LOADING -> {
                    showProgressbar(true)
                    hideKeyboard()
                }

                LoginViewModel.AUTH_ERROR -> {
                    viewModel.launch {
                        if (hasInternet())
                            showError(value as String)
                        else showError(resources.getString(R.string.error_no_internet))
                    }
                    smsVerificationDialog?.showProgressbar(false)
                    showProgressbar(false)
                }

                LoginViewModel.EMPTY_LIST -> {

                }

                LoginViewModel.FB_DB_ERROR -> {
                    showError(value.toString())
                    showProgressbar(false)
                }

                "showSmsDialog" -> {
                    if (smsVerificationDialog == null) startSmsVerification()
                    else smsVerificationDialog?.show()
                    showProgressbar(false)
                }

                "changed" -> {
                    smsVerificationDialog?.setSmsCode((value as VerificationSms).code)
                }
            }
        })


        viewModel.countries.observe(this, Observer {
            countries = it!!
            getUserIsoCode()?.let { code ->
                val result = countries.filter {
                    it.value.isoString == code
                }.iterator()

                if (result.hasNext()) {
                    this@LoginActivity.country = result.next().value
                    ignoreTextChange = true
                    initFields()
                }
            }
        })

        start.setOnClickListener(onNextClickListener)
        phoneField.setOnEditorActionListener(onEditorActionListener)
        phoneField.charRepresentation = '-'

        codeField.setOnClickListener {
            val intent = Intent(this, CountriesActivity::class.java)
            startActivityForResult(intent, 123)
        }
    }

    private fun initFields() {
        codeField.setText(country?.isoNumeric)
        phoneField.mask = country!!.mask
        phoneField.requestFocus()
        codeField.requestLayout()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 123 && data != null) {
            this@LoginActivity.country = data.getParcelableExtra("country")
            ignoreTextChange = true
            initFields()
        }
    }

    private fun getUserIsoCode(): String? {
        val tManager = applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        if (tManager != null) {
            return tManager.simCountryIso.toUpperCase()
        }
        return null
    }

    private var onEditorActionListener = TextView.OnEditorActionListener { view, id, _ ->
        if (id == EditorInfo.IME_ACTION_DONE) {
            onNextClickListener.onClick(view)
            return@OnEditorActionListener true
        }
        return@OnEditorActionListener false
    }

    private var onNextClickListener = View.OnClickListener {
        val phone = this.phoneField.text.toString()
        if (TextUtils.isEmpty(phone) || phone.contains("-")) {
            showError(getString(R.string.invalid_phone_number))
            return@OnClickListener
        }
        if (country != null) {
            viewModel.auth(country!!.isoNumeric + phone, this)
        }
    }

    private fun startSmsVerification() {
        smsVerificationDialog = SmsVerificationDialog(this, viewModel).builder(this)
        smsVerificationDialog!!.show()
    }

    private fun startRoleActivity() {
        smsVerificationDialog?.dismiss()
        hideKeyboard()
        val intent = Intent(this, ChooseRoleActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun showProgressbar(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_longAnimTime).toLong()

        root.visibility = if (show) View.GONE else View.VISIBLE
        root.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        root.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })
        progressbar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun hideKeyboard() {
        if (this.currentFocus != null) {
            val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(this.currentFocus.windowToken, 0)
        }
    }

    private fun showError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }
}