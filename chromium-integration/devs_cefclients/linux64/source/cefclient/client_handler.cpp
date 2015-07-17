// Copyright (c) 2013 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

#include "cefclient/client_handler.h"
#include <stdio.h>
#include <algorithm>
#include <set>
#include <sstream>
#include <string>
#include <vector>

#include "include/base/cef_bind.h"
#include "include/cef_browser.h"
#include "include/cef_frame.h"
#include "include/cef_path_util.h"
#include "include/cef_process_util.h"
#include "include/cef_trace.h"
#include "include/cef_url.h"
#include "include/wrapper/cef_closure_task.h"
#include "include/wrapper/cef_stream_resource_handler.h"
#include "cefclient/binding_test.h"
#include "cefclient/cefclient.h"
#include "cefclient/client_renderer.h"
#include "cefclient/client_switches.h"
#include "cefclient/dialog_test.h"
#include "cefclient/resource_util.h"
#include "cefclient/string_util.h"
#include "cefclient/window_test.h"
#include "DeveloperStudioProcess.h"

DeveloperStudioProcess *developerStudioProcess;

namespace {

// Custom menu command Ids.
enum client_menu_ids {
  CLIENT_ID_SHOW_DEVTOOLS   = MENU_ID_USER_FIRST,
  CLIENT_ID_CLOSE_DEVTOOLS,
  CLIENT_ID_INSPECT_ELEMENT,
  CLIENT_ID_TESTMENU_SUBMENU,
  CLIENT_ID_TESTMENU_CHECKITEM,
  CLIENT_ID_TESTMENU_RADIOITEM1,
  CLIENT_ID_TESTMENU_RADIOITEM2,
  CLIENT_ID_TESTMENU_RADIOITEM3,
};

const char kTestOrigin[] = "http://tests/";

// Retrieve the file name and mime type based on the specified url.
bool ParseTestUrl(const std::string& url,
                  std::string* file_name,
                  std::string* mime_type) {
  // Retrieve the path component.
  CefURLParts parts;
  CefParseURL(url, parts);
  std::string file = CefString(&parts.path);
  if (file.size() < 2)
    return false;

  // Remove the leading slash.
  file = file.substr(1);

  // Verify that the file name is valid.
  for(size_t i = 0; i < file.size(); ++i) {
    const char c = file[i];
    if (!isalpha(c) && !isdigit(c) && c != '_' && c != '.')
      return false;
  }

  // Determine the mime type based on the file extension, if any.
  size_t pos = file.rfind(".");
  if (pos != std::string::npos) {
    std::string ext = file.substr(pos + 1);
    if (ext == "html")
      *mime_type = "text/html";
    else if (ext == "png")
      *mime_type = "image/png";
    else
      return false;
  } else {
    // Default to an html extension if none is specified.
    *mime_type = "text/html";
    file += ".html";
  }

  *file_name = file;
  return true;
}

}  // namespace

int ClientHandler::browser_count_ = 0;

ClientHandler::ClientHandler()
  : browser_id_(0),
    is_closing_(false),
    main_handle_(NULL),
    edit_handle_(NULL),
    back_handle_(NULL),
    forward_handle_(NULL),
    stop_handle_(NULL),
    reload_handle_(NULL),
    focus_on_editable_field_(false) {
#if defined(OS_LINUX)
  gtk_dialog_ = NULL;
#endif

  // Read command line settings.
  CefRefPtr<CefCommandLine> command_line =
      CefCommandLine::GetGlobalCommandLine();
      developerStudioProcess = new DeveloperStudioProcess;

      if (developerStudioProcess->StartProcess() == 0) {
        std::string operatingURL = developerStudioProcess->GetURLFromFile();

 if (command_line->HasSwitch(cefclient::kUrl))
    startup_url_ = command_line->GetSwitchValue(cefclient::kUrl);
  if (startup_url_.empty()) {
    startup_url_ = operatingURL;
}

}
  mouse_cursor_change_disabled_ =
      command_line->HasSwitch(cefclient::kMouseCursorChangeDisabled);
}

ClientHandler::~ClientHandler() {
}

