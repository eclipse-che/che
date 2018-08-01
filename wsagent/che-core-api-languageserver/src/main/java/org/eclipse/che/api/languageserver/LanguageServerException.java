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
package org.eclipse.che.api.languageserver;

import org.eclipse.che.api.core.ServerException;

/** @author Anatoliy Bazko */
@SuppressWarnings("serial")
public class LanguageServerException extends ServerException {
  public LanguageServerException(String message) {
    super(message);
  }

  public LanguageServerException(String message, Throwable cause) {
    super(message, cause);
  }

  public LanguageServerException(Throwable cause) {
    super(cause);
  }
}
