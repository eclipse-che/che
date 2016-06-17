// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.util.loging;

import com.google.gwt.core.client.GWT;


/**
 * Simple Logging class that logs to the browser's console
 * So long as generating the parameters to pass to the logging methods is free
 * of side effects, all Logging code should compile out of your application if
 * logging is disabled.
 */
public class Log {

    private static final Logger delegate;

    static {
        LogConfig.setLogLevel(LogConfig.LogLevel.INFO);
        delegate = GWT.isClient() ? new BrowserLogger() : new DummyLogger();
    }

    public static void debug(Class<?> clazz, Object... args) {
        delegate.debug(clazz, args);
    }

    public static void error(Class<?> clazz, Object... args) {
        delegate.error(clazz, args);
    }

    public static void info(Class<?> clazz, Object... args) {
        delegate.info(clazz, args);
    }

    public static void warn(Class<?> clazz, Object... args) {
        delegate.warn(clazz, args);
    }

    public static boolean isLoggingEnabled() {
        return delegate.isLoggingEnabled();
    }

}
