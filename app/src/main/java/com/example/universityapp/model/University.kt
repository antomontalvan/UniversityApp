package com.example.universityapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Represents a single university as returned by the Hipolabs Universities API.
 *
 * Example JSON item:
 * {
 *   "name": "Universidad de Buenos Aires",
 *   "country": "Argentina",
 *   "alpha_two_code": "AR",
 *   "state-province": "Ciudad Autónoma de Buenos Aires",
 *   "domains": ["uba.ar"],
 *   "web_pages": ["http://www.uba.ar/"]
 * }
 *
 * @SerializedName maps JSON keys that are not idiomatic Kotlin names
 * (snake_case or containing a hyphen) to camelCase properties.
 *
 * Implementing Serializable allows sending a University object from
 * MainActivity to DetailActivity as an Intent extra.
 */
data class University(
    val name: String,
    val country: String,
    @SerializedName("alpha_two_code")
    val countryCode: String?,
    @SerializedName("state-province")
    val stateProvince: String?, // Frequently null in the API response
    val domains: List<String>?,
    @SerializedName("web_pages")
    val webPages: List<String>?
) : Serializable {

    /** Returns the first official website, or null if the API did not provide one. */
    val mainWebPage: String?
        get() = webPages?.firstOrNull()
}