bool ClientHandler::OnProcessMessageReceived(
    CefRefPtr<CefBrowser> browser,
    CefProcessId source_process,
    CefRefPtr<CefProcessMessage> message) {
  CEF_REQUIRE_UI_THREAD();

  if (message_router_->OnProcessMessageReceived(browser, source_process,
                                                message)) {
    return true;
  }

  // Check for messages from the client renderer.
  std::string message_name = message->GetName();
  if (message_name == client_renderer::kFocusedNodeChangedMessage) {
    // A message is sent from ClientRenderDelegate to tell us whether the
    // currently focused DOM node is editable. Use of |focus_on_editable_field_|
    // is redundant with CefKeyEvent.focus_on_editable_field in OnPreKeyEvent
    // but is useful for demonstration purposes.
    focus_on_editable_field_ = message->GetArgumentList()->GetBool(0);
    return true;
  }

  return false;
}

void ClientHandler::OnBeforeContextMenu(
    CefRefPtr<CefBrowser> browser,
    CefRefPtr<CefFrame> frame,
    CefRefPtr<CefContextMenuParams> params,
    CefRefPtr<CefMenuModel> model) {
  CEF_REQUIRE_UI_THREAD();

  if ((params->GetTypeFlags() & (CM_TYPEFLAG_PAGE | CM_TYPEFLAG_FRAME)) != 0) {
    // Add a separator if the menu already has items.
    if (model->GetCount() > 0)
      model->AddSeparator();

    // Add DevTools items to all context menus.
    model->AddItem(CLIENT_ID_SHOW_DEVTOOLS, "&Show DevTools");
    model->AddItem(CLIENT_ID_CLOSE_DEVTOOLS, "Close DevTools");
    model->AddSeparator();
    model->AddItem(CLIENT_ID_INSPECT_ELEMENT, "Inspect Element");

    // Test context menu features.
    BuildTestMenu(model);
  }
}

bool ClientHandler::OnContextMenuCommand(
    CefRefPtr<CefBrowser> browser,
    CefRefPtr<CefFrame> frame,
    CefRefPtr<CefContextMenuParams> params,
    int command_id,
    EventFlags event_flags) {
  CEF_REQUIRE_UI_THREAD();

  switch (command_id) {
    case CLIENT_ID_SHOW_DEVTOOLS:
      ShowDevTools(browser, CefPoint());
      return true;
    case CLIENT_ID_CLOSE_DEVTOOLS:
      CloseDevTools(browser);
      return true;
    case CLIENT_ID_INSPECT_ELEMENT:
      ShowDevTools(browser, CefPoint(params->GetXCoord(), params->GetYCoord()));
      return true;
    default:  // Allow default handling, if any.
      return ExecuteTestMenu(command_id);
  }
}

#if !defined(OS_LINUX)

bool ClientHandler::OnFileDialog(CefRefPtr<CefBrowser> browser,
                                 FileDialogMode mode,
                                 const CefString& title,
                                 const CefString& default_file_name,
                                 const std::vector<CefString>& accept_types,
                                 CefRefPtr<CefFileDialogCallback> callback) {
  CEF_REQUIRE_UI_THREAD();

  return false;
}

#endif  // !defined(OS_LINUX)

bool ClientHandler::OnConsoleMessage(CefRefPtr<CefBrowser> browser,
                                     const CefString& message,
                                     const CefString& source,
                                     int line) {
  CEF_REQUIRE_UI_THREAD();

  bool first_message;
  std::string logFile;

  {
    first_message = log_file_.empty();
    if (first_message) {
      std::stringstream ss;
      ss << AppGetWorkingDirectory();
#if defined(OS_WIN)
      ss << "\\";
#else
      ss << "/";
#endif
      ss << "console.log";
      log_file_ = ss.str();
    }
    logFile = log_file_;
  }

  FILE* file = fopen(logFile.c_str(), "a");
  if (file) {
    std::stringstream ss;
    ss << "Message: " << std::string(message) << "\r\nSource: " <<
        std::string(source) << "\r\nLine: " << line <<
        "\r\n-----------------------\r\n";
    fputs(ss.str().c_str(), file);
    fclose(file);

    if (first_message)
      SendNotification(NOTIFY_CONSOLE_MESSAGE);
  }

  return false;
}

