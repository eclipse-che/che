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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.constant.Infrastructure;
import org.testng.annotations.ITestOrConfiguration;

/**
 * This class is aimed to filter TestNG tests.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class TestFilter {

  private final boolean isMultiuser;
  private final Infrastructure infrastructure;
  private final String excludedGroups;

  @Inject
  public TestFilter(
      @Named("sys.excludedGroups") String excludedGroups,
      @Named("che.multiuser") boolean isMultiuser,
      @Named("che.infrastructure") Infrastructure infrastructure) {
    this.isMultiuser = isMultiuser;
    this.infrastructure = infrastructure;
    this.excludedGroups = excludedGroups;
  }

  /**
   * This method disable the test which belongs to test group which is being excluded or doesn't
   * comply current infrastructure, or doesn't support Singleuser/Multiuser type of Eclipse Che.
   *
   * @param annotation annotation of test method which reflects {@link org.testng.annotations.Test}
   *     annotation attributes.
   */
  public void excludeTestOfImproperGroup(ITestOrConfiguration annotation) {
    if (annotation.getGroups().length == 0) {
      return;
    }

    List<String> groups = new ArrayList<>(Arrays.asList(annotation.getGroups()));

    // exclude test with group from excludedGroups
    if (excludedGroups != null
        && Arrays.stream(excludedGroups.split(",")).anyMatch(groups::contains)) {
      annotation.setEnabled(false);
      return;
    }

    // exclude test which doesn't comply multiuser flag
    if (isMultiuser
        && groups.contains(TestGroup.SINGLEUSER)
        && !groups.contains(TestGroup.MULTIUSER)) {
      annotation.setEnabled(false);
      return;
    }

    // exclude test which doesn't comply singleuser flag
    if (!isMultiuser
        && groups.contains(TestGroup.MULTIUSER)
        && !groups.contains(TestGroup.SINGLEUSER)) {
      annotation.setEnabled(false);
      return;
    }

    // exclude test which doesn't support current infrastructure
    groups.remove(TestGroup.SINGLEUSER);
    groups.remove(TestGroup.MULTIUSER);
    groups.remove(TestGroup.GITHUB);
    groups.remove(TestGroup.UNDER_REPAIR);
    if (!groups.isEmpty() && !groups.contains(infrastructure.toString().toLowerCase())) {
      annotation.setEnabled(false);
    }
  }
}
