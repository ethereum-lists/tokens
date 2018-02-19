package org.ethereum.lists.tokens

import java.io.File

val mandatoryFields = listOf("name", "symbol", "address", "decimals")
val optionalFields = listOf("comment", "logo", "support", "community", "website", "github", "img-16x16", "img-128x128", "social", "ens_address")

val outDir = File("build/output")
val allNetworksTokenDir = File("tokens")

val websiteRegex = Regex("^https?://.*\\..*")

