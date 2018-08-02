/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.projecttype.wizard;

import org.junit.Assert;
import org.junit.Test;

public class PreSelectedProjectTypeManagerImplTest {

  @Test
  public void preSelectedProjectManagerWith2items() {
    PreSelectedProjectTypeManagerImpl manager = new PreSelectedProjectTypeManagerImpl();
    manager.setProjectTypeIdToPreselect("maven", 100);
    manager.setProjectTypeIdToPreselect("java", 10);
    Assert.assertEquals(
        "maven and java added, the lowest type should be returned. The project id type to preselect is",
        "java",
        manager.getPreSelectedProjectTypeId());
  }

  @Test
  public void preSelectedProjectManagerWith1items() {
    PreSelectedProjectTypeManagerImpl manager = new PreSelectedProjectTypeManagerImpl();
    manager.setProjectTypeIdToPreselect("maven", 100);
    Assert.assertEquals(
        "Only maven added, the project id type to preselect is",
        "maven",
        manager.getPreSelectedProjectTypeId());
  }

  @Test
  public void preSelectedProjectManagerEmpty() {
    PreSelectedProjectTypeManagerImpl manager = new PreSelectedProjectTypeManagerImpl();
    Assert.assertEquals(
        "No project type setted, the project id type to preselect should be empty",
        "",
        manager.getPreSelectedProjectTypeId());
  }

  @Test
  public void preSelectedProjectManagerWith3items() {
    PreSelectedProjectTypeManagerImpl manager = new PreSelectedProjectTypeManagerImpl();
    manager.setProjectTypeIdToPreselect("gulp", 1);
    manager.setProjectTypeIdToPreselect("maven", 100);
    manager.setProjectTypeIdToPreselect("java", 10);
    Assert.assertEquals(
        "gulp, maven and java added, the project id type to preselect is",
        "gulp",
        manager.getPreSelectedProjectTypeId());
  }
}
