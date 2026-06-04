package com.example.grainclassifier

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grainclassifier.adapter.HistoryAdapter
import com.example.grainclassifier.data.entity.ClassificationHistory
import com.example.grainclassifier.databinding.ActivitySavedResultsBinding
import com.example.grainclassifier.viewmodel.HistoryViewModel
import com.google.android.material.snackbar.Snackbar

class SavedResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedResultsBinding
    private lateinit var viewModel: HistoryViewModel
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivitySavedResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel using AndroidViewModel provider
        viewModel = ViewModelProvider(this)[HistoryViewModel::class.java]

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(
            onDeleteClicked = { historyRecord ->
                deleteHistoryItem(historyRecord)
            }
        )
        binding.rvSavedResults.apply {
            layoutManager = LinearLayoutManager(this@SavedResultsActivity)
            adapter = this@SavedResultsActivity.adapter
        }
    }

    private fun setupClickListeners() {
        // Back toolbar navigation button
        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun observeViewModel() {
        viewModel.allHistory.observe(this) { historyList ->
            if (historyList.isNullOrEmpty()) {
                binding.rvSavedResults.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
            } else {
                binding.rvSavedResults.visibility = View.VISIBLE
                binding.layoutEmptyState.visibility = View.GONE
                adapter.submitList(historyList)
            }
        }
    }

    private fun deleteHistoryItem(record: ClassificationHistory) {
        viewModel.delete(record)
        Snackbar.make(binding.root, "Saved record deleted.", Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                viewModel.insert(record)
            }
            .setActionTextColor(getColor(R.color.primary))
            .show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
