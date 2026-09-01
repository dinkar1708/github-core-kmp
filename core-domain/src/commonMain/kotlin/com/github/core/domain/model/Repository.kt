package com.github.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Owner(
    val id: Long,
    val login: String,
    val avatarUrl: String,
    val htmlUrl: String? = null,
    val type: String = "User"
)

@Serializable
data class License(
    val key: String,
    val name: String,
    val spdxId: String? = null,
    val url: String? = null
)

@Serializable
data class Repository(
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String? = null,
    val stargazersCount: Int = 0,
    val forksCount: Int = 0,
    val language: String? = null,
    val htmlUrl: String,
    val owner: Owner,
    val openIssuesCount: Int = 0,
    val defaultBranch: String = "main",
    val createdAt: String? = null,
    val updatedAt: String? = null
)
