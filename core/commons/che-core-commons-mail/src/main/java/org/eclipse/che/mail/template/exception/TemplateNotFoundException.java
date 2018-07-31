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
