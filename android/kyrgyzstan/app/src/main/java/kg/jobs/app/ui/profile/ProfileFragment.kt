package kg.jobs.app.ui.profile


import androidx.lifecycle.Observer
import android.os.Bundle
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.view.*
import androidx.core.view.isVisible
import kg.jobs.app.R
import kg.jobs.app.repository.application.State
import kg.jobs.app.startActivity
import kg.jobs.app.toPx
import kg.jobs.app.ui.profile.show.ProfileDetailActivity
import kg.jobs.app.ui.search.show.VacancyDetailActivity
import kg.jobs.app.ui.settings.SettingsActivity
import kotlinx.android.synthetic.main.fragment_profile.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.absoluteValue

class ProfileFragment : Fragment() {
    companion object {
        @JvmStatic
        fun create() =
                ProfileFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }

    private val viewModel :ProfileViewModel by viewModel()
    private val adapter = ApplicationAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        viewModel.applications.observe(this, Observer(
                adapter::submitList)
        )
        viewModel.applications.observe(this, Observer{
          if (it.size == 0){
              isEmptyLayout.visibility = View.VISIBLE
          }  else{
              isEmptyLayout.visibility = View.GONE
          }
        })

        viewModel.user.observe(this, Observer {
            profile_name.text = it?.name
            avatar.setImageURI(it?.image)
        })

        viewModel.networkState.observe(this, Observer {
            when (it) {
                State.LOADING -> progressBar.isVisible = true
                State.FAIL -> {
                    progressBar.isVisible = false
                    Snackbar.make(recyclerView, R.string.error_no_internet, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.retry) { viewModel.refresh() }
                            .show()
                }
                State.SUCCESS -> progressBar.isVisible = false
            }
        })
        adapter.onClick = { application ->
            startActivity<VacancyDetailActivity> {
                putExtra("application", application)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.init()
    }

    private fun initViews() {
        collapsingToolbar.title = getString(R.string.applications)
        appbar.addOnOffsetChangedListener(AppBarLayout.BaseOnOffsetChangedListener<AppBarLayout> { appBarLayout, verticalOffset ->
            val params = avatar.layoutParams.apply {
                width = 40.toPx + (40.toPx - (verticalOffset.absoluteValue / appBarLayout.totalScrollRange.toFloat() * 40.toPx)).toInt()
                height = 40.toPx + (40.toPx - (verticalOffset.absoluteValue / appBarLayout.totalScrollRange.toFloat() * 40.toPx)).toInt()
            }
            avatar.layoutParams = params
        })
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
        show_profile.setOnClickListener { activity!!.startActivity<ProfileDetailActivity>() }
        recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.add(0, Menu.FIRST, 0, getString(R.string.action_settings))?.apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setIcon(R.drawable.ic_settings)
            setOnMenuItemClickListener {
                activity!!.startActivity<SettingsActivity>()
                true
            }
        }
    }
}
