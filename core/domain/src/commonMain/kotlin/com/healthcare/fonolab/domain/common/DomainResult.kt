package com.healthcare.fonolab.domain.common

import kotlin.coroutines.cancellation.CancellationException

/**
 * Explicit result type used by use cases. Failures carry a typed [DomainError] instead of
 * leaking exceptions to the presentation layer.
 */
sealed interface DomainResult<out T> {
    data class Success<T>(val value: T) : DomainResult<T>
    data class Failure(val error: DomainError) : DomainResult<Nothing>
}

inline fun <T, R> DomainResult<T>.map(transform: (T) -> R): DomainResult<R> = when (this) {
    is DomainResult.Success -> DomainResult.Success(transform(value))
    is DomainResult.Failure -> this
}

inline fun <T, R> DomainResult<T>.flatMap(transform: (T) -> DomainResult<R>): DomainResult<R> = when (this) {
    is DomainResult.Success -> transform(value)
    is DomainResult.Failure -> this
}

fun <T> DomainResult<T>.valueOrNull(): T? = (this as? DomainResult.Success)?.value

/** Marker for validation problems; each feature exposes its own typed issues. */
interface ValidationIssue

sealed interface DomainError {
    data class Validation(val issues: List<ValidationIssue>) : DomainError
    data class NotFound(val entity: EntityType) : DomainError
    data class InvalidState(val reason: InvalidStateReason) : DomainError
    data class Storage(val cause: Throwable) : DomainError
    data class Content(val detail: String) : DomainError
}

enum class EntityType { PATIENT, GOAL, SESSION, ACTIVITY, CONTENT }

enum class InvalidStateReason {
    PATIENT_ARCHIVED,
    SESSION_ALREADY_IN_PROGRESS,
    SESSION_NOT_IN_PROGRESS,
    SESSION_ALREADY_CLOSED,
    ACTIVITY_ALREADY_CLOSED,
    DEMO_CONTENT_ALREADY_LOADED,
}

/**
 * Wraps a storage operation and converts unexpected exceptions into [DomainError.Storage].
 * Coroutine cancellation is always propagated.
 */
inline fun <T> storageCall(block: () -> T): DomainResult<T> = try {
    DomainResult.Success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (exception: Exception) {
    DomainResult.Failure(DomainError.Storage(exception))
}
