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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.UnusedCodeFix;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

/**
 * Create fixes which can remove unused code
 *
 * @see org.eclipse.jdt.internal.corext.fix.UnusedCodeFix
 */
public class UnusedCodeCleanUp extends AbstractMultiFix {

  public UnusedCodeCleanUp(Map<String, String> options) {
    super(options);
  }

  public UnusedCodeCleanUp() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  public CleanUpRequirements getRequirements() {
    boolean requireAST = requireAST();
    Map<String, String> requiredOptions = requireAST ? getRequiredOptions() : null;
    return new CleanUpRequirements(requireAST, false, false, requiredOptions);
  }

  private boolean requireAST() {
    boolean removeUnuseMembers = isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS);

    return removeUnuseMembers && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_METHODS)
        || removeUnuseMembers && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_CONSTRUCTORS)
        || removeUnuseMembers && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_FELDS)
        || removeUnuseMembers && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_TYPES)
        || isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_LOCAL_VARIABLES)
        || isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_IMPORTS)
            && !isEnabled(CleanUpConstants.ORGANIZE_IMPORTS);
  }

  /** {@inheritDoc} */
  @Override
  protected ICleanUpFix createFix(CompilationUnit compilationUnit) throws CoreException {
    boolean removeUnuseMembers = isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS);

    return UnusedCodeFix.createCleanUp(
        compilationUnit,
        removeUnuseMembers && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_METHODS),
        removeUnuseMembers && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_CONSTRUCTORS),
        removeUnuseMembers && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_FELDS),
        removeUnuseMembers && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_TYPES),
        isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_LOCAL_VARIABLES),
        isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_IMPORTS)
            && !isEnabled(CleanUpConstants.ORGANIZE_IMPORTS),
        false);
  }

  /** {@inheritDoc} */
  @Override
  protected ICleanUpFix createFix(CompilationUnit compilationUnit, IProblemLocation[] problems)
      throws CoreException {
    boolean removeMembers = isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS);

    return UnusedCodeFix.createCleanUp(
        compilationUnit,
        problems,
        removeMembers && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_METHODS),
        removeMembers && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_CONSTRUCTORS),
        removeMembers && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_FELDS),
        removeMembers && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_TYPES),
        isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_LOCAL_VARIABLES),
        isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_IMPORTS)
            && !isEnabled(CleanUpConstants.ORGANIZE_IMPORTS),
        false);
  }

  public Map<String, String> getRequiredOptions() {
    Map<String, String> result = new Hashtable<String, String>();

    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_IMPORTS)
        && !isEnabled(CleanUpConstants.ORGANIZE_IMPORTS))
      result.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.WARNING);

    boolean removeMembers = isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS);
    if (removeMembers
        && (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_METHODS)
            || isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_CONSTRUCTORS)
            || isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_FELDS)
            || isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_TYPES)))
      result.put(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.WARNING);

    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_LOCAL_VARIABLES))
      result.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.WARNING);

    return result;
  }

  /** {@inheritDoc} */
  @Override
  public String[] getStepDescriptions() {
    List<String> result = new ArrayList<String>();
    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_IMPORTS))
      result.add(MultiFixMessages.UnusedCodeMultiFix_RemoveUnusedImport_description);
    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS)
        && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_METHODS))
      result.add(MultiFixMessages.UnusedCodeMultiFix_RemoveUnusedMethod_description);
    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS)
        && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_CONSTRUCTORS))
      result.add(MultiFixMessages.UnusedCodeMultiFix_RemoveUnusedConstructor_description);
    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS)
        && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_TYPES))
      result.add(MultiFixMessages.UnusedCodeMultiFix_RemoveUnusedType_description);
    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS)
        && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_FELDS))
      result.add(MultiFixMessages.UnusedCodeMultiFix_RemoveUnusedField_description);
    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_LOCAL_VARIABLES))
      result.add(MultiFixMessages.UnusedCodeMultiFix_RemoveUnusedVariable_description);
    return result.toArray(new String[result.size()]);
  }

  /** {@inheritDoc} */
  @Override
  public String getPreview() {
    StringBuffer buf = new StringBuffer();

    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_IMPORTS)) {
    } else {
      buf.append("import pack.Bar;\n"); // $NON-NLS-1$
    }
    buf.append("class Example {\n"); // $NON-NLS-1$
    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS)
        && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_TYPES)) {
    } else {
      buf.append("    private class Sub {}\n"); // $NON-NLS-1$
    }
    buf.append("    public Example(boolean b) {}\n"); // $NON-NLS-1$
    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS)
        && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_CONSTRUCTORS)) {
    } else {
      buf.append("    private Example() {}\n"); // $NON-NLS-1$
    }
    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS)
        && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_FELDS)) {
    } else {
      buf.append("    private int fField;\n"); // $NON-NLS-1$
    }
    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS)
        && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_METHODS)) {
    } else {
      buf.append("    private void foo() {}\n"); // $NON-NLS-1$
    }
    buf.append("    public void bar() {\n"); // $NON-NLS-1$
    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_LOCAL_VARIABLES)) {
    } else {
      buf.append("        int i= 10;\n"); // $NON-NLS-1$
    }
    buf.append("    }\n"); // $NON-NLS-1$
    buf.append("}\n"); // $NON-NLS-1$

    return buf.toString();
  }

  /** {@inheritDoc} */
  public boolean canFix(ICompilationUnit compilationUnit, IProblemLocation problem) {
    if (UnusedCodeFix.isUnusedImport(problem))
      return isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_IMPORTS);

    if (UnusedCodeFix.isUnusedMember(problem))
      return isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS)
              && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_METHODS)
          || isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS)
              && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_CONSTRUCTORS)
          || isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS)
              && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_TYPES)
          || isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS)
              && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_FELDS)
          || isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_LOCAL_VARIABLES);

    return false;
  }

  /** {@inheritDoc} */
  @Override
  public int computeNumberOfFixes(CompilationUnit compilationUnit) {
    int result = 0;
    IProblem[] problems = compilationUnit.getProblems();
    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_IMPORTS)
        && !isEnabled(CleanUpConstants.ORGANIZE_IMPORTS)) {
      for (int i = 0; i < problems.length; i++) {
        int id = problems[i].getID();
        if (id == IProblem.UnusedImport
            || id == IProblem.DuplicateImport
            || id == IProblem.ConflictingImport
            || id == IProblem.CannotImportPackage
            || id == IProblem.ImportNotFound) result++;
      }
    }
    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS)
        && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_METHODS))
      result += getNumberOfProblems(problems, IProblem.UnusedPrivateMethod);
    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS)
        && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_CONSTRUCTORS))
      result += getNumberOfProblems(problems, IProblem.UnusedPrivateConstructor);
    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS)
        && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_TYPES))
      result += getNumberOfProblems(problems, IProblem.UnusedPrivateType);
    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_MEMBERS)
        && isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_PRIVATE_FELDS))
      result += getNumberOfProblems(problems, IProblem.UnusedPrivateField);
    if (isEnabled(CleanUpConstants.REMOVE_UNUSED_CODE_LOCAL_VARIABLES))
      result += getNumberOfProblems(problems, IProblem.LocalVariableIsNeverUsed);
    return result;
  }
}
