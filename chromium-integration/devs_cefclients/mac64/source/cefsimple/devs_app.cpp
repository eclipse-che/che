

#include "cefsimple/devs_app.h"
#include <string>
#include "cefsimple/simple_handler.h"
#include "cefsimple/util.h"
#include "include/cef_browser.h"
#include "include/cef_command_line.h"
#include <iostream>
#include <sys/socket.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <errno.h>
#include <stdio.h>
#include <pthread.h>
#ifdef __APPLE__
#include "CoreFoundation/CoreFoundation.h"
#include <objc/objc.h>
#endif
#include <iostream>
#include <sstream>


void GetBasePath(){

#ifdef __APPLE__
    CFBundleRef mainBundle = CFBundleGetMainBundle();
    CFURLRef resourcesURL = CFBundleCopyResourcesDirectoryURL(mainBundle);
    char path[PATH_MAX];
    
    if (!CFURLGetFileSystemRepresentation(resourcesURL, TRUE, (UInt8 *)path, PATH_MAX))
    {
        // error!
    }
    CFRelease(resourcesURL);
    
    chdir(path);
    std::cout << "Current Path: " << path << std::endl;
#endif
  
     std::string spath(path);
     DevsApp::basePath = spath + "/../../../";
    
}

void *server_startup_che_sh(void *x_void_ptr)
{
    
    std::string workerpath = "../../../bin/che.sh start &";
    char worker_cpath[1024];
    strncpy(worker_cpath, workerpath.c_str(), sizeof(worker_cpath));

    int ret_code = system(worker_cpath);
    if (ret_code == 0) {
        std::cout << "server started sucessfully";
    } else {
        std::cerr << "Error during the server startup: please refer log files more details " << ret_code << '\n';
    }
    return NULL;
}

std::string get_file_contents(const char *filename)
{
    std::FILE *fp = std::fopen(filename, "rb");
    if (fp)
    {
        std::string contents;
        std::fseek(fp, 0, SEEK_END);
        contents.resize(std::ftell(fp));
        std::rewind(fp);
        std::fread(&contents[0], 1, contents.size(), fp);
        std::fclose(fp);
        return(contents);
    }else{
        return "0";
    }
    
}

int delete_file(char file_cpath[1024]){
    
    int ret_code = std::remove(file_cpath);
    std::remove(url_cpath);
    if (ret_code == 0) {
        std::cout << "File was successfully deleted\n";
        return 0;
    } else {
        std::cerr << "Error during the deletion: " << ret_code << '\n';
        return 1;
    }
    
}

int GetServerPort(std::FILE *fp,char port_cpath[1024]){
    
    std::string server_pid;
    std::fclose(fp);
    server_pid = get_file_contents(port_cpath);
    int ret_code = std::remove(port_cpath);
    std::remove(port_cpath);
    if (ret_code == 0) {
        std::cout << "File was successfully deleted\n";
    } else {
        std::cerr << "Error during the deletion: " << ret_code << '\n';
    }
   int serverPORT = atoi(server_pid.c_str());
   return serverPORT;
}




void openWorkspaceWindow(){
    
    std::string workerpath = "../../../bin/wso2studio_mac_workspace.sh";
    char worker_cpath[1024];
    strncpy(worker_cpath, workerpath.c_str(), sizeof(worker_cpath));
    std::string bash_s = "/bin/bash";
    char bash[1024];
    strncpy(bash, bash_s.c_str(), sizeof(bash));
    std::string commad_s = "-c";
    char commad[1024];
    strncpy(commad, commad_s.c_str(), sizeof(commad));
    char *name[] = {bash,commad,worker_cpath, NULL };
    int pid = fork();
    if ( pid == 0 ) {
        int ret_code =  execvp(name[0], name);
        if (ret_code == 0) {
            std::cout << "workspace selector window opening sucessfull";
        } else {
            std::cerr << "Error during developer studio startup: please refer log files more details " << ret_code << '\n';
        }
    }
}

int startSever(){
    std::string portpath ="../../../bin/PORT";
    char port_cpath[1024];
    std::string server_pid;
    strncpy(port_cpath, portpath.c_str(), sizeof(port_cpath));
    int serverPORT = 0;
    while (true) {
        
        std::FILE *fp = std::fopen(port_cpath, "rb");
        if (fp) {
            serverPORT = GetServerPort(fp, port_cpath);
            break;
        }else{
            std::cout << "Waiting for PORT...";
            sleep(2);
            
        }
    }
    
    if (serverPORT < 0) {
        return -1; //need to exit application in case of PORT being -1
    } else {
        // start che sh if the port value is available
        //starting the server
        int x = 0;
        pthread_t inc_x_thread;
        if(pthread_create(&inc_x_thread, NULL, server_startup_che_sh, &x)) {
            fprintf(stderr, "Error creating thread\n");
            std::cout << "Error creating thread\n" << stderr << std::endl;
        }
        
    }
    return 0;
}


std::string readBrowserUrl(){

    std::string url;
    std::string urlpath ="../../../bin/url.txt";
    char url_cpath[1024];
    strncpy(url_cpath, urlpath.c_str(), sizeof(url_cpath));
    while (true) {
        std::FILE *fp = std::fopen(url_cpath, "rb");
        if (fp) {
            url =  get_file_contents(url_cpath);
            delete_file(url_cpath);
            return url;
        }else{
            std::cout << "Waiting for URL...";
            sleep(2);
        }
    }
    return 0;

}

void *shutdown_server_che_sh(void *x_void_ptr)
{
    std::string workerpath = "../../../bin/che.sh stop &";
    std::cout << "workerpath: " << workerpath << std::endl;
    
    char worker_cpath[1024];
    strncpy(worker_cpath, workerpath.c_str(), sizeof(worker_cpath));
    int ret_code = system(worker_cpath);
    if (ret_code == 0) {
        std::cout << "server started sucessfully";
    } else {
        std::cerr << "Error during the server startup: please refer log files more details " << ret_code << '\n';
    }
    return NULL;
}


std::string GetBrowserUrl(){
    openWorkspaceWindow();
    startSever();
    std::string url = readBrowserUrl();
    return url;
    }
}


int doclose(){
    
    pthread_t inc_x_thread;
    int x =0;
    if(pthread_create(&inc_x_thread, NULL, shutdown_server_che_sh, &x)) {
        fprintf(stderr, "Error creating thread\n");
        std::cout << "Error creating thread\n" << stderr << std::endl;
    }
    return 0;
}



