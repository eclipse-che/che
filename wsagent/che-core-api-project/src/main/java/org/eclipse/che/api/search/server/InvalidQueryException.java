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
package org.eclipse.che.api.search.server;

/** Razed in case if implementation specific format of search query is invalid */
public class InvalidQueryException extends Exception {

  public InvalidQueryException(String message, Throwable cause) {
    super(message, cause);
  }
}
