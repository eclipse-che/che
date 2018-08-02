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
package org.eclipse.che.mail.template.ST;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import javax.inject.Singleton;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.mail.template.Template;
import org.eclipse.che.mail.template.TemplateProcessor;
import org.eclipse.che.mail.template.exception.TemplateException;
import org.eclipse.che.mail.template.exception.TemplateNotFoundException;
import org.stringtemplate.v4.ST;

/**
 * {@link TemplateProcessor} implementation based on {@link ST}.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class STTemplateProcessorImpl implements TemplateProcessor {

  @Override
  public String process(String templateName, Map<String, Object> variables)
      throws TemplateException {
    ST st = new ST(resolve(templateName));
    variables.forEach(st::add);
    return st.render();
  }

  @Override
  public String process(Template template) throws TemplateException {
    return process(template.getName(), template.getAttributes());
  }

  private String resolve(String template) throws TemplateNotFoundException {
    try (Reader reader = new InputStreamReader(IoUtil.getResource(template))) {
      return CharStreams.toString(reader);
    } catch (IOException e) {
      throw new TemplateNotFoundException(e.getMessage(), e);
    }
  }
}