void ClientHandler::OnBeforeDownload(
    CefRefPtr<CefBrowser> browser,
    CefRefPtr<CefDownloadItem> download_item,
    const CefString& suggested_name,
    CefRefPtr<CefBeforeDownloadCallback> callback) {
  CEF_REQUIRE_UI_THREAD();

  // Continue the download and show the "Save As" dialog.
  callback->Continue(GetDownloadPath(suggested_name), true);
}

void ClientHandler::OnDownloadUpdated(
    CefRefPtr<CefBrowser> browser,
    CefRefPtr<CefDownloadItem> download_item,
    CefRefPtr<CefDownloadItemCallback> callback) {
  CEF_REQUIRE_UI_THREAD();

  if (download_item->IsComplete()) {
    SetLastDownloadFile(download_item->GetFullPath());
    SendNotification(NOTIFY_DOWNLOAD_COMPLETE);
  }
}

bool ClientHandler::OnDragEnter(CefRefPtr<CefBrowser> browser,
                                CefRefPtr<CefDragData> dragData,
                                CefDragHandler::DragOperationsMask mask) {
  CEF_REQUIRE_UI_THREAD();

  // Forbid dragging of link URLs.
  if (mask & DRAG_OPERATION_LINK)
    return true;

  return false;
}

bool ClientHandler::OnRequestGeolocationPermission(
      CefRefPtr<CefBrowser> browser,
      const CefString& requesting_url,
      int request_id,
      CefRefPtr<CefGeolocationCallback> callback) {
  CEF_REQUIRE_UI_THREAD();

  // Allow geolocation access from all websites.
  callback->Continue(true);
  return true;
}

#if !defined(OS_LINUX)

bool ClientHandler::OnJSDialog(CefRefPtr<CefBrowser> browser,
                               const CefString& origin_url,
                               const CefString& accept_lang,
                               JSDialogType dialog_type,
                               const CefString& message_text,
                               const CefString& default_prompt_text,
                               CefRefPtr<CefJSDialogCallback> callback,
                               bool& suppress_message) {
  CEF_REQUIRE_UI_THREAD();
  return false;
}

bool ClientHandler::OnBeforeUnloadDialog(
    CefRefPtr<CefBrowser> browser,
    const CefString& message_text,
    bool is_reload,
    CefRefPtr<CefJSDialogCallback> callback) {
  CEF_REQUIRE_UI_THREAD();
  return false;
}

void ClientHandler::OnResetDialogState(CefRefPtr<CefBrowser> browser) {
  CEF_REQUIRE_UI_THREAD();
}

#endif  // !defined(OS_LINUX)

bool ClientHandler::OnPreKeyEvent(CefRefPtr<CefBrowser> browser,
                                  const CefKeyEvent& event,
                                  CefEventHandle os_event,
                                  bool* is_keyboard_shortcut) {
  CEF_REQUIRE_UI_THREAD();

  if (!event.focus_on_editable_field && event.windows_key_code == 0x20) {
    // Special handling for the space character when an input element does not
    // have focus. Handling the event in OnPreKeyEvent() keeps the event from
    // being processed in the renderer. If we instead handled the event in the
    // OnKeyEvent() method the space key would cause the window to scroll in
    // addition to showing the alert box.
    if (event.type == KEYEVENT_RAWKEYDOWN) {
      browser->GetMainFrame()->ExecuteJavaScript(
          "alert('You pressed the space bar!');", "", 0);
    }
    return true;
  }

  return false;
}

bool ClientHandler::OnBeforePopup(CefRefPtr<CefBrowser> browser,
                                  CefRefPtr<CefFrame> frame,
                                  const CefString& target_url,
                                  const CefString& target_frame_name,
                                  const CefPopupFeatures& popupFeatures,
                                  CefWindowInfo& windowInfo,
                                  CefRefPtr<CefClient>& client,
                                  CefBrowserSettings& settings,
                                  bool* no_javascript_access) {
  CEF_REQUIRE_IO_THREAD();

  if (browser->GetHost()->IsWindowRenderingDisabled()) {
    // Cancel popups in off-screen rendering mode.
    return true;
  }
  return false;
}

