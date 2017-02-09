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
package org.eclipse.che.ide.ext.java.shared.dto.model;

/**
 * Common protocol for Java elements that must be opened before they can be
 * navigated or modified.
 *
 * @author Evgen Vidolob
 */
public interface Openable {

    /**
     * Workspace path of this element
     * @return the path
     */
    String getPath();

    void setPath(String path);

    /**
     * Project workspace path
     * @return the path
     */
    String getProjectPath();

    void setProjectPath(String path);
}
