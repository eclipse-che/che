/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.fix;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.VariableDeclarationFix;
import org.eclipse.jdt.ui.cleanup.CleanUpContext;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;

public class VariableDeclarationCleanUp extends AbstractCleanUp {

  public VariableDeclarationCleanUp(Map<String, String> options) {
    super(options);
  }

  public VariableDeclarationCleanUp() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  public CleanUpRequirements getRequirements() {
    return new CleanUpRequirements(requireAST(), false, false, null);
  }

  private boolean requireAST() {
    boolean addFinal = isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL);
    if (!addFinal) return false;

    return isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL_PRIVATE_FIELDS)
        || isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL_PARAMETERS)
        || isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL_LOCAL_VARIABLES);
  }

  /** {@inheritDoc} */
  @Override
  public ICleanUpFix createFix(CleanUpContext context) throws CoreException {
    CompilationUnit compilationUnit = context.getAST();
    if (compilationUnit == null) return null;

    boolean addFinal = isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL);
    if (!addFinal) return null;

    return VariableDeclarationFix.createCleanUp(
        compilationUnit,
        isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL_PRIVATE_FIELDS),
        isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL_PARAMETERS),
        isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL_LOCAL_VARIABLES));
  }

  /** {@inheritDoc} */
  @Override
  public String[] getStepDescriptions() {
    List<String> result = new ArrayList<String>();
    if (isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL)
        && isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL_PRIVATE_FIELDS))
      result.add(MultiFixMessages.VariableDeclarationCleanUp_AddFinalField_description);
    if (isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL)
        && isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL_PARAMETERS))
      result.add(MultiFixMessages.VariableDeclarationCleanUp_AddFinalParameters_description);
    if (isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL)
        && isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL_LOCAL_VARIABLES))
      result.add(MultiFixMessages.VariableDeclarationCleanUp_AddFinalLocals_description);

    return result.toArray(new String[result.size()]);
  }

  /** {@inheritDoc} */
  @Override
  public String getPreview() {
    StringBuffer buf = new StringBuffer();

    if (isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL)
        && isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL_PRIVATE_FIELDS)) {
      buf.append("private final int i= 0;\n"); // $NON-NLS-1$
    } else {
      buf.append("private int i= 0;\n"); // $NON-NLS-1$
    }
    if (isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL)
        && isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL_PARAMETERS)) {
      buf.append("public void foo(final int j) {\n"); // $NON-NLS-1$
    } else {
      buf.append("public void foo(int j) {\n"); // $NON-NLS-1$
    }
    if (isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL)
        && isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL_LOCAL_VARIABLES)) {
      buf.append("    final int k;\n"); // $NON-NLS-1$
      buf.append("    int h;\n"); // $NON-NLS-1$
      buf.append("    h= 0;\n"); // $NON-NLS-1$
    } else {
      buf.append("    int k, h;\n"); // $NON-NLS-1$
      buf.append("    h= 0;\n"); // $NON-NLS-1$
    }
    buf.append("}\n"); // $NON-NLS-1$

    return buf.toString();
  }
}
