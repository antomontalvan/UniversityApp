package com.example.universityapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universityapp.repository.HttpErrorException
import com.example.universityapp.repository.UniversityRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * ViewModel of the main screen (the "VM" in MVVM).
 *
 * Responsibilities:
 * - Receive user actions from the View (text typed in the search box).
 * - Ask the Repository for data.
 * - Expose the result as a [UniversityUIState] through LiveData.
 *
 * It survives configuration changes (e.g. screen rotation), so the results
 * are not lost and the request is not repeated.
 */
class UniversityViewModel(
    private val repository: UniversityRepository = UniversityRepository()
) : ViewModel() {

    // Private mutable state: only the ViewModel can change it.
    private val _uiState = MutableLiveData<UniversityUIState>(UniversityUIState.Idle)

    // Public read-only state: the Activity can only observe it.
    val uiState: LiveData<UniversityUIState> get() = _uiState

    // Reference to the running search, used to cancel it when the user keeps typing.
    private var searchJob: Job? = null

    // Last searched term, used by the "Retry" button.
    private var lastQuery: String = ""

    /**
     * Called on every change of the search text (real-time search).
     *
     * Applies a debounce: the request is only sent once the user stops typing
     * for [DEBOUNCE_MS] milliseconds. This avoids one request per keystroke.
     */
    fun onQueryChanged(query: String) {
        val cleanQuery = query.trim()

        // Ignore the change if this term was already searched and did not fail.
        // Example: after a screen rotation, Android restores the EditText text and
        // fires the listener again; the results are already in the ViewModel.
        if (cleanQuery == lastQuery && _uiState.value !is UniversityUIState.Error) return

        // Cancel the previous pending search (if any).
        searchJob?.cancel()

        if (cleanQuery.length < MIN_QUERY_LENGTH) {
            lastQuery = ""
            _uiState.value = UniversityUIState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            search(cleanQuery)
        }
    }

    /** Immediate search, without debounce (keyboard "search" key). */
    fun searchNow(query: String) {
        val cleanQuery = query.trim()
        if (cleanQuery.length < MIN_QUERY_LENGTH) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch { search(cleanQuery) }
    }

    /** Repeats the last search (used after an error). */
    fun retry() {
        if (lastQuery.isNotEmpty()) searchNow(lastQuery)
    }

    /**
     * Performs the request and translates the result (or the exception) into a UI state.
     *
     * viewModelScope runs on the main thread, so `.value` can be used safely.
     * Retrofit's suspend functions move the network work off the main thread by themselves.
     */
    private suspend fun search(query: String) {
        lastQuery = query
        _uiState.value = UniversityUIState.Loading

        try {
            val universities = repository.getUniversitiesByCountry(query)
            _uiState.value = if (universities.isEmpty()) {
                UniversityUIState.Empty(query)
            } else {
                UniversityUIState.Success(universities)
            }
        } catch (e: CancellationException) {
            // The search was cancelled because a newer one started: not an error.
            throw e
        } catch (e: IOException) {
            Log.e(TAG, "Network error", e)
            _uiState.value = UniversityUIState.Error(ErrorType.NO_CONNECTION)
        } catch (e: HttpErrorException) {
            Log.e(TAG, "Server error", e)
            _uiState.value = UniversityUIState.Error(ErrorType.SERVER, e.code)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
            _uiState.value = UniversityUIState.Error(ErrorType.UNKNOWN)
        }
    }

    companion object {
        private const val TAG = "UniversityViewModel"
        private const val MIN_QUERY_LENGTH = 3
        private const val DEBOUNCE_MS = 500L
    }
}
