package org.ethereum.lists.tokens

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.ethereum.lists.cilib.checkFields
import org.ethereum.lists.cilib.copyFields
import java.io.File
import java.lang.System.exit
import java.nio.file.Files


val networkMapping = mapOf("etc" to 61, "eth" to 1, "kov" to 42, "rin" to 4, "rop" to 3, "rsk" to 40, "ella" to 64, "esn" to 2)

fun main(args: Array<String>) {
    checkForTokenDefinitionsInWrongPath()

    allNetworksTokenDir.listFiles().forEach { singleNetworkTokenDirectory ->
        val jsonArray = JsonArray<JsonObject>()
        singleNetworkTokenDirectory.listFiles().forEach {
            try {
                checkTokenFile(it)
                val jsonObject = Parser().parse(it.reader()) as JsonObject
                jsonArray.add(jsonObject)

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
        if (path.isDirectory
                && !Files.isSameFile((path.parentFile ?: path).toPath(), allNetworksTokenDir.toPath())
                && !path.absolutePath.contains("/test_tokens/")) {
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
