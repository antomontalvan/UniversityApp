package com.example.universityapp.service

import com.example.universityapp.model.University
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface that describes the Hipolabs Universities API endpoints.
 *
 * Retrofit generates the real implementation at runtime: each annotated function
 * is translated into an HTTP request.
 *
 * Full request example:
 *   GET http://universities.hipolabs.com/search?country=Argentina
 */
interface UniversityService {

    /**
     * Searches universities by country name.
     *
     * - @GET("search") is appended to the base URL defined in [RetrofitClient].
     * - @Query("country") adds "?country=<value>" to the URL.
     * - The API returns a JSON array at the root level, so the body type is List<University>.
     * - `suspend` lets the call run inside a coroutine without blocking the UI thread.
     */
    @GET("search")
    suspend fun searchByCountry(
        @Query("country") country: String
    ): Response<List<University>>
}
