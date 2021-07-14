using ProkeyCoinsInfoGrabber.Models;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;

namespace ProkeyCoinsInfoGrabber
{
    class Program
    {
        //System.IO.Path.Combine(System.IO.Directory.GetParent(System.IO.Directory.GetCurrentDirectory()).Parent.Parent.Parent.FullName, "tokens\\eth");
        public static string ERC20TOKENS_DIRECTORY_PATH = "../../../../tokens/eth";
        public static int HOW_MANY_POPULAR_TOKEN_PAGES = 1;
        public static string COINGECKO_LISTCOINS_API_URL = "https://api.coingecko.com/api/v3/coins/list?include_platform=true";
        //System.IO.Path.Combine(System.IO.Directory.GetParent(System.IO.Directory.GetCurrentDirectory()).Parent.Parent.FullName,"data\\landingPageList.txt");
        public static string COINS_HAVE_LANDINGPAGE_PATH = "../../../../data/landingPageList.txt";
        public static string ETHPLORER_APIKEY = "EK-nFPsq-fhXwSwW-CofSq";

        static void Main(string[] args)
        {
            //Get Coins Have LandingPage
            List<string> landingPages = GetCoinsHaveLandingPage(COINS_HAVE_LANDINGPAGE_PATH);
            //Get eth directory file names(ERC20 Token addresses) as an array
            List<string> erc20TokenfileName_List = GetPreExistingErc20Tokens(ERC20TOKENS_DIRECTORY_PATH);
            List<CoinGeckoMarketCap> marketCaps = GetCoinGeckoMarketCap();
            List<ERC20Token> newErc20Tokens = GetNewPopularERC20Tokens(erc20TokenfileName_List, marketCaps, landingPages);
            if (newErc20Tokens != null && newErc20Tokens.Count > 0)
            {
                FunctionalityResult result = StoreNewTokensInFile(newErc20Tokens, ERC20TOKENS_DIRECTORY_PATH);
                if (result == FunctionalityResult.Succeed)
                {
                    ConsoleUtiliy.LogSuccess($"{newErc20Tokens.Count} json file(s) was/were stored successfully!");
                }
            }
            else
            {
                ConsoleUtiliy.LogInfo($"There is'nt any new token(json file) to store");
            }
        }

        /// <summary>
        /// Store new tokens in json files
        /// </summary>
        /// <param name="tokens"></param>
        /// <returns></returns>
        private static FunctionalityResult StoreNewTokensInFile(List<ERC20Token> tokens, string erc20DirectoryPath)
        {
            if(Directory.Exists(erc20DirectoryPath))
            {
                foreach (ERC20Token token in tokens)
                {
                    string fileFullPath = Path.Combine(erc20DirectoryPath, token.address+".json");
                    if (!File.Exists(fileFullPath))
                    {
                        string tokenJsonString = System.Text.Json.JsonSerializer.Serialize(token);
                        File.WriteAllText(fileFullPath, tokenJsonString);
                    }
                    else
                    {
                        ConsoleUtiliy.LogError($"A file named {token.address}.json, already exists, something must be wrong!");
                        return FunctionalityResult.NotFound;
                    }
                }
                return FunctionalityResult.Succeed;
            }
            else
            {
                return FunctionalityResult.NotFound;
            }
        }

        /// <summary>
        /// Get pre-existing erc20 tokens from token/eth
        /// </summary>
        /// <returns></returns>
        private static List<string> GetPreExistingErc20Tokens(string erc20TokensDirctoryAbsolutePath)
        {
            ConsoleUtiliy.LogInfo("Getting pre-existing Erc20 tokens...");

            List<string> erc20TokenfileName_List = new List<string>();
            try
            {
                if (System.IO.Directory.Exists(erc20TokensDirctoryAbsolutePath))
                {
                    string[] erc20TokenfileNames = System.IO.Directory.GetFiles(erc20TokensDirctoryAbsolutePath);
                    foreach (string tokenFileName in erc20TokenfileNames)
                    {
                        string[] fileNameParts = tokenFileName.Split(System.IO.Path.DirectorySeparatorChar);
                        string fileNameWithExt = fileNameParts[^1];
                        char[] trimJsonChars = new char[] { '.', 'j', 's', 'o', 'n' };
                        string fileNameWithoutExt = fileNameWithExt.TrimEnd(trimJsonChars);
                        erc20TokenfileName_List.Add(fileNameWithoutExt);
                    }
                }
                else
                {
                    ConsoleUtiliy.LogError($"Directory {erc20TokensDirctoryAbsolutePath} not found!");
                    return null;
                }
                
            }
            catch (System.IO.DirectoryNotFoundException)
            {
                ConsoleUtiliy.LogError($"Directory {erc20TokensDirctoryAbsolutePath} not found!");
                return null;
            }
            catch(Exception exp)
            {
                ConsoleUtiliy.LogError($"Error: {exp.Message}");
                return null;
            }
            return erc20TokenfileName_List;
        }

