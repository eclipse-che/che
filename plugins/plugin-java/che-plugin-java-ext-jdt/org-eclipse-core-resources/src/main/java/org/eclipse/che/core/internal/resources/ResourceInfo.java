/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.core.internal.resources;

/**
 * @author Evgen Vidolob
 */
public class ResourceInfo {

    private int type;

    public ResourceInfo(int type) {
        this.type = type;
    }

    /**
     * Returns the type setting for this info.  Valid values are
     * FILE, FOLDER, PROJECT,
     */
    public int getType() {
        return type;
    }
}
