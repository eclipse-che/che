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
