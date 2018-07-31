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
package org.eclipse.che;

import static com.google.common.base.Strings.nullToEmpty;

import com.google.inject.Provider;

/** @author Anton Korneta */
public class MachineTokenProvider implements Provider<String> {

  public static final String CHE_MACHINE_TOKEN = "CHE_MACHINE_TOKEN";

  @Override
  public String get() {
    return nullToEmpty(System.getenv(CHE_MACHINE_TOKEN));
  }
}
