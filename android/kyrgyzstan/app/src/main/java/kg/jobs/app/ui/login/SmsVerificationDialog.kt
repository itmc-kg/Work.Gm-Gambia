package kg.jobs.app.ui.login

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.os.Handler
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.sms_verification_dialog.*
import kg.jobs.app.R
import kg.jobs.app.toDp


class SmsVerificationDialog(context: Context, var viewModel: LoginViewModel) : BottomSheetDialog(context) {

    private var handler = Handler()
    private var seconds = 60

    fun builder(activity: Activity): SmsVerificationDialog {
        val view = layoutInflater.inflate(R.layout.sms_verification_dialog, null)
        setContentView(view)
        setDialogFullScreen(view, activity)
        init(activity)
        return this
    }

    fun init(activity: Activity) {
        this.account_description.text = context.getString(R.string.send_sms_description, viewModel.phone)

        next.setOnClickListener(onNextClickListener)
        sendSmsCode.setOnClickListener {
            viewModel.resentCode(activity)
            showError(context.getString(R.string.sms_code_send_your_phone))
            startTimer()
        }
    }

    fun showProgressbar(show: Boolean) {
        val shortAnimTime = context.resources.getInteger(android.R.integer.config_longAnimTime).toLong()

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

    fun setSmsCode(code: String?) {
        this.codeSms.setText(code)
    }

    private var onNextClickListener = View.OnClickListener {
        val code = this.codeSms.text.toString()
        if (TextUtils.isEmpty(code)) {
            showError(context.getString(R.string.invalid_code_number))
            return@OnClickListener
        }

        showProgressbar(true)
        viewModel.verification(code)
    }

    private fun setDialogFullScreen(view: View, activity: Activity) {
        val params = view.layoutParams
        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        view.measure(0, 0)
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels
        bottomSheetBehavior.peekHeight = screenHeight
        params.height = screenHeight - 25.toDp
        view.layoutParams = params
    }

    private fun showError(error: String) {
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }

    private val runnable = object : Runnable {
        override fun run() {
            seconds--
            if (seconds == 0) {
                sendSmsCode.text = context.getString(R.string.sms_timer, "0")
                stopTimer()
                return
            }
            sendSmsCode.text = context.getString(R.string.sms_timer,
                    if (seconds > 9) "$seconds" else "0$seconds")
            handler.postDelayed(this, 1000)
        }
    }

    private fun stopTimer() {
        handler.removeCallbacks(runnable)
        seconds = 60
        sendSmsCode.isClickable = true
        sendSmsCode.text = context.getString(R.string.resent_code_sms)
    }

    private fun startTimer() {
        handler.postDelayed(runnable, 100)
        sendSmsCode.isClickable = false
    }

    override fun dismiss() {
        super.dismiss()
        stopTimer()
    }

}