package org.ethereum.lists.tokens

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.ethereum.lists.cilib.checkFields
import org.ethereum.lists.cilib.copyFields
import org.kethereum.erc55.hasValidEIP55Checksum
import org.kethereum.erc55.withERC55Checksum
import org.kethereum.model.Address
import java.io.File

val networkMapping = mapOf("etc" to 61, "eth" to 1, "kov" to 42, "rin" to 4, "rop" to 3, "rsk" to 40, "ella" to 64)

fun main(args: Array<String>) {

    allNetworksTokenDir.listFiles().forEach { singleNetworkTokenDirectory ->
        val jsonArray = JsonArray<JsonObject>()
        singleNetworkTokenDirectory.listFiles().forEach {
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

fun JsonArray<*>.writeJSON(pathName: String, filename: String) {
    val fullOutDir = File(outDir, pathName)
    fullOutDir.mkdirs()
    val fullOutFile = File(fullOutDir, filename + ".json")

    fullOutFile.writeText(toJsonString(false))
}
