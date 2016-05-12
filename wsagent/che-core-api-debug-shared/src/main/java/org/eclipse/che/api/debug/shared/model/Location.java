/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.debug.shared.model;

/**
 * @author Anatoliy Bazko
 */
public interface Location {
    /**
     *  The target, e.g.: file, fqn, memory address etc.
     */
    String getTarget();

    /**
     * The line number in a file or in a class.
     */
    int getLineNumber();
}
