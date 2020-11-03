package kg.jobs.app.ui.login

import android.app.Activity
import androidx.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import kg.jobs.app.R
import kotlinx.android.synthetic.main.activity_countries.*
import kotlinx.android.synthetic.main.appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class CountriesActivity : AppCompatActivity() {

    private val viewModel: CountriesViewModel by viewModel()
    lateinit var adapter: CountriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countries)

        setSupportActionBar(toolbar)
        supportActionBar?.title = getText(R.string.chose_a_country)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        adapter = CountriesAdapter {
            setResult(Activity.RESULT_OK, Intent().putExtra("country", it))
            finish()
        }

        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        viewModel.countriesList.observe(this, Observer(adapter::setData))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu!!)

        val search = menu.findItem(R.id.search)
        search.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                adapter.searchViewExpanded()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                adapter.searchViewCollapsed()
                return true
            }
        })
        val searchView = search.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.search(newText!!)
                return true
            }
        })

        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
    }
}

