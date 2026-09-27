package com.example.universityapp.viewmodel

import com.example.universityapp.model.University

/**
 * All the possible states of the main screen.
 *
 * A sealed class works like an enum, but each state can carry its own data
 * (for example, Success carries the list). The `when` in the Activity is forced
 * by the compiler to handle every state, so no case can be forgotten.
 */
sealed class UniversityUIState {

    /** Initial state: nothing has been searched yet (or the query is too short). */
    object Idle : UniversityUIState()

    /** A request is in progress: the ProgressBar must be shown. */
    object Loading : UniversityUIState()

    /** The request succeeded and returned at least one university. */
    data class Success(val universities: List<University>) : UniversityUIState()

    /** The request succeeded but the country has no universities (or does not exist). */
    data class Empty(val query: String) : UniversityUIState()

    /** The request failed. The type lets the UI pick the right message. */
    data class Error(val type: ErrorType, val httpCode: Int? = null) : UniversityUIState()
}

/**
 * Error categories. The ViewModel does not build user-facing texts (it has no Context);
 * the Activity maps each type to a string resource.
 */
enum class ErrorType {
    NO_CONNECTION, // No internet or the server could not be reached
    SERVER,        // The server answered with an HTTP error code
    UNKNOWN        // Any other unexpected error (e.g. malformed JSON)
}