void ClientHandler::OnAfterCreated(CefRefPtr<CefBrowser> browser) {
  CEF_REQUIRE_UI_THREAD();
ClientHandler::OnTitleChange(browser);
  if (!message_router_) {
    // Create the browser-side router for query handling.
    CefMessageRouterConfig config;
    message_router_ = CefMessageRouterBrowserSide::Create(config);

    // Register handlers with the router.
    CreateMessageHandlers(message_handler_set_);
    MessageHandlerSet::const_iterator it = message_handler_set_.begin();
    for (; it != message_handler_set_.end(); ++it)
      message_router_->AddHandler(*(it), false);
  }

  // Disable mouse cursor change if requested via the command-line flag.
  if (mouse_cursor_change_disabled_)
    browser->GetHost()->SetMouseCursorChangeDisabled(true);

  if (!GetBrowser())   {
    base::AutoLock lock_scope(lock_);
    // We need to keep the main child window, but not popup windows
    browser_ = browser;
    browser_id_ = browser->GetIdentifier();
  } else if (browser->IsPopup()) {
    // Add to the list of popup browsers.
    popup_browsers_.push_back(browser);

    // Give focus to the popup browser. Perform asynchronously because the
    // parent window may attempt to keep focus after launching the popup.
    CefPostTask(TID_UI,
        base::Bind(&CefBrowserHost::SetFocus, browser->GetHost().get(), true));
  }

  browser_count_++;
}

bool ClientHandler::DoClose(CefRefPtr<CefBrowser> browser) {
  CEF_REQUIRE_UI_THREAD();

 developerStudioProcess->StopProcess();

  // Closing the main window requires special handling. See the DoClose()
  // documentation in the CEF header for a detailed destription of this
  // process.
  if (GetBrowserId() == browser->GetIdentifier()) {
    base::AutoLock lock_scope(lock_);
    // Set a flag to indicate that the window close should be allowed.
    is_closing_ = true;
  }

  // Allow the close. For windowed browsers this will result in the OS close
  // event being sent.
  return false;
}

void ClientHandler::OnBeforeClose(CefRefPtr<CefBrowser> browser) {
  CEF_REQUIRE_UI_THREAD();

  message_router_->OnBeforeClose(browser);

  if (GetBrowserId() == browser->GetIdentifier()) {
    {
      base::AutoLock lock_scope(lock_);
      // Free the browser pointer so that the browser can be destroyed
      browser_ = NULL;
    }

    if (osr_handler_.get()) {
      osr_handler_->OnBeforeClose(browser);
      osr_handler_ = NULL;
    }
  } else if (browser->IsPopup()) {
    // Remove from the browser popup list.
    BrowserList::iterator bit = popup_browsers_.begin();
    for (; bit != popup_browsers_.end(); ++bit) {
      if ((*bit)->IsSame(browser)) {
        popup_browsers_.erase(bit);
        break;
      }
    }
  }

  if (--browser_count_ == 0) {
    // All browser windows have closed.
    // Remove and delete message router handlers.
    MessageHandlerSet::const_iterator it = message_handler_set_.begin();
    for (; it != message_handler_set_.end(); ++it) {
      message_router_->RemoveHandler(*(it));
      delete *(it);
    }
    message_handler_set_.clear();
    message_router_ = NULL;

    // Quit the application message loop.
    AppQuitMessageLoop();
  }
}

void ClientHandler::OnLoadingStateChange(CefRefPtr<CefBrowser> browser,
                                         bool isLoading,
                                         bool canGoBack,
                                         bool canGoForward) {
  CEF_REQUIRE_UI_THREAD();

  SetLoading(isLoading);
  SetNavState(canGoBack, canGoForward);
}

