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
package org.eclipse.che.multiuser.api.permission.shared.model;

import java.util.List;

/**
 * Describes permissions domain
 *
 * @author Sergii Leschenko
 * @author gazarenkov
 */
public interface PermissionsDomain {
  /** @return id of permissions domain */
  String getId();

  /** @return true if domain requires non nullable value for instance field or false otherwise */
  Boolean isInstanceRequired();

  /** @return list actions which are allowed for domain */
  List<String> getAllowedActions();
}
