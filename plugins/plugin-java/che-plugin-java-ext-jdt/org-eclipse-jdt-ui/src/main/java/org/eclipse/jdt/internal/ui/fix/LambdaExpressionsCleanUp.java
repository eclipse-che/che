/**
 * ***************************************************************************** Copyright (c) 2013
 * IBM Corporation and others. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
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
import org.eclipse.jdt.internal.corext.fix.LambdaExpressionsFix;
import org.eclipse.jdt.ui.cleanup.CleanUpContext;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;

public class LambdaExpressionsCleanUp extends AbstractCleanUp {

  public LambdaExpressionsCleanUp(Map<String, String> options) {
    super(options);
  }

  public LambdaExpressionsCleanUp() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  public CleanUpRequirements getRequirements() {
    return new CleanUpRequirements(requireAST(), false, false, null);
  }

  private boolean requireAST() {
    boolean convertFunctionalInterfaces = isEnabled(CleanUpConstants.CONVERT_FUNCTIONAL_INTERFACES);
    if (!convertFunctionalInterfaces) return false;

    return isEnabled(CleanUpConstants.USE_LAMBDA)
        || isEnabled(CleanUpConstants.USE_ANONYMOUS_CLASS_CREATION);
  }

  /** {@inheritDoc} */
  @Override
  public ICleanUpFix createFix(CleanUpContext context) throws CoreException {
    CompilationUnit compilationUnit = context.getAST();
    if (compilationUnit == null) return null;

    boolean convertFunctionalInterfaces = isEnabled(CleanUpConstants.CONVERT_FUNCTIONAL_INTERFACES);
    if (!convertFunctionalInterfaces) return null;

    return LambdaExpressionsFix.createCleanUp(
        compilationUnit,
        isEnabled(CleanUpConstants.USE_LAMBDA),
        isEnabled(CleanUpConstants.USE_ANONYMOUS_CLASS_CREATION));
  }

  /** {@inheritDoc} */
  @Override
  public String[] getStepDescriptions() {
    List<String> result = new ArrayList<String>();
    if (isEnabled(CleanUpConstants.CONVERT_FUNCTIONAL_INTERFACES)) {
      if (isEnabled(CleanUpConstants.USE_LAMBDA)) {
        result.add(MultiFixMessages.LambdaExpressionsCleanUp_use_lambda_where_possible);
      }
      if (isEnabled(CleanUpConstants.USE_ANONYMOUS_CLASS_CREATION)) {
        result.add(MultiFixMessages.LambdaExpressionsCleanUp_use_anonymous);
      }
    }

    return result.toArray(new String[result.size()]);
  }

  /** {@inheritDoc} */
  @Override
  public String getPreview() {
    StringBuffer buf = new StringBuffer();

    boolean convert = isEnabled(CleanUpConstants.CONVERT_FUNCTIONAL_INTERFACES);
    boolean useLambda = isEnabled(CleanUpConstants.USE_LAMBDA);
    boolean useAnonymous = isEnabled(CleanUpConstants.USE_ANONYMOUS_CLASS_CREATION);

    boolean firstLambda = convert && useLambda;
    boolean secondLambda = !(convert && useAnonymous);

    if (firstLambda) {
      buf.append("IntConsumer c = i -> {\n"); // $NON-NLS-1$
      buf.append("    System.out.println(i);\n"); // $NON-NLS-1$
      buf.append("};\n"); // $NON-NLS-1$
      buf.append("\n"); // $NON-NLS-1$
      buf.append("\n"); // $NON-NLS-1$
    } else {
      buf.append("IntConsumer c = new IntConsumer() {\n"); // $NON-NLS-1$
      buf.append("    @Override public void accept(int value) {\n"); // $NON-NLS-1$
      buf.append("        System.out.println(i);\n"); // $NON-NLS-1$
      buf.append("    }\n"); // $NON-NLS-1$
      buf.append("};\n"); // $NON-NLS-1$
    }

    if (secondLambda) {
      buf.append("Runnable r = () -> { /* do something */ };\n"); // $NON-NLS-1$
      buf.append("\n"); // $NON-NLS-1$
      buf.append("\n"); // $NON-NLS-1$
      buf.append("\n"); // $NON-NLS-1$
      buf.append("\n"); // $NON-NLS-1$
    } else {
      buf.append("Runnable r = new Runnable() {\n"); // $NON-NLS-1$
      buf.append("    @Override public void run() {\n"); // $NON-NLS-1$
      buf.append("        //do something\n"); // $NON-NLS-1$
      buf.append("    }\n"); // $NON-NLS-1$
      buf.append("};\n"); // $NON-NLS-1$
    }
    return buf.toString();
  }
}
