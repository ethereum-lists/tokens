# Background

This is a project which emerged from the ashes of [MyEtherWallet/ethereum-lists](https://github.com/MyEtherWallet/ethereum-lists) after the split of MyEtherWallet and MyCrypto. For some more context see [this blog post](https://walleth.org/2018/02/15/ethereum-lists).

# Tokens

Information related to tokens. ERC-20 compliant or compatible only, please.

## Fields:

### Mandatory

-  `symbol`:    Short ticker style symbol of token.
-  `name`:      Longer human version of token.
-  `address`:   Ethereum (or other chain) address of ERC-20 token.
-  `decimals`:  The decimals of the token. As Number and not String.


### Optional

-  `logo`:      An optional logo of your token. Must be a **square** (recommended: 128x128) PNG w/ transparent background. Please compress using https://tinypng.com/
-  `support`:   A support email, support URL, or other way people can get assistance regarding the token.
-  `github`:    Where token or project-related code lives.
-  `community`: Twitter, Reddit, Slack or wherever else people hang out.
-  `website`:   Official URL of the website.


# The assembled lists

This repository has the tokens as single files. This makes it easier for contributors to add new tokens, for reviewers to get a good view on the change and also makes it easier to merge in tokens from other sources. Projects will most likely want to use the assembled lists. The CI server is already building them - so you can just [go to the commit-list](https://github.com/ethereum-lists/tokens/commits/master) and click on the green checkmark behind the last commit. There you see kontinuum/run - and the details link there brings to the assembled files on IPFS.

# Usages

- [WallETH](https://walleth.org)
- [MyCrypto](https://mycrypto.com)
- [TREZOR](https://trezor.io) - they even [import via IPFS ;-)](https://github.com/trezor/trezor-common/blob/master/ethereum_tokens-gen.py)
- please let us know when you do (you don't need to but it would be nice!)

# Maintainers

- [409H](https://github.com/409H) (409H - EtherAddressLookup)

- [tayvano](https://github.com/tayvano) (tayvano - [MyCrypto](https://mycrypto.com))

- [ligi](https://github.com/ligi) (ligi - [WallETH](https://walleth.org))

- You!

# A last note

This list is maintained by volunteers in the community &amp; people like you around the internet. It may not always be up to date, and it may occasionally get it wrong. If you find an error or omission, please open an issue or make a PR with any corrections.
