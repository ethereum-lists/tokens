
namespace ProkeyCoinsInfoGrabber.Models
{
    class EthplorerApiError: ResponseError<EthplorerApiErrorResponse>
    {
        //Error will be inherited
    }
    
    class EthplorerApiErrorResponse
    {
        public int code { get; set; }
        public string message { get; set; }
    }

}
