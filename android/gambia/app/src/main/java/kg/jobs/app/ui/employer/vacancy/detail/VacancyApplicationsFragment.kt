package kg.jobs.app.ui.employer.vacancy.detail

import android.app.Activity
import androidx.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.adgvcxz.cardlayoutmanager.CardLayoutManager
import com.adgvcxz.cardlayoutmanager.CardSnapHelper
import com.adgvcxz.cardlayoutmanager.OnCardSwipeListener
import kg.jobs.app.R
import kg.jobs.app.model.Application
import kg.jobs.app.model.Vacancy
import kg.jobs.app.startActivityForResult
import kg.jobs.app.ui.dialogs.LoadingDialog
import kg.jobs.app.ui.employer.vacancy.detail.applicationDetail.ApplicationDetailActivity
import kg.jobs.app.ui.messenger.ChatActivity
import kotlinx.android.synthetic.main.fragment_vacancy_applications.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VacancyApplicationsFragment : Fragment() {
    companion object {
        fun create(vacancy: Vacancy): Fragment {
            return VacancyApplicationsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("vacancy", vacancy)
                }
            }
        }
    }

    private val viewModel by viewModel<VacancyApplicationsViewModel>()
    private val adapter = ApplicationAdapter()
    private lateinit var layoutManager: CardLayoutManager
    private val loadingDialog by lazy { LoadingDialog(activity!!) }
    var isLoad = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vacancy_applications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        viewModel.application.observe(this, Observer { adapter.add(it) })
        viewModel.applicationState.observe(this, Observer {
            val (state, application) = it!!
            when (state) {
                VacancyApplicationsViewModel.State.LOADING -> {
                    loadingDialog.show()
                }
                VacancyApplicationsViewModel.State.ACCEPTED -> {
                    loadingDialog.dismiss()
                    adapter.update(application)
                }
                VacancyApplicationsViewModel.State.REJECTED -> {
                    loadingDialog.dismiss()
                    adapter.remove(application)
                }
                VacancyApplicationsViewModel.State.SUCCESS,
                VacancyApplicationsViewModel.State.ERROR -> {
                    loadingDialog.dismiss()
                }
            }
        })

        viewModel.loadData.observe(this, Observer {
            Log.e("loadData", "$it")
            isLoad = it
            isChildEmpty()
        })

        viewModel.state.observe(this, Observer {
            when (it) {
                VacancyApplicationsViewModel.State.LOADING -> progressbar.isVisible = true
                VacancyApplicationsViewModel.State.ERROR -> progressbar.isVisible = false
                VacancyApplicationsViewModel.State.SUCCESS -> progressbar.isVisible = false
            }
        })

        viewModel.openChat.observe(this, Observer { ChatActivity.start(activity!!, it) })

        adapter.onRightClick = { viewModel.rightClick(it) }
        adapter.onLeftClick = {
            AlertDialog.Builder(activity!!)
                    .setMessage(getString(R.string.dialog_reject_text))
                    .setPositiveButton(getString(R.string.reject)) { dialog, _ ->
                         viewModel.reject(it)
                        dialog.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                    .show()
        }
        adapter.onClick = {
            startActivityForResult<ApplicationDetailActivity>(1111) {
                putExtra("application", it)
            }
        }
        viewModel.init(arguments!!.get("vacancy") as Vacancy)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1111 && resultCode == Activity.RESULT_OK && data != null) {
            when (data.getStringExtra("action")) {
                "accept" ->
                    adapter.update(data.getParcelableExtra<Application>("application"))
                "reject" -> {
                    adapter.remove(data.getParcelableExtra("application"))
                }
            }

        } else super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initViews() {
        layoutManager = CardLayoutManager()
        layoutManager.setYInterval(30)
        recyclerView.layoutManager = layoutManager
        CardSnapHelper().attachToRecyclerView(recyclerView)
        layoutManager.setOnCardSwipeListener(object : OnCardSwipeListener {
            override fun onAnimInStop(p0: View?, p1: Int) {

            }

            override fun onAnimOutStart(p0: View?, position: Int, p2: Int) {
                if (position == adapter.itemCount - 1) {
                    val data = adapter.getItemPosition(position)
                    adapter.remove(data)
//                    adapter.deleteItem(position)
                }
                isChildEmpty()
            }

            override fun onAnimOutStop(p0: View?, position: Int, direction: Int) {
//                if (position == adapter.itemCount - 1) {
//                    adapter.remove(adapter.getItemPosition(position))
//                }
//                isChildEmpty()
            }

            override fun onAnimInStart(p0: View?, p1: Int) {
            }

            override fun onSwipe(view: View, position: Int, dx: Int, dy: Int) {
            }
        })
        recyclerView.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingDialog.dismiss()
    }

    private fun isChildEmpty(){
        if (!isLoad) {
            Log.e("isChildEmpty", "${layoutManager.childCount}")
            if (layoutManager.childCount == 1 || layoutManager.childCount == 0) {
                progressbar.visibility = View.GONE
                cardEmpty.visibility = View.VISIBLE
            } else {
                cardEmpty.visibility = View.GONE
            }
        }
    }

}