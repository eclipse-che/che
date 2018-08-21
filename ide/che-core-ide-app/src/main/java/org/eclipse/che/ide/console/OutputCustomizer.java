/*
 * Copyright (c) 2017 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.console;

/**
 * An interface to allow output text customizations
 *
 * @author Victor Rubezhny
 */
public interface OutputCustomizer {
  /** Checks if the specified text can be/has to be customized */
  boolean canCustomize(String text);

  /** Returns the result of customization for the specified text */
  String customize(String text);
}
