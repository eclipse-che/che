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

import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.cleanup.CleanUpContext;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public abstract class AbstractCleanUp implements ICleanUp {

  private CleanUpOptions fOptions;

  protected AbstractCleanUp() {}

  protected AbstractCleanUp(Map<String, String> settings) {
    setOptions(new MapCleanUpOptions(settings));
  }

  /*
   * @see org.eclipse.jdt.ui.cleanup.ICleanUp#setOptions(org.eclipse.jdt.ui.cleanup.CleanUpOptions)
   * @since 3.5
   */
  public void setOptions(CleanUpOptions options) {
    Assert.isLegal(options != null);
    fOptions = options;
  }

  /*
   * @see org.eclipse.jdt.ui.cleanup.ICleanUp#getStepDescriptions()
   * @since 3.5
   */
  public String[] getStepDescriptions() {
    return new String[0];
  }

  /** @return code snippet complying to current options */
  public String getPreview() {
    return ""; // $NON-NLS-1$
  }

  /*
   * @see org.eclipse.jdt.ui.cleanup.ICleanUp#getRequirements()
   * @since 3.5
   */
  public CleanUpRequirements getRequirements() {
    return new CleanUpRequirements(false, false, false, null);
  }

  /*
   * @see org.eclipse.jdt.ui.cleanup.ICleanUp#checkPreConditions(org.eclipse.jdt.core.IJavaProject, org.eclipse.jdt.core.ICompilationUnit[], org.eclipse.core.runtime.IProgressMonitor)
   * @since 3.5
   */
  public RefactoringStatus checkPreConditions(
      IJavaProject project, ICompilationUnit[] compilationUnits, IProgressMonitor monitor)
      throws CoreException {
    return new RefactoringStatus();
  }

  /*
   * @see org.eclipse.jdt.ui.cleanup.ICleanUp#createFix(org.eclipse.jdt.ui.cleanup.CleanUpContext)
   * @since 3.5
   */
  public ICleanUpFix createFix(CleanUpContext context) throws CoreException {
    return null;
  }

  /*
   * @see org.eclipse.jdt.ui.cleanup.ICleanUp#checkPostConditions(org.eclipse.core.runtime.IProgressMonitor)
   * @since 3.5
   */
  public RefactoringStatus checkPostConditions(IProgressMonitor monitor) throws CoreException {
    return new RefactoringStatus();
  }

  /**
   * @param key the name of the option
   * @return <code>true</code> if option with <code>key</code> is enabled
   */
  protected boolean isEnabled(String key) {
    Assert.isNotNull(fOptions);
    Assert.isLegal(key != null);
    return fOptions.isEnabled(key);
  }
}
