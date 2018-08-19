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
package org.eclipse.che.selenium.core.inject;

import static org.eclipse.che.selenium.core.TestGroup.DOCKER;
import static org.eclipse.che.selenium.core.TestGroup.GITHUB;
import static org.eclipse.che.selenium.core.TestGroup.K8S;
import static org.eclipse.che.selenium.core.TestGroup.MULTIUSER;
import static org.eclipse.che.selenium.core.TestGroup.OPENSHIFT;
import static org.eclipse.che.selenium.core.TestGroup.OSIO;
import static org.eclipse.che.selenium.core.TestGroup.SINGLEUSER;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.eclipse.che.selenium.core.constant.Infrastructure;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
public class TestFilterTest {

  public static final String SOME_GROUP = "some_group";
  public static final String[] EMPTY_TEST_GROUPS = {};
  public static final String EMPTY_EXCLUDED_GROUPS = "";
  public static final boolean CHE_SINGLEUSER = false;
  public static final boolean CHE_MULTIUSER = true;
  @Mock private ITestAnnotation mockTestAnnotation;

  @BeforeMethod
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test(dataProvider = "disableTestGroupManagementData")
  public void shouldDisableTest(
      String[] testGroups,
      String excludedGroups,
      boolean isMultiuser,
      Infrastructure infrastructure) {
    // given
    TestFilter testFilter = new TestFilter(excludedGroups, isMultiuser, infrastructure);
    doReturn(testGroups).when(mockTestAnnotation).getGroups();

    // when
    testFilter.excludeTestOfImproperGroup(mockTestAnnotation);

    // then
    verify(mockTestAnnotation).setEnabled(false);
  }

  @DataProvider
  public Object[][] disableTestGroupManagementData() {
    return new Object[][] {
      {new String[] {GITHUB}, GITHUB, CHE_MULTIUSER, Infrastructure.OPENSHIFT},
      {new String[] {GITHUB}, SOME_GROUP + "," + GITHUB, CHE_SINGLEUSER, Infrastructure.OPENSHIFT},
      {new String[] {OPENSHIFT}, GITHUB, CHE_MULTIUSER, Infrastructure.DOCKER},
      {new String[] {MULTIUSER}, SOME_GROUP, CHE_SINGLEUSER, Infrastructure.DOCKER},
      {
        new String[] {OPENSHIFT, OSIO}, EMPTY_EXCLUDED_GROUPS, CHE_SINGLEUSER, Infrastructure.DOCKER
      },
      {
        new String[] {DOCKER, SINGLEUSER},
        EMPTY_EXCLUDED_GROUPS,
        CHE_MULTIUSER,
        Infrastructure.DOCKER
      },
      {new String[] {OPENSHIFT}, EMPTY_EXCLUDED_GROUPS, CHE_SINGLEUSER, Infrastructure.DOCKER},
      {
        new String[] {K8S, SINGLEUSER, MULTIUSER},
        EMPTY_EXCLUDED_GROUPS,
        CHE_MULTIUSER,
        Infrastructure.DOCKER
      }
    };
  }

  @Test(dataProvider = "enableTestGroupManagementData")
  public void shouldEnableTest(
      String[] testGroups,
      String excludedGroups,
      boolean isMultiuser,
      Infrastructure infrastructure) {
    // given
    TestFilter testFilter = new TestFilter(excludedGroups, isMultiuser, infrastructure);
    doReturn(testGroups).when(mockTestAnnotation).getGroups();

    // when
    testFilter.excludeTestOfImproperGroup(mockTestAnnotation);

    // then
    verify(mockTestAnnotation, never()).setEnabled(false);
  }

  @DataProvider
  public Object[][] enableTestGroupManagementData() {
    return new Object[][] {
      {EMPTY_TEST_GROUPS, EMPTY_EXCLUDED_GROUPS, CHE_SINGLEUSER, Infrastructure.OPENSHIFT},
      {EMPTY_TEST_GROUPS, EMPTY_EXCLUDED_GROUPS, CHE_MULTIUSER, Infrastructure.DOCKER},
      {EMPTY_TEST_GROUPS, GITHUB, CHE_SINGLEUSER, Infrastructure.DOCKER},
      {new String[] {GITHUB}, SOME_GROUP, CHE_MULTIUSER, Infrastructure.OPENSHIFT},
      {
        new String[] {GITHUB, OPENSHIFT},
        EMPTY_EXCLUDED_GROUPS,
        CHE_MULTIUSER,
        Infrastructure.OPENSHIFT
      },
      {
        new String[] {GITHUB, OPENSHIFT, DOCKER},
        EMPTY_EXCLUDED_GROUPS,
        CHE_MULTIUSER,
        Infrastructure.DOCKER
      },
      {new String[] {MULTIUSER}, EMPTY_EXCLUDED_GROUPS, CHE_MULTIUSER, Infrastructure.OPENSHIFT},
      {
        new String[] {GITHUB, SINGLEUSER, MULTIUSER},
        EMPTY_EXCLUDED_GROUPS,
        CHE_SINGLEUSER,
        Infrastructure.OPENSHIFT
      },
      {
        new String[] {SINGLEUSER, MULTIUSER, OPENSHIFT},
        EMPTY_EXCLUDED_GROUPS,
        CHE_MULTIUSER,
        Infrastructure.OPENSHIFT
      }
    };
  }
}
