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
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.internal.core.refactoring.descriptors.DescriptorMessages;
import org.eclipse.jdt.internal.core.refactoring.descriptors.JavaRefactoringDescriptorUtil;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Refactoring descriptor for the rename java element refactoring.
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
public final class RenameJavaElementDescriptor extends JavaRefactoringDescriptor {

  /** The delegate attribute */
  private static final String ATTRIBUTE_DELEGATE = "delegate"; // $NON-NLS-1$

  /** The deprecate attribute */
  private static final String ATTRIBUTE_DEPRECATE = "deprecate"; // $NON-NLS-1$

  /** The hierarchical attribute */
  private static final String ATTRIBUTE_HIERARCHICAL = "hierarchical"; // $NON-NLS-1$

  /** The match strategy attribute */
  private static final String ATTRIBUTE_MATCH_STRATEGY = "matchStrategy"; // $NON-NLS-1$

  /** The parameter attribute */
  private static final String ATTRIBUTE_PARAMETER = "parameter"; // $NON-NLS-1$

  /** The patterns attribute */
  private static final String ATTRIBUTE_PATTERNS = "patterns"; // $NON-NLS-1$

  /** The qualified attribute */
  private static final String ATTRIBUTE_QUALIFIED = "qualified"; // $NON-NLS-1$

  /** The rename getter attribute */
  private static final String ATTRIBUTE_RENAME_GETTER = "getter"; // $NON-NLS-1$

  /** The rename setter attribute */
  private static final String ATTRIBUTE_RENAME_SETTER = "setter"; // $NON-NLS-1$

  /** The similar declarations attribute */
  private static final String ATTRIBUTE_SIMILAR_DECLARATIONS = "similarDeclarations"; // $NON-NLS-1$

  /** The textual matches attribute */
  private static final String ATTRIBUTE_TEXTUAL_MATCHES = "textual"; // $NON-NLS-1$

  /**
   * Similar declaration updating strategy which finds exact names and embedded names as well
   * (value: <code>2</code>).
   */
  public static final int STRATEGY_EMBEDDED = 2;

  /** Similar declaration updating strategy which finds exact names only (value: <code>1</code>). */
  public static final int STRATEGY_EXACT = 1;

  /**
   * Similar declaration updating strategy which finds exact names, embedded names and name suffixes
   * (value: <code>3</code>).
   */
  public static final int STRATEGY_SUFFIX = 3;

  /**
   * @deprecated Replaced by {@link
   *     org.eclipse.ltk.core.refactoring.resource.RenameResourceDescriptor#ID}
   */
  private static final String RENAME_RESOURCE = IJavaRefactorings.RENAME_RESOURCE;

  /** The delegate attribute */
  private boolean fDelegate = false;

  /** The deprecate attribute */
  private boolean fDeprecate = false;

  /** The hierarchical attribute */
  private boolean fHierarchical = false;

  /**
   * The java element attribute. WARNING: may not exist, see comment in {@link
   * JavaRefactoringDescriptorUtil#handleToElement(org.eclipse.jdt.core.WorkingCopyOwner, String,
   * String, boolean)}.
   */
  private IJavaElement fJavaElement = null;

  /** The match strategy */
  private int fMatchStrategy = STRATEGY_EXACT;

  /** The name attribute */
  private String fName = null;

  /** The patterns attribute */
  private String fPatterns = null;

  /** The qualified attribute */
  private boolean fQualified = false;

  /** The references attribute */
  private boolean fReferences = false;

  /** The rename getter attribute */
  private boolean fRenameGetter = false;

  /** The rename setter attribute */
  private boolean fRenameSetter = false;

  /** The similar declarations attribute */
  private boolean fSimilarDeclarations = false;

  /** The textual attribute */
  private boolean fTextual = false;

