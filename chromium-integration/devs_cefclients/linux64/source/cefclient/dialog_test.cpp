// Copyright (c) 2012 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

#include "cefclient/dialog_test.h"
#include "include/cef_browser.h"

#include <string>

namespace dialog_test {

namespace {

const char kTestUrl[] = "http://tests/dialogs";
const char kFileOpenMessageName[] = "DialogTest.FileOpen";
const char kFileOpenMultipleMessageName[] = "DialogTest.FileOpenMultiple";
const char kFileSaveMessageName[] = "DialogTest.FileSave";

// Callback executed when the file dialog is dismissed.
class DialogCallback : public CefRunFileDialogCallback {
 public:
  explicit DialogCallback(
      CefRefPtr<CefMessageRouterBrowserSide::Callback> router_callback)
      : router_callback_(router_callback) {
  }

  virtual void OnFileDialogDismissed(
      CefRefPtr<CefBrowserHost> browser_host,
      const std::vector<CefString>& file_paths) OVERRIDE {
    // Send a message back to the render process with the list of file paths.
    std::string response;
    for (int i = 0; i < static_cast<int>(file_paths.size()); ++i) {
      if (!response.empty())
        response += "|";  // Use a delimiter disallowed in file paths.
      response += file_paths[i];
    }

    router_callback_->Success(response);
    router_callback_ = NULL;
  }

 private:
  CefRefPtr<CefMessageRouterBrowserSide::Callback> router_callback_;

  IMPLEMENT_REFCOUNTING(DialogCallback);
};

// Handle messages in the browser process.
class Handler : public CefMessageRouterBrowserSide::Handler {
 public:
  Handler() {}

  // Called due to cefQuery execution in dialogs.html.
  virtual bool OnQuery(CefRefPtr<CefBrowser> browser,
                       CefRefPtr<CefFrame> frame,
                       int64 query_id,
                       const CefString& request,
                       bool persistent,
                       CefRefPtr<Callback> callback) OVERRIDE {
    // Only handle messages from the test URL.
    const std::string& url = frame->GetURL();
    if (url.find(kTestUrl) != 0)
      return false;

    // Sample file type filter.
    std::vector<CefString> file_types;
    file_types.push_back("text/*");
    file_types.push_back(".log");
    file_types.push_back(".patch");

    CefRefPtr<DialogCallback> dialog_callback(new DialogCallback(callback));

    const std::string& message_name = request;
    if (message_name == kFileOpenMessageName) {
      browser->GetHost()->RunFileDialog(FILE_DIALOG_OPEN, "My Open Dialog",
          "test.txt", file_types, dialog_callback.get());
    } else if (message_name == kFileOpenMultipleMessageName) {
      browser->GetHost()->RunFileDialog(FILE_DIALOG_OPEN_MULTIPLE,
          "My Open Multiple Dialog", CefString(), file_types,
          dialog_callback.get());
    } else if (message_name == kFileSaveMessageName) {
      browser->GetHost()->RunFileDialog(FILE_DIALOG_SAVE, "My Save Dialog",
          "test.txt", file_types, dialog_callback.get());
    } else {
      NOTREACHED();
    }

    return true;
  }
};

}  // namespace

void CreateMessageHandlers(ClientHandler::MessageHandlerSet& handlers) {
  handlers.insert(new Handler());
}

}  // namespace dialog_test
