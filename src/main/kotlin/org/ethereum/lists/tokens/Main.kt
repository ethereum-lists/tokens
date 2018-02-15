package org.ethereum.lists.tokens

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.ethereum.lists.cilib.checkFields
import org.ethereum.lists.cilib.copyFields
import java.io.File

val outDir = File("build/output")

fun main(args: Array<String>) {

    File("tokens").listFiles().forEach {

        val jsonArray = JsonArray<JsonObject>()
        it.listFiles().forEach {
            val jsonObject = Parser().parse(it.reader()) as JsonObject
            jsonArray.add(jsonObject)
        }

        val mandatoryFields = listOf("name", "symbol", "address", "decimals")
        val optionalFields = listOf("logo", "support", "community", "website", "github", "img-16x16", "img-128x128", "social", "ens_address")

        jsonArray.checkFields(mandatoryFields, optionalFields)
        jsonArray.writeJSON("full", it.name)
        jsonArray.copyFields(mandatoryFields).writeJSON("minified", it.name)
    }
}

fun JsonArray<*>.writeJSON(pathName: String, filename: String) {
    val fullOutDir = File(outDir, pathName)
    fullOutDir.mkdirs()
    val fullOutFile = File(fullOutDir, filename + ".json")

    fullOutFile.writeText(toJsonString(false))
}