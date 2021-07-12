using ProkeyCoinsInfoGrabber.Models;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Text.Json;
using System.Threading.Tasks;

namespace ProkeyCoinsInfoGrabber
{
    class Program
    {
        public static string ERC20TOKENS_DIRECTORY_PATH = System.IO.Path.Combine(System.IO.Directory.GetParent(System.IO.Directory.GetCurrentDirectory()).Parent.Parent.Parent.FullName, "tokens\\eth");
        public static int HOW_MANY_POPULAR_TOKEN_PAGES = 1;
        public static string COINGECKO_LISTCOINS_API_URL = "https://api.coingecko.com/api/v3/coins/list?include_platform=true";
        public static string COINS_HAVE_LANDINGPAGE_PATH = System.IO.Path.Combine(System.IO.Directory.GetParent(System.IO.Directory.GetCurrentDirectory()).Parent.Parent.FullName,"data\\landingPageList.txt");
        static void Main(string[] args)
        {
            //Get Coins Have LandingPage
            List<string> landingPages = GetCoinsHaveLandingPage(COINS_HAVE_LANDINGPAGE_PATH);
            //Get eth directory file names(ERC20 Token addresses) as an array
            Dictionary<string, string> erc20TokenfileName_List = GetPreExistingErc20Tokens(ERC20TOKENS_DIRECTORY_PATH);
            List<CoinGeckoMarketCap> marketCaps = GetCoinGeckoMarketCap();
            List<ERC20Token> newErc20Tokens = GetNewPopularERC20Tokens(erc20TokenfileName_List, marketCaps, landingPages);
        }

      
        /// <summary>
        /// Get pre-existing erc20 tokens from token/eth
        /// </summary>
        /// <returns></returns>
        private static Dictionary<string, string> GetPreExistingErc20Tokens(string erc20TokensDirctoryAbsolutePath)
        {
            ConsoleUtiliy.LogInfo("Getting pre-existing Erc20 tokens...");

            Dictionary<string, string> erc20TokenfileName_List = new Dictionary<string, string>();
            try
            {
                string[] erc20TokenfileNames = System.IO.Directory.GetFiles(erc20TokensDirctoryAbsolutePath);
                foreach (string tokenFileName in erc20TokenfileNames)
                {
                    string[] fileNameParts = tokenFileName.Split(System.IO.Path.DirectorySeparatorChar);
                    string fileNameWithExt = fileNameParts[^1];
                    char[] trimJsonChars = new char[] { '.', 'j', 's', 'o', 'n' };
                    string fileNameWithoutExt = fileNameWithExt.TrimEnd(trimJsonChars);
                    erc20TokenfileName_List.Add(fileNameWithoutExt, fileNameWithoutExt);
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
        private static List<ERC20Token> GetNewPopularERC20Tokens(Dictionary<string, string> erc20TokenFromfileNames_List, List<CoinGeckoMarketCap> marketCaps, List<string> landingPages)
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
                if (!erc20TokenFromfileNames_List.ContainsKey(erc20Token.address)) newERC20Token_List.Add(erc20Token);
            }
            #endregion

            return newERC20Token_List;
        }

    }
}
