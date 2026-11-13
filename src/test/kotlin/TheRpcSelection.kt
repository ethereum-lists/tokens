import com.sun.net.httpserver.HttpServer
import org.ethereum.lists.tokens.selectRpcUrl
import org.junit.Test
import org.kethereum.model.ChainId
import java.net.InetSocketAddress
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class TheRpcSelection {

    @Test
    fun fallsBackWhenBlockNumberFailsOrIsNull() {
        LocalJsonRpcServer("0x1", BlockNumberResponse.ERROR).use { failing ->
            LocalJsonRpcServer("0x1", BlockNumberResponse.NULL).use { missing ->
                LocalJsonRpcServer("0x1", BlockNumberResponse.VALUE).use { healthy ->
                    assertEquals(
                        healthy.url,
                        selectRpcUrl(listOf(failing.url, missing.url, healthy.url), ChainId(1))
                    )
                    assertEquals(listOf("eth_chainId", "eth_blockNumber"), failing.methods)
                    assertEquals(listOf("eth_chainId", "eth_blockNumber"), missing.methods)
                    assertEquals(listOf("eth_chainId", "eth_blockNumber"), healthy.methods)
                }
            }
        }
    }

    @Test
    fun skipsAHealthyEndpointOnTheWrongChain() {
        LocalJsonRpcServer("0x2", BlockNumberResponse.VALUE).use { wrongChain ->
            LocalJsonRpcServer("0x1", BlockNumberResponse.VALUE).use { correctChain ->
                assertEquals(
                    correctChain.url,
                    selectRpcUrl(listOf(wrongChain.url, correctChain.url), ChainId(1))
                )
                assertEquals(listOf("eth_chainId"), wrongChain.methods)
            }
        }
    }

    @Test
    fun failsWhenConfiguredEndpointsAreAllUnhealthy() {
        LocalJsonRpcServer("0x1", BlockNumberResponse.ERROR).use { unavailable ->
            assertFailsWith<IllegalStateException> {
                selectRpcUrl(listOf(unavailable.url), ChainId(1))
            }
            assertEquals(listOf("eth_chainId", "eth_blockNumber"), unavailable.methods)
        }
    }

    @Test
    fun preservesNullForAnEmptyEndpointList() {
        assertNull(selectRpcUrl(emptyList(), ChainId(1)))
    }
}

private enum class BlockNumberResponse {
    ERROR,
    NULL,
    VALUE
}

private class LocalJsonRpcServer(
    private val chainId: String,
    private val blockNumberResponse: BlockNumberResponse
) : AutoCloseable {
    private val observedMethods = mutableListOf<String>()
    private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)

    val url: String
        get() = "http://127.0.0.1:" + server.address.port

    val methods: List<String>
        get() = observedMethods.toList()

    init {
        server.createContext("/") { exchange ->
            val request = exchange.requestBody.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val method = Regex("\"method\"\\s*:\\s*\"([^\"]+)\"")
                .find(request)?.groupValues?.get(1)
                ?: error("Missing JSON-RPC method")
            val id = Regex("\"id\"\\s*:\\s*([^,}\\s]+)")
                .find(request)?.groupValues?.get(1)
                ?: error("Missing JSON-RPC request id")
            observedMethods.add(method)

            val response = when (method) {
                "eth_chainId" -> """{"jsonrpc":"2.0","id":$id,"result":"$chainId"}"""
                "eth_blockNumber" -> when (blockNumberResponse) {
                    BlockNumberResponse.ERROR ->
                        """{"jsonrpc":"2.0","id":$id,"error":{"code":-32046,"message":"unavailable"}}"""
                    BlockNumberResponse.NULL -> """{"jsonrpc":"2.0","id":$id,"result":null}"""
                    BlockNumberResponse.VALUE -> """{"jsonrpc":"2.0","id":$id,"result":"0x123"}"""
                }
                else -> error("Unexpected JSON-RPC method: $method")
            }.toByteArray(Charsets.UTF_8)

            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, response.size.toLong())
            exchange.responseBody.use { it.write(response) }
        }
        server.start()
    }

    override fun close() {
        server.stop(0)
    }
}
