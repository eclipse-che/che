// Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

#ifndef CEF_LIBCEF_DLL_CEF_MACROS_H_
#define CEF_LIBCEF_DLL_CEF_MACROS_H_
#pragma once

#ifdef BUILDING_CEF_SHARED
#include "base/macros.h"
#else  // !BUILDING_CEF_SHARED

// A macro to disallow the copy constructor and operator= functions
// This should be used in the private: declarations for a class
#define DISALLOW_COPY_AND_ASSIGN(TypeName) \
  TypeName(const TypeName&);               \
  void operator=(const TypeName&)

#endif  // !BUILDING_CEF_SHARED

#endif  // CEF_LIBCEF_DLL_CEF_MACROS_H_
