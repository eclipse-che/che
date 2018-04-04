/**
 * ***************************************************************************** Copyright (c) 2006,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.ui;

import java.util.regex.Pattern;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;

/**
 * Gives access to the import rewrite configured with the settings as specified in the user
 * interface. These settings are kept in JDT UI for compatibility reasons.
 *
 * <p>This class is not intended to be subclassed or instantiated by clients.
 *
 * @since 3.2
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CodeStyleConfiguration {

  private static final Pattern SEMICOLON_PATTERN = Pattern.compile(";"); // $NON-NLS-1$

  private CodeStyleConfiguration() {
    // do not instantiate and subclass
  }

  /**
   * Returns a {@link ImportRewrite} using {@link ImportRewrite#create(ICompilationUnit, boolean)}
   * and configures the rewriter with the settings as specified in the JDT UI preferences.
   *
   * <p>
   *
   * @param cu the compilation unit to create the rewriter on
   * @param restoreExistingImports specifies if the existing imports should be kept or removed.
   * @return the new rewriter configured with the settings as specified in the JDT UI preferences.
   * @throws JavaModelException thrown when the compilation unit could not be accessed.
   * @see ImportRewrite#create(ICompilationUnit, boolean)
   */
  public static ImportRewrite createImportRewrite(
      ICompilationUnit cu, boolean restoreExistingImports) throws JavaModelException {
    return configureImportRewrite(ImportRewrite.create(cu, restoreExistingImports));
  }

  /**
   * Returns a {@link ImportRewrite} using {@link ImportRewrite#create(CompilationUnit, boolean)}
   * and configures the rewriter with the settings as specified in the JDT UI preferences.
   *
   * @param astRoot the AST root to create the rewriter on
   * @param restoreExistingImports specifies if the existing imports should be kept or removed.
   * @return the new rewriter configured with the settings as specified in the JDT UI preferences.
   * @see ImportRewrite#create(CompilationUnit, boolean)
   */
  public static ImportRewrite createImportRewrite(
      CompilationUnit astRoot, boolean restoreExistingImports) {
    return configureImportRewrite(ImportRewrite.create(astRoot, restoreExistingImports));
  }

  private static ImportRewrite configureImportRewrite(ImportRewrite rewrite) {
    // TODO Configure
    IJavaProject project = rewrite.getCompilationUnit().getJavaProject();
    String order =
        PreferenceConstants.getPreference(PreferenceConstants.ORGIMPORTS_IMPORTORDER, project);
    if (order.endsWith(";")) { // $NON-NLS-1$
      order = order.substring(0, order.length() - 1);
    }
    String[] split = SEMICOLON_PATTERN.split(order, -1);
    rewrite.setImportOrder(split);

    String thres =
        PreferenceConstants.getPreference(
            PreferenceConstants.ORGIMPORTS_ONDEMANDTHRESHOLD, project);
    try {
      int num = Integer.parseInt(thres);
      if (num == 0) num = 1;
      rewrite.setOnDemandImportThreshold(num);
    } catch (NumberFormatException e) {
      // ignore
    }
    String thresStatic =
        PreferenceConstants.getPreference(
            PreferenceConstants.ORGIMPORTS_STATIC_ONDEMANDTHRESHOLD, project);
    try {
      int num = Integer.parseInt(thresStatic);
      if (num == 0) num = 1;
      rewrite.setStaticOnDemandImportThreshold(num);
    } catch (NumberFormatException e) {
      // ignore
    }
    return rewrite;
  }
}
