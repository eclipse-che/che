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
package org.eclipse.che.multiuser.resource.model;

import java.util.List;

/**
 * Represents limit of resources which are available for free usage by some account.
 *
 * @author Sergii Leschenko
 */
public interface FreeResourcesLimit {
  /** Returns id of account that can use free resources. */
  String getAccountId();

  /** Returns resources which are available for free usage. */
  List<? extends Resource> getResources();
}
