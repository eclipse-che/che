/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che;

import static com.google.common.base.Strings.nullToEmpty;

import com.google.inject.Provider;

/** @author Anton Korneta */
public class UserTokenProvider implements Provider<String> {

  public static final String USER_TOKEN = "USER_TOKEN";

  @Override
  public String get() {
    return nullToEmpty(System.getenv(USER_TOKEN));
  }
}
