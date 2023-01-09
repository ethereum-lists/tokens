package org.ethereum.lists.tokens

import java.io.File
import kotlin.system.exitProcess

suspend fun main(args: Array<String>) {

    if (args.isEmpty()) {
        error("fileToCheck not specified :-)\nPlease execute like this: ./gradlew -PfileToCheckh=/path/you/want/to/import")
    }

    val fileToCheck = File(args[0])

    if (!fileToCheck.isFile) {
        error("fileToCheck ($fileToCheck) is not a file")
    }

    val parentFile = fileToCheck.parentFile

    if (parentFile?.parentFile == allNetworksTokenDir) {
        println("checking $fileToCheck")
        checkTokenFile(fileToCheck, true, getChainId(parentFile.name))
    } else {
        println("ignoring $fileToCheck")
    }

}

private fun error(message: String) {
    println("Error: $message")
    exitProcess(1)
}