package com.github.core.domain

import com.github.core.domain.error.DomainError
import com.github.core.domain.validation.PaginationValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaginationValidatorTest {
    private val validator = PaginationValidator(maxPerPage = 100, defaultPerPage = 30)

    @Test
    fun testValidPagination() {
        println("🧪 [core-domain] Testing PaginationValidator: page=2, perPage=50...")
        val result = validator.validate(page = 2, perPage = 50)
        assertTrue(result.isSuccess)
        val pagination = result.getOrNull()
        assertEquals(2, pagination?.page)
        assertEquals(50, pagination?.perPage)
        println("✅ [core-domain] PaginationValidator accepted valid parameters")
    }

    @Test
    fun testDefaultPerPage() {
        println("🧪 [core-domain] Testing PaginationValidator: default perPage resolution...")
        val result = validator.validate(page = 1, perPage = null)
        assertTrue(result.isSuccess)
        assertEquals(30, result.getOrNull()?.perPage)
        println("✅ [core-domain] Default perPage resolved to 30")
    }

    @Test
    fun testInvalidPageRejected() {
        println("🧪 [core-domain] Testing PaginationValidator: invalid page 0 rejection...")
        val result = validator.validate(page = 0)
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is DomainError.ValidationError)
        assertEquals("page", exception.field)
        println("✅ [core-domain] Rejected invalid page: ${exception.message}")
    }

    @Test
    fun testExceedingMaxPerPageRejected() {
        println("🧪 [core-domain] Testing PaginationValidator: perPage > 100 rejection...")
        val result = validator.validate(page = 1, perPage = 150)
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is DomainError.ValidationError)
        assertEquals("perPage", exception.field)
        println("✅ [core-domain] Rejected excessive perPage: ${exception.message}")
    }
}
