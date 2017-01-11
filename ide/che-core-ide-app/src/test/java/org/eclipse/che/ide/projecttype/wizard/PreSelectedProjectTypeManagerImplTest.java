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

import org.eclipse.che.ide.projecttype.wizard.PreSelectedProjectTypeManagerImpl;

import org.junit.Assert;
import org.junit.Test;

public class PreSelectedProjectTypeManagerImplTest {


    @Test
    public void preSelectedProjectManagerWith2items() {
        PreSelectedProjectTypeManagerImpl manager = new PreSelectedProjectTypeManagerImpl();
        manager.setProjectTypeIdToPreselect("maven", 100);
        manager.setProjectTypeIdToPreselect("java", 10);
        Assert.assertEquals("maven and java added, the lowest type should be returned. The project id type to preselect is", "java",
                            manager.getPreSelectedProjectTypeId());
    }

    @Test
    public void preSelectedProjectManagerWith1items() {
        PreSelectedProjectTypeManagerImpl manager = new PreSelectedProjectTypeManagerImpl();
        manager.setProjectTypeIdToPreselect("maven", 100);
        Assert.assertEquals("Only maven added, the project id type to preselect is", "maven", manager.getPreSelectedProjectTypeId());
    }

    @Test
    public void preSelectedProjectManagerEmpty() {
        PreSelectedProjectTypeManagerImpl manager = new PreSelectedProjectTypeManagerImpl();
        Assert.assertEquals("No project type setted, the project id type to preselect should be empty", "",
                            manager.getPreSelectedProjectTypeId());
    }

    @Test
    public void preSelectedProjectManagerWith3items() {
        PreSelectedProjectTypeManagerImpl manager = new PreSelectedProjectTypeManagerImpl();
        manager.setProjectTypeIdToPreselect("gulp", 1);
        manager.setProjectTypeIdToPreselect("maven", 100);
        manager.setProjectTypeIdToPreselect("java", 10);
        Assert.assertEquals("gulp, maven and java added, the project id type to preselect is", "gulp",
                            manager.getPreSelectedProjectTypeId());
    }

}
