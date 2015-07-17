// Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

#ifndef CEF_LIBCEF_DLL_CEF_LOGGING_H_
#define CEF_LIBCEF_DLL_CEF_LOGGING_H_
#pragma once

#include "include/cef_task.h"

#ifdef BUILDING_CEF_SHARED
#include "base/logging.h"
#else  // !BUILDING_CEF_SHARED
#include <assert.h>  // NOLINT(build/include_order)

#ifndef NDEBUG
#define DCHECK(condition) assert(condition)
#else
#define DCHECK(condition) ((void)0)
#endif

#define DCHECK_EQ(val1, val2) DCHECK(val1 == val2)
#define DCHECK_NE(val1, val2) DCHECK(val1 != val2)
#define DCHECK_LE(val1, val2) DCHECK(val1 <= val2)
#define DCHECK_LT(val1, val2) DCHECK(val1 < val2)
#define DCHECK_GE(val1, val2) DCHECK(val1 >= val2)
#define DCHECK_GT(val1, val2) DCHECK(val1 > val2)
#endif  // !BUILDING_CEF_SHARED

#define CEF_REQUIRE_UI_THREAD()       DCHECK(CefCurrentlyOn(TID_UI));
#define CEF_REQUIRE_IO_THREAD()       DCHECK(CefCurrentlyOn(TID_IO));
#define CEF_REQUIRE_FILE_THREAD()     DCHECK(CefCurrentlyOn(TID_FILE));
#define CEF_REQUIRE_RENDERER_THREAD() DCHECK(CefCurrentlyOn(TID_RENDERER));

#endif  // CEF_LIBCEF_DLL_CEF_LOGGING_H_
