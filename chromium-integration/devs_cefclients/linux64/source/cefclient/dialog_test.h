// Copyright (c) 2012 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

#ifndef CEF_TESTS_CEFCLIENT_DIALOG_TEST_H_
#define CEF_TESTS_CEFCLIENT_DIALOG_TEST_H_
#pragma once

#include "cefclient/client_handler.h"

namespace dialog_test {

/// Handler creation. Called from ClientHandler.
void CreateMessageHandlers(ClientHandler::MessageHandlerSet& handlers);

}  // namespace dialog_test

#endif  // CEF_TESTS_CEFCLIENT_DIALOG_TEST_H_
