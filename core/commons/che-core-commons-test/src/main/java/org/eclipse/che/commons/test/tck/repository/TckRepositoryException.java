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
package org.eclipse.che.commons.test.tck.repository;

import java.util.Collection;

/**
 * Thrown when any error occurs during {@link TckRepository#createAll(Collection)} or {@link
 * TckRepository#removeAll()} invocation. Usually wraps exceptions occurred during the
 * storing/removing.
 *
 * @author Yevhenii Voevodin
 */
public class TckRepositoryException extends Exception {

  public TckRepositoryException(String message) {
    super(message);
  }

  public TckRepositoryException(String message, Throwable cause) {
    super(message, cause);
  }
}
