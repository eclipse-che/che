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
package org.eclipse.che.plugin.python.shared;

/**
 * The utility class for constants.
 *
 * @author Valeriy Svydenko
 */
public final class ProjectAttributes {
  public static final String LANGUAGE = "language";
  public static final String PYTHON_ID = "python";
  public static final String PYTHON_NAME = "Python";
  public static final String PYTHON_CATEGORY = "Python";
  public static final String PYTHON_EXT = "py";

  private ProjectAttributes() {
    throw new UnsupportedOperationException("Unused constructor.");
  }
}
