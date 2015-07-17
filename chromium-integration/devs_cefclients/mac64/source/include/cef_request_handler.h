// Copyright (c) 2011 Marshall A. Greenblatt. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//    * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//    * Neither the name of Google Inc. nor the name Chromium Embedded
// Framework nor the names of its contributors may be used to endorse
// or promote products derived from this software without specific prior
// written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// ---------------------------------------------------------------------------
//
// The contents of this file must follow a specific format in order to
// support the CEF translator tool. See the translator.README.txt file in the
// tools directory for more information.
//

#ifndef CEF_INCLUDE_CEF_REQUEST_HANDLER_H_
#define CEF_INCLUDE_CEF_REQUEST_HANDLER_H_
#pragma once

#include "include/cef_auth_callback.h"
#include "include/cef_base.h"
#include "include/cef_browser.h"
#include "include/cef_frame.h"
#include "include/cef_resource_handler.h"
#include "include/cef_response.h"
#include "include/cef_request.h"
#include "include/cef_web_plugin.h"

///
// Callback interface used for asynchronous continuation of quota requests.
///
/*--cef(source=library)--*/
class CefQuotaCallback : public virtual CefBase {
 public:
  ///
  // Continue the quota request. If |allow| is true the request will be
  // allowed. Otherwise, the request will be denied.
  ///
  /*--cef(capi_name=cont)--*/
  virtual void Continue(bool allow) =0;

  ///
  // Cancel the quota request.
  ///
  /*--cef()--*/
  virtual void Cancel() =0;
};


///
// Callback interface used for asynchronous continuation of url requests when
// invalid SSL certificates are encountered.
///
/*--cef(source=library)--*/
class CefAllowCertificateErrorCallback : public virtual CefBase {
 public:
  ///
  // Continue the url request. If |allow| is true the request will be
  // continued. Otherwise, the request will be canceled.
  ///
  /*--cef(capi_name=cont)--*/
  virtual void Continue(bool allow) =0;
};


///
// Implement this interface to handle events related to browser requests. The
// methods of this class will be called on the thread indicated.
///
/*--cef(source=client)--*/
class CefRequestHandler : public virtual CefBase {
 public:
  typedef cef_termination_status_t TerminationStatus;

  ///
  // Called on the UI thread before browser navigation. Return true to cancel
  // the navigation or false to allow the navigation to proceed. The |request|
  // object cannot be modified in this callback.
  // CefLoadHandler::OnLoadingStateChange will be called twice in all cases.
  // If the navigation is allowed CefLoadHandler::OnLoadStart and
  // CefLoadHandler::OnLoadEnd will be called. If the navigation is canceled
  // CefLoadHandler::OnLoadError will be called with an |errorCode| value of
  // ERR_ABORTED.
  ///
  /*--cef()--*/
  virtual bool OnBeforeBrowse(CefRefPtr<CefBrowser> browser,
                              CefRefPtr<CefFrame> frame,
                              CefRefPtr<CefRequest> request,
                              bool is_redirect) {
    return false;
  }

  ///
  // Called on the IO thread before a resource request is loaded. The |request|
  // object may be modified. To cancel the request return true otherwise return
  // false.
  ///
  /*--cef()--*/
  virtual bool OnBeforeResourceLoad(CefRefPtr<CefBrowser> browser,
                                    CefRefPtr<CefFrame> frame,
                                    CefRefPtr<CefRequest> request) {
    return false;
  }

  ///
  // Called on the IO thread before a resource is loaded. To allow the resource
  // to load normally return NULL. To specify a handler for the resource return
  // a CefResourceHandler object. The |request| object should not be modified in
  // this callback.
  ///
  /*--cef()--*/
  virtual CefRefPtr<CefResourceHandler> GetResourceHandler(
      CefRefPtr<CefBrowser> browser,
      CefRefPtr<CefFrame> frame,
      CefRefPtr<CefRequest> request) {
    return NULL;
  }