  /**
   * Creates a new refactoring descriptor.
   *
   * @param id the unique id of the rename refactoring
   * @see IJavaRefactorings
   */
  public RenameJavaElementDescriptor(final String id) {
    super(id);
    Assert.isLegal(checkId(id), "Refactoring id is not a rename refactoring id"); // $NON-NLS-1$
  }

  /**
   * Creates a new refactoring descriptor.
   *
   * @param id the ID of this descriptor
   * @param project the non-empty name of the project associated with this refactoring, or <code>
   *     null</code> for a workspace refactoring
   * @param description a non-empty human-readable description of the particular refactoring
   *     instance
   * @param comment the human-readable comment of the particular refactoring instance, or <code>null
   *     </code> for no comment
   * @param arguments a map of arguments that will be persisted and describes all settings for this
   *     refactoring
   * @param flags the flags of the refactoring descriptor
   * @throws IllegalArgumentException if the argument map contains invalid keys/values
   * @since 1.2
   */
  public RenameJavaElementDescriptor(
      String id, String project, String description, String comment, Map arguments, int flags) {
    super(id, project, description, comment, arguments, flags);
    Assert.isLegal(checkId(id), "Refactoring id is not a rename refactoring id"); // $NON-NLS-1$
    fName = JavaRefactoringDescriptorUtil.getString(fArguments, ATTRIBUTE_NAME);
    if (getID().equals(IJavaRefactorings.RENAME_TYPE_PARAMETER)) {
      fJavaElement =
          JavaRefactoringDescriptorUtil.getJavaElement(fArguments, ATTRIBUTE_INPUT, getProject());
      String parameterName =
          JavaRefactoringDescriptorUtil.getString(fArguments, ATTRIBUTE_PARAMETER);
      if (fJavaElement instanceof IType) {
        fJavaElement = ((IType) fJavaElement).getTypeParameter(parameterName);
      }
      if (fJavaElement instanceof IMethod) {
        fJavaElement = ((IMethod) fJavaElement).getTypeParameter(parameterName);
      }
    } else
      fJavaElement =
          JavaRefactoringDescriptorUtil.getJavaElement(fArguments, ATTRIBUTE_INPUT, getProject());
    final int type = fJavaElement.getElementType();
    if (type != IJavaElement.PACKAGE_FRAGMENT_ROOT)
      fReferences =
          JavaRefactoringDescriptorUtil.getBoolean(fArguments, ATTRIBUTE_REFERENCES, fReferences);
    if (type == IJavaElement.FIELD) {
      fRenameGetter =
          JavaRefactoringDescriptorUtil.getBoolean(
              fArguments, ATTRIBUTE_RENAME_GETTER, fRenameGetter);
      fRenameSetter =
          JavaRefactoringDescriptorUtil.getBoolean(
              fArguments, ATTRIBUTE_RENAME_SETTER, fRenameSetter);
    }
    switch (type) {
      case IJavaElement.PACKAGE_FRAGMENT:
      case IJavaElement.TYPE:
      case IJavaElement.FIELD:
        fTextual =
            JavaRefactoringDescriptorUtil.getBoolean(
                fArguments, ATTRIBUTE_TEXTUAL_MATCHES, fTextual);
        break;
      default:
        break;
    }
    switch (type) {
      case IJavaElement.METHOD:
      case IJavaElement.FIELD:
        fDeprecate =
            JavaRefactoringDescriptorUtil.getBoolean(fArguments, ATTRIBUTE_DEPRECATE, fDeprecate);
        fDelegate =
            JavaRefactoringDescriptorUtil.getBoolean(fArguments, ATTRIBUTE_DELEGATE, fDelegate);
        break;
      default:
        break;
    }
    switch (type) {
      case IJavaElement.PACKAGE_FRAGMENT:
      case IJavaElement.TYPE:
        fQualified =
            JavaRefactoringDescriptorUtil.getBoolean(fArguments, ATTRIBUTE_QUALIFIED, fQualified);
        fPatterns = JavaRefactoringDescriptorUtil.getString(fArguments, ATTRIBUTE_PATTERNS, true);
        break;
      default:
        break;
    }
    switch (type) {
      case IJavaElement.TYPE:
        fSimilarDeclarations =
            JavaRefactoringDescriptorUtil.getBoolean(
                fArguments, ATTRIBUTE_SIMILAR_DECLARATIONS, fSimilarDeclarations);
        fMatchStrategy =
            JavaRefactoringDescriptorUtil.getInt(
                fArguments, ATTRIBUTE_MATCH_STRATEGY, fMatchStrategy);
        break;
      default:
        break;
    }
    switch (type) {
      case IJavaElement.PACKAGE_FRAGMENT:
        fHierarchical =
            JavaRefactoringDescriptorUtil.getBoolean(
                fArguments, ATTRIBUTE_HIERARCHICAL, fHierarchical);
        break;
      default:
        break;
    }
  }

