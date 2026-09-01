package com.github.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Repository(
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val stargazersCount: Int,
    val forksCount: Int,
    val language: String?,
    val htmlUrl: String,
    val owner: Owner
)

@Serializable
data class Owner(
    val id: Long,
    val login: String,
    val avatarUrl: String
)

@Serializable
data class SearchResult(
    val totalCount: Int,
    val items: List<Repository>
)
