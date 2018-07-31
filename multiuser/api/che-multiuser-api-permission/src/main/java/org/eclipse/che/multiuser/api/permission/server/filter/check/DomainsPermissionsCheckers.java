/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.api.permission.server.filter.check;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Represents a set of domain-specific permissions checkers.
 *
 * @author Anton Korneta
 */
@Singleton
public class DomainsPermissionsCheckers {

  private final Map<String, SetPermissionsChecker> domain2setPermissionsChecker;
  private final DefaultSetPermissionsChecker defaultSetPermissionsChecker;
  private final Map<String, RemovePermissionsChecker> domain2removePermissionsChecker;
  private final DefaultRemovePermissionsChecker defaultRemovePermissionsChecker;

  @Inject
  public DomainsPermissionsCheckers(
      Map<String, SetPermissionsChecker> domain2setPermissionsChecker,
      DefaultSetPermissionsChecker defaultPermissionsChecker,
      Map<String, RemovePermissionsChecker> domain2removePermissionsChecker,
      DefaultRemovePermissionsChecker defaultRemovePermissionsChecker) {
    this.domain2setPermissionsChecker = domain2setPermissionsChecker;
    this.defaultSetPermissionsChecker = defaultPermissionsChecker;
    this.domain2removePermissionsChecker = domain2removePermissionsChecker;
    this.defaultRemovePermissionsChecker = defaultRemovePermissionsChecker;
  }

  public SetPermissionsChecker getSetChecker(String domain) {
    if (domain2setPermissionsChecker.containsKey(domain)) {
      return domain2setPermissionsChecker.get(domain);
    }
    return defaultSetPermissionsChecker;
  }

  public RemovePermissionsChecker getRemoveChecker(String domain) {
    if (domain2removePermissionsChecker.containsKey(domain)) {
      return domain2removePermissionsChecker.get(domain);
    }
    return defaultRemovePermissionsChecker;
  }
}
