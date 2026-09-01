package com.github.core.domain

import com.github.core.domain.error.DomainError
import com.github.core.domain.fake.FakeGithubRepository
import com.github.core.domain.model.SortField
import com.github.core.domain.model.SortOrder
import com.github.core.domain.usecase.SearchRepositoriesUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchRepositoriesUseCaseTest {
    private val fakeRepository = FakeGithubRepository()
    private val useCase = SearchRepositoriesUseCase(repository = fakeRepository)

    @Test
    fun testSuccessfulSearchExecution() = runTest {
        println("🧪 [core-domain] Testing SearchRepositoriesUseCase: successful search...")
        val result = useCase.execute(
            query = "kmp-headless",
            sortField = SortField.STARS,
            sortOrder = SortOrder.DESC,
            page = 1,
            perPage = 20
        )

        assertTrue(result.isSuccess)
        val searchResult = result.getOrNull()
        assertEquals(1, searchResult?.totalCount)
        assertEquals("github-core-kmp", searchResult?.items?.first()?.name)
        println("✅ [core-domain] SearchRepositoriesUseCase returned verified results: ${searchResult?.items?.first()?.fullName}")
    }

    @Test
    fun testEmptyQueryFailsValidation() = runTest {
        println("🧪 [core-domain] Testing SearchRepositoriesUseCase: empty query input...")
        val result = useCase.execute(query = "   ")
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is DomainError.ValidationError)
        assertEquals("query", error.field)
        println("✅ [core-domain] UseCase rejected invalid query with ValidationError: ${error.message}")
    }

    @Test
    fun testRepositoryFailurePropagates() = runTest {
        println("🧪 [core-domain] Testing SearchRepositoriesUseCase: network error propagation...")
        fakeRepository.searchResultToReturn = Result.failure(
            DomainError.NetworkError("GitHub API unavailable", statusCode = 503)
        )

        val result = useCase.execute(query = "compose")
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is DomainError.NetworkError)
        assertEquals(503, error.statusCode)
        println("✅ [core-domain] UseCase correctly propagated NetworkError: ${error.message}")
    }
}
