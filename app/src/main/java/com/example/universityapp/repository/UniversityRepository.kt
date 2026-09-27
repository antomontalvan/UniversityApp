package com.example.universityapp.repository

import android.util.Log
import com.example.universityapp.model.University
import com.example.universityapp.service.RetrofitClient
import com.example.universityapp.service.UniversityService

/**
 * Single source of truth for university data (Repository Pattern).
 *
 * The ViewModel does not know whether data comes from the network, a local
 * database or a cache: it only asks the repository. Swapping the data source
 * later would not require changes in the ViewModel or the UI.
 *
 * The service is received through the constructor (manual dependency injection),
 * with a default value so it can be created without arguments.
 */
class UniversityRepository(
    private val service: UniversityService = RetrofitClient.universityService
) {

    /**
     * Fetches the universities of the given country.
     *
     * @return the list of universities (may be empty if the country has no results).
     * @throws HttpErrorException if the server answers with a non-2xx status code.
     * @throws java.io.IOException if there is no connection or the request fails.
     */
    suspend fun getUniversitiesByCountry(country: String): List<University> {
        Log.d(TAG, "Requesting universities for country: $country")

        // The coroutine suspends here until the API responds, without freezing the UI.
        val response = service.searchByCountry(country)

        if (!response.isSuccessful) {
            Log.e(TAG, "HTTP error: ${response.code()}")
            throw HttpErrorException(response.code())
        }

        val universities = response.body().orEmpty()
        Log.d(TAG, "Universities received: ${universities.size}")

        // Sorted alphabetically for a better browsing experience.
        return universities.sortedBy { it.name }
    }

    companion object {
        private const val TAG = "UniversityRepository"
    }
}

/** Thrown when the API returns an unsuccessful HTTP status code (4xx / 5xx). */
class HttpErrorException(val code: Int) : Exception("HTTP error $code")
