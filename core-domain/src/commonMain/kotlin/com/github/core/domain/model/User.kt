package com.github.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,
    val login: String,
    val name: String? = null,
    val avatarUrl: String,
    val bio: String? = null,
    val publicRepos: Int = 0,
    val followers: Int = 0,
    val following: Int = 0,
    val htmlUrl: String,
    val location: String? = null,
    val company: String? = null,
    val blog: String? = null
)
