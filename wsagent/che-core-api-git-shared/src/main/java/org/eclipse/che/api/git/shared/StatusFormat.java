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
package org.eclipse.che.api.git.shared;

/**
 * Format for the status output.
 */
public enum StatusFormat {

    /**
     * Short format, to show the user (has colors) but harder to understand than long format.
     */
    SHORT,
    /**
     * Long format, to show the user.
     */
    LONG,
    /**
     * Porcelain format, made to be fed to scripts and guaranteed to not change.
     */
    PORCELAIN
}
