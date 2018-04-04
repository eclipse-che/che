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
 * Variable resolver for variable <code>importStatic</code>. Resolves to static import statements.
 *
 * @since 3.4
 */
public class StaticImportResolver extends TemplateVariableResolver {

  public StaticImportResolver(String type, String description) {
    super(type, description);
  }

  /** Default ctor for instantiation by the extension point. */
  public StaticImportResolver() {}

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.templates.TemplateVariableResolver#resolve(org.eclipse.jface.text.templates.TemplateVariable, org.eclipse.jface.text.templates.TemplateContext)
   */
  @Override
  public void resolve(TemplateVariable variable, TemplateContext context) {
    variable.setUnambiguous(true);
    variable.setValue(""); // $NON-NLS-1$

    if (context instanceof JavaContext) {
      JavaContext jc = (JavaContext) context;
      List<String> params = variable.getVariableType().getParams();
      if (params.size() > 0) {
        for (Iterator<String> iterator = params.iterator(); iterator.hasNext(); ) {
          String qualifiedMemberName = iterator.next();
          jc.addStaticImport(qualifiedMemberName);
        }
      }
    } else {
      super.resolve(variable, context);
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.templates.TemplateVariableResolver#resolveAll(org.eclipse.jface.text.templates.TemplateContext)
   */
  @Override
  protected String[] resolveAll(TemplateContext context) {
    return new String[0];
  }
}
