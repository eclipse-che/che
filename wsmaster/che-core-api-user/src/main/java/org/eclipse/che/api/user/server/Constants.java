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
package org.eclipse.che.api.user.server;

/**
 * Constants for User/Profile/Preferences API.
 *
 * @author Yevhenii Voevodin
 * @author Max Shaposhnik
 */
public final class Constants {

  /** Profile link relationships. */
  public static final String LINK_REL_CURRENT_PROFILE = "current_profile";

  public static final String LINK_REL_CURRENT_PROFILE_ATTRIBUTES = "current_profile.attributes";
  public static final String LINK_REL_PROFILE = "profile";
  public static final String LINK_REL_PROFILE_ATTRIBUTES = "profile.attributes";

  /** User links relationships. */
  public static final String LINK_REL_USER = "user";

  public static final String LINK_REL_CURRENT_USER = "current_user";
  public static final String LINK_REL_CURRENT_USER_PASSWORD = "current_user.password";
  public static final String LINK_REL_CURRENT_USER_SETTINGS = "current_user.settings";

  /** Preferences links relationships. */
  public static final String LINK_REL_PREFERENCES = "preferences";

  public static final String LINK_REL_SELF = "self";

  public static final int ID_LENGTH = 16;
  public static final int PASSWORD_LENGTH = 10;

  private Constants() {}
}
