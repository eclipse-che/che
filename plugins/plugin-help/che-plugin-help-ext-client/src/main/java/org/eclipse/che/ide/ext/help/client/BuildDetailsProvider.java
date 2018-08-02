/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.help.client;

/**
 * Provides formatted string which contains information about build environment (version, date,
 * revision, etc.) which depends on implementation.
 *
 * @author Vlad Zhukovskyi
 * @since 6.7.0
 */
public interface BuildDetailsProvider {

  /**
   * Returns the formatted environment information.
   *
   * @return environment info
   */
  String getBuildDetails();
}
