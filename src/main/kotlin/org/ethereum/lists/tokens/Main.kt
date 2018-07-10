package org.ethereum.lists.tokens

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.squareup.moshi.Moshi
import org.ethereum.lists.cilib.checkFields
import org.ethereum.lists.cilib.copyFields
import org.ethereum.lists.tokens.model.Token
import org.kethereum.erc55.hasValidEIP55Checksum
import org.kethereum.erc55.withERC55Checksum
import org.kethereum.model.Address
import java.io.File
import java.lang.System.exit
import java.nio.file.Files


val networkMapping = mapOf("etc" to 61, "eth" to 1, "kov" to 42, "rin" to 4, "rop" to 3, "rsk" to 40, "ella" to 64)


fun main(args: Array<String>) {
    checkForTokenDefinitionsInWrongPath()

    val moshi = Moshi.Builder().build()

    allNetworksTokenDir.listFiles().forEach { singleNetworkTokenDirectory ->
        val jsonArray = JsonArray<JsonObject>()
        singleNetworkTokenDirectory.listFiles().forEach {
            try {
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
                moshi.adapter(Token::class.java).fromJson(it.readText())
            } catch (e: Exception) {
                println("Problem with $it: $e")
                exit(1)
            }
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
            path.list().firstOrNull { it.startsWith("0x") }?.let {
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
