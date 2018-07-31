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
package org.eclipse.che.security.oauth.shared;

/**
 * Represents an User with unique identifier. Have such interface to be able use GWT AutoBean
 * feature. Any interface that represents an User should extend this interface.
 */
public interface User {
  String getId();

  void setId(String id);

  String getName();

  void setName(String name);

  String getEmail();

  void setEmail(String email);
}
