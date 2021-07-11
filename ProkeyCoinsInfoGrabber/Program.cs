using System;
using System.Collections.Generic;

namespace ProkeyCoinsInfoGrabber
{
    class Program
    {
        public static string ERC20TOKENS_DIRECTORY_PATH = "tokens\\eth";
        static void Main(string[] args)
        {
            //Get eth directory file names(ERC20 Token addresses) as an array
            List<string> erc20TokenfileName_List = GetPreExistingErc20Tokens();
            foreach (var item in erc20TokenfileName_List)
            {
                Console.WriteLine(item);
            }
        }

        private static List<string> GetPreExistingErc20Tokens()
        {
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
    }
}
