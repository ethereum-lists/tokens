package org.ethereum.lists.tokens

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.ethereum.lists.cilib.checkFields
import org.ethereum.lists.cilib.copyFields
import org.kethereum.erc55.hasValidEIP55Checksum
import org.kethereum.model.Address
import java.io.File

val outDir = File("build/output")

val websiteRegex = Regex("^https?://.*\\..*")

fun main(args: Array<String>) {


    File("tokens").listFiles().forEach { token_directory ->
        val jsonArray = JsonArray<JsonObject>()
        token_directory.listFiles().forEach {
            val jsonObject = Parser().parse(it.reader()) as JsonObject
            val address = Address(jsonObject["address"] as String)
            when {
                !address.hasValidEIP55Checksum()
                -> throw IllegalArgumentException("The address is not valid with ERC-55 checksum " + address.toString())

                it.name != "${address.hex}.json"
                -> throw IllegalArgumentException("Filename must be the address + .json for \n" + it.name + " \n" + address.hex)
            }
            if (jsonObject.containsKey("website")) {
                val website = jsonObject["website"] as String
                if (website.isNotEmpty() && !website.matches(websiteRegex)) {
                    throw IllegalArgumentException("Website $website invalid for $address")
                }
            }
            jsonArray.add(jsonObject)
        }

        val mandatoryFields = listOf("name", "symbol", "address", "decimals")
        val optionalFields = listOf("comment", "logo", "support", "community", "website", "github", "img-16x16", "img-128x128", "social", "ens_address")

        jsonArray.checkFields(mandatoryFields, optionalFields)
        jsonArray.writeJSON("full", token_directory.name)
        jsonArray.copyFields(mandatoryFields).writeJSON("minified", token_directory.name)
    }
}

fun JsonArray<*>.writeJSON(pathName: String, filename: String) {
    val fullOutDir = File(outDir, pathName)
    fullOutDir.mkdirs()
    val fullOutFile = File(fullOutDir, filename + ".json")

    fullOutFile.writeText(toJsonString(false))
}