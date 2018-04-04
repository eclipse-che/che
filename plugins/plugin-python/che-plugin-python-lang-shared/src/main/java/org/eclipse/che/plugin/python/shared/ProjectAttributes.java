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
package org.eclipse.che.plugin.python.shared;

/**
 * The utility class for constants.
 *
 * @author Valeriy Svydenko
 */
public final class ProjectAttributes {
  public static String LANGUAGE = "language";
  public static String PYTHON_ID = "python";
  public static String PYTHON_NAME = "Python";
  public static String PYTHON_CATEGORY = "Python";
  public static String PYTHON_EXT = "py";

  private ProjectAttributes() {
    throw new UnsupportedOperationException("Unused constructor.");
  }
}
