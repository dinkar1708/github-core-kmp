package com.github.core.domain

import com.github.core.domain.error.DomainError
import com.github.core.domain.validation.QueryValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QueryValidatorTest {
    private val validator = QueryValidator(minLength = 1, maxLength = 256)

    @Test
    fun testValidQuery() {
        println("🧪 [core-domain] Testing QueryValidator: valid query 'kotlin'...")
        val result = validator.validate("  kotlin  ")
        assertTrue(result.isSuccess)
        assertEquals("kotlin", result.getOrNull())
        println("✅ [core-domain] QueryValidator trimmed and accepted query successfully")
    }

    @Test
    fun testBlankQueryRejected() {
        println("🧪 [core-domain] Testing QueryValidator: blank query rejection...")
        val result = validator.validate("   ")
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is DomainError.ValidationError)
        assertEquals("query", exception.field)
        println("✅ [core-domain] QueryValidator correctly rejected blank query: ${exception.message}")
    }

    @Test
    fun testMaxLengthExceeded() {
        println("🧪 [core-domain] Testing QueryValidator: max length query bound...")
        val longQuery = "a".repeat(300)
        val result = validator.validate(longQuery)
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is DomainError.ValidationError)
        assertEquals("query", exception.field)
        println("✅ [core-domain] QueryValidator correctly rejected overly long query")
    }
}
