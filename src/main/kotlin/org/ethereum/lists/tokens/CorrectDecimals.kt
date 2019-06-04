package org.ethereum.lists.tokens

import com.beust.klaxon.Klaxon

fun main() {

    allNetworksTokenDir.listFiles().forEach { singleNetworkTokenDirectory ->
        singleNetworkTokenDirectory.listFiles().forEach {
            val jsonObject =it.reader().use { reader ->
                Klaxon().parseJsonObject(reader)
            }
            val decimals = jsonObject["decimals"]
            if (decimals is String) {
                println("got string decimal - rewrite")
                jsonObject["decimals"] =  Integer.parseInt( decimals )
            }
            it.writeText(jsonObject.toJsonString(true))
        }
    }
}
