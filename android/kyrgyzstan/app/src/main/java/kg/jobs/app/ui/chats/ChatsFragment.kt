package kg.jobs.app.ui.chats

import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kg.jobs.app.R
import kg.jobs.app.customViews.RecyclerViewDivider
import kg.jobs.app.model.Vacancy
import kg.jobs.app.toPx
import kg.jobs.app.ui.messenger.ChatActivity
import kotlinx.android.synthetic.main.fragment_chats.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ChatsFragment : Fragment() {
    companion object {
        @JvmStatic
        fun create(vacancy: Vacancy? = null, chatId: String = "") =
                ChatsFragment().apply {
                    arguments = Bundle().apply {
                        putString("vacancyId", vacancy?.id ?: "")
                        putString("chatId", chatId)
                    }
                }
    }

    private val viewModel by viewModel<ChatsViewModel> {
        parametersOf(arguments?.getString("vacancyId") ?: "",
                arguments?.getString("chatId") ?: "")
    }
    private val adapter = ChatAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).apply {
            setSupportActionBar(toolbar)
            supportActionBar?.title = getString(R.string.chats_title)
        }
        viewModel.emptyChatsText.observe(this, Observer {
            textView10.text = it
        })
        viewModel.status.observe(this, Observer {
            when (it) {
                Status.DATA -> {
                    empty_chat_group.isVisible = false
                    progressBar.isVisible = false
                }
                Status.EMPTY -> {
                    empty_chat_group.isVisible = true
                    progressBar.isVisible = false
                }
                Status.LOADING -> {
                    progressBar.isVisible = true
                    empty_chat_group.isVisible = false
                }
                Status.ERROR -> {
                    empty_chat_group.isVisible = false
                    progressBar.isVisible = false
                }
                Status.CHECK_DATA -> {
                    if (adapter.itemCount == 0) {
                        empty_chat_group.isVisible = true
                    }else{
                        empty_chat_group.isVisible = false
                    }
                }
            }
        })
        viewModel.chat.observe(this, Observer(adapter::addChat))
        viewModel.openChat.observe(this, Observer { chat ->
            ChatActivity.start(activity!!, chat)
        })
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(RecyclerViewDivider.builder(context)
                .leftMarginPx(56.toPx)
                .color(R.color.light_grey_1, R.dimen.divider)
                .build())
        (recyclerView.itemAnimator as androidx.recyclerview.widget.SimpleItemAnimator).supportsChangeAnimations = false
        adapter.onClick = { chat -> ChatActivity.start(activity!!, chat) }
    }

}