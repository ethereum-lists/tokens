using System;
using System.Collections.Generic;
using System.Text;

namespace ProkeyCoinsInfoGrabber
{
    public static class ConsoleUtiliy
    {
        public static void LogInfo(string message)
        {
            Console.ForegroundColor = ConsoleColor.Yellow;
            Console.WriteLine(message);
            Console.ResetColor();
        }
    }
}
