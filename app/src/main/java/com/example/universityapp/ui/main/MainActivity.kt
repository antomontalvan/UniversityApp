package com.example.universityapp.ui.main

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.universityapp.R
import com.example.universityapp.databinding.ActivityMainBinding
import com.example.universityapp.model.University
import com.example.universityapp.ui.detail.DetailActivity
import com.example.universityapp.viewmodel.ErrorType
import com.example.universityapp.viewmodel.UniversityUIState
import com.example.universityapp.viewmodel.UniversityViewModel

/**
 * Main screen (the "View" in MVVM).
 *
 * It only draws the state it receives from the ViewModel and forwards user
 * actions to it. It contains no business logic and no network code.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: UniversityViewModel
    private lateinit var adapter: UniversityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()

        // ViewModelProvider returns the same ViewModel instance after a rotation.
        viewModel = ViewModelProvider(this)[UniversityViewModel::class.java]

        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    /**
     * Edge-to-edge handling: the header gradient is drawn behind the status bar,
     * so only the header content is pushed down by the status bar height.
     * The rest of the screen avoids the side and bottom system bars.
     */
    private fun applyWindowInsets() {
        // Original padding from the XML, read once so it is not added twice.
        val headerTopPadding = binding.layoutHeader.paddingTop

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            binding.layoutHeader.updatePadding(top = headerTopPadding + systemBars.top)
            insets
        }
    }

    private fun setupRecyclerView() {
        adapter = UniversityAdapter(emptyList()) { university -> openDetail(university) }
        binding.rvUniversities.layoutManager = LinearLayoutManager(this)
        binding.rvUniversities.adapter = adapter
    }

    private fun setupListeners() {
        // Real-time search: every text change is sent to the ViewModel (which applies a debounce).
        binding.etCountrySearch.doAfterTextChanged { text ->
            viewModel.onQueryChanged(text?.toString().orEmpty())
        }

        // Keyboard "search" key: search immediately, without waiting for the debounce.
        binding.etCountrySearch.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.searchNow(textView.text.toString())
                true
            } else {
                false
            }
        }

        binding.btnRetry.setOnClickListener { viewModel.retry() }

        // Suggestion chips: fill the search box with the chip text and search right away.
        val suggestionChips = listOf(
            binding.chipArgentina,
            binding.chipSpain,
            binding.chipUnitedStates,
            binding.chipJapan
        )
        suggestionChips.forEach { chip ->
            chip.setOnClickListener {
                val country = chip.text.toString()
                binding.etCountrySearch.setText(country)
                binding.etCountrySearch.setSelection(country.length)
                viewModel.searchNow(country)
            }
        }
    }

    /** Observes the UI state and updates the views for each case. */
    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is UniversityUIState.Idle -> {
                    adapter.updateItems(emptyList())
                    showMessage(getString(R.string.message_idle), showRetry = false)
                }

                is UniversityUIState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.layoutMessage.visibility = View.GONE
                    binding.tvResultsCount.visibility = View.GONE
                }

                is UniversityUIState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutMessage.visibility = View.GONE
                    binding.rvUniversities.visibility = View.VISIBLE
                    binding.tvResultsCount.visibility = View.VISIBLE
                    binding.tvResultsCount.text = resources.getQuantityString(
                        R.plurals.results_count, state.universities.size, state.universities.size
                    )
                    adapter.updateItems(state.universities)
                }

                is UniversityUIState.Empty -> {
                    adapter.updateItems(emptyList())
                    showMessage(getString(R.string.message_empty, state.query), showRetry = false)
                }

                is UniversityUIState.Error -> {
                    adapter.updateItems(emptyList())
                    val message = when (state.type) {
                        ErrorType.NO_CONNECTION -> getString(R.string.error_no_connection)
                        ErrorType.SERVER -> getString(R.string.error_server, state.httpCode ?: 0)
                        ErrorType.UNKNOWN -> getString(R.string.error_unknown)
                    }
                    showMessage(message, showRetry = true)
                }
            }
        }
    }

    /** Shows the central message block (idle, empty or error) and hides the list. */
    private fun showMessage(message: String, showRetry: Boolean) {
        binding.progressBar.visibility = View.GONE
        binding.rvUniversities.visibility = View.GONE
        binding.tvResultsCount.visibility = View.GONE
        binding.layoutMessage.visibility = View.VISIBLE
        binding.tvMessage.text = message
        binding.btnRetry.visibility = if (showRetry) View.VISIBLE else View.GONE
        // Suggestions only make sense for Idle/Empty; on errors the Retry button is shown instead.
        binding.chipGroupSuggestions.visibility = if (showRetry) View.GONE else View.VISIBLE
    }

    /** Navigates to the second Activity, sending the selected university as an Intent extra. */
    private fun openDetail(university: University) {
        startActivity(DetailActivity.newIntent(this, university))
    }
}
