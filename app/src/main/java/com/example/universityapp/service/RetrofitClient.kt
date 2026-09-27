package com.example.universityapp.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton that builds and exposes the Retrofit instance.
 *
 * Using an `object` guarantees a single Retrofit instance for the whole app,
 * avoiding the cost of rebuilding it on every request.
 */
object RetrofitClient {

    // The Hipolabs API only works over HTTP. Cleartext traffic to this domain
    // is explicitly allowed in res/xml/network_security_config.xml.
    private const val BASE_URL = "http://universities.hipolabs.com/"

    /** Lazily created service: it is only built the first time it is accessed. */
    val universityService: UniversityService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Gson converts the JSON response into Kotlin objects (University).
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UniversityService::class.java)
    }
}
