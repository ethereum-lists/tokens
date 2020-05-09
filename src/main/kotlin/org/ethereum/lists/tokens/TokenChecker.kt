package org.ethereum.lists.tokens

import com.beust.klaxon.Klaxon
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import kotlinx.coroutines.delay
import org.ethereum.lists.tokens.model.Token
import org.kethereum.erc55.hasValidERC55Checksum
import org.kethereum.erc55.isValid
import org.kethereum.erc55.withERC55Checksum
import org.kethereum.erc1191.hasValidERC1191Checksum
import org.kethereum.erc1191.withERC1191Checksum
import org.kethereum.model.Address
import org.kethereum.model.ChainId
import org.kethereum.rpc.min3.getMin3RPC
import org.komputing.kethereum.erc20.ERC20RPCConnector
import java.io.File
import java.io.IOException
import java.math.BigInteger
import java.time.format.DateTimeFormatter

open class InvalidTokenException(message: String) : IllegalArgumentException(message)
class InvalidChecksum(message: String) : InvalidTokenException("The address is not valid with ERC-55 checksum $message")
class Invalid1191Checksum(message: String) : InvalidTokenException("The address is not valid with EIP-1191 checksum $message")

class InvalidAddress(address: Address) : InvalidTokenException("The address is not valid $address")
class InvalidDecimals : InvalidTokenException("Decimals must be a number")
class InvalidSymbol : InvalidTokenException("Symbol must be a string")
class InvalidFileName : InvalidTokenException("Filename must be the address + .json")
class InvalidWebsite : InvalidTokenException("Website invalid")
class InvalidJSON(message: String?) : InvalidTokenException("JSON invalid $message")
class InvalidDeprecationMigrationType : InvalidTokenException("Invalid Deprecation Migration type - currently only auto and instructions: is allowed")
class InvalidDeprecationTime : InvalidTokenException("Invalid Deprecation Time - Must be ISO8601")

val onChainCheckFile = File("onChainCheck.lst")
val notToProcessFiles by lazy {
    onChainCheckFile.readText().split("\n")
}

val onChainIgnore by lazy {
    File("onChainIgnore.lst").readText().split("\n")
}

suspend fun checkTokenFile(file: File, onChainCheck: Boolean = false, chainId: ChainId? = null) {
    val handle = file.name.removeSuffix(".json")
    if (onChainCheck && (notToProcessFiles.contains(handle) || onChainIgnore.contains(handle))) {
        return
    }

    val jsonObject = Klaxon().parseJsonObject(file.reader())
    val address = Address(jsonObject["address"] as String)
    val address_eip1191 = if (jsonObject["address_eip1191"] != null) Address(jsonObject["address_eip1191"] as String) else null

    when {
        !address.isValid() -> throw InvalidAddress(address)

        address_eip1191 != null && !(address_eip1191.isValid()) -> throw InvalidAddress(address)

        (!address.hasValidERC55Checksum())
        -> throw InvalidChecksum(address.toString() + " expected: " + address.withERC55Checksum())

        (address_eip1191 != null && chainId != null && !address_eip1191.hasValidERC1191Checksum(chainId))
        -> throw Invalid1191Checksum(address_eip1191.toString() + " expected: " + address_eip1191.withERC1191Checksum(chainId))

        file.name != "${address.hex}.json" -> throw InvalidFileName()
    }
    if (jsonObject["decimals"] !is Int) {
        throw InvalidDecimals()
    }

    if (jsonObject["symbol"] !is String) {
        throw InvalidSymbol()
    }

    val decimals = jsonObject["decimals"] as Int
    val symbol = jsonObject["symbol"] as String

    if (onChainCheck) {
        val rpc = getMin3RPC(listOf("https://in3-v2.slock.it/mainnet/nd-1"))
        val contract = ERC20RPCConnector(address, rpc)

        if (jsonObject["invalid_erc20_decimals"] as? Boolean != true) {

            val contractDecimals = retryIO(times = 7) { contract.decimals() }
            if (contractDecimals != decimals.toBigInteger()) {
                throw InvalidTokenException("decimals reported from contract ($contractDecimals) do not match decimals in json ($decimals)")
            }
        }

        if (jsonObject["invalid_erc20_symbol"] as? Boolean != true) {
            val contractSymbol = retryIO(times = 7) { contract.symbol() }
            if (contractSymbol != symbol) {
                throw InvalidTokenException("symbol reported from contract ($contractSymbol) do not match symbol in json ($symbol)")
            }
        }
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

    onChainCheckFile.appendText(handle + "\n")
}

suspend fun <T> retryIO(
        times: Int = Int.MAX_VALUE,
        initialDelay: Long = 100, // 0.1 second
        maxDelay: Long = 100000,
        factor: Double = 2.0,
        block: suspend () -> T): T {
    var currentDelay = initialDelay
    repeat(times - 1) { _ ->
        try {
            block()?.let { return it }
        } catch (e: IOException) {
            // you can log an error here and/or make a more finer-grained
            // analysis of the cause to see if retry is needed
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // last attempt
}