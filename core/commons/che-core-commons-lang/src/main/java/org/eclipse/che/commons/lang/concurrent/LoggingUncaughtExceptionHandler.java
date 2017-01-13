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
package org.eclipse.che.commons.lang.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes uncaught exceptions in threads being run by {@link java.util.concurrent.ExecutorService} into application log.
 *
 * @author Max Shaposhnik
 */
public class LoggingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingUncaughtExceptionHandler.class);

    private static final LoggingUncaughtExceptionHandler INSTANCE = new LoggingUncaughtExceptionHandler();

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOG.error(String.format("Runtime exception caught in thread %s. Message: %s", t.getName(), e.getLocalizedMessage()), e);
    }

    public static LoggingUncaughtExceptionHandler getInstance() {
        return INSTANCE;
    }

    private LoggingUncaughtExceptionHandler() {
    }

}
