package com.github.core.domain

import com.github.core.domain.error.DomainError
import com.github.core.domain.fake.FakeGithubRepository
import com.github.core.domain.usecase.GetRepositoryDetailUseCase
import com.github.core.domain.usecase.GetUserProfileUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DetailAndUserUseCaseTest {
    private val fakeRepository = FakeGithubRepository()

    @Test
    fun testGetRepositoryDetailSuccess() = runTest {
        println("🧪 [core-domain] Testing GetRepositoryDetailUseCase: valid owner and repo...")
        val useCase = GetRepositoryDetailUseCase(repository = fakeRepository)
        val result = useCase.execute(owner = "dinkar1708", repo = "github-core-kmp")

        assertTrue(result.isSuccess)
        val repo = result.getOrNull()
        assertEquals("github-core-kmp", repo?.name)
        println("✅ [core-domain] GetRepositoryDetailUseCase returned repo: ${repo?.fullName}")
    }

    @Test
    fun testGetRepositoryDetailEmptyParams() = runTest {
        println("🧪 [core-domain] Testing GetRepositoryDetailUseCase: empty params rejection...")
        val useCase = GetRepositoryDetailUseCase(repository = fakeRepository)
        val result = useCase.execute(owner = "  ", repo = "github-core-kmp")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is DomainError.ValidationError)
        assertEquals("owner", error.field)
        println("✅ [core-domain] Correctly rejected empty owner parameter")
    }

    @Test
    fun testGetUserProfileSuccess() = runTest {
        println("🧪 [core-domain] Testing GetUserProfileUseCase: valid username...")
        val useCase = GetUserProfileUseCase(repository = fakeRepository)
        val result = useCase.execute(username = "dinkar1708")

        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertEquals("dinkar1708", user?.login)
        assertEquals("Dinakar Prasad Maurya", user?.name)
        println("✅ [core-domain] GetUserProfileUseCase returned user: ${user?.name}")
    }
}