void ClientHandler::OnLoadError(CefRefPtr<CefBrowser> browser,
                                CefRefPtr<CefFrame> frame,
                                ErrorCode errorCode,
                                const CefString& errorText,
                                const CefString& failedUrl) {
  CEF_REQUIRE_UI_THREAD();

  // Don't display an error for downloaded files.
  if (errorCode == ERR_ABORTED)
    return;

  // Don't display an error for external protocols that we allow the OS to
  // handle. See OnProtocolExecution().
  if (errorCode == ERR_UNKNOWN_URL_SCHEME) {
    std::string urlStr = frame->GetURL();
    if (urlStr.find("spotify:") == 0)
      return;
  }

  // Display a load error message.
  std::stringstream ss;
  ss << "<html><body bgcolor=\"white\">"
        "<h2>Failed to load URL " << std::string(failedUrl) <<
        " with error " << std::string(errorText) << " (" << errorCode <<
        ").</h2></body></html>";
  frame->LoadString(ss.str(), failedUrl);
}

bool ClientHandler::OnBeforeBrowse(CefRefPtr<CefBrowser> browser,
                                   CefRefPtr<CefFrame> frame,
                                   CefRefPtr<CefRequest> request,
                                   bool is_redirect) {
  CEF_REQUIRE_UI_THREAD();

  message_router_->OnBeforeBrowse(browser, frame);
  return false;
}

CefRefPtr<CefResourceHandler> ClientHandler::GetResourceHandler(
    CefRefPtr<CefBrowser> browser,
    CefRefPtr<CefFrame> frame,
    CefRefPtr<CefRequest> request) {
  CEF_REQUIRE_IO_THREAD();

  std::string url = request->GetURL();
  if (url.find(kTestOrigin) == 0) {
    // Handle URLs in the test origin.
    std::string file_name, mime_type;
    if (ParseTestUrl(url, &file_name, &mime_type)) {
      if (file_name == "request.html") {
        // Show the request contents.
        std::string dump;
        DumpRequestContents(request, dump);
        std::string str = "<html><body bgcolor=\"white\"><pre>" + dump +
                          "</pre></body></html>";
        CefRefPtr<CefStreamReader> stream =
            CefStreamReader::CreateForData(
                static_cast<void*>(const_cast<char*>(str.c_str())),
                str.size());
        DCHECK(stream.get());
        return new CefStreamResourceHandler("text/html", stream);
      } else {
        // Load the resource from file.
        CefRefPtr<CefStreamReader> stream =
            GetBinaryResourceReader(file_name.c_str());
        if (stream.get())
          return new CefStreamResourceHandler(mime_type, stream);
      }
    }
  }

  return NULL;
}

bool ClientHandler::OnQuotaRequest(CefRefPtr<CefBrowser> browser,
                                   const CefString& origin_url,
                                   int64 new_size,
                                   CefRefPtr<CefQuotaCallback> callback) {
  CEF_REQUIRE_IO_THREAD();

  static const int64 max_size = 1024 * 1024 * 20;  // 20mb.

  // Grant the quota request if the size is reasonable.
  callback->Continue(new_size <= max_size);
  return true;
}

void ClientHandler::OnProtocolExecution(CefRefPtr<CefBrowser> browser,
                                        const CefString& url,
                                        bool& allow_os_execution) {
  CEF_REQUIRE_UI_THREAD();

  std::string urlStr = url;

  // Allow OS execution of Spotify URIs.
  if (urlStr.find("spotify:") == 0)
    allow_os_execution = true;
}

void ClientHandler::OnRenderProcessTerminated(CefRefPtr<CefBrowser> browser,
                                              TerminationStatus status) {
  CEF_REQUIRE_UI_THREAD();

  message_router_->OnRenderProcessTerminated(browser);

  // Load the startup URL if that's not the website that we terminated on.
  CefRefPtr<CefFrame> frame = browser->GetMainFrame();
  std::string url = frame->GetURL();
  std::transform(url.begin(), url.end(), url.begin(), tolower);

  std::string startupURL = GetStartupURL();
 
  if (startupURL != "chrome://crash" && !url.empty() &&
      url.find(startupURL) != 0) {
    frame->LoadURL(startupURL);
  }
}

