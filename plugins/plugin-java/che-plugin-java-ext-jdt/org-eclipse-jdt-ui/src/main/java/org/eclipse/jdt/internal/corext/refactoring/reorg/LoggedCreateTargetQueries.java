/**
 * ***************************************************************************** Copyright (c) 2006,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.reorg;

import java.util.Arrays;
import java.util.List;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.util.CoreUtility;

/**
 * Logged implementation of new create target queries.
 *
 * @since 3.3
 */
public final class LoggedCreateTargetQueries implements ICreateTargetQueries {

  /** Default implementation of create target query */
  private final class CreateTargetQuery implements ICreateTargetQuery {

    private void createJavaProject(IProject project) throws CoreException {
      if (!project.exists()) {
        //				BuildPathsBlock.createProject(project, null, new NullProgressMonitor());
        //				BuildPathsBlock.addJavaNature(project, new NullProgressMonitor());
        throw new UnsupportedOperationException("createJavaProject");
      }
    }

    private void createPackageFragmentRoot(IPackageFragmentRoot root) throws CoreException {
      final IJavaProject project = root.getJavaProject();
      if (!project.exists()) createJavaProject(project.getProject());
      final IFolder folder = project.getProject().getFolder(root.getElementName());
      if (!folder.exists()) CoreUtility.createFolder(folder, true, true, new NullProgressMonitor());
      final List<IClasspathEntry> list = Arrays.asList(project.getRawClasspath());
      list.add(JavaCore.newSourceEntry(folder.getFullPath()));
      project.setRawClasspath(
          list.toArray(new IClasspathEntry[list.size()]), new NullProgressMonitor());
    }

    /** {@inheritDoc} */
    public Object getCreatedTarget(final Object selection) {
      final Object target = fLog.getCreatedElement(selection);
      if (target instanceof IPackageFragment) {
        final IPackageFragment fragment = (IPackageFragment) target;
        final IJavaElement parent = fragment.getParent();
        if (parent instanceof IPackageFragmentRoot) {
          try {
            final IPackageFragmentRoot root = (IPackageFragmentRoot) parent;
            if (!root.exists()) createPackageFragmentRoot(root);
            if (!fragment.exists())
              root.createPackageFragment(
                  fragment.getElementName(), true, new NullProgressMonitor());
          } catch (CoreException exception) {
            JavaPlugin.log(exception);
            return null;
          }
        }
      } else if (target instanceof IFolder) {
        try {
          final IFolder folder = (IFolder) target;
          final IProject project = folder.getProject();
          if (!project.exists()) createJavaProject(project);
          if (!folder.exists())
            CoreUtility.createFolder(folder, true, true, new NullProgressMonitor());
        } catch (CoreException exception) {
          JavaPlugin.log(exception);
          return null;
        }
      }
      return target;
    }

    /** {@inheritDoc} */
    public String getNewButtonLabel() {
      return "unused"; // $NON-NLS-1$
    }
  }

  /** The create target execution log */
  private final CreateTargetExecutionLog fLog;

  /**
   * Creates a new logged create target queries.
   *
   * @param log the create target execution log
   */
  public LoggedCreateTargetQueries(final CreateTargetExecutionLog log) {
    Assert.isNotNull(log);
    fLog = log;
  }

  /** {@inheritDoc} */
  public ICreateTargetQuery createNewPackageQuery() {
    return new CreateTargetQuery();
  }

  /**
   * Returns the create target execution log.
   *
   * @return the create target execution log
   */
  public CreateTargetExecutionLog getCreateTargetExecutionLog() {
    return fLog;
  }
}
