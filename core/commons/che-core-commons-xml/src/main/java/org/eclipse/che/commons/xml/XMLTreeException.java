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
package org.eclipse.che.commons.xml;

/** @author Eugene Voevodin */
public class XMLTreeException extends RuntimeException {

  public static XMLTreeException wrap(Exception ex) {
    return new XMLTreeException(ex.getMessage(), ex);
  }

  public XMLTreeException(String message) {
    super(message);
  }

  public XMLTreeException(String message, Throwable cause) {
    super(message, cause);
  }
}
