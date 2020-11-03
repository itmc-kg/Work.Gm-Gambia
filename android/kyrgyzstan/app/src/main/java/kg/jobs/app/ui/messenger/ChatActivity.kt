package kg.jobs.app.ui.messenger

import androidx.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import kg.jobs.app.R
import kg.jobs.app.model.Chat
import kg.jobs.app.startActivity
import kg.jobs.app.ui.dialogs.LoadingDialog
import kg.jobs.app.ui.employer.show.EmployerDetailActivity
import kg.jobs.app.ui.profile.show.ProfileDetailActivity
import kotlinx.android.synthetic.main.activity_chat.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ChatActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context, chat: Chat?) {
            chat?.also {
                context.startActivity<ChatActivity> {
                    putExtra("chat", chat)
                }
            }
        }
    }

    private val viewModel: ChatViewModel by viewModel {
        val chat = intent.getParcelableExtra<Chat>("chat")
        if (chat == null) finish()
        parametersOf(chat)
    }
    private val adapter = MessageAdapter()
    private val loadingDialog by lazy { LoadingDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.title = ""

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        recyclerView.adapter = adapter
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        viewModel.newMessage.observe(this, Observer {
            it?.also(adapter::addMessage)
            recyclerView.smoothScrollToPosition(0)
        })

        viewModel.chatInfo.observe(this, Observer {
            avatar.setImageURI(it?.image)
            chat_title.text = it?.name
        })

        viewModel.vacancy.observe(this, Observer {
            chat_vacancy.text = it
        })

        viewModel.openEmployer.observe(this, Observer {
            startActivity<EmployerDetailActivity> {
                putExtra("employer_id", it)
            }
        })

        viewModel.openEmployee.observe(this, Observer {
            startActivity<ProfileDetailActivity> {
                putExtra("employee_id", it)
            }
        })

        viewModel.state.observe(this, Observer {
            when (it) {
                ChatViewModel.State.DISMISS -> loadingDialog.dismiss()
                ChatViewModel.State.LOADING -> loadingDialog.show()
            }
        })

        chat_info.setOnClickListener {
            viewModel.openChatInfo()
        }

        send_button.setOnClickListener {
            val text = editText.text.toString()
            editText.setText("")
            viewModel.sendMessage(text)
        }
    }
}
