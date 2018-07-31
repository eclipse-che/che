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
package org.eclipse.che.api.core.model.user;

import java.util.List;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Defines the user model.
 *
 * @author Yevhenii Voevodin
 */
public interface User {

  /**
   * Returns the identifier of the user (e.g. "user0x124567890"). The identifier value is unique and
   * mandatory.
   */
  String getId();

  /**
   * Returns the user's email (e.g. user@codenvy.com). The email is unique, mandatory and updatable.
   */
  String getEmail();

  /** Returns the user's name (e.g. name_example). The name is unique, mandatory and updatable. */
  String getName();

  /**
   * Returns the list of the user's aliases, the aliases are the values which identify user in the
   * system with third party ids (e.g. if user is registered within google oauth the aliases list
   * may contain 'google:user_identifier' alias).
   *
   * <p>Note that user's {@link #getEmail() email} and {@link #getName() name} are not a part of the
   * result, and returned list never contains those values. Also note that returned aliases are
   * unique, so there are no two users who have the alias in common.
   */
  List<String> getAliases();

  /**
   * Returns the user's password. The returned value may be the password placeholder such as 'none'
   * or even null, depends on the context.
   */
  @Nullable
  String getPassword();
}