bool ClientHandler::GetRootScreenRect(CefRefPtr<CefBrowser> browser,
                                      CefRect& rect) {
  CEF_REQUIRE_UI_THREAD();
  if (!osr_handler_.get())
    return false;
  return osr_handler_->GetRootScreenRect(browser, rect);
}

bool ClientHandler::GetViewRect(CefRefPtr<CefBrowser> browser, CefRect& rect) {
  CEF_REQUIRE_UI_THREAD();
  if (!osr_handler_.get())
    return false;
  return osr_handler_->GetViewRect(browser, rect);
}

bool ClientHandler::GetScreenPoint(CefRefPtr<CefBrowser> browser,
                                   int viewX,
                                   int viewY,
                                   int& screenX,
                                   int& screenY) {
  CEF_REQUIRE_UI_THREAD();
  if (!osr_handler_.get())
    return false;
  return osr_handler_->GetScreenPoint(browser, viewX, viewY, screenX, screenY);
}

bool ClientHandler::GetScreenInfo(CefRefPtr<CefBrowser> browser,
                                  CefScreenInfo& screen_info) {
  CEF_REQUIRE_UI_THREAD();
  if (!osr_handler_.get())
    return false;
  return osr_handler_->GetScreenInfo(browser, screen_info);
}

void ClientHandler::OnPopupShow(CefRefPtr<CefBrowser> browser,
                                bool show) {
  CEF_REQUIRE_UI_THREAD();
  if (!osr_handler_.get())
    return;
  return osr_handler_->OnPopupShow(browser, show);
}

void ClientHandler::OnPopupSize(CefRefPtr<CefBrowser> browser,
                                const CefRect& rect) {
  CEF_REQUIRE_UI_THREAD();
  if (!osr_handler_.get())
    return;
  return osr_handler_->OnPopupSize(browser, rect);
}

void ClientHandler::OnPaint(CefRefPtr<CefBrowser> browser,
                            PaintElementType type,
                            const RectList& dirtyRects,
                            const void* buffer,
                            int width,
                            int height) {
  CEF_REQUIRE_UI_THREAD();
  if (!osr_handler_.get())
    return;
  osr_handler_->OnPaint(browser, type, dirtyRects, buffer, width, height);
}

void ClientHandler::OnCursorChange(CefRefPtr<CefBrowser> browser,
                                   CefCursorHandle cursor,
                                   CursorType type,
                                   const CefCursorInfo& custom_cursor_info) {
  CEF_REQUIRE_UI_THREAD();
  if (!osr_handler_.get())
    return;
  osr_handler_->OnCursorChange(browser, cursor, type, custom_cursor_info);
}

bool ClientHandler::StartDragging(CefRefPtr<CefBrowser> browser,
    CefRefPtr<CefDragData> drag_data,
    CefRenderHandler::DragOperationsMask allowed_ops,
    int x, int y) {
  CEF_REQUIRE_UI_THREAD();
  if (!osr_handler_.get())
    return false;
  return osr_handler_->StartDragging(browser, drag_data, allowed_ops, x, y);
}

void ClientHandler::UpdateDragCursor(CefRefPtr<CefBrowser> browser,
    CefRenderHandler::DragOperation operation) {
  CEF_REQUIRE_UI_THREAD();
  if (!osr_handler_.get())
    return;
  osr_handler_->UpdateDragCursor(browser, operation);
}

void ClientHandler::SetMainWindowHandle(ClientWindowHandle handle) {
  if (!CefCurrentlyOn(TID_UI)) {
    // Execute on the UI thread.
    CefPostTask(TID_UI,
        base::Bind(&ClientHandler::SetMainWindowHandle, this, handle));
    return;
  }

  main_handle_ = handle;
}

ClientWindowHandle ClientHandler::GetMainWindowHandle() const {
  CEF_REQUIRE_UI_THREAD();
  return main_handle_;
}

void ClientHandler::SetEditWindowHandle(ClientWindowHandle handle) {
  if (!CefCurrentlyOn(TID_UI)) {
    // Execute on the UI thread.
    CefPostTask(TID_UI,
        base::Bind(&ClientHandler::SetEditWindowHandle, this, handle));
    return;
  }

  edit_handle_ = handle;
}

