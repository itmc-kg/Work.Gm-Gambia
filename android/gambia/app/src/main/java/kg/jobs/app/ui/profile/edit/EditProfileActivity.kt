package kg.jobs.app.ui.profile.edit

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
import kg.jobs.app.*
import kg.jobs.app.customViews.NestedScrollToolbarElevation
import kg.jobs.app.customViews.RecyclerViewDivider
import kg.jobs.app.ui.MainActivity
import kg.jobs.app.ui.dialogs.LoadingDialog
import kg.jobs.app.ui.filter.PreFilterActivity
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File


class EditProfileActivity : AppCompatActivity() {
    private val viewModel: EditProfileViewModel  by viewModel()
    private val eduAdapter = EditEducationAdapter()
    private val workAdapter = EditWorkAdapter()
    private val loadingDialog by lazy { LoadingDialog(this) }

    private var fromProfile = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        fromProfile = intent.getBooleanExtra("from_profile", false)
        if (fromProfile) next.setText(R.string.save)

        edu_recyclerView.apply {
            adapter = eduAdapter
            addItemDecoration(RecyclerViewDivider.builder(context)
                    .hideLastDivider()
                    .color(R.color.light_grey_1, R.dimen.divider)
                    .build())
            isNestedScrollingEnabled = false
        }
        work_recyclerView.apply {
            adapter = workAdapter
            addItemDecoration(RecyclerViewDivider.builder(context)
                    .hideLastDivider()
                    .color(R.color.light_grey_1, R.dimen.divider)
                    .build())
            isNestedScrollingEnabled = false
        }
        nestedScrollView.setOnScrollChangeListener(NestedScrollToolbarElevation(appbar))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.title = ""

        viewModel.state.observe(this, Observer {
            when (it) {
                EditProfileViewModel.State.DATA -> {
                    loadingDialog.dismiss()
                }
                EditProfileViewModel.State.LOADING -> {
                    loadingDialog.show()
                }
                EditProfileViewModel.State.CLOSE -> {
                    if (!fromProfile) {
                        startActivity<PreFilterActivity>()
                        finish()
                    } else {
                        startActivity<MainActivity>()
                        finish()
                    }
                }
            }
        })
        viewModel.user.observe(this, Observer {
            name.setText(it?.name)
            avatar.setImageURI(it?.image)
        })

        viewModel.educations.observe(this, Observer(eduAdapter::update))
        viewModel.works.observe(this, Observer(workAdapter::update))
        viewModel.error.observe(this, Observer { text ->
            toast(text!!, Toast.LENGTH_SHORT)
        })

        eduAdapter.onChanged = viewModel::educationChanged
        workAdapter.onChanged = viewModel::workChanged
        add_edu.setOnClickListener { viewModel.addNewEducation() }
        add_work.setOnClickListener { viewModel.addNewWork() }
        name.afterTextChanged(viewModel::setName)
        next.setOnClickListener { viewModel.save() }

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
