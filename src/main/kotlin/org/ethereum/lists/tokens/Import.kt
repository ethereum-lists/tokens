package org.ethereum.lists.tokens

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import org.kethereum.erc55.withERC55Checksum
import org.kethereum.model.Address
import java.io.File
import java.lang.System.exit
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    if (args.isEmpty()) {
        error("importPath not specified :-)\nPlease execute like this: ./gradlew -PimportPath=/path/you/want/to/import")
    }

    val importPath = File(args[0])

    if (!importPath.isDirectory) {
        error("importPath ($importPath) is not a directory")
    }

    importPath.listFiles()?.forEach { tokenPath ->
        tokenPath.listFiles()?.forEach { tokenFile ->
            print("processing " + tokenFile.name + " .. ")
            val array = tokenFile.reader ().use { reader ->
                Klaxon().parseJsonArray(reader).map { it as JsonObject }
            }
            println("contains " + array.size + " entries ")

            array.forEach { element ->
                element.checkFields(mandatoryFields, optionalFields)
            }

            val newPath = tokenFile.name.substringAfter("-").substringBefore(".")

            val destinationPath = File(allNetworksTokenDir, newPath)

            if (destinationPath.exists() && array.isNotEmpty()) {

                var newCount = 0
                array.forEach {
                    val trimmedAddress = (it["address"] as String).trim()

                    val erc55Hex = Address(trimmedAddress).withERC55Checksum().hex
                    val jsonFile = File(destinationPath, "$erc55Hex.json")
                    it["address"] = erc55Hex
                    if (!jsonFile.exists()) {
                        println("importing $trimmedAddress")
                        newCount++
                        jsonFile.writeText(it.toJsonString(true))
                    }
                }
                println("Imported $newCount new entries")
            }
        }
    }
}