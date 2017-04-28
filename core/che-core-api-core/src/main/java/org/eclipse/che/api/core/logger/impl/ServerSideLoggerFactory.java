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
package org.eclipse.che.api.core.logger.impl;

import org.eclipse.che.api.core.logger.commons.Logger;
import org.eclipse.che.api.core.logger.commons.LoggerFactory;

public class ServerSideLoggerFactory implements LoggerFactory {
    public Logger get(Class<?> type) {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(type);
        return new Logger() {
            @Override
            public void trace(String msg) {
                logger.trace(msg);
            }

            @Override
            public void trace(String format, Object... arguments) {
                logger.trace(format, arguments);
            }

            @Override
            public void trace(String msg, Throwable t) {
                logger.trace(msg, t);
            }

            @Override
            public void debug(String msg) {
                logger.debug(msg);
            }

            @Override
            public void debug(String format, Object... arguments) {
                logger.debug(format, arguments);
            }

            @Override
            public void debug(String msg, Throwable t) {
                logger.debug(msg, t);
            }

            @Override
            public void info(String msg) {
                logger.info(msg);
            }

            @Override
            public void info(String format, Object... arguments) {
                logger.info(format, arguments);
            }

            @Override
            public void info(String msg, Throwable t) {
                logger.info(msg, t);
            }

            @Override
            public void warn(String msg) {
                logger.warn(msg);
            }

            @Override
            public void warn(String format, Object... arguments) {
                logger.warn(format, arguments);
            }

            @Override
            public void warn(String msg, Throwable t) {
                logger.warn(msg, t);
            }

            @Override
            public void error(String msg) {
                logger.error(msg);
            }

            @Override
            public void error(String format, Object... arguments) {
                logger.error(format, arguments);
            }

            @Override
            public void error(String msg, Throwable t) {
                logger.error(msg, t);
            }
        };
    }
}
