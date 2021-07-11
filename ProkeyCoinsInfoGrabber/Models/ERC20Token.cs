
using System;
using System.ComponentModel.DataAnnotations;

namespace ProkeyCoinsInfoGrabber.Models
{
    class ERC20Token: CoinBasenIfo
    {
        //Inherited fields
        //"symbol": "GST2",
        //"name": "Gastoken.io",

        //"address": "0x0000000000b3F879cb30FE243b4Dfee438691c04",
        public string address { get; set; }

        //"decimals": 2,
        public int decimals { get; set; }

        //"ens_address": "",
        public string ens_address { get; set; }

        //"website": "",
        public string website { get; set; }

        // "logo": {
        public ERC20TokenLogo logo { get; set; }

        //"support": {
        public ERC20TokenSupport support { get; set; }

        //"social": {
        public ERC20TokenSocial social { get; set; }
    }
    class ERC20TokenLogo
    {
        //    "logo": {
        //  "src": "",
        //  "width": "",
        //  "height": "",
        //  "ipfs_hash": ""
        //}
        public string src { get; set; }
        public int width { get; set; }
        public int height { get; set; }
        public string ipfs_hash { get; set; }
    }
    class ERC20TokenSupport
    {
        //  "support": {
        //"email": "",
        //"url": ""
        //  },
        [EmailAddress]
        public string email { get; set; }
        public Uri url { get; set; }
    }
    
    class ERC20TokenSocial
    {
        //    "social": {
        //  "blog": "",
        public Uri blog { get; set; }

        //  "chat": "",
        public Uri chat { get; set; }

        //  "discord": "",
        public Uri discord { get; set; }

        //  "facebook": "",
        public Uri facebook { get; set; }

        //  "forum": "",
        public Uri forum { get; set; }

        //  "github": "",
        public Uri github { get; set; }

        //  "gitter": "",
        public Uri gitter { get; set; }

        //  "instagram": "",
        public Uri instagram { get; set; }

        //  "linkedin": "",
        public Uri linkedin { get; set; }

        //  "reddit": "",
        public Uri reddit { get; set; }

        //  "slack": "",
        public Uri slack { get; set; }

        //  "telegram": "",
        public Uri telegram { get; set; }

        //  "twitter": "",
        public Uri twitter { get; set; }

        //  "youtube": ""
        public Uri youtube { get; set; }

        //}
    }
}
/*
 {
    "symbol": "GST2",
    "address": "0x0000000000b3F879cb30FE243b4Dfee438691c04",
    "decimals": 2,
    "name": "Gastoken.io",
    "ens_address": "",
    "website": "",
    "logo": {
      "src": "",
      "width": "",
      "height": "",
      "ipfs_hash": ""
    },
    "support": {
      "email": "",
      "url": ""
    },
    "social": {
      "blog": "",
      "chat": "",
      "discord": "",
      "facebook": "",
      "forum": "",
      "github": "",
      "gitter": "",
      "instagram": "",
      "linkedin": "",
      "reddit": "",
      "slack": "",
      "telegram": "",
      "twitter": "",
      "youtube": ""
    }
}
 */