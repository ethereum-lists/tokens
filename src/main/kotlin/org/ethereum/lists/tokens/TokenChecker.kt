package org.ethereum.lists.tokens

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import kotlinx.coroutines.delay
import org.ethereum.lists.chains.https.getChains
import org.ethereum.lists.tokens.model.Token
import org.kethereum.erc55.hasValidERC55Checksum
import org.kethereum.erc55.isValid
import org.kethereum.erc55.withERC55Checksum
import org.kethereum.erc1191.hasValidERC1191Checksum
import org.kethereum.erc1191.withERC1191Checksum
import org.kethereum.model.Address
import org.kethereum.model.ChainId
import org.kethereum.rpc.*
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
class InvalidDeprecationMigrationType :
    InvalidTokenException("Invalid Deprecation Migration type - currently only auto, instructions and announcement is allowed")

class InvalidDeprecationTime : InvalidTokenException("Invalid Deprecation Time - Must be ISO8601")

val onChainCheckFile = File("onChainCheck.lst")
val notToProcessFiles by lazy {
    onChainCheckFile.readText().split("\n")
}

val onChainIgnore by lazy {
    File("onChainIgnore.lst").readText().split("\n")
}

val rpcMap = mutableMapOf<BigInteger, EthereumRPC>()

val chains = getChains()
fun getRPC(chainId: ChainId): EthereumRPC? {
    if (chainId.value.toLong() == 2020L) {
        //https://github.com/ethereum-lists/tokens/issues/770
        return null
    }
    if (rpcMap[chainId.value] == null) {
        val chain = chains?.first { it.chainId == chainId.value.toLong() }

        val rpc = chain?.rpc?.firstOrNull {
            try {
                HttpEthereumRPC(it).chainId()?.value == chainId.value
            } catch (e: Exception) {
                false
            }
        }

        if (rpc != null) {
            rpcMap[chainId.value] = HttpEthereumRPC(rpc)
        }

    }
    return rpcMap[chainId.value]
}

suspend fun checkTokenFile(file: File, onChainCheck: Boolean = false, chainId: ChainId? = null) {

    file.reader().use { reader ->
        Klaxon().parseJsonObject(reader).checkFields(mandatoryFields, optionalFields)
    }
    val handle = file.name.removeSuffix(".json")
    if (onChainCheck && (notToProcessFiles.contains(handle) || onChainIgnore.contains(handle))) {
        return
    }

    val jsonObject = Klaxon().parseJsonObject(file.reader())
    val address = Address(jsonObject["address"] as String)
    val addressEIP1191 = if (jsonObject["address_eip1191"] != null) Address(jsonObject["address_eip1191"] as String) else null

    when {
        !address.isValid() -> throw InvalidAddress(address)

        addressEIP1191 != null && !(addressEIP1191.isValid()) -> throw InvalidAddress(address)

        (!address.hasValidERC55Checksum())
        -> throw InvalidChecksum(address.toString() + " expected: " + address.withERC55Checksum())

        (addressEIP1191 != null && chainId != null && !addressEIP1191.hasValidERC1191Checksum(chainId))
        -> throw Invalid1191Checksum(addressEIP1191.toString() + " expected: " + addressEIP1191.withERC1191Checksum(chainId))

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

    val rpc = chainId?.let { getRPC(it) }
    if (onChainCheck && rpc != null) {

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
                safeMigrationType == "auto" -> Unit
                safeMigrationType.startsWith("instructions:") -> Unit
                safeMigrationType.startsWith("announcement:") -> Unit
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
    block: suspend () -> T
): T {
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

fun JsonObject.checkFields(mandatoryFields: List<String>, optionalFields: List<String>) {
    if (!keys.containsAll(mandatoryFields)) {
        throw IllegalArgumentException("$this does not contain " + mandatoryFields.minus(keys))
    }

    mandatoryFields.forEach {
        if (this[it] is String && string(it)?.isBlank() == true) {
            throw IllegalArgumentException("$this has blank value for $it")
        }
    }


    val unknownFields = keys.minus(mandatoryFields.plus(optionalFields))
    if (unknownFields.isNotEmpty()) {
        throw IllegalArgumentException("$this contains unknown " + unknownFields)
    }

}