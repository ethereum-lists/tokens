package org.ethereum.lists.tokens

import org.kethereum.model.ChainId
import org.kethereum.rpc.HttpEthereumRPC

internal fun selectRpcUrl(rpcUrls: List<String>?, chainId: ChainId): String? {
    if (rpcUrls.isNullOrEmpty()) return null

    return rpcUrls.firstNotNullOfOrNull { url ->
        try {
            val rpc = HttpEthereumRPC(url)
            if (rpc.chainId()?.value == chainId.value && rpc.blockNumber() != null) url else null
        } catch (_: Exception) {
            null
        }
    } ?: throw IllegalStateException("No healthy RPC endpoint available for the requested chain")
}
