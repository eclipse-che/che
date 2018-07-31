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
package org.eclipse.che.multiuser.organization.shared.model;

import java.util.List;

/**
 * Describes relations of user and organization
 *
 * @author gazarenkov
 * @author Sergii Leschenko
 */
public interface Member {
  /** Returns id of user */
  String getUserId();

  /** Returns id of organization */
  String getOrganizationId();

  /** Returns list of actions that user can perform in organization */
  List<String> getActions();
}
