package com.github.core.domain

import com.github.core.domain.error.DomainError
import com.github.core.domain.fake.FakeGithubRepository
import com.github.core.domain.usecase.GetRepositoryDetailUseCase
import com.github.core.domain.usecase.GetUserProfileUseCase
import com.github.core.domain.usecase.GetUserRepositoriesUseCase
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

    @Test
    fun testGetUserRepositoriesSuccess() = runTest {
        println("🧪 [core-domain] Testing GetUserRepositoriesUseCase: valid username and pagination...")
        val useCase = GetUserRepositoriesUseCase(repository = fakeRepository)
        val result = useCase.execute(username = "dinkar1708", page = 1, perPage = 10)

        assertTrue(result.isSuccess)
        val repos = result.getOrNull()
        assertEquals(1, repos?.size)
        assertEquals("github-core-kmp", repos?.first()?.name)
        println("✅ [core-domain] GetUserRepositoriesUseCase returned ${repos?.size} repositories")
    }

    @Test
    fun testGetUserRepositoriesEmptyUsernameFails() = runTest {
        println("🧪 [core-domain] Testing GetUserRepositoriesUseCase: empty username...")
        val useCase = GetUserRepositoriesUseCase(repository = fakeRepository)
        val result = useCase.execute(username = "   ")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is DomainError.ValidationError)
        assertEquals("username", error.field)
        println("✅ [core-domain] Correctly rejected empty username")
    }

    @Test
    fun testGetUserRepositoriesInvalidPageFails() = runTest {
        println("🧪 [core-domain] Testing GetUserRepositoriesUseCase: invalid page...")
        val useCase = GetUserRepositoriesUseCase(repository = fakeRepository)
        val result = useCase.execute(username = "dinkar1708", page = 0)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is DomainError.ValidationError)
        assertEquals("page", error.field)
        println("✅ [core-domain] Correctly rejected page 0")
    }

    @Test
    fun testThrowingMethodsForSwiftInterop() = runTest {
        println("🧪 [core-domain] Testing @Throws convenience methods for Swift async/await...")
        val detailUseCase = GetRepositoryDetailUseCase(repository = fakeRepository)
        val userProfileUseCase = GetUserProfileUseCase(repository = fakeRepository)
        val userReposUseCase = GetUserRepositoriesUseCase(repository = fakeRepository)

        val repo = detailUseCase.get("dinkar1708", "github-core-kmp")
        assertEquals("github-core-kmp", repo.name)

        val user = userProfileUseCase.get("dinkar1708")
        assertEquals("dinkar1708", user.login)

        val repos = userReposUseCase.get("dinkar1708", 1, 30)
        assertEquals(1, repos.size)
        println("✅ [core-domain] All throwing methods executed and unwrapped successfully")
    }
}
