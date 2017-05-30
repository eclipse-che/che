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
package org.eclipse.che.ide.projecttype.wizard;

import org.eclipse.che.ide.api.project.type.wizard.PreSelectedProjectTypeManager;

public class PreSelectedProjectTypeManagerImpl implements PreSelectedProjectTypeManager {


    protected String projectTypeId = "";
    private   int    priority      = 0;


    @Override
    public String getPreSelectedProjectTypeId() {
        return projectTypeId;
    }

    @Override
    public void setProjectTypeIdToPreselect(String projectTypeId, int priority) {
        if ("".equals(this.projectTypeId) || priority <= this.priority) {
            this.projectTypeId = projectTypeId;
            this.priority = priority;
        }
    }


}
