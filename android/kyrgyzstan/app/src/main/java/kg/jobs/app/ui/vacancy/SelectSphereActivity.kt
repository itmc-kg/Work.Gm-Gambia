package kg.jobs.app.ui.vacancy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kg.jobs.app.R
import kg.jobs.app.model.JobSphere
import kg.jobs.app.repository.FilterRepository
import kg.jobs.app.ui.dialogs.LoadingDialog
import kotlinx.android.synthetic.main.activity_select_sphere.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SelectSphereActivity : AppCompatActivity() {

    private val repo: FilterRepository by inject()
    private lateinit var adapter: SelectAdapter
    private var items = mutableListOf<JobSphere>()
    private val loadingDialog by lazy { LoadingDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_sphere)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        collapsingToolbar.title = getString(R.string.sphere_hint)
        toolbar.setNavigationOnClickListener { finish() }
        loadingDialog.show()
        GlobalScope.launch(Dispatchers.Main) {
            repo.jobSpheres()?.let(items::addAll)
            adapter = SelectAdapter(items.map { it.name })
            items.indexOfFirst { it.id == intent?.getParcelableExtra<JobSphere>("job_sphere")?.id }
                    .takeIf { it >= 0 }
                    ?.also {
                        adapter.selectedPosition = it
                    }
            recyclerView.adapter = adapter
            loadingDialog.dismiss()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, Menu.FIRST, 0, getString(R.string.action_done))?.apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setIcon(R.drawable.ic_done_black_24dp)
            setOnMenuItemClickListener {
                setResult(Activity.RESULT_OK, Intent().putExtra("job_sphere", items[adapter.selectedPosition]))
                finish()
                true
            }
        }
        return super.onCreateOptionsMenu(menu)
    }
}