  /**
   * Checks whether the refactoring id is valid.
   *
   * @param id the refactoring id
   * @return the outcome of the validation
   */
  private boolean checkId(final String id) {
    Assert.isNotNull(id);
    if (id.equals(IJavaRefactorings.RENAME_COMPILATION_UNIT)) return true;
    else if (id.equals(IJavaRefactorings.RENAME_ENUM_CONSTANT)) return true;
    else if (id.equals(IJavaRefactorings.RENAME_FIELD)) return true;
    else if (id.equals(IJavaRefactorings.RENAME_JAVA_PROJECT)) return true;
    else if (id.equals(IJavaRefactorings.RENAME_LOCAL_VARIABLE)) return true;
    else if (id.equals(IJavaRefactorings.RENAME_METHOD)) return true;
    else if (id.equals(IJavaRefactorings.RENAME_PACKAGE)) return true;
    else if (id.equals(RENAME_RESOURCE)) return true;
    else if (id.equals(IJavaRefactorings.RENAME_SOURCE_FOLDER)) return true;
    else if (id.equals(IJavaRefactorings.RENAME_TYPE)) return true;
    else if (id.equals(IJavaRefactorings.RENAME_TYPE_PARAMETER)) return true;
    return false;
  }

  /** {@inheritDoc} */
  protected void populateArgumentMap() {
    super.populateArgumentMap();
    JavaRefactoringDescriptorUtil.setString(fArguments, ATTRIBUTE_NAME, fName);
    if (getID().equals(IJavaRefactorings.RENAME_TYPE_PARAMETER)) {
      final ITypeParameter parameter = (ITypeParameter) fJavaElement;
      JavaRefactoringDescriptorUtil.setJavaElement(
          fArguments, ATTRIBUTE_INPUT, getProject(), parameter.getDeclaringMember());
      JavaRefactoringDescriptorUtil.setString(
          fArguments, ATTRIBUTE_PARAMETER, parameter.getElementName());
    } else
      JavaRefactoringDescriptorUtil.setJavaElement(
          fArguments, ATTRIBUTE_INPUT, getProject(), fJavaElement);
    final int type = fJavaElement.getElementType();
    if (type != IJavaElement.PACKAGE_FRAGMENT_ROOT)
      JavaRefactoringDescriptorUtil.setBoolean(fArguments, ATTRIBUTE_REFERENCES, fReferences);
    if (type == IJavaElement.FIELD) {
      JavaRefactoringDescriptorUtil.setBoolean(fArguments, ATTRIBUTE_RENAME_GETTER, fRenameGetter);
      JavaRefactoringDescriptorUtil.setBoolean(fArguments, ATTRIBUTE_RENAME_SETTER, fRenameSetter);
    }
    switch (type) {
      case IJavaElement.PACKAGE_FRAGMENT:
      case IJavaElement.TYPE:
      case IJavaElement.FIELD:
        JavaRefactoringDescriptorUtil.setBoolean(fArguments, ATTRIBUTE_TEXTUAL_MATCHES, fTextual);
        break;
      default:
        break;
    }
    switch (type) {
      case IJavaElement.METHOD:
      case IJavaElement.FIELD:
        JavaRefactoringDescriptorUtil.setBoolean(fArguments, ATTRIBUTE_DEPRECATE, fDeprecate);
        JavaRefactoringDescriptorUtil.setBoolean(fArguments, ATTRIBUTE_DELEGATE, fDelegate);
        break;
      default:
        break;
    }
    switch (type) {
      case IJavaElement.PACKAGE_FRAGMENT:
      case IJavaElement.TYPE:
        JavaRefactoringDescriptorUtil.setBoolean(fArguments, ATTRIBUTE_QUALIFIED, fQualified);
        JavaRefactoringDescriptorUtil.setString(fArguments, ATTRIBUTE_PATTERNS, fPatterns);
        break;
      default:
        break;
    }
    switch (type) {
      case IJavaElement.TYPE:
        JavaRefactoringDescriptorUtil.setBoolean(
            fArguments, ATTRIBUTE_SIMILAR_DECLARATIONS, fSimilarDeclarations);
        JavaRefactoringDescriptorUtil.setInt(fArguments, ATTRIBUTE_MATCH_STRATEGY, fMatchStrategy);
        break;
      default:
        break;
    }
    switch (type) {
      case IJavaElement.PACKAGE_FRAGMENT:
        JavaRefactoringDescriptorUtil.setBoolean(fArguments, ATTRIBUTE_HIERARCHICAL, fHierarchical);
        break;
      default:
        break;
    }
  }

