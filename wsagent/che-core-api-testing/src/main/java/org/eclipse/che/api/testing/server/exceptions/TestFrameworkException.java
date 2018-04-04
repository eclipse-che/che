/**
 * ***************************************************************************** Copyright (c) 2016
 * Rogue Wave Software, Inc. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Rogue Wave Software, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.api.testing.server.exceptions;

import org.eclipse.che.api.core.ServerException;

/**
 * Test framework specific exception.
 *
 * @author Bartlomiej Laczkowski
 */
@SuppressWarnings("serial")
public class TestFrameworkException extends ServerException {

  public TestFrameworkException(String message) {
    super(message);
  }

  public TestFrameworkException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
