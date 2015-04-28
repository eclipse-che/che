# Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
# reserved. Use of this source code is governed by a BSD-style license that
# can be found in the LICENSE file.

#
# Source files.
#

# cefclient sources.
set(CEFCLIENT_SRCS
  binding_test.cpp
  binding_test.h
  bytes_write_handler.cpp
  bytes_write_handler.h
  cefclient.cpp
  cefclient.h
  client_app.cpp
  client_app.h
  client_app_delegates.cpp
  client_handler.cpp
  client_handler.h
  client_renderer.cpp
  client_renderer.h
  client_switches.cpp
  client_switches.h
  dialog_test.cpp
  dialog_test.h
  dragdrop_events.h
  osrenderer.cpp
  osrenderer.h
  performance_test.cpp
  performance_test.h
  performance_test_setup.h
  performance_test_tests.cpp
  resource_util.h
  scheme_test.cpp
  scheme_test.h
  string_util.cpp
  string_util.h
  window_test.cpp
  window_test.h
  )
set(CEFCLIENT_SRCS_LINUX
  cefclient_gtk.cpp
  cefclient_osr_widget_gtk.cpp
  cefclient_osr_widget_gtk.h
  client_handler_gtk.cpp
  print_handler_gtk.cpp
  print_handler_gtk.h
  resource_util_linux.cpp
  resource_util_posix.cpp
  window_test_gtk.cpp
  )
set(CEFCLIENT_SRCS_MACOSX
  cefclient_mac.mm
  cefclient_osr_widget_mac.h
  cefclient_osr_widget_mac.mm
  client_handler_mac.mm
  resource_util_mac.mm
  resource_util_posix.cpp
  window_test_mac.mm
  )
set(CEFCLIENT_SRCS_WINDOWS
  cefclient.exe.manifest
  cefclient.rc
  cefclient_osr_dragdrop_win.cpp
  cefclient_osr_dragdrop_win.h
  cefclient_osr_widget_win.cpp
  cefclient_osr_widget_win.h
  cefclient_win.cpp
  client_handler_win.cpp
  resource.h
  resource_util_win.cpp
  window_test_win.cpp
  )
APPEND_PLATFORM_SOURCES(CEFCLIENT_SRCS)
source_group(cefclient FILES ${CEFCLIENT_SRCS})

set(CEFCLIENT_RES_SRCS
  res/binding.html
  res/dialogs.html
  res/localstorage.html
  res/logo.png
  res/osr_test.html
  res/other_tests.html
  res/performance.html
  res/performance2.html
  res/transparency.html
  res/window.html
  res/xmlhttprequest.html
  )
set(CEFCLIENT_RES_SRCS_WINDOWS
  res/cefclient.ico
  res/small.ico
  )
APPEND_PLATFORM_SOURCES(CEFCLIENT_RES_SRCS)
source_group(cefclient\\\\res FILES ${CEFCLIENT_RES_SRCS})

set(CEFCLIENT_SRCS
  ${CEFCLIENT_SRCS}
  ${CEFCLIENT_RES_SRCS}
  )

# cefclient helper sources.
set(CEFCLIENT_HELPER_SRCS_MACOSX
  binding_test.cpp
  binding_test.h
  client_app.cpp
  client_app.h
  client_app_delegates.cpp
  client_handler.cpp
  client_handler.h
  client_handler_mac.mm
  client_renderer.cpp
  client_renderer.h
  client_switches.cpp
  client_switches.h
  dialog_test.cpp
  dialog_test.h
  performance_test.cpp
  performance_test.h
  performance_test_setup.h
  performance_test_tests.cpp
  process_helper_mac.cpp
  resource_util.h
  resource_util_mac.mm
  resource_util_posix.cpp
  scheme_test.cpp
  scheme_test.h
  string_util.cpp
  string_util.h
  window_test.cpp
  window_test.h
  window_test_mac.mm
  )
APPEND_PLATFORM_SOURCES(CEFCLIENT_HELPER_SRCS)
source_group(cefclient FILES ${CEFCLIENT_HELPER_SRCS})

# cefclient resources.
set(CEFCLIENT_RESOURCES_MAC_SRCS_MACOSX
  mac/Info.plist
  mac/cefclient.icns
  )
APPEND_PLATFORM_SOURCES(CEFCLIENT_RESOURCES_MAC_SRCS)
source_group(cefclient\\\\mac FILES ${CEFCLIENT_RESOURCES_MAC_SRCS})

