// Copyright (c) 2013 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

//edited by Awanthika

#include "cefsimple/simple_app.h"
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



//extern  pthread_t inc_x_thread;
 char pild_killcmd[1024];

 SimpleApp::SimpleApp() {
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


void dyname() {
    const char *arg ="timestamp";
    char cmd[1024] = {0}; // change this for more length
    const char *base = "bash a.sh "; // note trailine ' ' (space)
    sprintf(cmd, "%s", base);
    sprintf(cmd, "%s%s ", cmd, arg);
    system(cmd);
}


void *server_startup_che_sh(void *x_void_ptr)
{
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
    spath = spath + "/../../../";
  
 std::string workerpath = "../../../bin/che.sh start &";
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

void SimpleApp::OnContextInitialized() {
    
  REQUIRE_UI_THREAD();
    
  // Information used when creating the native window.
  CefWindowInfo window_info;

  window_info.width = 1400;
  window_info.height = 800;
    

#if defined(OS_WIN)
  // On Windows we need to specify certain flags that will be passed to
  // CreateWindowEx().
  window_info.SetAsPopup(NULL, "Developer Studio 4.0.0");
#endif

  // SimpleHandler implements browser-level callbacks.
  CefRefPtr<SimpleHandler> handler(new SimpleHandler());

  // Specify CEF browser settings here.
  CefBrowserSettings browser_settings;
   
  //Getting the execution path
    std::string url = DevsApp::GetBrowserUrl();
  //load the browser when url file is available
  CefBrowserHost::CreateBrowser(window_info, handler.get(), url, browser_settings, NULL);
    
    
}

