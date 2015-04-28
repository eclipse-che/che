
#include <string>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <stdio.h>


class DevsApp
{
public:
    static int serverPort;
    static std::string url;
    static std::string workspace;
    static std::string basePath;
    
public:
    static std::string GetBrowserUrl();
    static std::string readUrl();
    static void GetBasePath();
    static int GetServerPort();
    static int doclose();
};
