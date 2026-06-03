package com.example.grainclassifier

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grainclassifier.adapter.HistoryAdapter
import com.example.grainclassifier.data.entity.ClassificationHistory
import com.example.grainclassifier.databinding.ActivityHistoryBinding
import com.example.grainclassifier.viewmodel.HistoryViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * Screen displaying the classification history list.
 * Follows MVVM architecture by observing data from HistoryViewModel.
 */
class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: HistoryViewModel
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup ViewBinding
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel using AndroidViewModel standard provider
        viewModel = ViewModelProvider(this)[HistoryViewModel::class.java]

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    /**
     * Initializes the RecyclerView with HistoryAdapter.
     */
    private fun setupRecyclerView() {
        adapter = HistoryAdapter(
            onDeleteClicked = { historyRecord ->
                deleteHistoryItem(historyRecord)
            }
        )
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = this@HistoryActivity.adapter
        }
    }

    /**
     * Setups click listeners for navigation and global actions.
     */
    private fun setupClickListeners() {
        // Safe slide-out transition matching main screen
        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Prompts confirmation before clearing all history
        binding.btnClearAll.setOnClickListener {
            showClearAllConfirmation()
        }
    }

    /**
     * Observes historical records database changes from ViewModel.
     */
    private fun observeViewModel() {
        viewModel.allHistory.observe(this) { historyList ->
            if (historyList.isNullOrEmpty()) {
                // Show empty state UI if list is blank
                binding.rvHistory.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.btnClearAll.isEnabled = false
                binding.btnClearAll.alpha = 0.5f
            } else {
                // Show list UI and hide empty state
                binding.rvHistory.visibility = View.VISIBLE
                binding.layoutEmptyState.visibility = View.GONE
                binding.btnClearAll.isEnabled = true
                binding.btnClearAll.alpha = 1.0f
                adapter.submitList(historyList)
            }
        }
    }

    /**
     * Deletes a single history item and gives visual feedback with Snackbar.
     */
    private fun deleteHistoryItem(record: ClassificationHistory) {
        viewModel.delete(record)
        Snackbar.make(binding.root, "Classification record deleted.", Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                viewModel.insert(record)
            }
            .setActionTextColor(getColor(R.color.primary))
            .show()
    }

    /**
     * Shows a confirmation dialog before deleting all history records.
     */
    private fun showClearAllConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Clear History")
            .setMessage("Are you sure you want to delete all history?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Clear All") { dialog, _ ->
                viewModel.clearAll()
                dialog.dismiss()
                Snackbar.make(binding.root, "Classification history cleared.", Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
