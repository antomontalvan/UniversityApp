package com.example.universityapp.ui.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.universityapp.databinding.ItemUniversityBinding
import com.example.universityapp.model.University

/**
 * Adapter for the RecyclerView that shows the list of universities.
 *
 * @param items Initial list of universities to display.
 * @param onItemClick Lambda executed when a row is clicked. The Adapter does not
 * know anything about navigation: it only notifies the Activity.
 */
class UniversityAdapter(
    private var items: List<University>,
    private val onItemClick: (University) -> Unit
) : RecyclerView.Adapter<UniversityAdapter.UniversityViewHolder>() {

    /**
     * Inflates the row layout (item_university.xml) and creates the ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UniversityViewHolder {
        val binding = ItemUniversityBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UniversityViewHolder(binding)
    }

    /**
     * Binds the university at the given position to an existing ViewHolder.
     */
    override fun onBindViewHolder(holder: UniversityViewHolder, position: Int) {
        holder.bind(items[position])
    }

    /**
     * Returns the total number of items in the list.
     */
    override fun getItemCount(): Int = items.size

    /**
     * Replaces the list and notifies the adapter that the data has changed.
     * @param newItems The new list of universities.
     *
     * notifyDataSetChanged() redraws the whole list. It is acceptable here because
     * every search replaces the entire list, so the lint warning is suppressed.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<University>) {
        items = newItems
        notifyDataSetChanged()
    }

    /**
     * Custom ViewHolder: holds the views of a single row through View Binding,
     * so they are not looked up again on every scroll.
     */
    inner class UniversityViewHolder(
        private val binding: ItemUniversityBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Assigns the values of a University to the row views.
         * @param university The university to display.
         */
        fun bind(university: University) {
            binding.tvUniversityName.text = university.name
            binding.tvCountryCode.text = university.countryCode.orEmpty()
            binding.tvUniversityDomain.text = university.domains?.firstOrNull().orEmpty()

            // The province is optional in the API: hide the text when it is missing.
            val province = university.stateProvince
            if (province.isNullOrBlank()) {
                binding.tvUniversityProvince.visibility = View.GONE
            } else {
                binding.tvUniversityProvince.visibility = View.VISIBLE
                binding.tvUniversityProvince.text = province
            }

            binding.root.setOnClickListener { onItemClick(university) }
        }
    }
}
