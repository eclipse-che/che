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

    /**
     * Returns path to the resource.
     */
    String getResourcePath();

    /**
     * Returns true if breakpoint resource is external resource, or false otherwise.
     */
    boolean isExternalResource();

    /**
     * Returns external resource id in case if {@link #isExternalResource()} return true.
     */
    int getExternalResourceId();

    /**
     * Returns project path, for resource which we are debugging now.
     */
    String getResourceProjectPath();
}
