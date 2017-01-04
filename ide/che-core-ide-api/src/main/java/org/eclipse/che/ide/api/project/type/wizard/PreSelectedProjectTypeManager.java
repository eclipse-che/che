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
package org.eclipse.che.ide.api.project.type.wizard;

/**
 * Provides, to extensions, a way to set the default project type to be pre selected. Project wizards will use
 * {@link PreSelectedProjectTypeManager#getPreSelectedProjectTypeId()} to get the type to be preselected
 */
public interface PreSelectedProjectTypeManager {

    /**
     * To be used by project wizards to get the project type to preselect when no type is selected.
     *
     * @return The project id or an empty string if none.
     */
    String getPreSelectedProjectTypeId();

    /**
     * Set projectType to preselect. lowest priority value will get selected.
     */
    void setProjectTypeIdToPreselect(String projectTypeId, int priority);

}
