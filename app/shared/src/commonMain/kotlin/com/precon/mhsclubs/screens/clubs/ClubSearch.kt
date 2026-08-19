package com.precon.mhsclubs.screens.clubs

import com.precon.mhsclubs.model.Club

/**
 * Extension for fuzzy-matching a list of clubs against a query string.
 *
 * Matches against name, code, and description.
 */
fun Iterable<Club>.fuzzyMatch(query: String): List<Club> {
    if (query.isBlank()) return this.toList()

    val normalizedQuery = query.trim().lowercase()
    
    // First, try exact containment matches in priority order (name, code, then description)
    val exactMatches = filter { club ->
        club.name.lowercase().contains(normalizedQuery) ||
                club.code.lowercase().contains(normalizedQuery) ||
                club.description.lowercase().contains(normalizedQuery)
    }
    
    if (exactMatches.isNotEmpty()) return exactMatches

    // If no exact matches, try "fuzzy" matching by checking if all characters of the query
    // appear in order in the target string.
    return filter { club ->
        club.name.isFuzzyMatch(normalizedQuery) ||
                club.code.isFuzzyMatch(normalizedQuery) ||
                club.description.isFuzzyMatch(normalizedQuery)
    }
}

private fun String.isFuzzyMatch(query: String): Boolean {
    val target = this.lowercase()
    var queryIdx = 0
    for (char in target) {
        if (queryIdx < query.length && char == query[queryIdx]) {
            queryIdx++
        }
    }
    return queryIdx == query.length
}
