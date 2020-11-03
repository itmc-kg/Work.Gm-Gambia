package kg.jobs.app

import android.annotation.TargetApi
import android.app.Activity
import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import androidx.core.content.FileProvider
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.google.android.gms.tasks.Task
import kg.jobs.app.repository.LocalPrefData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Created by kani on 1/4/18.
 */

fun timestamp() = System.currentTimeMillis()

fun Context.toast(message: String, duration: Int = Toast.LENGTH_LONG): Toast =
        Toast.makeText(this, message, duration).apply {
            show()
        }

fun Context.getCaptureImageOutputUri(fileName: String): Uri? {
    val dir = File("")
    if (dir.exists())
        return Uri.fromFile(File(dir, fileName))
    else {
        Crashlytics.logException(
                IOException("android.content.Context.getExternalCacheDir() does not exist"))
    }
    return null
}

fun Context.takePictureIntent(fileName: String): Intent {
    var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

    val imageUri = this.getCaptureImageOutputUri(fileName)
    if (imageUri != null) {
        val image = File(imageUri.path)
        if (Build.VERSION.SDK_INT >= 24) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    FileProvider.getUriForFile(this,
                            BuildConfig.APPLICATION_ID + ".provider",
                            image))
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

    } else intent = Intent()
    return intent
}

suspend fun hasInternet(): Boolean {
    return try {
        withContext(Dispatchers.Default) { InetAddress.getByName("google.com") }
        true
    } catch (e: UnknownHostException) {
        false
    }
}

val Int.toDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.toPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val displayHeight = Resources.getSystem().displayMetrics.heightPixels
val displayWidth = Resources.getSystem().displayMetrics.widthPixels


inline fun <reified T : Activity> Activity.startActivityForResult(requestCode: Int, action: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    intent.action()
    startActivityForResult(intent, requestCode)
}

inline fun <reified T : Activity> Fragment.startActivityForResult(requestCode: Int, action: Intent.() -> Unit = {}) {
    val intent = Intent(activity, T::class.java)
    intent.action()
    startActivityForResult(intent, requestCode)
}

inline fun <reified T : Activity> Context.startActivity(vararg flags: Int, action: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    flags.forEach { intent.addFlags(it) }
    intent.action()
    startActivity(intent)
}

inline fun <reified T : Activity> Fragment.startActivity(vararg flags: Int, action: Intent.() -> Unit = {}) {
    val intent = Intent(activity, T::class.java)
    flags.forEach { intent.addFlags(it) }
    intent.action()
    startActivity(intent)
}

inline fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

fun View.setBackgroundColor(color: String) {
    val bg = background
    try {
        when (bg) {
            is GradientDrawable -> bg.setColor(Color.parseColor(color))
            is ShapeDrawable -> bg.paint.color = Color.parseColor(color)
            is ColorDrawable -> bg.color = Color.parseColor(color)
        }
    } catch (e: Exception) {

    }
}

inline fun Date?.parse(format: String): String {
    return if (this != null) SimpleDateFormat(format, Locale.getDefault()).format(this)
    else ""
}

inline fun Date.humanizedDate(context: Context): String {
    return when {
        DateUtils.isToday(time) -> context.getString(R.string.today)
        else -> SimpleDateFormat("d/MM/yyyy", Locale.getDefault()).format(this)
    }
}

suspend fun <T> Task<T?>.await(): Task<T?> {
    return withContext(Dispatchers.Default) {
        suspendCancellableCoroutine<Task<T?>> { continuation ->
            addOnCompleteListener {
                continuation.resume(it)
            }
        }
    }
}

suspend fun <T> Task<T?>.awaitWithException(): T? {
    return withContext(Dispatchers.Default) {
        suspendCancellableCoroutine<T?> { continuation ->
            addOnCompleteListener {
                if (it.isSuccessful)
                    continuation.resume(it.result)
                else {
                    Log.e("Error", it.exception.toString())
                    continuation.resumeWithException(it.exception ?: Exception("unknown"))
                }
            }
        }
    }
}


fun updateLocale(context: Context): Context {
    val localPrefData = LocalPrefData(context)
//    val language = when (localPrefData.country()) {
//        "KG" -> "ru"
//        else -> "en"
//    }
    val language  = LangPref(context).getLang(context)
    val locale = Locale(language)
    Locale.setDefault(locale)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        updateResourcesLocale(context, locale)
    } else
    return if (localPrefData.hasCountry()) updateResourcesLocaleLegacy(context, locale)
    else context
}

@TargetApi(Build.VERSION_CODES.N)
private fun updateResourcesLocale(context: Context, locale: Locale): Context {
    val configuration = context.resources.configuration
    configuration.setLocale(locale)
    return context.createConfigurationContext(configuration)
}


private fun updateResourcesLocaleLegacy(context: Context, locale: Locale): Context {
    val resources = context.resources
    val configuration = resources.configuration
    configuration.locale = locale
    resources.updateConfiguration(configuration, resources.displayMetrics)
    return context
}