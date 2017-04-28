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
 * Creates logger instances
 */
public interface LoggerFactory {
    /**
     * Creates logger instance that corresponds to a specific class type
     *
     * @param type class type
     * @return logger instance
     */
    Logger get(Class<?> type);
}
