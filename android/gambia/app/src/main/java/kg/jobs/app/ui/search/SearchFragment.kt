package kg.jobs.app.ui.search


import android.app.Activity
import androidx.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.adgvcxz.cardlayoutmanager.CardLayoutManager
import com.adgvcxz.cardlayoutmanager.CardSnapHelper
import com.adgvcxz.cardlayoutmanager.OnCardSwipeListener
import kg.jobs.app.R
import kg.jobs.app.model.Vacancy
import kg.jobs.app.startActivity
import kg.jobs.app.startActivityForResult
import kg.jobs.app.ui.dialogs.LoadingDialog
import kg.jobs.app.ui.filter.FilterActivity
import kg.jobs.app.ui.search.show.VacancyDetailActivity
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.progressbar
import kotlinx.android.synthetic.main.fragment_search.recyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModel()
    private val adapter = VacancyAdapter()
    private lateinit var layoutManager: CardLayoutManager
    private val loadingDialog by lazy { LoadingDialog(activity!!) }
    var isLoad = true
    var lastRemoveVacancy: Vacancy? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        viewModel.vacancy.observe(this, Observer {
            adapter.add(it)
            isChildEmpty()
        })

        viewModel.loadData.observe(this, Observer {
            Log.e("loadData", "$it")
            isLoad = it
        })

        viewModel.applyState.observe(this, Observer {
            val (state, vacancy) = it!!
            when (state) {
                SearchViewModel.State.LOADING -> {
                    loadingDialog.show()
                }
                SearchViewModel.State.ACCEPT -> viewModel.launch {
                    loadingDialog.dismiss()
                    adapter.accepted(layoutManager.topPosition)
                    delay(1000)
                    adapter.remove(vacancy)
                }
                SearchViewModel.State.DECLINE -> viewModel.launch {
                    loadingDialog.dismiss()
                    adapter.declined(layoutManager.topPosition)
                    delay(1000)
                    adapter.remove(vacancy)

                }
                SearchViewModel.State.ERROR -> {
                    loadingDialog.dismiss()
                }
            }
        })

        viewModel.state.observe(this, Observer {
            when (it) {
                SearchViewModel.State.LOADING -> progressbar.isVisible = true
                SearchViewModel.State.ERROR -> {
                    progressbar.isVisible = false
                }
                SearchViewModel.State.SUCCESS -> {
                    progressbar.isVisible = false
                }
            }
            isChildEmpty()
        })

        adapter.onAccept = {
            viewModel.accept(it)
            isChildEmpty()
        }
        adapter.onDecline = {
            recyclerView.smoothScrollToPosition(layoutManager.topPosition + 1)

        }

        adapter.onClick = {
            startActivityForResult<VacancyDetailActivity>(1111) {
                putExtra("vacancy", it)
            }
        }

        return_vacancy.setOnClickListener {
            return_vacancy.visibility = View.GONE
            adapter.returnData(lastRemoveVacancy)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1111 && resultCode == Activity.RESULT_OK && data != null) {
            when (data.getStringExtra("action")) {
                "accept" -> adapter.remove(data.getParcelableExtra("vacancy"))
                "decline" -> {
                    recyclerView.smoothScrollToPosition(layoutManager.topPosition + 1)
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
            }

            override fun onAnimOutStop(p0: View?, position: Int, direction: Int) {
                val data = adapter.getItemPosition(position)
                removeVacancy(data)

            }

            override fun onAnimInStart(p0: View?, p1: Int) {
            }

            override fun onSwipe(view: View, position: Int, dx: Int, dy: Int) {
            }
        })
        filter.setOnClickListener {
            activity!!.startActivity<FilterActivity>()
        }
        recyclerView.adapter = adapter
    }

    private fun removeVacancy(vacancy: Vacancy?) {
        if (vacancy != null) {
            lastRemoveVacancy = vacancy
            if (lastRemoveVacancy != null) {
                avatar_last.setImageURI(lastRemoveVacancy!!.employer?.image)
            }
            return_vacancy.visibility = View.VISIBLE
            isChildEmpty()
        }
    }

    private fun isChildEmpty() {
        if (!isLoad) {
            if (layoutManager.childCount == 0) {
                progressbar.visibility = View.GONE
                cardEmpty.visibility = View.VISIBLE
            } else {
                cardEmpty.visibility = View.GONE
            }
        } else {
            if (layoutManager.childCount == 0 || adapter.getItemSize() == 0) {
                progressbar.visibility = View.VISIBLE
            } else {
                progressbar.visibility = View.GONE
            }
        }
    }


}
