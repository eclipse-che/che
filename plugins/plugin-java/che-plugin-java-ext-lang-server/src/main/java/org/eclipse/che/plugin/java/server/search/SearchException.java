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
package org.eclipse.che.plugin.java.server.search;

import org.eclipse.che.plugin.java.server.JdtException;

/**
 * Represents problem in Java search.
 *
 * @author Evgen Vidolob
 */
public class SearchException extends JdtException {
  public SearchException(String message) {
    super(message);
  }

  public SearchException(String message, Throwable cause) {
    super(message, cause);
  }
}