  /**
   * Determines whether the delegate for a Java element should be declared as deprecated.
   *
   * <p>Note: Deprecation of the delegate is currently applicable to the Java elements {@link
   * IMethod} and {@link IField}. The default is to not deprecate the delegate.
   *
   * @param deprecate <code>true</code> to deprecate the delegate, <code>false</code> otherwise
   */
  public void setDeprecateDelegate(final boolean deprecate) {
    fDeprecate = deprecate;
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
   * <p>Note: Qualified name updating is currently applicable to the Java elements {@link
   * IPackageFragment} and {@link IType}. The default is to use no file name patterns (meaning that
   * all files are processed).
   *
   * @param patterns the non-empty file name patterns string
   */
  public void setFileNamePatterns(final String patterns) {
    Assert.isNotNull(patterns);
    Assert.isLegal(!"".equals(patterns), "Pattern must not be empty"); // $NON-NLS-1$ //$NON-NLS-2$
    fPatterns = patterns;
  }

  /**
   * Sets the Java element to be renamed.
   *
   * <p>Note: If the Java element to be renamed is of type {@link IJavaElement#JAVA_PROJECT},
   * clients are required to to set the project name to <code>null</code>.
   *
   * @param element the Java element to be renamed
   */
  public void setJavaElement(final IJavaElement element) {
    Assert.isNotNull(element);
    fJavaElement = element;
  }

  /**
   * Determines whether the the original Java element should be kept as delegate to the renamed one.
   *
   * <p>Note: Keeping of original elements as delegates is currently applicable to the Java elements
   * {@link IMethod} and {@link IField}. The default is to not keep the original as delegate.
   *
   * @param delegate <code>true</code> to keep the original, <code>false</code> otherwise
   */
  public void setKeepOriginal(final boolean delegate) {
    fDelegate = delegate;
  }

  /**
   * Determines which strategy should be used during similar declaration updating.
   *
   * <p>Valid arguments are {@link #STRATEGY_EXACT}, {@link #STRATEGY_EMBEDDED} or {@link
   * #STRATEGY_SUFFIX}.
   *
   * <p>Note: Similar declaration updating is currently applicable to Java elements of type {@link
   * IType}. The default is to use the {@link #STRATEGY_EXACT} match strategy.
   *
   * @param strategy the match strategy to use
   */
  public void setMatchStrategy(final int strategy) {
    Assert.isLegal(
        strategy == STRATEGY_EXACT || strategy == STRATEGY_EMBEDDED || strategy == STRATEGY_SUFFIX,
        "Wrong match strategy argument"); // $NON-NLS-1$
    fMatchStrategy = strategy;
  }

  /**
   * Sets the new name to rename the Java element to.
   *
   * @param name the non-empty new name to set
   */
  public void setNewName(final String name) {
    Assert.isNotNull(name);
    Assert.isLegal(!"".equals(name), "Name must not be empty"); // $NON-NLS-1$//$NON-NLS-2$
    fName = name;
  }

  /**
   * Sets the project name of this refactoring.
   *
   * <p>Note: If the Java element to be renamed is of type {@link IJavaElement#JAVA_PROJECT},
   * clients are required to to set the project name to <code>null</code>.
   *
   * <p>The default is to associate the refactoring with the workspace.
   *
   * @param project the non-empty project name to set, or <code>null</code> for the workspace
   * @see #getProject()
   */
  public void setProject(final String project) {
    super.setProject(project);
  }

  /**
   * Determines whether getter methods for the Java element should be renamed.
   *
   * <p>Note: Renaming of getter methods is applicable for {@link IField} elements which do not
   * represent enum constants only. The default is to not rename any getter methods.
   *
   * @param rename <code>true</code> to rename getter methods, <code>false</code> otherwise
   */
  public void setRenameGetters(final boolean rename) {
    fRenameGetter = rename;
  }

  /**
   * Determines whether setter methods for the Java element should be renamed.
   *
   * <p>Note: Renaming of setter methods is applicable for {@link IField} elements which do not
   * represent enum constants only. The default is to not rename any setter methods.
   *
   * @param rename <code>true</code> to rename setter methods, <code>false</code> otherwise
   */
  public void setRenameSetters(final boolean rename) {
    fRenameSetter = rename;
  }

  /**
   * Determines whether other Java elements in the hierarchy of the input element should be renamed
   * as well.
   *
   * <p>Note: Hierarchical updating is currently applicable for Java elements of type {@link
   * IPackageFragment}. The default is to not update Java elements hierarchically.
   *
   * @param update <code>true</code> to update hierarchically, <code>false</code> otherwise
   */
  public void setUpdateHierarchy(final boolean update) {
    fHierarchical = update;
  }

  /**
   * Determines whether qualified names of the Java element should be renamed.
   *
   * <p>Qualified name updating adapts fully qualified names of the Java element to be renamed in
   * non-Java text files. Clients may specify file name patterns by calling {@link
   * #setFileNamePatterns(String)} to constrain the set of text files to be processed.
   *
   * <p>Note: Qualified name updating is currently applicable to the Java elements {@link
   * IPackageFragment} and {@link IType}. The default is to not rename qualified names.
   *
   * @param update <code>true</code> to update qualified names, <code>false</code> otherwise
   */
  public void setUpdateQualifiedNames(final boolean update) {
    fQualified = update;
  }

  /**
   * Determines whether references to the Java element should be renamed.
   *
   * <p>Note: Reference updating is currently applicable to all Java element types except {@link
   * IPackageFragmentRoot}. The default is to not update references.
   *
   * @param update <code>true</code> to update references, <code>false</code> otherwise
   */
  public void setUpdateReferences(final boolean update) {
    fReferences = update;
  }

  /**
   * Determines whether similar declarations of the Java element should be updated.
   *
   * <p>Note: Similar declaration updating is currently applicable to Java elements of type {@link
   * IType}. The default is to not update similar declarations.
   *
   * @param update <code>true</code> to update similar declarations, <code>false</code> otherwise
   */
  public void setUpdateSimilarDeclarations(final boolean update) {
    fSimilarDeclarations = update;
  }

  /**
   * Determines whether textual occurrences of the Java element should be renamed.
   *
   * <p>Textual occurrence updating adapts textual occurrences of the Java element to be renamed in
   * Java comments and Java strings.
   *
   * <p>Note: Textual occurrence updating is currently applicable to the Java elements {@link
   * IPackageFragment}, {@link IType} and {@link IField}. The default is to not rename textual
   * occurrences.
   *
   * @param update <code>true</code> to update occurrences, <code>false</code> otherwise
   */
  public void setUpdateTextualOccurrences(final boolean update) {
    fTextual = update;
  }

  /** {@inheritDoc} */
  public RefactoringStatus validateDescriptor() {
    RefactoringStatus status = super.validateDescriptor();
    if (fName == null || "".equals(fName)) // $NON-NLS-1$
    status.merge(
          RefactoringStatus.createFatalErrorStatus(
              DescriptorMessages.RenameResourceDescriptor_no_new_name));
    if (fJavaElement == null)
      status.merge(
          RefactoringStatus.createFatalErrorStatus(
              DescriptorMessages.RenameJavaElementDescriptor_no_java_element));
    else {
      final int type = fJavaElement.getElementType();
      if (type == IJavaElement.JAVA_PROJECT && getProject() != null)
        status.merge(
            RefactoringStatus.createFatalErrorStatus(
                DescriptorMessages.RenameJavaElementDescriptor_project_constraint));
      if (type == IJavaElement.PACKAGE_FRAGMENT_ROOT && fReferences)
        status.merge(
            RefactoringStatus.createFatalErrorStatus(
                DescriptorMessages.RenameJavaElementDescriptor_reference_constraint));
      if (fTextual) {
        switch (type) {
          case IJavaElement.PACKAGE_FRAGMENT:
          case IJavaElement.TYPE:
          case IJavaElement.FIELD:
            break;
          default:
            status.merge(
                RefactoringStatus.createFatalErrorStatus(
                    DescriptorMessages.RenameJavaElementDescriptor_textual_constraint));
        }
      }
      if (fDeprecate) {
        switch (type) {
          case IJavaElement.METHOD:
          case IJavaElement.FIELD:
            break;
          default:
            status.merge(
                RefactoringStatus.createFatalErrorStatus(
                    DescriptorMessages.RenameJavaElementDescriptor_deprecation_constraint));
        }
      }
      if (fDelegate) {
        switch (type) {
          case IJavaElement.METHOD:
          case IJavaElement.FIELD:
            break;
          default:
            status.merge(
                RefactoringStatus.createFatalErrorStatus(
                    DescriptorMessages.RenameJavaElementDescriptor_delegate_constraint));
        }
      }
      if (fRenameGetter || fRenameSetter) {
        if (type != IJavaElement.FIELD)
          status.merge(
              RefactoringStatus.createFatalErrorStatus(
                  DescriptorMessages.RenameJavaElementDescriptor_accessor_constraint));
      }
      if (fQualified) {
        switch (type) {
          case IJavaElement.PACKAGE_FRAGMENT:
          case IJavaElement.TYPE:
            break;
          default:
            status.merge(
                RefactoringStatus.createFatalErrorStatus(
                    DescriptorMessages.RenameJavaElementDescriptor_qualified_constraint));
        }
      }
      if (fSimilarDeclarations) {
        switch (type) {
          case IJavaElement.TYPE:
            break;
          default:
            status.merge(
                RefactoringStatus.createFatalErrorStatus(
                    DescriptorMessages.RenameJavaElementDescriptor_similar_constraint));
        }
      }
      if (fHierarchical) {
        switch (type) {
          case IJavaElement.PACKAGE_FRAGMENT:
            break;
          default:
            status.merge(
                RefactoringStatus.createFatalErrorStatus(
                    DescriptorMessages.RenameJavaElementDescriptor_hierarchical_constraint));
        }
      }
    }
    return status;
  }
}
