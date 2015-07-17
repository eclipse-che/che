// Copyright (c) 2013 Marshall A. Greenblatt. All rights reserved.
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

#ifndef CEF_INCLUDE_CEF_REQUEST_CONTEXT_H_
#define CEF_INCLUDE_CEF_REQUEST_CONTEXT_H_
#pragma once

#include "include/cef_request_context_handler.h"

///
// A request context provides request handling for a set of related browser
// objects. A request context is specified when creating a new browser object
// via the CefBrowserHost static factory methods. Browser objects with different
// request contexts will never be hosted in the same render process. Browser
// objects with the same request context may or may not be hosted in the same
// render process depending on the process model. Browser objects created
// indirectly via the JavaScript window.open function or targeted links will
// share the same render process and the same request context as the source
// browser. When running in single-process mode there is only a single render
// process (the main process) and so all browsers created in single-process mode
// will share the same request context. This will be the first request context
// passed into a CefBrowserHost static factory method and all other request
// context objects will be ignored.
///
/*--cef(source=library,no_debugct_check)--*/
class CefRequestContext : public virtual CefBase {
 public:
  ///
  // Returns the global context object.
  ///
  /*--cef()--*/
  static CefRefPtr<CefRequestContext> GetGlobalContext();

  ///
  // Creates a new context object with the specified handler.
  ///
  /*--cef(optional_param=handler)--*/
  static CefRefPtr<CefRequestContext> CreateContext(
      CefRefPtr<CefRequestContextHandler> handler);

  ///
  // Returns true if this object is pointing to the same context as |that|
  // object.
  ///
  /*--cef()--*/
  virtual bool IsSame(CefRefPtr<CefRequestContext> other) =0;

  ///
  // Returns true if this object is the global context.
  ///
  /*--cef()--*/
  virtual bool IsGlobal() =0;

  ///
  // Returns the handler for this context if any.
  ///
  /*--cef()--*/
  virtual CefRefPtr<CefRequestContextHandler> GetHandler() =0;
};

#endif  // CEF_INCLUDE_CEF_REQUEST_CONTEXT_H_
