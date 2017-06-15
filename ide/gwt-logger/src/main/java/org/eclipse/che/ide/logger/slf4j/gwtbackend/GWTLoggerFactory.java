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
package org.eclipse.che.ide.logger.slf4j.gwtbackend;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.HashMap;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

/**
 *
 */
public class GWTLoggerFactory implements ILoggerFactory {
    private final HashMap<String, Logger> loggers = new HashMap<>();

    @Override
    public Logger getLogger(String name) {
        if (name == null || ROOT_LOGGER_NAME.equalsIgnoreCase(name)) {
            name = "";
        }
        Logger logger = loggers.get(name);
        if (logger == null) {
            logger = new GwtLoggerSlf4jBackend(name);
            loggers.put(name, logger);
        }
        return logger;
    }
}
