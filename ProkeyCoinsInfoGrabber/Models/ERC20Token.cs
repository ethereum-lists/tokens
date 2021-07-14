
using System;
using System.ComponentModel.DataAnnotations;

namespace ProkeyCoinsInfoGrabber.Models
{
    class ERC20Token
    {
        public string name { get; set; } = string.Empty;
        public string symbol { get; set; } = string.Empty;

        //"address": "0x0000000000b3F879cb30FE243b4Dfee438691c04",
        public string address { get; set; } = string.Empty;

        //"decimals": 2,
        public int decimals { get; set; }

        //"ens_address": "",
        public string ens_address { get; set; } = string.Empty;

        //"website": "",
        public string website { get; set; } = string.Empty;

        // "logo": {
        public ERC20TokenLogo logo { get; set; } = new ERC20TokenLogo();

        //"support": {
        public ERC20TokenSupport support { get; set; } = new ERC20TokenSupport();

        //"social": {
        public ERC20TokenSocial social { get; set; } = new ERC20TokenSocial();
    }
    class ERC20TokenLogo
    {
        //    "logo": {
        //  "src": "",
        //  "width": "",
        //  "height": "",
        //  "ipfs_hash": ""
        //}
        public string src { get; set; } = string.Empty;
        public string width { get; set; } = string.Empty;
        public string height { get; set; } = string.Empty;
        public string ipfs_hash { get; set; } = string.Empty;        
    }
    class ERC20TokenSupport
    {
        //  "support": {
        //"email": "",
        //"url": ""
        //  },
        [EmailAddress]
        public string email { get; set; } = string.Empty;
        public string url { get; set; } = string.Empty;
    }
    
    class ERC20TokenSocial
    {
        //    "social": {
        //  "blog": "",
        public string blog { get; set; } = string.Empty;

        //  "chat": "",
        public string chat { get; set; } = string.Empty;

        //  "discord": "",
        public string discord { get; set; } = string.Empty;

        //  "facebook": "",
        public string facebook { get; set; } = string.Empty;

        //  "forum": "",
        public string forum { get; set; } = string.Empty;

        //  "github": "",
        public string github { get; set; } = string.Empty;

        //  "gitter": "",
        public string gitter { get; set; } = string.Empty;

        //  "instagram": "",
        public string instagram { get; set; } = string.Empty;

        //  "linkedin": "",
        public string linkedin { get; set; } = string.Empty;

        //  "reddit": "",
        public string reddit { get; set; } = string.Empty;

        //  "slack": "",
        public string slack { get; set; } = string.Empty;

        //  "telegram": "",
        public string telegram { get; set; } = string.Empty;

        //  "twitter": "",
        public string twitter { get; set; } = string.Empty;

        //  "youtube": ""
        public string youtube { get; set; } = string.Empty;

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