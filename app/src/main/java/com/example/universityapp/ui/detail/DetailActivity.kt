package com.example.universityapp.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.universityapp.databinding.ActivityDetailBinding
import com.example.universityapp.model.University
import com.example.universityapp.viewmodel.DetailViewModel

/**
 * Second Activity: container for UniversityDetailFragment.
 *
 * Data flow:
 * MainActivity --(Intent extra)--> DetailActivity --(shared DetailViewModel)--> Fragment
 *
 * The Fragment is declared directly in activity_detail.xml through a FragmentContainerView
 * (android:name attribute), so no FragmentTransaction is needed here.
 */
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    /**
     * ViewModel scoped to this Activity. The Fragment gets this same instance
     * using `by activityViewModels()`.
     */
    private val viewModel: DetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enables full-screen layout (behind the system bars)
        enableEdgeToEdge()

        // Layout inflation using View Binding
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Adjusts the padding so the content is not hidden under the system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Toolbar back arrow closes this Activity and returns to the list.
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Reads the university sent by MainActivity.
        // IntentCompat handles the API differences of getSerializableExtra.
        val university = IntentCompat.getSerializableExtra(
            intent, EXTRA_UNIVERSITY, University::class.java
        )

        if (university == null) {
            // Should never happen, but this screen cannot work without data.
            finish()
            return
        }

        // Stores the university in the shared ViewModel: the Fragment will observe it.
        viewModel.selectUniversity(university)
    }

    companion object {
        private const val EXTRA_UNIVERSITY = "extra_university"

        /**
         * Builds the Intent to open this Activity with the selected university.
         * Keeping the extra key private avoids typos in other classes.
         */
        fun newIntent(context: Context, university: University): Intent =
            Intent(context, DetailActivity::class.java).apply {
                putExtra(EXTRA_UNIVERSITY, university)
            }
    }
}
