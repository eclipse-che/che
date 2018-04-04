/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
