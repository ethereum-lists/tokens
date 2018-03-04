package org.ethereum.lists.tokens

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser

fun main(args: Array<String>) {

    allNetworksTokenDir.listFiles().forEach { singleNetworkTokenDirectory ->
        singleNetworkTokenDirectory.listFiles().forEach {
            val reader = it.reader()
            val jsonObject = Parser().parse(reader) as JsonObject
            val decimals = jsonObject["decimals"]
            if (decimals is String) {
                println("got string decimal - rewrite")
                jsonObject["decimals"] =  Integer.parseInt( decimals )
            }
            reader.close()
            it.writeText(jsonObject.toJsonString(true))
        }
    }
}
