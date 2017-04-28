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
package org.eclipse.che.ide.logger.impl;

import org.eclipse.che.api.core.logger.commons.Logger;
import org.eclipse.che.api.core.logger.commons.LoggerFactory;
import org.eclipse.che.ide.util.loging.Log;

public class ClientSideLoggerFactory implements LoggerFactory {
    public Logger get(final Class<?> type) {
        return new Logger() {
            @Override
            public void trace(String msg) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void trace(String format, Object... arguments) {
                throw new UnsupportedOperationException();

            }

            @Override
            public void trace(String msg, Throwable t) {
                throw new UnsupportedOperationException();

            }

            @Override
            public void debug(String msg) {
                Log.debug(type, msg);
            }

            @Override
            public void debug(String format, Object... arguments) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void debug(String msg, Throwable t) {
                Log.debug(type, msg, t);
            }

            @Override
            public void info(String msg) {
                Log.info(type, msg);
            }

            @Override
            public void info(String format, Object... arguments) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void info(String msg, Throwable t) {
                Log.info(type, msg, t);
            }

            @Override
            public void warn(String msg) {
                Log.warn(type, msg);
            }

            @Override
            public void warn(String format, Object... arguments) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void warn(String msg, Throwable t) {
                Log.warn(type, msg, t);
            }

            @Override
            public void error(String msg) {
                Log.error(type, msg);
            }

            @Override
            public void error(String format, Object... arguments) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void error(String msg, Throwable t) {
                Log.error(type, msg, t);
            }
        };
    }
}
