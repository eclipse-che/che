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
package org.eclipse.che.mail.template.exception;

/**
 * Should be thrown when unable to resolve template by given path.
 *
 * @author Anton Korneta
 */
public class TemplateNotFoundException extends TemplateException {

  public TemplateNotFoundException(String message) {
    super(message);
  }

  public TemplateNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