void ClientHandler::SetButtonWindowHandles(ClientWindowHandle backHandle,
                                           ClientWindowHandle forwardHandle,
                                           ClientWindowHandle reloadHandle,
                                           ClientWindowHandle stopHandle) {
  if (!CefCurrentlyOn(TID_UI)) {
    // Execute on the UI thread.
    CefPostTask(TID_UI,
        base::Bind(&ClientHandler::SetButtonWindowHandles, this,
                   backHandle, forwardHandle, reloadHandle, stopHandle));
    return;
  }

  back_handle_ = backHandle;
  forward_handle_ = forwardHandle;
  reload_handle_ = reloadHandle;
  stop_handle_ = stopHandle;
}

void ClientHandler::SetOSRHandler(CefRefPtr<RenderHandler> handler) {
  if (!CefCurrentlyOn(TID_UI)) {
    // Execute on the UI thread.
    CefPostTask(TID_UI,
        base::Bind(&ClientHandler::SetOSRHandler, this, handler));
    return;
  }

  osr_handler_ = handler;
}

CefRefPtr<ClientHandler::RenderHandler> ClientHandler::GetOSRHandler() const {
  return osr_handler_; 
}

CefRefPtr<CefBrowser> ClientHandler::GetBrowser() const {
  base::AutoLock lock_scope(lock_);
  return browser_;
}

int ClientHandler::GetBrowserId() const {
  base::AutoLock lock_scope(lock_);
  return browser_id_;
}

void ClientHandler::CloseAllBrowsers(bool force_close) {
  if (!CefCurrentlyOn(TID_UI)) {
    // Execute on the UI thread.
    CefPostTask(TID_UI,
        base::Bind(&ClientHandler::CloseAllBrowsers, this, force_close));
    return;
  }

  if (!popup_browsers_.empty()) {
    // Request that any popup browsers close.
    BrowserList::const_iterator it = popup_browsers_.begin();
    for (; it != popup_browsers_.end(); ++it)
      (*it)->GetHost()->CloseBrowser(force_close);
  }

  if (browser_.get()) {
    // Request that the main browser close.
    browser_->GetHost()->CloseBrowser(force_close);
  }
}

bool ClientHandler::IsClosing() const {
  base::AutoLock lock_scope(lock_);
  return is_closing_;
}

std::string ClientHandler::GetLogFile() const {
  CEF_REQUIRE_UI_THREAD();
  return log_file_;
}

void ClientHandler::SetLastDownloadFile(const std::string& fileName) {
  CEF_REQUIRE_UI_THREAD();
  last_download_file_ = fileName;
}

std::string ClientHandler::GetLastDownloadFile() const {
  CEF_REQUIRE_UI_THREAD();
  return last_download_file_;
}

void ClientHandler::ShowDevTools(CefRefPtr<CefBrowser> browser,
                                 const CefPoint& inspect_element_at) {
  CefWindowInfo windowInfo;
  CefBrowserSettings settings;

#if defined(OS_WIN)
  windowInfo.SetAsPopup(browser->GetHost()->GetWindowHandle(), "DevTools");
#endif

  browser->GetHost()->ShowDevTools(windowInfo, this, settings,
                                   inspect_element_at);
}

void ClientHandler::CloseDevTools(CefRefPtr<CefBrowser> browser) {
  browser->GetHost()->CloseDevTools();
}

std::string ClientHandler::GetStartupURL() const {
  return startup_url_;

}

void ClientHandler::BeginTracing() {
  if (!CefCurrentlyOn(TID_UI)) {
    // Execute on the UI thread.
    CefPostTask(TID_UI, base::Bind(&ClientHandler::BeginTracing, this));
    return;
  }

  CefBeginTracing(CefString(), NULL);
}

