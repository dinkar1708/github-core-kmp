package com.github.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult<T>(
    val totalCount: Int,
    val items: List<T>,
    val incompleteResults: Boolean = false
)

enum class SortField(val paramValue: String) {
    STARS("stars"),
    FORKS("forks"),
    UPDATED("updated"),
    HELP_WANTED_ISSUES("help-wanted-issues");

    companion object {
        fun fromValue(value: String): SortField =
            entries.find { it.paramValue.equals(value, ignoreCase = true) } ?: STARS
    }
}

enum class SortOrder(val paramValue: String) {
    ASC("asc"),
    DESC("desc");

    companion object {
        fun fromValue(value: String): SortOrder =
            entries.find { it.paramValue.equals(value, ignoreCase = true) } ?: DESC
    }
}