        /// <summary>
        /// Get Top marketcap from coingecko
        /// </summary>
        /// <returns></returns>
        private static List<CoinGeckoMarketCap> GetCoinGeckoMarketCap()
        {
            ConsoleUtiliy.LogInfo($"Reading {HOW_MANY_POPULAR_TOKEN_PAGES * 250} Coingecko Marketcaps ...");
            List<CoinGeckoMarketCap> marketCaps = new List<CoinGeckoMarketCap>();
            int page = 1;
            while (page <= HOW_MANY_POPULAR_TOKEN_PAGES)
            {
                try
                {
                    using (WebClient wc = new WebClient())
                    {
                        var json = wc.DownloadString($"https://api.coingecko.com/api/v3/coins/markets?vs_currency=USD&order=market_cap_desc&per_page=250&page={page}&sparkline=false");

                        List<CoinGeckoMarketCap> markets = JsonSerializer.Deserialize<List<CoinGeckoMarketCap>>(json);
                        marketCaps.AddRange(markets);

                    }
                    page++;
                }
                catch(WebException webExp)
                {
                    ConsoleUtiliy.LogError($"Web exeption: {webExp.Message}");
                    return null;
                }
            }
            return marketCaps;         
           
        }
        
        /// <summary>
        /// Get coins with landing page from txt file
        /// </summary>
        /// <param name="filePath"></param>
        /// <returns></returns>
        private static List<string> GetCoinsHaveLandingPage(string filePath)
        {
            List<string> landingPages = new List<string>();
            if (File.Exists(filePath))
            {
                using StreamReader sr = new StreamReader(filePath);
                while (sr.EndOfStream == false)
                {
                    landingPages.Add(sr.ReadLine().Trim());
                }
            }
            return landingPages;
        }

        /// <summary>
        /// Get CoinGecko all coins list 
        /// Request URL:https://api.coingecko.com/api/v3/coins/list?include_platform=true
        /// </summary>
        /// <returns></returns>
        static async Task<string> GetCoinGeckoCoinsList()
        {
            try
            {
                using HttpClient httpClient = new HttpClient();
                var response = await httpClient.GetAsync(COINGECKO_LISTCOINS_API_URL);
                string responseContent = await response.Content.ReadAsStringAsync();
                return responseContent;
            }
            catch (HttpRequestException httpExp)
            {
                ConsoleUtiliy.LogError("HttpRequestException in CoinGecko Coins List API: " + httpExp.Message);
                return string.Empty;
            }
            catch (Exception exp)
            {
                ConsoleUtiliy.LogError("Exception in CoinGecko Coins List API: " + exp.Message);
                return string.Empty;
            }

        }

