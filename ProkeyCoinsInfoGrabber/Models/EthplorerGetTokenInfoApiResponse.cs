
namespace ProkeyCoinsInfoGrabber.Models
{
    class EthplorerGetTokenInfoApiResponse: CoinBaseInfo
    {
        //name and symbol would be inherited
        //name: "Everex",
        //symbol: "EVX",

        //address: "0xf3db5fa2c66b7af3eb0c0b782510816cbe4813b8",
        public string address { get; set; }

        //decimals: "4",
        public string decimals { get; set; }

    }
}
