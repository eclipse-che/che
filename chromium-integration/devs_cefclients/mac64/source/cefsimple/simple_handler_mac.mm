// Copyright (c) 2013 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

#include "cefsimple/simple_handler.h"

#import <Cocoa/Cocoa.h>

#include "cefsimple/util.h"
#include "include/cef_browser.h"
NSWindow* window;
void SimpleHandler::OnTitleChange(CefRefPtr<CefBrowser> browser,
                                  const CefString& title) {
  REQUIRE_UI_THREAD();

  NSView* view = (NSView*)browser->GetHost()->GetWindowHandle();
  window = [view window];
  std::string titleStr("Developer Studio");
  NSString* str = [NSString stringWithUTF8String:titleStr.c_str()];
  [window setTitle:str];
}


void SimpleHandler::SendNotification(NotificationType type) {
    SEL sel = nil;
    switch(type) {
        case NOTIFY_CONSOLE_MESSAGE:
            sel = @selector(notifyConsoleMessage:);
            break;
        case NOTIFY_DOWNLOAD_COMPLETE:
            sel = @selector(notifyDownloadComplete:);
            break;
        case NOTIFY_DOWNLOAD_ERROR:
            sel = @selector(notifyDownloadError:);
            break;
    }
    
    if (sel == nil)
        return;

     NSObject* delegate = [window delegate];
    [delegate performSelectorOnMainThread:sel withObject:nil waitUntilDone:NO];
}

std::string SimpleHandler::GetDownloadPath(const std::string& file_name) {
    return std::string();
}
