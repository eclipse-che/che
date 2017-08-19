/**
 * ***************************************************************************** Copyright (c) 2007,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.template.java;

import java.util.Iterator;
import java.util.List;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

/**
 * Resolver for the <code>link</code> variable. Resolves to a first parameter puts the value into
 * linked mode. Shows proposals for each parameter.
 *
 * @since 3.4
 */
public class LinkResolver extends TemplateVariableResolver {

  private String[] fProposals;

  public LinkResolver(String type, String description) {
    super(type, description);
  }

  /** Default ctor for instantiation by the extension point. */
  public LinkResolver() {}

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.templates.TemplateVariableResolver#resolve(org.eclipse.jface.text.templates.TemplateVariable, org.eclipse.jface.text.templates.TemplateContext)
   */
  @Override
  public void resolve(TemplateVariable variable, TemplateContext context) {

    variable.setUnambiguous(false);

    if (variable instanceof JavaVariable) {
      JavaContext jc = (JavaContext) context;
      JavaVariable jv = (JavaVariable) variable;

      List<String> params = variable.getVariableType().getParams();
      if (params.size() > 0) {
        fProposals = new String[params.size()];
        int i = 0;
        for (Iterator<String> iterator = params.iterator(); iterator.hasNext(); ) {
          String param = iterator.next();
          fProposals[i] = param;
          i++;
        }
        jv.setChoices(fProposals);
        jv.setCurrentChoice(fProposals[0]);

        jc.markAsUsed(jv.getDefaultValue());
      } else {
        fProposals = new String[] {variable.getDefaultValue()};
        super.resolve(variable, context);
        return;
      }
    } else super.resolve(variable, context);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.templates.TemplateVariableResolver#resolveAll(org.eclipse.jface.text.templates.TemplateContext)
   */
  @Override
  protected String[] resolveAll(TemplateContext context) {
    return fProposals;
  }
}
