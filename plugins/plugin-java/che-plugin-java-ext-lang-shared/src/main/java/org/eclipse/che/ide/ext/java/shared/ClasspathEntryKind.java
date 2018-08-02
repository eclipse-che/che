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
package org.eclipse.che.ide.ext.java.shared;

/**
 * Entry kind constants describe a type of classpath entry.
 *
 * @author Valeriy Svydenko
 */
public class ClasspathEntryKind {
  public static final int LIBRARY = 1;
  public static final int PROJECT = 2;
  public static final int SOURCE = 3;
  public static final int VARIABLE = 4;
  public static final int CONTAINER = 5;
}
