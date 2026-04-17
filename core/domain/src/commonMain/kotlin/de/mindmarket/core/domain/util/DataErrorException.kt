package de.mindmarket.core.domain.util

class DataErrorException(
    val error: DataError
): Exception()