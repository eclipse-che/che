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
package org.eclipse.che.ide.api.editor.text;

/**
 * Indicates the attempt to access a non-existing position category in a document.
 *
 * <p>This class is not intended to be serialized.
 *
 * @see Document
 */
public class BadPositionCategoryException extends Exception {

  /**
   * Serial version UID for this class.
   *
   * <p>Note: This class is not intended to be serialized.
   */
  private static final long serialVersionUID = 3761405300745713206L;

  /** Creates a new bad position category exception. */
  public BadPositionCategoryException() {
    super();
  }

  /**
   * Creates a new bad position category exception.
   *
   * @param message the exception's message
   */
  public BadPositionCategoryException(String message) {
    super(message);
  }
}