void ClientHandler::EndTracing() {
  if (!CefCurrentlyOn(TID_UI)) {
    // Execute on the UI thread.
    CefPostTask(TID_UI, base::Bind(&ClientHandler::EndTracing, this));
    return;
  }

  class Client : public CefEndTracingCallback,
                  public CefRunFileDialogCallback {
    public:
    explicit Client(CefRefPtr<ClientHandler> handler)
        : handler_(handler) {
      RunDialog();
    }

    void RunDialog() {
      static const char kDefaultFileName[] = "trace.txt";
      std::string path = handler_->GetDownloadPath(kDefaultFileName);
      if (path.empty())
        path = kDefaultFileName;

      // Results in a call to OnFileDialogDismissed.
      handler_->GetBrowser()->GetHost()->RunFileDialog(
          FILE_DIALOG_SAVE, CefString(), path, std::vector<CefString>(),
          this);
    }

    virtual void OnFileDialogDismissed(
        CefRefPtr<CefBrowserHost> browser_host,
        const std::vector<CefString>& file_paths) OVERRIDE {
      CEF_REQUIRE_UI_THREAD();
      if (!file_paths.empty()) {
        // File selected. Results in a call to OnEndTracingComplete.
        CefEndTracing(file_paths.front(), this);
      } else {
        // No file selected. Discard the trace data.
        CefEndTracing(CefString(), NULL);
      }
    }

    virtual void OnEndTracingComplete(
        const CefString& tracing_file) OVERRIDE {
      CEF_REQUIRE_UI_THREAD();
      handler_->SetLastDownloadFile(tracing_file.ToString());
      handler_->SendNotification(NOTIFY_DOWNLOAD_COMPLETE);
    }

    private:
    CefRefPtr<ClientHandler> handler_;

    IMPLEMENT_REFCOUNTING(Client);
  };

  new Client(this);
}

bool ClientHandler::Save(const std::string& path, const std::string& data) {
  FILE* f = fopen(path.c_str(), "w");
  if (!f)
    return false;
  size_t total = 0;
  do {
    size_t write = fwrite(data.c_str() + total, 1, data.size() - total, f);
    if (write == 0)
      break;
    total += write;
  } while (total < data.size());
  fclose(f);
  return true;
}

// static
void ClientHandler::CreateMessageHandlers(MessageHandlerSet& handlers) {
  // Create the dialog test handlers.
  dialog_test::CreateMessageHandlers(handlers);

  // Create the binding test handlers.
  binding_test::CreateMessageHandlers(handlers);

  // Create the window test handlers.
  window_test::CreateMessageHandlers(handlers);
}

void ClientHandler::BuildTestMenu(CefRefPtr<CefMenuModel> model) {
  if (model->GetCount() > 0)
    model->AddSeparator();

  // Build the sub menu.
  CefRefPtr<CefMenuModel> submenu =
      model->AddSubMenu(CLIENT_ID_TESTMENU_SUBMENU, "Context Menu Test");
  submenu->AddCheckItem(CLIENT_ID_TESTMENU_CHECKITEM, "Check Item");
  submenu->AddRadioItem(CLIENT_ID_TESTMENU_RADIOITEM1, "Radio Item 1", 0);
  submenu->AddRadioItem(CLIENT_ID_TESTMENU_RADIOITEM2, "Radio Item 2", 0);
  submenu->AddRadioItem(CLIENT_ID_TESTMENU_RADIOITEM3, "Radio Item 3", 0);

  // Check the check item.
  if (test_menu_state_.check_item)
    submenu->SetChecked(CLIENT_ID_TESTMENU_CHECKITEM, true);

  // Check the selected radio item.
  submenu->SetChecked(
      CLIENT_ID_TESTMENU_RADIOITEM1 + test_menu_state_.radio_item, true);
}

bool ClientHandler::ExecuteTestMenu(int command_id) {
  if (command_id == CLIENT_ID_TESTMENU_CHECKITEM) {
    // Toggle the check item.
    test_menu_state_.check_item ^= 1;
    return true;
  } else if (command_id >= CLIENT_ID_TESTMENU_RADIOITEM1 &&
             command_id <= CLIENT_ID_TESTMENU_RADIOITEM3) {
    // Store the selected radio item.
    test_menu_state_.radio_item = (command_id - CLIENT_ID_TESTMENU_RADIOITEM1);
    return true;
  }

  // Allow default handling to proceed.
  return false;
}

