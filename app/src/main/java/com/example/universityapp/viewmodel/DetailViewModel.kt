package com.example.universityapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.universityapp.model.University

/**
 * ViewModel shared between DetailActivity and UniversityDetailFragment.
 *
 * DetailActivity receives the selected university through the Intent and stores it here.
 * The Fragment obtains the same instance with `by activityViewModels()` and observes it,
 * so no Bundle arguments are needed. The data also survives screen rotation.
 */
class DetailViewModel : ViewModel() {

    // Private mutable state: only this ViewModel can change it.
    private val _selectedUniversity = MutableLiveData<University>()

    /**
     * Exposed LiveData with the selected university.
     * The detail Fragment observes it to know what to display.
     */
    val selectedUniversity: LiveData<University> get() = _selectedUniversity

    /**
     * Updates the selected university.
     * @param university The university the user tapped in the list.
     */
    fun selectUniversity(university: University) {
        _selectedUniversity.value = university
    }
}
