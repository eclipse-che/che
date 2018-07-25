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
package org.eclipse.che.mail.template;

import java.util.Map;
import org.eclipse.che.mail.template.exception.TemplateException;
import org.eclipse.che.mail.template.exception.TemplateNotFoundException;

/**
 * Provides ability to process templates.
 *
 * <p>Note that variables definition format is implementation specific.
 *
 * @author Anton Korneta
 * @author Sergii Leshchenko
 */
public interface TemplateProcessor {

  /**
   * Process specified template with given variables.
   *
   * @param templateName the template name which will be used for processing
   * @param variables the variables to used while processing of the given template
   * @return processed template as string
   * @throws TemplateNotFoundException when given {@code template} not found
   * @throws TemplateException when any another problem occurs during the template processing
   * @see ClassLoader#getResource(String)
   */
  String process(String templateName, Map<String, Object> variables) throws TemplateException;

  /**
   * Process the specified template.
   *
   * @param template the template to process
   * @return processed template as string
   * @throws TemplateNotFoundException when given {@code template} not found
   * @throws TemplateException when any another problem occurs during the template processing
   */
  String process(Template template) throws TemplateException;
}
