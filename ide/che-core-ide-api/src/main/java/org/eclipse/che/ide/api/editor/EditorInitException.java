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
package org.eclipse.che.ide.api.editor;

/**
 * A checked exception indicating a editor cannot be initialized correctly. The message text
 * provides a further description of the problem.
 *
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:
 */
public class EditorInitException extends Exception {

  /** */
  private static final long serialVersionUID = 1L;

  /**
   * @param message
   * @param cause
   */
  public EditorInitException(String message, Throwable cause) {
    super(message, cause);
  }

  /** @param message */
  public EditorInitException(String message) {
    super(message);
  }
}
