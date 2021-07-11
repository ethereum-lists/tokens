using System;
using System.Collections.Generic;
using System.Net;

namespace ProkeyCoinsInfoGrabber
{
    class Program
    {
        public static string ERC20TOKENS_DIRECTORY_PATH = "tokens\\eth";
        public static int HOW_MANY_POPULAR_TOKEN_PAGES = 1;
        static void Main(string[] args)
        {
            //Get eth directory file names(ERC20 Token addresses) as an array
            List<string> erc20TokenfileName_List = GetPreExistingErc20Tokens();
            List<CoinGeckoMarketCap> marketCaps = GetMarketcap();
        }

        private static List<string> GetPreExistingErc20Tokens()
        {
            ConsoleUtiliy.LogInfo("Getting pre-existing Erc20 tokens...");
            List<string> erc20TokenfileName_List = new List<string>();
            string erc20TokensDirctoryAbsolutePath = System.IO.Path.Combine(System.IO.Directory.GetParent(System.IO.Directory.GetCurrentDirectory()).Parent.Parent.Parent.FullName, ERC20TOKENS_DIRECTORY_PATH);
            string[] erc20TokenfileNames = System.IO.Directory.GetFiles(erc20TokensDirctoryAbsolutePath);
            foreach (string tokenFileName in erc20TokenfileNames)
            {
                string[] fileNameParts = tokenFileName.Split(System.IO.Path.DirectorySeparatorChar);
                string fileNameWithExt = fileNameParts[^1];
                char[] trimJsonChars = new char[] { '.', 'j', 's', 'o', 'n' };
                string fileNameWithoutExt = fileNameWithExt.TrimEnd(trimJsonChars);
                erc20TokenfileName_List.Add(fileNameWithoutExt);
            }
            return erc20TokenfileName_List;
        }

        static List<CoinGeckoMarketCap> GetMarketcap()
        {
            ConsoleUtiliy.LogInfo($"Reading {HOW_MANY_POPULAR_TOKEN_PAGES * 250} Coingecko Marketcaps, please wait...");
            List<CoinGeckoMarketCap> marketCaps = new List<CoinGeckoMarketCap>();
            int page = 1;
            while (page <= HOW_MANY_POPULAR_TOKEN_PAGES)
            {
                using (WebClient wc = new WebClient())
                {
                    var json = wc.DownloadString($"https://api.coingecko.com/api/v3/coins/markets?vs_currency=USD&order=market_cap_desc&per_page=250&page={page}&sparkline=false");

                    List<CoinGeckoMarketCap> markets = System.Text.Json.JsonSerializer.Deserialize<List<CoinGeckoMarketCap>>(json);
                    marketCaps.AddRange(markets);

                }
                page++;
            }

            return marketCaps;
        }
    }
}
