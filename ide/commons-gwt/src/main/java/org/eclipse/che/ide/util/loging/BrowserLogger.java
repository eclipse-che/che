/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.util.loging;

import org.eclipse.che.ide.util.ExceptionUtils;

import com.google.gwt.core.client.JavaScriptObject;


/**
 * @author <a href="mailto:evidolob@codenvy.com">Evgen Vidolob</a>
 * @version $Id:
 */
class BrowserLogger implements Logger {



    /** @see org.eclipse.che.ide.util.loging.Logger#debug(java.lang.Class, java.lang.Object[]) */
    @Override
    public void debug(Class<?> clazz, Object... args) {
        // DEBUG is the lowest log level, but we use <= for consistency, and in
        // case we ever decide to introduce a SPAM level.
        if (LogConfig.getLogLevel().ordinal() <= LogConfig.LogLevel.DEBUG.ordinal()) {
            log(clazz, LogConfig.LogLevel.DEBUG, args);
        }

    }

    /** @see org.eclipse.che.ide.util.loging.Logger#error(java.lang.Class, java.lang.Object[]) */
    @Override
    public void error(Class<?> clazz, Object... args) {
        log(clazz, LogConfig.LogLevel.ERROR, args);
    }

    /** @see org.eclipse.che.ide.util.loging.Logger#info(java.lang.Class, java.lang.Object[]) */
    @Override
    public void info(Class<?> clazz, Object... args) {
        if (LogConfig.getLogLevel().ordinal() <= LogConfig.LogLevel.INFO.ordinal()) {
            log(clazz, LogConfig.LogLevel.INFO, args);
        }
    }

    /** @see org.eclipse.che.ide.util.loging.Logger#isLoggingEnabled() */
    @Override
    public boolean isLoggingEnabled() {
        return true;
    }

    /** @see org.eclipse.che.ide.util.loging.Logger#warn(java.lang.Class, java.lang.Object[]) */
    @Override
    public void warn(Class<?> clazz, Object... args) {
        if (LogConfig.getLogLevel().ordinal() <= LogConfig.LogLevel.WARNING.ordinal()) {
            log(clazz, LogConfig.LogLevel.WARNING, args);
        }
    }

    private static native void invokeBrowserLogger(String logFuncName, Object o) /*-{
        try {
            if ($wnd.console && $wnd.console[logFuncName]) {
                $wnd.console[logFuncName](o);
            }
        } catch (e) {
            console.log("EXCEPTION : " + e.message);
        }
    }-*/;

    private static void log(Class<?> clazz, LogConfig.LogLevel logLevel, Object... args) {
        String prefix =
                new StringBuilder(logLevel.toString()).append(" (").append(clazz.getName()).append("): ").toString();

        for (Object o : args) {
            if (o instanceof String) {
                logToBrowser(logLevel, prefix + (String)o);
            } else if (o instanceof Throwable) {
                Throwable t = (Throwable)o;
                logToBrowser(logLevel, prefix + ExceptionUtils.getStackTraceAsString(t));
            } else if (o instanceof JavaScriptObject) {
                logToBrowser(logLevel, prefix + "(JSO below)");
                logToBrowser(logLevel, o);
            } else {
                logToBrowser(logLevel, prefix + (o != null ? o.toString() : "(null)"));
            }
        }
    }

    private static void logToBrowser(LogConfig.LogLevel logLevel, Object o) {
        switch (logLevel) {
            case DEBUG:
                invokeBrowserLogger("debug", o);
                break;
            case INFO:
                invokeBrowserLogger("info", o);
                break;
            case WARNING:
                invokeBrowserLogger("warn", o);
                break;
            case ERROR:
                invokeBrowserLogger("error", o);
                break;
            default:
                invokeBrowserLogger("log", o);
        }
    }

}
