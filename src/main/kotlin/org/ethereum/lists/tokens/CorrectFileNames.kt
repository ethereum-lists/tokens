package org.ethereum.lists.tokens

import com.beust.klaxon.Klaxon
import org.kethereum.model.Address
import java.io.File

fun main() {

    allNetworksTokenDir.listFiles().forEach { singleNetworkTokenDirectory ->
        singleNetworkTokenDirectory.listFiles().forEach {
            val jsonObject = it.reader().use { reader ->
                Klaxon().parseJsonObject(reader)
            }
            val address = Address(jsonObject["address"] as String)
            if (it.name != "${address.hex}.json") {
                val result = it.renameTo(File(singleNetworkTokenDirectory, "${address.hex}.json"))
                println("processing ${it.name} -> $result")
            }
        }
    }
}
