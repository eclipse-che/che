// Copyright (c) 2013 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

//edited by Awanthika

#include "cefsimple/simple_handler.h"

#include <sstream>
#include <string>
#include "cefsimple/util.h"
#include "include/cef_app.h"
#include "include/cef_runnable.h"
#include "cefsimple/simple_app.h"
#include <stdio.h>
#include <iostream>
 

extern char pild_killcmd[1024];;

namespace {

SimpleHandler* g_instance = NULL;

}  // namespace

SimpleHandler::SimpleHandler()
    : is_closing_(false) {
  ASSERT(!g_instance);
  g_instance = this;
}

void *shutdown_server_che_sh(void *x_void_ptr)
{
//#ifdef __APPLE__
//    CFBundleRef mainBundle = CFBundleGetMainBundle();
//    CFURLRef resourcesURL = CFBundleCopyResourcesDirectoryURL(mainBundle);
//    char path[PATH_MAX];
//    
//    if (!CFURLGetFileSystemRepresentation(resourcesURL, TRUE, (UInt8 *)path, PATH_MAX))
//    {
//        // error!
//    }
//    CFRelease(resourcesURL);
    
//    chdir(path);
//    std::cout << "Current Path: " << path << std::endl;
//#endif
//    std::string spath(path);
//    spath = spath + "/../../../";
    
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


SimpleHandler::~SimpleHandler() {
  g_instance = NULL;
}

// static
SimpleHandler* SimpleHandler::GetInstance() {
  return g_instance;
}

void SimpleHandler::OnAfterCreated(CefRefPtr<CefBrowser> browser) {
  REQUIRE_UI_THREAD();

  // Add to the list of existing browsers.
  browser_list_.push_back(browser);
}

bool SimpleHandler::DoClose(CefRefPtr<CefBrowser> browser) {
  REQUIRE_UI_THREAD();

  if (browser_list_.size() == 1) {
    // Set a flag to indicate that the window close should be allowed.
    is_closing_ = true;
  }

  // Allow the close. For windowed browsers this will result in the OS close
  // event being sent.
  return false;
}

void SimpleHandler::OnBeforeClose(CefRefPtr<CefBrowser> browser) {
  REQUIRE_UI_THREAD();
    //
    // Remove from the list of existing browsers.
  BrowserList::iterator bit = browser_list_.begin();
  for (; bit != browser_list_.end(); ++bit) {
    if ((*bit)->IsSame(browser)) {
      browser_list_.erase(bit);
      break;
    }
  }

  if (browser_list_.empty()) {
    // All browser windows have closed. Quit the application message loop.
    
    CefQuitMessageLoop();
  }
}


void SimpleHandler::OnBeforeDownload(
                                     CefRefPtr<CefBrowser> browser,
                                     CefRefPtr<CefDownloadItem> download_item,
                                     const CefString& suggested_name,
                                     CefRefPtr<CefBeforeDownloadCallback> callback) {
    REQUIRE_UI_THREAD();
    
  
    // Continue the download and show the "Save As" dialog.
    callback->Continue(GetDownloadPath(suggested_name), true);
    //callback->
    
    
}

void SimpleHandler::OnDownloadUpdated(
                                      CefRefPtr<CefBrowser> browser,
                                      CefRefPtr<CefDownloadItem> download_item,
                                      CefRefPtr<CefDownloadItemCallback> callback) {
    REQUIRE_UI_THREAD();
    
    if (download_item->IsComplete()) {
        SetLastDownloadFile(download_item->GetFullPath());
        SendNotification(NOTIFY_DOWNLOAD_COMPLETE);
    }
}

void SimpleHandler::SetLastDownloadFile(const std::string& fileName) {
    REQUIRE_UI_THREAD();
    last_download_file_ = fileName;
}

std::string SimpleHandler::GetLastDownloadFile() const {
    REQUIRE_UI_THREAD();
    return last_download_file_;
}


void SimpleHandler::OnLoadError(CefRefPtr<CefBrowser> browser,
                                CefRefPtr<CefFrame> frame,
                                ErrorCode errorCode,
                                const CefString& errorText,
                                const CefString& failedUrl) {
  REQUIRE_UI_THREAD();

  // Don't display an error for downloaded files.
  if (errorCode == ERR_ABORTED)
    return;

  // Display a load error message.
  std::stringstream ss;
  ss << "<html><body bgcolor=\"white\">"
        "<h2>Failed to load URL " << std::string(failedUrl) <<
        " with error " << std::string(errorText) << " (" << errorCode <<
        ").</h2></body></html>";
  frame->LoadString(ss.str(), failedUrl);
}

void SimpleHandler::CloseAllBrowsers(bool force_close) {
  if (!CefCurrentlyOn(TID_UI)) {      
    CefPostTask(TID_UI,
        NewCefRunnableMethod(this, &SimpleHandler::CloseAllBrowsers,
                             force_close));
    return;
  }

  if (browser_list_.empty())
    return;

  BrowserList::const_iterator it = browser_list_.begin();
  for (; it != browser_list_.end(); ++it)
    (*it)->GetHost()->CloseBrowser(force_close);
}
