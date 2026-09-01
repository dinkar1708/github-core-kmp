package com.github.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchRepositoriesResponseDto(
    @SerialName("total_count")
    val totalCount: Int,
    @SerialName("incomplete_results")
    val incompleteResults: Boolean = false,
    @SerialName("items")
    val items: List<RepositoryDto> = emptyList()
)

@Serializable
data class RepositoryDto(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("stargazers_count")
    val stargazersCount: Int = 0,
    @SerialName("forks_count")
    val forksCount: Int = 0,
    @SerialName("language")
    val language: String? = null,
    @SerialName("html_url")
    val htmlUrl: String,
    @SerialName("owner")
    val owner: OwnerDto,
    @SerialName("open_issues_count")
    val openIssuesCount: Int = 0,
    @SerialName("default_branch")
    val defaultBranch: String = "main",
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class OwnerDto(
    @SerialName("id")
    val id: Long,
    @SerialName("login")
    val login: String,
    @SerialName("avatar_url")
    val avatarUrl: String,
    @SerialName("html_url")
    val htmlUrl: String? = null,
    @SerialName("type")
    val type: String = "User"
)

@Serializable
data class UserDto(
    @SerialName("id")
    val id: Long,
    @SerialName("login")
    val login: String,
    @SerialName("name")
    val name: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String,
    @SerialName("bio")
    val bio: String? = null,
    @SerialName("public_repos")
    val publicRepos: Int = 0,
    @SerialName("followers")
    val followers: Int = 0,
    @SerialName("following")
    val following: Int = 0,
    @SerialName("html_url")
    val htmlUrl: String,
    @SerialName("location")
    val location: String? = null,
    @SerialName("company")
    val company: String? = null
)
