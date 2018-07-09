package org.ethereum.lists.tokens

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import kotlinx.serialization.json.JSON
import kotlinx.serialization.*
import org.ethereum.lists.cilib.checkFields
import org.ethereum.lists.cilib.copyFields
import org.kethereum.erc55.hasValidEIP55Checksum
import org.kethereum.erc55.withERC55Checksum
import org.kethereum.model.Address
import java.io.File
import java.nio.file.Files


val networkMapping = mapOf("etc" to 61, "eth" to 1, "kov" to 42, "rin" to 4, "rop" to 3, "rsk" to 40, "ella" to 64)

@Serializable
data class Logo(val src: String,
                @Optional
                val width: String? = null,
                @Optional
                val height: String? = null,
                @Optional
                val ipfs_hash: String? = null)

@Serializable
data class Support(val email: String,
                   @Optional
                   val url: String? = null)

@Serializable
data class Social(
        @Optional
        val blog: String? = null,
        @Optional
        val chat: String? = null,
        @Optional
        val facebook: String? = null,
        @Optional
        val forum: String? = null,
        @Optional
        val discord: String? = null,
        @Optional
        val github: String? = null,
        @Optional
        val gitter: String? = null,
        @Optional
        val instagram: String? = null,
        @Optional
        val linkedin: String? = null,
        @Optional
        val reddit: String? = null,
        @Optional
        val slack: String? = null,
        @Optional
        val telegram: String? = null,
        @Optional
        val twitter: String? = null,
        @Optional
        val Medium: String? = null,
        @Optional
        val bitcointalk: String? = null,
        @Optional
        val googleplus: String? = null,
        @Optional
        val vimeo: String? = null,
        @Optional
        val youtube: String? = null)


@Serializable
data class Token(val symbol: String,
                 val name: String,
                 val address: String,
                 val decimals: Int,
                 @Optional
                 val website: String? = null,
                 @Optional
                 val ens_address: String? = null,
                 @Optional
                 val comment: String? = null,
                 @Optional
                 val logo: Logo? = null,
                 @Optional
                 val support: Support? = null,
                 @Optional
                 val social: Social? = null)

fun main(args: Array<String>) {
    checkForTokenDefinitionsInWrongPath()

    allNetworksTokenDir.listFiles().forEach { singleNetworkTokenDirectory ->
        val jsonArray = JsonArray<JsonObject>()
        singleNetworkTokenDirectory.listFiles().forEach {
            println("processing " + it.absolutePath)
            val jsonObject = Parser().parse(it.reader()) as JsonObject
            val address = Address(jsonObject["address"] as String)
            when {
                !address.hasValidEIP55Checksum()
                -> throw IllegalArgumentException("The address is not valid with ERC-55 checksum " + address + " expected: " + address.withERC55Checksum())

                it.name != "${address.hex}.json"
                -> throw IllegalArgumentException("Filename must be the address + .json for \n" + it.name + " \n" + address.hex)
            }
            if (jsonObject["decimals"] is String) {
                throw IllegalArgumentException("Decimals must not be a string - make it a number!")
            }
            if (jsonObject.containsKey("website")) {
                val website = jsonObject["website"] as String
                if (website.isNotEmpty() && !website.matches(websiteRegex)) {
                    throw IllegalArgumentException("Website $website invalid for $address")
                }
            }
            jsonArray.add(jsonObject)
            JSON.parse<Token>(it.readText())
        }

        jsonArray.checkFields(mandatoryFields, optionalFields)
        jsonArray.writeJSON("full", singleNetworkTokenDirectory.name)
        val minified = jsonArray.copyFields(mandatoryFields)
        minified.writeJSON("minified", singleNetworkTokenDirectory.name)
        networkMapping[singleNetworkTokenDirectory.name]?.let {
            minified.writeJSON("minifiedByNetworkId", it.toString())
        }
    }
}

private fun checkForTokenDefinitionsInWrongPath() {
    File(".").walk().forEach { path ->
        if (path.isDirectory && !Files.isSameFile((path.parentFile ?: path).toPath(), allNetworksTokenDir.toPath())) {
            path.list().firstOrNull() { it.startsWith("0x") }?.let {
                throw IllegalArgumentException("There is a token definition file ($it) placed in a directory where it does not belong (${path.absolutePath})")
            }
        }
    }
}

fun JsonArray<*>.writeJSON(pathName: String, filename: String) {
    val fullOutDir = File(outDir, pathName)
    fullOutDir.mkdirs()
    val fullOutFile = File(fullOutDir, "$filename.json")

    fullOutFile.writeText(toJsonString(false))
}
