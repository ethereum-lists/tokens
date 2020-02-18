package org.ethereum.lists.tokens

import com.beust.klaxon.Klaxon
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import org.ethereum.lists.tokens.model.Token
import org.kethereum.erc55.hasValidERC55Checksum
import org.kethereum.erc55.isValid
import org.kethereum.erc55.withERC55Checksum
import org.kethereum.model.Address
import java.io.File
import java.math.BigInteger
import java.time.format.DateTimeFormatter

open class InvalidTokenException(message: String) : IllegalArgumentException(message)
class InvalidChecksum(message: String) : InvalidTokenException("The address is not valid with ERC-55 checksum $message")

class InvalidAddress(address: Address) : InvalidTokenException("The address is not valid $address")
class InvalidDecimals : InvalidTokenException("Decimals must be a number")
class InvalidFileName : InvalidTokenException("Filename must be the address + .json")
class InvalidWebsite : InvalidTokenException("Website invalid")
class InvalidJSON(message: String?) : InvalidTokenException("JSON invalid $message")
class InvalidDeprecationMigrationType : InvalidTokenException("Invalid Deprecation Migration type - currently only auto and instructions: is allowed")
class InvalidDeprecationTime : InvalidTokenException("Invalid Deprecation Time - Must be ISO8601")

fun checkTokenFile(file: File) {
    val jsonObject = Klaxon().parseJsonObject(file.reader())
    val address = Address(jsonObject["address"] as String)
    when {
        !address.isValid() -> throw InvalidAddress(address)

        !address.hasValidERC55Checksum()
        -> throw InvalidChecksum(address.toString() + " expected: " + address.withERC55Checksum())

        file.name != "${address.hex}.json" -> throw InvalidFileName()
    }
    if (jsonObject["decimals"] !is Int) {
        throw InvalidDecimals()
    }
    if (jsonObject.containsKey("website")) {
        val website = jsonObject["website"] as String
        if (website.isNotEmpty() && !website.matches(websiteRegex)) {
            throw InvalidWebsite()
        }
    }
    try {
        val token = moshi.adapter(Token::class.java).failOnUnknown().fromJson(file.readText())

        token?.deprecation?.let {
            val safeMigrationType: String = it.migration_type ?: "auto"
            when {
                safeMigrationType == "auto" || safeMigrationType.startsWith("instructions:") -> Unit
                safeMigrationType.startsWith("newchain:auto:") -> BigInteger(safeMigrationType.replace("newchain:auto:", ""))
                else -> throw InvalidDeprecationMigrationType()
            }


            it.time?.let {
                try {
                    DateTimeFormatter.ISO_DATE_TIME.parse(it)
                } catch (e: Exception) {
                    throw InvalidDeprecationTime()
                }
            }
        }

    } catch (e: JsonEncodingException) {
        throw InvalidJSON(e.message)
    } catch (e: JsonDataException) {
        throw InvalidJSON(e.message)
    }

}