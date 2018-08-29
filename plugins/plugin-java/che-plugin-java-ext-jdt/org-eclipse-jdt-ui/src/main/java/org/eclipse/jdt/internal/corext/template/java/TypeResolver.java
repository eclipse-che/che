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

import java.util.List;
import org.eclipse.jdt.internal.ui.text.template.contentassist.MultiVariable;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

/**
 * Resolves to either a simple type name and an import or a fully qualified type name if a
 * conflicting import exists.
 *
 * @since 3.4
 */
public class TypeResolver extends TemplateVariableResolver {

  private final String fDefaultType;

  /** Default ctor for instantiation by the extension point. */
  public TypeResolver() {
    this("java.lang.Object"); // $NON-NLS-1$
  }

  TypeResolver(String defaultType) {
    fDefaultType = defaultType;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.templates.TemplateVariableResolver#resolve(org.eclipse.jface.text.templates.TemplateVariable, org.eclipse.jface.text.templates.TemplateContext)
   */
  @Override
  public void resolve(TemplateVariable variable, TemplateContext context) {
    List<String> params = variable.getVariableType().getParams();
    String param;
    if (params.size() == 0) param = fDefaultType;
    else param = params.get(0);

    JavaContext jc = (JavaContext) context;
    MultiVariable mv = (MultiVariable) variable;

    String reference = jc.addImport(param);
    mv.setValue(reference);
    mv.setUnambiguous(true);
  }
}
