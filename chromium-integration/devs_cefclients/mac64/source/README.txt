Chromium Embedded Framework (CEF) Standard Binary Distribution for Mac OS-X
-------------------------------------------------------------------------------

Date:             June 11, 2014

CEF Version:      3.1750.1738
CEF URL:          http://chromiumembedded.googlecode.com/svn/branches/1750/cef3@1738

Chromium Verison: 33.0.1750.170
Chromium URL:     http://chromiumembedded.googlecode.com/svn/branches/1750/cef3@1738

This distribution contains all components necessary to build and distribute an
application using CEF on the Mac OS-X platform. Please see the LICENSING
section of this document for licensing terms and conditions.


CONTENTS
--------

cefclient   Contains the cefclient sample application configured to build
            using the files in this distribution. This application demonstrates
            a wide range of CEF functionalities.

cefsimple   Contains the cefsimple sample application configured to build
            using the files in this distribution. This application demonstrates
            the minimal functionality required to create a browser window.

Debug       Contains the "Chromium Embedded Framework.framework" and other
            components required to run the debug version of CEF-based
            applications.

include     Contains all required CEF header files.

libcef_dll  Contains the source code for the libcef_dll_wrapper static library
            that all applications using the CEF C++ API must link against.

Release     Contains the "Chromium Embedded Framework.framework" and other
            components required to run the release version of CEF-based
            applications.

tools       Scripts that perform post-processing on Mac release targets.


USAGE
-----

Xcode 3 and 4: Open the cefclient.xcodeproj project and build.

Please visit the CEF Website for additional usage information.

http://code.google.com/p/chromiumembedded


REDISTRIBUTION
--------------

This binary distribution contains the below components. Components listed under
the "required" section must be redistributed with all applications using CEF.
Components listed under the "optional" section may be excluded if the related
features will not be used.

Applications using CEF on OS X must follow a specific app bundle structure.
Replace "cefclient" in the below example with your application name.

cefclient.app/
  Contents/
    Frameworks/
      Chromium Embedded Framework.framework/
        Chromium Embedded Framework <= main application library
        Libraries/
          ffmpegsumo.so <= HTML5 audio/video support library
        Resources/
          cef.pak, devtools_resources.pak <= non-localized resources and strings
          crash_inspector, crash_report_sender <= breakpad support
          en.lproj/, ... <= locale-specific resources and strings
          Info.plist
      libplugin_carbon_interpose.dylib <= plugin support library
      cefclient Helper.app/
        Contents/
          Info.plist
          MacOS/
            cefclient Helper <= helper executable
          Pkginfo
      cefclient Helper EH.app/
        Contents/
          Info.plist
          MacOS/
            cefclient Helper EH <= helper executable
          Pkginfo
      cefclient Helper NP.app/
        Contents/
          Info.plist
          MacOS/
            cefclient Helper NP <= helper executable
          Pkginfo
      Info.plist
    MacOS/
      cefclient <= cefclient application executable
    Pkginfo
    Resources/
      binding.html, ... <= cefclient application resources

The "Chromium Embedded Framework.framework" is an unversioned framework that
contains CEF binaries and resources. Executables (cefclient, cefclient Helper,
etc) are linked to the "Chromium Embedded Framework" library using
install_name_tool and a path relative to @executable_path.

The "cefclient Helper" apps are used for executing separate processes
(renderer, plugin, etc) with different characteristics. They need to have
separate app bundles and Info.plist files so that, among other things, they
don't show dock icons. The "EH" helper, which is used when launching plugin
processes, has the MH_NO_HEAP_EXECUTION bit cleared to allow an executable
heap. The "NP" helper, which is used when launching NaCl plugin processes
only, has the MH_PIE bit cleared to disable ASLR. This is set up as part of
the build process using scripts from the tools/ directory. Examine the Xcode
project included with the binary distribution or the originating cefclient.gyp
file for a better idea of the script dependencies.

Required components:

* CEF framework library
    Chromium Embedded Framework.framework/Chromium Embedded Framework

* Plugin support library
    libplugin_carbon_interpose.dylib

Optional components:

* Localized resources
    Chromium Embedded Framework.framework/Resources/*.lproj/
  Note: Contains localized strings for WebKit UI controls. A .pak file is loaded
  from this folder based on the CefSettings.locale value. Only configured
  locales need to be distributed. If no locale is configured the default locale
  of "en" will be used. Locale file loading can be disabled completely using
  CefSettings.pack_loading_disabled.

* Other resources
    Chromium Embedded Framework.framework/Resources/cef.pak
    Chromium Embedded Framework.framework/Resources/devtools_resources.pak
  Note: Contains WebKit image and inspector resources. Pack file loading can be
  disabled completely using CefSettings.pack_loading_disabled. The resources
  directory path can be customized using CefSettings.resources_dir_path.

* FFmpeg audio and video support
    Chromium Embedded Framework.framework/Libraries/ffmpegsumo.so
  Note: Without this component HTML5 audio and video will not function.

* Breakpad support
    Chromium Embedded Framework.framework/Resources/crash_inspector
    Chromium Embedded Framework.framework/Resources/crash_report_sender
    Chromium Embedded Framework.framework/Resources/Info.plist
  Note: Without these components breakpad support will not function.


LICENSING
---------

The CEF project is BSD licensed. Please read the LICENSE.txt file included with
this binary distribution for licensing terms and conditions. Other software
included in this distribution is provided under other licenses. Please visit
"about:credits" in a CEF-based application for complete Chromium and third-party
licensing information.
