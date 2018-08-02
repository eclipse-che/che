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
package org.eclipse.che.ide.ext.java.client.newsourcefile;

/**
 * Type of Java source file.
 *
 * @author Artem Zatsarynnyi
 */
enum JavaSourceFileType {
  CLASS("Class"),
  INTERFACE("Interface"),
  ENUM("Enum"),
  ANNOTATION("Annotation");

  private final String value;

  private JavaSourceFileType(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
