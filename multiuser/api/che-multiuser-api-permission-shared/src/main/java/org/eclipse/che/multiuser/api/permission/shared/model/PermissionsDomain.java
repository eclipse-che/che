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
