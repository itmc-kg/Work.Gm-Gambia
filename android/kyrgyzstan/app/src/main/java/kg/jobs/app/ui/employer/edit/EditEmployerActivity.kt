package kg.jobs.app.ui.employer.edit

import android.Manifest
import android.app.Activity
import androidx.lifecycle.Observer
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.yalantis.ucrop.UCrop
import kg.jobs.app.R
import kg.jobs.app.customViews.NestedScrollToolbarElevation
import kg.jobs.app.startActivity
import kg.jobs.app.timestamp
import kg.jobs.app.toast
import kg.jobs.app.ui.dialogs.LoadingDialog
import kg.jobs.app.ui.employer.VacanciesActivity
import kg.jobs.app.ui.vacancy.EditVacancyActivity
import kotlinx.android.synthetic.main.activity_edit_employer.*
import kotlinx.android.synthetic.main.appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class EditEmployerActivity : AppCompatActivity() {
    private val viewModel: EditEmployerViewModel by viewModel()
    private val loadingDialog by lazy { LoadingDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_employer)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener { finish() }
        nestedScrollView.setOnScrollChangeListener(NestedScrollToolbarElevation(appbar))

        val fromStart = intent.getBooleanExtra("start", false)
        if (!fromStart) next.text = getString(R.string.save)

        viewModel.error.observe(this, Observer { toast(it!!, Toast.LENGTH_SHORT) })
        viewModel.selectedRegion.observe(this, Observer(region::setText))
        viewModel.events.observe(this, Observer {
            when (it) {
                EditEmployerViewModel.Event.ERROR,
                EditEmployerViewModel.Event.SUCCESS -> loadingDialog.dismiss()
                EditEmployerViewModel.Event.CLOSE -> {
                    loadingDialog.dismiss()
                    if (fromStart) startActivity<EditVacancyActivity>()
                    else startActivity<VacanciesActivity>()
                }
                EditEmployerViewModel.Event.LOADING -> loadingDialog.show()
            }
        })
        viewModel.regions.observe(this, Observer { regions ->
            val selectRegionDialog = AlertDialog.Builder(this)
                    .setItems(regions!!.map { it.name }.toTypedArray()) { dialog, which ->
                        viewModel.selectRegion(regions[which])
                        dialog.dismiss()
                    }
                    .create()
            region.setOnClickListener { selectRegionDialog.show() }
        })
        viewModel.employer.observe(this, Observer {
            name.setText(it?.name)
            about.setText(it?.about)
            city.setText(it?.city)
            avatar.setImageURI(it?.image)
        })

        next.setOnClickListener {
            viewModel.save(name.text.toString(),
                    about.text.toString(),
                    city.text.toString())
        }

        avatar.setOnClickListener {
            askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE) {
                val getIntent = Intent(Intent.ACTION_GET_CONTENT)
                getIntent.type = "image/*"
                val pickIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                pickIntent.type = "image/*"
                val chooserIntent = Intent.createChooser(getIntent, getString(R.string.choose_source))
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
                startActivityForResult(chooserIntent, 1111)

            }.onDeclined { e ->
                AlertDialog.Builder(this)
                        .setMessage(getString(R.string.dialog_storage_permission_text))
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            e.askAgain()
                            dialog.dismiss()
                        }
                        .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                        .show()
                if (e.foreverDenied.isNotEmpty()) {
                    AlertDialog.Builder(this)
                            .setMessage(getString(R.string.dialog_open_settings_text))
                            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                                e.goToSettings()
                                dialog.dismiss()
                            }
                            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                            .show()
                }
            }
        }

        viewModel.init()
    }

    private fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "${timestamp()}_c.jpg"))
        val context = this
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(1000, 1000)
                .withOptions(UCrop.Options().apply {
                    setCompressionFormat(Bitmap.CompressFormat.JPEG)
                    setCompressionQuality(80)
                    setToolbarTitle("")
                    setToolbarColor(ContextCompat.getColor(context, R.color.white))
                    setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                    setToolbarWidgetColor(ContextCompat.getColor(context, R.color.bluish_black))
                    setActiveWidgetColor(ContextCompat.getColor(context, R.color.dark_grey_1))
                })
                .start(this)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            data?.also { i ->
                val resultUri = UCrop.getOutput(i)
                avatar.setImageURI(resultUri.toString())
                resultUri?.also { viewModel.newImage(it) }
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            data?.also {
                val cropError = UCrop.getError(data)
                Log.e("cropError", cropError.toString())
            }
        } else if (requestCode == 1111 && data?.data != null) {
            startCrop(data.data)
        }
    }
}
