/**
 * ***************************************************************************** Copyright (c) 2006,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.core.refactoring.descriptors;

import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.internal.core.refactoring.descriptors.DescriptorMessages;
import org.eclipse.jdt.internal.core.refactoring.descriptors.JavaRefactoringDescriptorUtil;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Refactoring descriptor for the move refactoring.
 *
 * <p>An instance of this refactoring descriptor may be obtained by calling {@link
 * RefactoringContribution#createDescriptor()} on a refactoring contribution requested by invoking
 * {@link RefactoringCore#getRefactoringContribution(String)} with the appropriate refactoring id.
 *
 * <p>Note: this class is not intended to be instantiated by clients.
 *
 * @since 1.1
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class MoveDescriptor extends JavaRefactoringDescriptor {

  /** The destination attribute (a java element) */
  private static final String ATTRIBUTE_DESTINATION = "destination"; // $NON-NLS-1$

  /** The files attribute */
  private static final String ATTRIBUTE_FILES = "files"; // $NON-NLS-1$

  /** The folders attribute */
  private static final String ATTRIBUTE_FOLDERS = "folders"; // $NON-NLS-1$

  /** The fragments attribute */
  private static final String ATTRIBUTE_FRAGMENTS = "fragments"; // $NON-NLS-1$

  /** The members attribute */
  private static final String ATTRIBUTE_MEMBERS = "members"; // $NON-NLS-1$

  /** The patterns attribute */
  private static final String ATTRIBUTE_PATTERNS = "patterns"; // $NON-NLS-1$

  /** The policy attribute */
  private static final String ATTRIBUTE_POLICY = "policy"; // $NON-NLS-1$

  /** The qualified attribute */
  private static final String ATTRIBUTE_QUALIFIED = "qualified"; // $NON-NLS-1$

  /** The roots attribute */
  private static final String ATTRIBUTE_ROOTS = "roots"; // $NON-NLS-1$

  /** The target attribute (a resource) */
  private static final String ATTRIBUTE_TARGET = "target"; // $NON-NLS-1$

  /** The units attribute */
  private static final String ATTRIBUTE_UNITS = "units"; // $NON-NLS-1$

  /** The move members policy */
  private static final String POLICY_MOVE_MEMBERS = "org.eclipse.jdt.ui.moveMembers"; // $NON-NLS-1$

  /** The move packages policy */
  private static final String POLICY_MOVE_PACKAGES =
      "org.eclipse.jdt.ui.movePackages"; // $NON-NLS-1$

  /** The move resources policy */
  private static final String POLICY_MOVE_RESOURCES =
      "org.eclipse.jdt.ui.moveResources"; // $NON-NLS-1$

  /** The move package fragment roots policy */
  private static final String POLICY_MOVE_ROOTS = "org.eclipse.jdt.ui.moveRoots"; // $NON-NLS-1$

  /** The destination, either an IJavaElement, or a full IPath */
  private Object fDestination;

  /** The files */
  private IPath[] fFiles;

  /** The folders */
  private IPath[] fFolders;

  /** The package fragments */
  private IPackageFragment[] fFragments;

  /** The members */
  private IMember[] fMembers;

  /** The move policy */
  private String fMovePolicy = null;

  /** The patterns attribute */
  private String fPatterns = null;

  /** The qualified attribute */
  private boolean fQualified = false;

  /** The references attribute */
  private boolean fReferences = false;

  /** The package fragment roots */
  private IPackageFragmentRoot[] fRoots;

  /** The compilation units */
  private ICompilationUnit[] fUnits;

  /** Creates a new refactoring descriptor. */
  public MoveDescriptor() {
    super(IJavaRefactorings.MOVE);
  }

  /**
   * Creates a new refactoring descriptor.
   *
   * @param project the non-empty name of the project associated with this refactoring, or <code>
   *     null</code> for a workspace refactoring
   * @param description a non-empty human-readable description of the particular refactoring
   *     instance
   * @param comment the human-readable comment of the particular refactoring instance, or <code>null
   *     </code> for no comment
   * @param arguments a map of arguments that will be persisted and describes all settings for this
   *     refactoring
   * @param flags the flags of the refactoring descriptor
   * @since 1.2
   */
  public MoveDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    super(IJavaRefactorings.MOVE, project, description, comment, arguments, flags);
    fMovePolicy = JavaRefactoringDescriptorUtil.getString(fArguments, ATTRIBUTE_POLICY);

    fDestination =
        JavaRefactoringDescriptorUtil.getJavaElement(
            fArguments, ATTRIBUTE_DESTINATION, project, true);
    if (fDestination == null)
      fDestination =
          JavaRefactoringDescriptorUtil.getResourcePath(fArguments, ATTRIBUTE_TARGET, project);

    if (POLICY_MOVE_RESOURCES.equals(fMovePolicy)) {
      fReferences =
          JavaRefactoringDescriptorUtil.getBoolean(fArguments, ATTRIBUTE_REFERENCES, fReferences);
      fQualified =
          JavaRefactoringDescriptorUtil.getBoolean(fArguments, ATTRIBUTE_QUALIFIED, fQualified);
      fPatterns = JavaRefactoringDescriptorUtil.getString(fArguments, ATTRIBUTE_PATTERNS, true);
      int offset = 1;
      fFiles =
          JavaRefactoringDescriptorUtil.getResourcePathArray(
              fArguments, ATTRIBUTE_FILES, ATTRIBUTE_ELEMENT, offset, project);
      offset += fFiles.length;
      fFolders =
          JavaRefactoringDescriptorUtil.getResourcePathArray(
              fArguments, ATTRIBUTE_FOLDERS, ATTRIBUTE_ELEMENT, offset, project);
      offset += fFolders.length;
      fUnits =
          (ICompilationUnit[])
              JavaRefactoringDescriptorUtil.getJavaElementArray(
                  fArguments,
                  ATTRIBUTE_UNITS,
                  ATTRIBUTE_ELEMENT,
                  offset,
                  project,
                  ICompilationUnit.class);
    } else if (POLICY_MOVE_ROOTS.equals(fMovePolicy)) {
      fRoots =
          (IPackageFragmentRoot[])
              JavaRefactoringDescriptorUtil.getJavaElementArray(
                  fArguments,
                  ATTRIBUTE_ROOTS,
                  ATTRIBUTE_ELEMENT,
                  1,
                  project,
                  IPackageFragmentRoot.class);
    } else if (POLICY_MOVE_PACKAGES.equals(fMovePolicy)) {
      fFragments =
          (IPackageFragment[])
              JavaRefactoringDescriptorUtil.getJavaElementArray(
                  fArguments,
                  ATTRIBUTE_FRAGMENTS,
                  ATTRIBUTE_ELEMENT,
                  1,
                  project,
                  IPackageFragment.class);
    } else if (POLICY_MOVE_MEMBERS.equals(fMovePolicy)) {
      fMembers =
          (IMember[])
              JavaRefactoringDescriptorUtil.getJavaElementArray(
                  fArguments, ATTRIBUTE_MEMBERS, ATTRIBUTE_ELEMENT, 1, project, IMember.class);
    }
  }

  /** {@inheritDoc} */
  protected void populateArgumentMap() {
    super.populateArgumentMap();
    JavaRefactoringDescriptorUtil.setString(fArguments, ATTRIBUTE_POLICY, fMovePolicy);
    final String project = getProject();
    if (fDestination instanceof IJavaElement)
      JavaRefactoringDescriptorUtil.setJavaElement(
          fArguments, ATTRIBUTE_DESTINATION, project, (IJavaElement) fDestination);
    else if (fDestination instanceof IPath)
      JavaRefactoringDescriptorUtil.setResourcePath(
          fArguments, ATTRIBUTE_TARGET, project, (IPath) fDestination);
    if (POLICY_MOVE_RESOURCES.equals(fMovePolicy)) {
      JavaRefactoringDescriptorUtil.setBoolean(fArguments, ATTRIBUTE_REFERENCES, fReferences);
      JavaRefactoringDescriptorUtil.setBoolean(fArguments, ATTRIBUTE_QUALIFIED, fQualified);
      JavaRefactoringDescriptorUtil.setString(fArguments, ATTRIBUTE_PATTERNS, fPatterns);
      int offset = 1;
      JavaRefactoringDescriptorUtil.setResourcePathArray(
          fArguments, ATTRIBUTE_FILES, ATTRIBUTE_ELEMENT, project, fFiles, offset);
      offset += fFiles.length;
      JavaRefactoringDescriptorUtil.setResourcePathArray(
          fArguments, ATTRIBUTE_FOLDERS, ATTRIBUTE_ELEMENT, project, fFolders, offset);
      offset += fFolders.length;
      JavaRefactoringDescriptorUtil.setJavaElementArray(
          fArguments, ATTRIBUTE_UNITS, ATTRIBUTE_ELEMENT, project, fUnits, offset);
    } else if (POLICY_MOVE_ROOTS.equals(fMovePolicy)) {
      JavaRefactoringDescriptorUtil.setJavaElementArray(
          fArguments, ATTRIBUTE_ROOTS, ATTRIBUTE_ELEMENT, project, fRoots, 1);
    } else if (POLICY_MOVE_PACKAGES.equals(fMovePolicy)) {
      JavaRefactoringDescriptorUtil.setJavaElementArray(
          fArguments, ATTRIBUTE_FRAGMENTS, ATTRIBUTE_ELEMENT, project, fFragments, 1);
    } else if (POLICY_MOVE_MEMBERS.equals(fMovePolicy)) {
      JavaRefactoringDescriptorUtil.setJavaElementArray(
          fArguments, ATTRIBUTE_MEMBERS, ATTRIBUTE_ELEMENT, project, fMembers, 1);
    }
  }

  /**
   * Sets the destination of the move.
   *
   * <p>Note: Clients may call only one of the <code>setDestination</code> methods.
   *
   * @param element the java element
   */
  public void setDestination(IJavaElement element) {
    Assert.isNotNull(element);
    fDestination = element;
  }

  /**
   * Sets the destination of the move.
   *
   * <p>Note: Clients may call only one of the <code>setDestination</code> methods.
   *
   * @param resource the resource
   */
  public void setDestination(IResource resource) {
    Assert.isNotNull(resource);
    fDestination = resource.getFullPath();
  }

  /**
   * Sets the file name patterns to use during qualified name updating.
   *
   * <p>The syntax of the file name patterns is a sequence of individual name patterns, separated by
   * comma. Additionally, wildcard characters '*' (any string) and '?' (any character) may be used.
   *
   * <p>Note: If file name patterns are set, qualified name updating must be enabled by calling
   * {@link #setUpdateQualifiedNames(boolean)}.
   *
   * <p>Note: Qualified name updating is currently applicable to files, folders and compilation
   * units. The default is to not update qualified names.
   *
   * @param patterns the non-empty file name patterns string
   */
  public void setFileNamePatterns(final String patterns) {
    Assert.isNotNull(patterns);
    Assert.isLegal(!"".equals(patterns), "Pattern must not be empty"); // $NON-NLS-1$ //$NON-NLS-2$
    fPatterns = patterns;
  }

  /**
   * Sets the members to move.
   *
   * <p>Note: Clients must only call one of the <code>setMoveXXX</code> methods.
   *
   * @param members the members to move
   */
  public void setMoveMembers(final IMember[] members) {
    Assert.isNotNull(members);
    Assert.isTrue(
        fMovePolicy == null,
        "Clients must only call one of the 'setMoveXXX' methods."); // $NON-NLS-1$
    fMembers = members;
    fMovePolicy = POLICY_MOVE_MEMBERS;
  }

  /**
   * Sets the package fragment roots to move.
   *
   * <p>Note: Clients must only call one of the <code>setMoveXXX</code> methods.
   *
   * @param roots the package fragment roots to move
   */
  public void setMovePackageFragmentRoots(final IPackageFragmentRoot[] roots) {
    Assert.isNotNull(roots);
    Assert.isTrue(
        fMovePolicy == null,
        "Clients must only call one of the 'setMoveXXX' methods."); // $NON-NLS-1$
    fRoots = roots;
    fMovePolicy = POLICY_MOVE_ROOTS;
  }

  /**
   * Sets the package fragments to move.
   *
   * <p>Note: Clients must only call one of the <code>setMoveXXX</code> methods.
   *
   * @param fragments the package fragments to move
   */
  public void setMovePackages(final IPackageFragment[] fragments) {
    Assert.isNotNull(fragments);
    Assert.isTrue(
        fMovePolicy == null,
        "Clients must only call one of the 'setMoveXXX' methods."); // $NON-NLS-1$
    fFragments = fragments;
    fMovePolicy = POLICY_MOVE_PACKAGES;
  }

  /**
   * Sets the resources and compilation units to move.
   *
   * <p>Note: Clients must only call one of the <code>setMoveXXX</code> methods.
   *
   * @param files the files to move
   * @param folders the folders to move
   * @param units the compilation units to move
   */
  public void setMoveResources(
      final IFile[] files, final IFolder[] folders, final ICompilationUnit[] units) {
    Assert.isNotNull(files);
    Assert.isNotNull(folders);
    Assert.isNotNull(units);
    Assert.isTrue(
        fMovePolicy == null,
        "Clients must only call one of the 'setMoveXXX' methods."); // $NON-NLS-1$
    fFiles = new IPath[files.length];
    for (int i = 0; i < files.length; i++) {
      fFiles[i] = files[i].getFullPath();
    }
    fFolders = new IPath[folders.length];
    for (int i = 0; i < folders.length; i++) {
      fFolders[i] = folders[i].getFullPath();
    }
    fUnits = units;
    fMovePolicy = POLICY_MOVE_RESOURCES;
  }

  /**
   * Determines whether qualified names of the Java element should be renamed.
   *
   * <p>Qualified name updating adapts fully qualified names of the Java element to be renamed in
   * non-Java text files. Clients may specify file name patterns by calling {@link
   * #setFileNamePatterns(String)} to constrain the set of text files to be processed.
   *
   * <p>Note: Qualified name updating is currently applicable to files, folders and compilation
   * units. The default is to use no file name patterns (meaning that all files are processed).
   *
   * @param update <code>true</code> to update qualified names, <code>false</code> otherwise
   */
  public void setUpdateQualifiedNames(final boolean update) {
    fQualified = update;
  }

  /**
   * Determines whether references to the Java element should be renamed.
   *
   * @param update <code>true</code> to update references, <code>false</code> otherwise
   */
  public void setUpdateReferences(final boolean update) {
    fReferences = update;
  }

  /** {@inheritDoc} */
  public RefactoringStatus validateDescriptor() {
    RefactoringStatus status = super.validateDescriptor();
    if (!status.hasFatalError()) {
      if (fMovePolicy == null)
        status.merge(
            RefactoringStatus.createFatalErrorStatus(
                DescriptorMessages.MoveDescriptor_no_elements_set));
      if (fDestination == null)
        status.merge(
            RefactoringStatus.createFatalErrorStatus(
                DescriptorMessages.MoveDescriptor_no_destination_set));
    }
    return status;
  }
}
