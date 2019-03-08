package org.ethereum.lists.tokens.model

data class Deprecation(
        val new_address: String?,
        val announcement_url: String? = null,
        val time: String? = null, // ISO8601
        val migration_type: String? = null // currently only "auto" is allowed
)