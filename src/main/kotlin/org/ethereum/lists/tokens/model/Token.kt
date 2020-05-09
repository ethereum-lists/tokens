package org.ethereum.lists.tokens.model

data class Token(val symbol: String,
                 val name: String,
                 val address: String,
                 val invalid_erc20_symbol: Boolean?,
                 val invalid_erc20_decimals: Boolean?,
                 val decimals: Int,
                 val type: String?,
                 val website: String? = null,
                 val ens_address: String? = null,
                 val comment: String? = null,
                 val logo: Logo? = null,
                 val support: Support? = null,
                 val social: Social? = null,
                 val deprecation: Deprecation? = null,
                 val address_eip1191: String? = null)