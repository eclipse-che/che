// Copyright (c) 2013 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

// Defines all of the command line switches used by cefclient.

#ifndef CEF_TESTS_CEFCLIENT_CEFCLIENT_SWITCHES_H_
#define CEF_TESTS_CEFCLIENT_CEFCLIENT_SWITCHES_H_
#pragma once

namespace cefclient {

extern const char kMultiThreadedMessageLoop[];
extern const char kCachePath[];
extern const char kUrl[];
extern const char kOffScreenRenderingEnabled[];
extern const char kOffScreenFrameRate[];
extern const char kTransparentPaintingEnabled[];
extern const char kShowUpdateRect[];
extern const char kMouseCursorChangeDisabled[];

}  // namespace cefclient

#endif  // CEF_TESTS_CEFCLIENT_CEFCLIENT_SWITCHES_H_
