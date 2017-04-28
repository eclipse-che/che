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
package org.eclipse.che.api.core.logger.commons;

/**
 * Basic logging interface
 */
public interface Logger {

    void trace(String msg);

    void trace(String format, Object... arguments);

    void trace(String msg, Throwable t);

    void debug(String msg);

    void debug(String format, Object... arguments);

    void debug(String msg, Throwable t);

    void info(String msg);

    void info(String format, Object... arguments);

    void info(String msg, Throwable t);

    void warn(String msg);

    void warn(String format, Object... arguments);

    void warn(String msg, Throwable t);

    void error(String msg);

    void error(String format, Object... arguments);

    void error(String msg, Throwable t);
}