  ///
  // Called on the IO thread when a resource load is redirected. The |old_url|
  // parameter will contain the old URL. The |new_url| parameter will contain
  // the new URL and can be changed if desired.
  ///
  /*--cef()--*/
  virtual void OnResourceRedirect(CefRefPtr<CefBrowser> browser,
                                  CefRefPtr<CefFrame> frame,
                                  const CefString& old_url,
                                  CefString& new_url) {}

  ///
  // Called on the IO thread when the browser needs credentials from the user.
  // |isProxy| indicates whether the host is a proxy server. |host| contains the
  // hostname and |port| contains the port number. Return true to continue the
  // request and call CefAuthCallback::Continue() when the authentication
  // information is available. Return false to cancel the request.
  ///
  /*--cef(optional_param=realm)--*/
  virtual bool GetAuthCredentials(CefRefPtr<CefBrowser> browser,
                                  CefRefPtr<CefFrame> frame,
                                  bool isProxy,
                                  const CefString& host,
                                  int port,
                                  const CefString& realm,
                                  const CefString& scheme,
                                  CefRefPtr<CefAuthCallback> callback) {
    return false;
  }

  ///
  // Called on the IO thread when JavaScript requests a specific storage quota
  // size via the webkitStorageInfo.requestQuota function. |origin_url| is the
  // origin of the page making the request. |new_size| is the requested quota
  // size in bytes. Return true and call CefQuotaCallback::Continue() either in
  // this method or at a later time to grant or deny the request. Return false
  // to cancel the request.
  ///
  /*--cef(optional_param=realm)--*/
  virtual bool OnQuotaRequest(CefRefPtr<CefBrowser> browser,
                              const CefString& origin_url,
                              int64 new_size,
                              CefRefPtr<CefQuotaCallback> callback) {
    return false;
  }

  ///
  // Called on the UI thread to handle requests for URLs with an unknown
  // protocol component. Set |allow_os_execution| to true to attempt execution
  // via the registered OS protocol handler, if any.
  // SECURITY WARNING: YOU SHOULD USE THIS METHOD TO ENFORCE RESTRICTIONS BASED
  // ON SCHEME, HOST OR OTHER URL ANALYSIS BEFORE ALLOWING OS EXECUTION.
  ///
  /*--cef()--*/
  virtual void OnProtocolExecution(CefRefPtr<CefBrowser> browser,
                                   const CefString& url,
                                   bool& allow_os_execution) {}

  ///
  // Called on the UI thread to handle requests for URLs with an invalid
  // SSL certificate. Return true and call CefAllowCertificateErrorCallback::
  // Continue() either in this method or at a later time to continue or cancel
  // the request. Return false to cancel the request immediately. If |callback|
  // is empty the error cannot be recovered from and the request will be
  // canceled automatically. If CefSettings.ignore_certificate_errors is set
  // all invalid certificates will be accepted without calling this method.
  ///
  /*--cef()--*/
  virtual bool OnCertificateError(
      cef_errorcode_t cert_error,
      const CefString& request_url,
      CefRefPtr<CefAllowCertificateErrorCallback> callback) {
    return false;
  }

  ///
  // Called on the browser process IO thread before a plugin is loaded. Return
  // true to block loading of the plugin.
  ///
  /*--cef(optional_param=url,optional_param=policy_url)--*/
  virtual bool OnBeforePluginLoad(CefRefPtr<CefBrowser> browser,
                                  const CefString& url,
                                  const CefString& policy_url,
                                  CefRefPtr<CefWebPluginInfo> info) {
    return false;
  }

  ///
  // Called on the browser process UI thread when a plugin has crashed.
  // |plugin_path| is the path of the plugin that crashed.
  ///
  /*--cef()--*/
  virtual void OnPluginCrashed(CefRefPtr<CefBrowser> browser,
                               const CefString& plugin_path) {}

  ///
  // Called on the browser process UI thread when the render process
  // terminates unexpectedly. |status| indicates how the process
  // terminated.
  ///
  /*--cef()--*/
  virtual void OnRenderProcessTerminated(CefRefPtr<CefBrowser> browser,
                                         TerminationStatus status) {}
};

#endif  // CEF_INCLUDE_CEF_REQUEST_HANDLER_H_