        /// <summary>
        ///1- Get Tokens that are popular and erc20 but
        ///2- They are not in token/eth yet
        ///3- get some info such as decimal from ethplorer 
        /// </summary>
        /// <param name="erc20TokenFromfileNames_List"></param>
        /// <param name="marketCaps"></param>
        /// <param name="landingPages"></param>
        /// <returns></returns>
        private static List<ERC20Token> GetNewPopularERC20Tokens(List<string> erc20TokenFromfileNames_List, List<CoinGeckoMarketCap> marketCaps, List<string> landingPages)
        {
            List<ERC20Token> erc20TokensList = new List<ERC20Token>();
            ConsoleUtiliy.LogInfo("Getting coin list from coingecko ...");
            #region 1-Get Tokens that are popular and erc20
            //! Get Id of coin from coingecko coin/list api
            string coinsListResponse = GetCoinGeckoCoinsList().Result;

            if (!string.IsNullOrEmpty(coinsListResponse))
            {
                try
                {
                    ConsoleUtiliy.LogInfo("Parsing coingecko coins list...");
                    //List of coins that was gotten from coingecko
                    List<CoingeckoCoinsListAPIResponse> coinsList = System.Text.Json.JsonSerializer.Deserialize<List<CoingeckoCoinsListAPIResponse>>(coinsListResponse);
                    foreach (CoinGeckoMarketCap marketCapInfoItem in marketCaps)
                    {
                        //Get Tokens that are popular and erc20
                        var coin = coinsList.SingleOrDefault(c => c.id.Equals(marketCapInfoItem.id) && !string.IsNullOrEmpty(c.platforms.ethereum));
                        if (coin != null)
                        {
                            //Add Coin to erc list
                            erc20TokensList.Add(new ERC20Token()
                            {
                                //https://api.coingecko.com/api/v3/asset_platforms
                                //id: "ethereum",
                                //chain_identifier: 1,
                                //name: "Ethereum",
                                //chain_id = 1,
                                address = coin.platforms.ethereum,
                                symbol = marketCapInfoItem.symbol,
                                decimals = 8, //int.Parse(coindecimal),
                                name = marketCapInfoItem.name,
                                //priority = marketCapInfoItem.market_cap_rank.HasValue ? marketCapInfoItem.market_cap_rank.Value : 100,
                                //hasLanding = landingPages.Any(l => l == marketCapInfoItem.name),
                            });
                        }
                    }                   
                }
                catch (System.Text.Json.JsonException jsonException)
                {
                    ConsoleUtiliy.LogError("JsonException of coin list string: " + jsonException.Message);
                    return null;
                }
                catch (Exception exp)
                {
                    ConsoleUtiliy.LogError("Exception of getting id from coin list(coingecko coin/list): " + exp.Message);
                    return null;
                }
            }
            else
            {
                ConsoleUtiliy.LogError("Coins list is empty, check your connection please!");
                return null;
            }
            #endregion

            //2- They are not in token/eth yet
            #region 2- They are not in token/eth yet
            List<ERC20Token> newERC20Token_List = new List<ERC20Token>();
            foreach (ERC20Token erc20Token in erc20TokensList)
            {
                if (!erc20TokenFromfileNames_List.Any(t => t.Equals(erc20Token.address,StringComparison.OrdinalIgnoreCase))) newERC20Token_List.Add(erc20Token);
            }
            #endregion

            //3- get some info such as decimal from ethplorer 
            #region  3- get some info such as decimal from ethplorer
            FunctionalityResult result = GetDecimalFromEthplorerApi(newERC20Token_List);
            if (result == FunctionalityResult.Succeed)
            {
                if (newERC20Token_List.Count > 0)
                {
                    ConsoleUtiliy.LogSuccess($"{newERC20Token_List.Count} tokens found and neccessary data surfed!");
                }
                return newERC20Token_List;
            }
            return null;
            #endregion

        }


        /// <summary>
        /// Get Decimal From Ethplorer Api
        /// </summary>
        /// <param name="tokens"></param>
        /// <returns></returns>
        private static FunctionalityResult GetDecimalFromEthplorerApi(List<ERC20Token> tokens)
        {
            using HttpClient httpClient = new HttpClient();
            string responseContent = string.Empty;
            ConsoleUtiliy.LogInfo("Geting decimal from Ethplorer api, this may take a few minutes, ...");
            int i = 1;
            foreach (ERC20Token erc20Token in tokens)
            {
                try
                {
                    string url = $"https://api.ethplorer.io/getTokenInfo/{erc20Token.address}?apiKey={ETHPLORER_APIKEY}";
                    ConsoleUtiliy.LogInfo($"Get {i} of {tokens.Count} tokens info(https://api.ethplorer.io/getTokenInfo/{erc20Token.address})");
                    HttpResponseMessage response = httpClient.GetAsync(url).Result;
                    responseContent = response.Content.ReadAsStringAsync().Result;
                    EthplorerGetTokenInfoApiResponse tokenInfo = System.Text.Json.JsonSerializer.Deserialize<EthplorerGetTokenInfoApiResponse>(responseContent);
                    erc20Token.decimals = int.Parse(tokenInfo.decimals);
                    i++;
                }
                catch (System.Text.Json.JsonException)
                {
                    EthplorerApiError error = System.Text.Json.JsonSerializer.Deserialize<EthplorerApiError>(responseContent);
                    ConsoleUtiliy.LogError("Error in Ethplorer api: " + error.error.message);
                    return FunctionalityResult.Exception;
                }
                catch (HttpRequestException httpExp)
                {
                    ConsoleUtiliy.LogError("Http Exception, Check your connection! " + httpExp.Message);
                    return FunctionalityResult.Exception;
                }
                catch (Exception exp)
                {
                    ConsoleUtiliy.LogError("Exception: " + exp.Message);
                    return FunctionalityResult.Exception;
                }
                //api ethplorer
                Thread.Sleep(2000);
            }
            return FunctionalityResult.Succeed;

        }

    }
}
