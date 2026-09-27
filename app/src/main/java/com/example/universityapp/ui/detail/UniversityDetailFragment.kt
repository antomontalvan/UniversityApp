package com.example.universityapp.ui.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.universityapp.R
import com.example.universityapp.databinding.FragmentUniversityDetailBinding
import com.example.universityapp.model.University
import com.example.universityapp.viewmodel.DetailViewModel

/**
 * Fragment that shows the detailed information of the selected university:
 * official web domains, active link to its website, country code and state/province.
 */
class UniversityDetailFragment : Fragment() {

    private var _binding: FragmentUniversityDetailBinding? = null

    /**
     * Safe access property to the binding. Only valid between onCreateView and onDestroyView.
     */
    private val binding get() = _binding!!

    /**
     * Reference to the ViewModel shared with DetailActivity.
     * It provides the selected university without passing Bundle arguments.
     */
    private val viewModel: DetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUniversityDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observes the selected university in the shared ViewModel.
        // viewLifecycleOwner stops the observation when the Fragment view is destroyed.
        viewModel.selectedUniversity.observe(viewLifecycleOwner) { university ->
            showUniversity(university)
        }
    }

    /**
     * Updates the UI with the university data.
     * Optional fields show "Not specified" when the API does not provide them.
     */
    private fun showUniversity(university: University) {
        val notAvailable = getString(R.string.detail_not_available)

        binding.tvDetailName.text = university.name
        binding.tvDetailCountry.text = university.country
        binding.tvDetailCountryCode.text = university.countryCode ?: notAvailable
        binding.tvDetailProvince.text =
            university.stateProvince?.takeIf { it.isNotBlank() } ?: notAvailable

        // One domain per line.
        binding.tvDetailDomains.text =
            university.domains?.takeIf { it.isNotEmpty() }?.joinToString("\n") ?: notAvailable

        // Active link to the official website.
        val webPage = university.mainWebPage
        if (webPage.isNullOrBlank()) {
            binding.tvDetailWebsite.text = notAvailable
            binding.btnOpenWebsite.visibility = View.GONE
        } else {
            binding.tvDetailWebsite.text = webPage
            binding.btnOpenWebsite.visibility = View.VISIBLE
            binding.btnOpenWebsite.setOnClickListener { openWebsite(webPage) }
            binding.tvDetailWebsite.setOnClickListener { openWebsite(webPage) }
        }

        // Fallback always available: the API data is not maintained, so some
        // official websites are down or have invalid SSL certificates.
        binding.btnSearchWeb.setOnClickListener {
            openWebsite(buildSearchUrl(university.name))
        }
    }

    /**
     * Builds a Google search URL for the given university name.
     * Uri.encode escapes spaces and accented characters (e.g. "Córdoba").
     */
    private fun buildSearchUrl(universityName: String): String =
        "https://www.google.com/search?q=" + Uri.encode(universityName)

    /**
     * Opens the URL in the device browser using an implicit Intent.
     */
    private fun openWebsite(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.error_no_browser, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Binding cleanup to avoid memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
