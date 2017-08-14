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
package org.eclipse.che.ide.api.vcs;

/**
 * Indicates that specified resource has VCS status attribute.
 *
 * @author Igor Vinokur
 */
public interface HasVcsStatus {

    /**
     * Returns VCS status attribute of the resource.
     */
    VcsStatus getVcsStatus();

    /**
     * Set VCS status attribute to the resource.
     *
     * @param vcsStatus
     *         VCS status to set
     */
    void setVcsStatus(VcsStatus vcsStatus);
}
