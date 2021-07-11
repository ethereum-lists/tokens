
namespace ProkeyCoinsInfoGrabber.Models
{
    public class CoinGeckoMarketCap:CoinBasenIfo
    {
        public string image { get; set; }
        public long market_cap { set; get; }
        public int? market_cap_rank { set; get; }
    }
}
