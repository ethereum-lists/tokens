package org.ethereum.lists.tokens

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

val mandatoryFields = listOf("name", "symbol", "address", "decimals")
val optionalFields = listOf("comment", "logo", "support", "community", "website", "github", "img-16x16", "img-128x128", "social", "ens_address", "deprecation", "type")

val outDir = File("build/output")
val allNetworksTokenDir = File("tokens")

val websiteRegex = Regex("^https?://.*\\..*")

val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
