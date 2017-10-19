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
import org.eclipse.jdt.internal.corext.fix.ConvertLoopFix;
import org.eclipse.jdt.ui.cleanup.CleanUpContext;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;

public class ConvertLoopCleanUp extends AbstractCleanUp {

  public ConvertLoopCleanUp(Map<String, String> options) {
    super(options);
  }

  public ConvertLoopCleanUp() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  public CleanUpRequirements getRequirements() {
    return new CleanUpRequirements(
        isEnabled(CleanUpConstants.CONTROL_STATMENTS_CONVERT_FOR_LOOP_TO_ENHANCED),
        false,
        false,
        null);
  }

  /** {@inheritDoc} */
  @Override
  public ICleanUpFix createFix(CleanUpContext context) throws CoreException {
    CompilationUnit compilationUnit = context.getAST();
    if (compilationUnit == null) return null;

    boolean convertForLoops =
        isEnabled(CleanUpConstants.CONTROL_STATMENTS_CONVERT_FOR_LOOP_TO_ENHANCED);

    return ConvertLoopFix.createCleanUp(
        compilationUnit,
        convertForLoops,
        convertForLoops,
        isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL)
            && isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL_LOCAL_VARIABLES));
  }

  /** {@inheritDoc} */
  @Override
  public String[] getStepDescriptions() {
    List<String> result = new ArrayList<String>();

    if (isEnabled(CleanUpConstants.CONTROL_STATMENTS_CONVERT_FOR_LOOP_TO_ENHANCED))
      result.add(MultiFixMessages.Java50CleanUp_ConvertToEnhancedForLoop_description);

    return result.toArray(new String[result.size()]);
  }

  /** {@inheritDoc} */
  @Override
  public String getPreview() {
    StringBuffer buf = new StringBuffer();

    if (isEnabled(CleanUpConstants.CONTROL_STATMENTS_CONVERT_FOR_LOOP_TO_ENHANCED)) {
      buf.append("for (int element : ids) {\n"); // $NON-NLS-1$
      buf.append("    double value= element / 2; \n"); // $NON-NLS-1$
      buf.append("    System.out.println(value);\n"); // $NON-NLS-1$
      buf.append("}\n"); // $NON-NLS-1$
    } else {
      buf.append("for (int i = 0; i < ids.length; i++) {\n"); // $NON-NLS-1$
      buf.append("    double value= ids[i] / 2; \n"); // $NON-NLS-1$
      buf.append("    System.out.println(value);\n"); // $NON-NLS-1$
      buf.append("}\n"); // $NON-NLS-1$
    }

    return buf.toString();
  }
}