set(CEFCLIENT_RESOURCES_MAC_ENGLISH_LPROJ_SRCS_MACOSX
  mac/English.lproj/InfoPlist.strings
  mac/English.lproj/MainMenu.xib
  )
APPEND_PLATFORM_SOURCES(CEFCLIENT_RESOURCES_MAC_ENGLISH_LPROJ_SRCS)
source_group(cefclient\\\\mac\\\\English.lproj FILES ${CEFCLIENT_RESOURCES_MAC_ENGLISH_LPROJ_SRCS})

set(CEFCLIENT_RESOURCES_RES_SRCS
  res/binding.html
  res/dialogs.html
  res/localstorage.html
  res/logo.png
  res/osr_test.html
  res/other_tests.html
  res/performance.html
  res/performance2.html
  res/transparency.html
  res/window.html
  res/xmlhttprequest.html
  )
source_group(cefclient\\\\res FILES ${CEFCLIENT_RESOURCES_RES_SRCS})

set(CEFCLIENT_RESOURCES_SRCS
  ${CEFCLIENT_RESOURCES_MAC_SRCS}
  ${CEFCLIENT_RESOURCES_MAC_ENGLISH_LPROJ_SRCS}
  ${CEFCLIENT_RESOURCES_RES_SRCS}
  )


#
# Shared configuration.
#

# Target executable names.
set(CEF_TARGET "cefclient")
if(OS_MACOSX)
  set(CEF_HELPER_TARGET "cefclient Helper")
endif()

# Logical target used to link the libcef library.
ADD_LOGICAL_TARGET("libcef_lib" "${CEF_LIB_DEBUG}" "${CEF_LIB_RELEASE}")

# Determine the target output directory.
SET_CEF_TARGET_OUT_DIR()


#
# Linux configuration.
#

if(OS_LINUX)
  # Find required libraries and update compiler/linker variables.
  FIND_LINUX_LIBRARIES("gmodule-2.0 gtk+-2.0 gthread-2.0 gtk+-unix-print-2.0 gtkglext-1.0")

  # Executable target.
  add_executable(${CEF_TARGET} ${CEFCLIENT_SRCS})
  add_dependencies(${CEF_TARGET} libcef_dll_wrapper)
  target_link_libraries(${CEF_TARGET} libcef_lib libcef_dll_wrapper ${CEF_STANDARD_LIBS})

  # Set rpath so that libraries can be placed next to the executable.
  set_target_properties(${CEF_TARGET} PROPERTIES INSTALL_RPATH "$ORIGIN")
  set_target_properties(${CEF_TARGET} PROPERTIES BUILD_WITH_INSTALL_RPATH TRUE)
  set_target_properties(${CEF_TARGET} PROPERTIES RUNTIME_OUTPUT_DIRECTORY ${CEF_TARGET_OUT_DIR})

  # Copy CEF binary and resource files to the target output directory.
  COPY_FILES("${CEF_TARGET}" "${CEF_BINARY_FILES}" "${CEF_BINARY_DIR}" "${CEF_TARGET_OUT_DIR}")
  COPY_FILES("${CEF_TARGET}" "${CEF_RESOURCE_FILES}" "${CEF_RESOURCE_DIR}" "${CEF_TARGET_OUT_DIR}")

  # Copy cefclient resource files to the target output directory.
  COPY_FILES("${CEF_TARGET}" "${CEFCLIENT_RESOURCES_SRCS}" "${CMAKE_CURRENT_SOURCE_DIR}" "${CEF_TARGET_OUT_DIR}")
  # Rename the "res" directory to "files".
  RENAME_DIRECTORY("${CEF_TARGET}" "${CEF_TARGET_OUT_DIR}/res" "${CEF_TARGET_OUT_DIR}/files")

  # Set SUID permissions on the chrome-sandbox target.
  SET_LINUX_SUID_PERMISSIONS("${CEF_TARGET}" "${CEF_TARGET_OUT_DIR}/chrome-sandbox")
endif()


#
# Mac OS X configuration.
#

