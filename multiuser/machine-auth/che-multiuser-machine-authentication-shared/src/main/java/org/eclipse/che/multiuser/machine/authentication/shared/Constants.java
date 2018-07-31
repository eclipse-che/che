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
package org.eclipse.che.multiuser.machine.authentication.shared;

/** @author Anton Korneta */
public class Constants {

  public static final String USER_ID_CLAIM = "uid";
  public static final String USER_NAME_CLAIM = "uname";
  public static final String WORKSPACE_ID_CLAIM = "wsid";

  public static final String MACHINE_TOKEN_KIND = "machine_token";

  public static final String SIGNATURE_ALGORITHM_ENV = "CHE_MACHINE_AUTH_SIGNATURE__ALGORITHM";
  public static final String SIGNATURE_PUBLIC_KEY_ENV = "CHE_MACHINE_AUTH_SIGNATURE__PUBLIC__KEY";

  private Constants() {}
}
