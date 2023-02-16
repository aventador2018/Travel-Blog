package com.example.travelblog

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.travelblog.adapter.MainAdapter
import com.example.travelblog.databinding.ActivityMainBinding
import com.example.travelblog.http.Blog
import com.example.travelblog.repository.BlogRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    companion object {
        private const val SORT_TITLE = 0 // 1
        private const val SORT_DATE = 1 // 1
    }

    private var currentSort = SORT_DATE // 2

    private lateinit var binding: ActivityMainBinding
    private val adapter = MainAdapter { blog ->
        BlogDetailsActivity.start(this, blog)
    }

    private val repository by lazy { BlogRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.materialToolBar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.sort) {
                onSortClicked()
            }
            false
        }

        val searchItem = binding.materialToolBar.menu.findItem(R.id.search) // 1
        val searchView = searchItem.actionView as SearchView // 2
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener { // 3
            override fun onQueryTextSubmit(query: String): Boolean = false

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.filter(newText) // 4
                return true
            }
        })

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        binding.refresh.setOnRefreshListener { // 1
            loadDataFromNetwork()
        }

        loadDataFromDatabase()
        loadDataFromNetwork()
    }

    private fun loadDataFromDatabase() {
        repository.loadDataFromDatabase { blogList: List<Blog> ->
            runOnUiThread {
                adapter.setData(blogList)
                sortData()
            }
        }
    }

    private fun loadDataFromNetwork() {
        binding.refresh.isRefreshing = true // 1
        repository.loadDataFromNetwork( // 2
            onSuccess = { blogList: List<Blog> ->
                runOnUiThread { // 3
                    binding.refresh.isRefreshing = false
                    adapter.setData(blogList)
                    sortData()
                }
            },
            onError = {
                runOnUiThread {
                    binding.refresh.isRefreshing = false
                    showErrorSnackbar()
                }
            }
        )
    }

    private fun showErrorSnackbar() {
        Snackbar.make(binding.root,
            "Error during loading blog articles", Snackbar.LENGTH_INDEFINITE).run {
            setActionTextColor(resources.getColor(R.color.orange500))
            setAction("Retry") {
                loadDataFromNetwork()
                dismiss()
            }
        }.show()
    }

    private fun onSortClicked() {
        val items = arrayOf("Title", "Date")
        MaterialAlertDialogBuilder(this)
            .setTitle("Sort order")
            .setSingleChoiceItems(items, currentSort) { dialog: DialogInterface, which: Int ->
                dialog.dismiss()
                currentSort = which
                sortData()
            }.show()
    }

    private fun sortData() {
        if (currentSort == SORT_TITLE) {
            adapter.sortByTitle()
        } else if (currentSort == SORT_DATE) {
            adapter.sortByDate()
        }
    }
}