if(OS_MACOSX)
  # Output paths for the app bundles.
  set(CEF_APP "${CEF_TARGET_OUT_DIR}/${CEF_TARGET}.app")
  set(CEF_HELPER_APP "${CEF_TARGET_OUT_DIR}/${CEF_HELPER_TARGET}.app")

  # Variable referenced from Info.plist files.
  set(PRODUCT_NAME "${CEF_TARGET}")

  # Helper executable target.
  add_executable(${CEF_HELPER_TARGET} MACOSX_BUNDLE ${CEFCLIENT_HELPER_SRCS})
  add_dependencies(${CEF_HELPER_TARGET} libcef_dll_wrapper)
  target_link_libraries(${CEF_HELPER_TARGET} libcef_lib libcef_dll_wrapper ${CEF_STANDARD_LIBS})
  set_target_properties(${CEF_HELPER_TARGET} PROPERTIES
    MACOSX_BUNDLE_INFO_PLIST ${CMAKE_CURRENT_SOURCE_DIR}/mac/helper-Info.plist
    )

  # Fix the framework link in the helper executable.
  FIX_MACOSX_HELPER_FRAMEWORK_LINK(${CEF_HELPER_TARGET} ${CEF_HELPER_APP})

  # Main executable target.
  add_executable(${CEF_TARGET} MACOSX_BUNDLE ${CEFCLIENT_RESOURCES_SRCS} ${CEFCLIENT_SRCS})
  add_dependencies(${CEF_TARGET} libcef_dll_wrapper "${CEF_HELPER_TARGET}")
  target_link_libraries(${CEF_TARGET} libcef_lib libcef_dll_wrapper ${CEF_STANDARD_LIBS} "-framework OpenGL")
  set_target_properties(${CEF_TARGET} PROPERTIES
    RESOURCE "${CEFCLIENT_RESOURCES_SRCS}"
    MACOSX_BUNDLE_INFO_PLIST ${CMAKE_CURRENT_SOURCE_DIR}/mac/Info.plist
    )

  # Copy files into the main app bundle.
  add_custom_command(
    TARGET ${CEF_TARGET}
    POST_BUILD
    # Copy the helper app bundle into the Frameworks directory.
    COMMAND ${CMAKE_COMMAND} -E copy_directory
            "${CEF_HELPER_APP}"
            "${CEF_APP}/Contents/Frameworks/${CEF_HELPER_TARGET}.app"
    # Copy the CEF framework into the Frameworks directory.
    COMMAND ${CMAKE_COMMAND} -E copy_directory
            "${CEF_BINARY_DIR}/Chromium Embedded Framework.framework"
            "${CEF_APP}/Contents/Frameworks/Chromium Embedded Framework.framework"
    VERBATIM
    )

  # Fix the framework link in the main executable.
  FIX_MACOSX_MAIN_FRAMEWORK_LINK(${CEF_TARGET} ${CEF_APP})

  # Make the other helper app bundles.
  MAKE_MACOSX_HELPERS(${CEF_TARGET} ${CEF_APP})

  if(NOT ${CMAKE_GENERATOR} STREQUAL "Xcode")
    # Manually process and copy over resource files.
    # The Xcode generator handles this via the set_target_properties RESOURCE directive.
    set(PREFIXES "mac/" "res/")  # Remove these prefixes from input file paths.
    COPY_MACOSX_RESOURCES("${CEFCLIENT_RESOURCES_SRCS}" "${PREFIXES}" "${CEF_TARGET}" "${CMAKE_CURRENT_SOURCE_DIR}" "${CEF_APP}")
  endif()
endif()


#
# Windows configuration.
#

if(OS_WINDOWS)
  # Executable target.
  add_executable(${CEF_TARGET} WIN32 ${CEFCLIENT_SRCS})
  add_dependencies(${CEF_TARGET} libcef_dll_wrapper)
  target_link_libraries(${CEF_TARGET} libcef_lib libcef_dll_wrapper ${CEF_STANDARD_LIBS} "glu32.lib" "opengl32.lib")

  if(USE_SANDBOX)
    # Logical target used to link the cef_sandbox library.
    ADD_LOGICAL_TARGET("cef_sandbox_lib" "${CEF_SANDBOX_LIB_DEBUG}" "${CEF_SANDBOX_LIB_RELEASE}")
    target_link_libraries(${CEF_TARGET} cef_sandbox_lib ${CEF_SANDBOX_STANDARD_LIBS})
  endif()

  # Add the custom manifest files to the executable.
  ADD_WINDOWS_MANIFEST("${CEF_TARGET}")

  # Copy CEF binary and resource files to the target output directory.
  COPY_FILES("${CEF_TARGET}" "${CEF_BINARY_FILES}" "${CEF_BINARY_DIR}" "${CEF_TARGET_OUT_DIR}")
  COPY_FILES("${CEF_TARGET}" "${CEF_RESOURCE_FILES}" "${CEF_RESOURCE_DIR}" "${CEF_TARGET_OUT_DIR}")
endif()
