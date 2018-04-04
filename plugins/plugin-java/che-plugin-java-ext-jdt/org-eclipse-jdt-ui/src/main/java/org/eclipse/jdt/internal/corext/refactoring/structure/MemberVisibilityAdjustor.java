/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.structure;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.corext.dom.ModifierRewrite;
import org.eclipse.jdt.internal.corext.dom.VariableDeclarationRewrite;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringScopeFactory;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringSearchEngine2;
import org.eclipse.jdt.internal.corext.refactoring.SearchResultGroup;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.util.LRUMap;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.corext.util.SearchUtils;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.ltk.core.refactoring.CategorizedTextEditGroup;
import org.eclipse.ltk.core.refactoring.GroupCategory;
import org.eclipse.ltk.core.refactoring.GroupCategorySet;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;

/**
 * Helper class to adjust the visibilities of members with respect to a reference element.
 *
 * @since 3.1
 */
public final class MemberVisibilityAdjustor {

  /**
   * The visibility group category set.
   *
   * @since 3.2
   */
  public static final GroupCategorySet SET_VISIBILITY_ADJUSTMENTS =
      new GroupCategorySet(
          new GroupCategory(
              "org.eclipse.jdt.internal.corext.visibilityAdjustments", // $NON-NLS-1$
              RefactoringCoreMessages.MemberVisibilityAdjustor_adjustments_name,
              RefactoringCoreMessages.MemberVisibilityAdjustor_adjustments_description));

  /** Description of a member visibility adjustment */
  public static class IncomingMemberVisibilityAdjustment implements IVisibilityAdjustment {

    /** The keyword representing the adjusted visibility */
    protected final ModifierKeyword fKeyword;

    /** The member whose visibility has been adjusted */
    protected final IMember fMember;

    /** Does the visibility adjustment need rewriting? */
    protected boolean fNeedsRewriting = true;

    /** The associated refactoring status */
    protected final RefactoringStatus fRefactoringStatus;

    /**
     * Creates a new incoming member visibility adjustment.
     *
     * @param member the member which is adjusted
     * @param keyword the keyword representing the adjusted visibility
     * @param status the associated status, or <code>null</code>
     */
    public IncomingMemberVisibilityAdjustment(
        final IMember member, final ModifierKeyword keyword, final RefactoringStatus status) {
      Assert.isNotNull(member);
      Assert.isTrue(!(member instanceof IInitializer));
      Assert.isTrue(isVisibilityKeyword(keyword));
      fMember = member;
      fKeyword = keyword;
      fRefactoringStatus = status;
    }

    /**
     * Returns the visibility keyword.
     *
     * @return the visibility keyword
     */
    public final ModifierKeyword getKeyword() {
      return fKeyword;
    }

    /**
     * Returns the adjusted member.
     *
     * @return the adjusted member
     */
    public final IMember getMember() {
      return fMember;
    }

    /**
     * Returns the associated refactoring status.
     *
     * @return the associated refactoring status
     */
    public final RefactoringStatus getStatus() {
      return fRefactoringStatus;
    }

    /**
     * Does the visibility adjustment need rewriting?
     *
     * @return <code>true</code> if it needs rewriting, <code>false</code> otherwise
     */
    public final boolean needsRewriting() {
      return fNeedsRewriting;
    }

    /**
     * Rewrites the visibility adjustment.
     *
     * @param adjustor the java element visibility adjustor
     * @param rewrite the AST rewrite to use
     * @param root the root of the AST used in the rewrite
     * @param group the text edit group description to use, or <code>null</code>
     * @param status the refactoring status, or <code>null</code>
     * @throws JavaModelException if an error occurs
     */
    protected final void rewriteVisibility(
        final MemberVisibilityAdjustor adjustor,
        final ASTRewrite rewrite,
        final CompilationUnit root,
        final CategorizedTextEditGroup group,
        final RefactoringStatus status)
        throws JavaModelException {
      Assert.isNotNull(adjustor);
      Assert.isNotNull(rewrite);
      Assert.isNotNull(root);
      final int visibility = fKeyword != null ? fKeyword.toFlagValue() : Modifier.NONE;
      if (fMember instanceof IField && !Flags.isEnum(fMember.getFlags())) {
        final VariableDeclarationFragment fragment =
            ASTNodeSearchUtil.getFieldDeclarationFragmentNode((IField) fMember, root);
        final FieldDeclaration declaration = (FieldDeclaration) fragment.getParent();
        VariableDeclarationFragment[] fragmentsToChange =
            new VariableDeclarationFragment[] {fragment};
        VariableDeclarationRewrite.rewriteModifiers(
            declaration,
            fragmentsToChange,
            visibility,
            ModifierRewrite.VISIBILITY_MODIFIERS,
            rewrite,
            group);
        if (status != null) adjustor.fStatus.merge(status);
      } else if (fMember != null) {
        final BodyDeclaration declaration = ASTNodeSearchUtil.getBodyDeclarationNode(fMember, root);
        if (declaration != null) {
          ModifierRewrite.create(rewrite, declaration).setVisibility(visibility, group);
          if (status != null) adjustor.fStatus.merge(status);
        }
      }
    }

    /*
     * @see org.eclipse.jdt.internal.corext.refactoring.structure.MemberVisibilityAdjustor.IVisibilityAdjustment#rewriteVisibility(org
     * .eclipse.jdt.internal.corext.refactoring.structure.MemberVisibilityAdjustor, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void rewriteVisibility(
        final MemberVisibilityAdjustor adjustor, final IProgressMonitor monitor)
        throws JavaModelException {
      Assert.isNotNull(adjustor);
      Assert.isNotNull(monitor);
      try {
        monitor.beginTask("", 1); // $NON-NLS-1$
        monitor.setTaskName(RefactoringCoreMessages.MemberVisibilityAdjustor_adjusting);
        if (fNeedsRewriting) {
          if (adjustor.fRewrite != null && adjustor.fRoot != null)
            rewriteVisibility(
                adjustor, adjustor.fRewrite, adjustor.fRoot, null, fRefactoringStatus);
          else {
            final CompilationUnitRewrite rewrite =
                adjustor.getCompilationUnitRewrite(fMember.getCompilationUnit());
            rewriteVisibility(
                adjustor,
                rewrite.getASTRewrite(),
                rewrite.getRoot(),
                rewrite.createCategorizedGroupDescription(
                    Messages.format(
                        RefactoringCoreMessages.MemberVisibilityAdjustor_change_visibility,
                        getLabel(getKeyword())),
                    SET_VISIBILITY_ADJUSTMENTS),
                fRefactoringStatus);
          }
        } else if (fRefactoringStatus != null) adjustor.fStatus.merge(fRefactoringStatus);
        monitor.worked(1);
      } finally {
        monitor.done();
      }
    }

    /**
     * Determines whether the visibility adjustment needs rewriting.
     *
     * @param rewriting <code>true</code> if it needs rewriting, <code>false</code> otherwise
     */
    public final void setNeedsRewriting(final boolean rewriting) {
      fNeedsRewriting = rewriting;
    }
  }

  /** Interface for visibility adjustments */
  public interface IVisibilityAdjustment {

    /**
     * Rewrites the visibility adjustment.
     *
     * @param adjustor the java element visibility adjustor
     * @param monitor the progress monitor to use
     * @throws JavaModelException if an error occurs
     */
    public void rewriteVisibility(MemberVisibilityAdjustor adjustor, IProgressMonitor monitor)
        throws JavaModelException;
  }

  /** Description of an outgoing member visibility adjustment */
  public static class OutgoingMemberVisibilityAdjustment
      extends IncomingMemberVisibilityAdjustment {

    /**
     * Creates a new outgoing member visibility adjustment.
     *
     * @param member the member which is adjusted
     * @param keyword the keyword representing the adjusted visibility
     * @param status the associated status
     */
    public OutgoingMemberVisibilityAdjustment(
        final IMember member, final ModifierKeyword keyword, final RefactoringStatus status) {
      super(member, keyword, status);
    }

    /*
     * @see org.eclipse.jdt.internal.corext.refactoring.structure.MemberVisibilityAdjustor.IVisibilityAdjustment#rewriteVisibility(org.eclipse.jdt.internal.corext.refactoring.structure.MemberVisibilityAdjustor, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void rewriteVisibility(
        final MemberVisibilityAdjustor adjustor, final IProgressMonitor monitor)
        throws JavaModelException {
      Assert.isNotNull(adjustor);
      Assert.isNotNull(monitor);
      try {
        monitor.beginTask("", 1); // $NON-NLS-1$
        monitor.setTaskName(RefactoringCoreMessages.MemberVisibilityAdjustor_adjusting);
        if (fNeedsRewriting) {
          final CompilationUnitRewrite rewrite =
              adjustor.getCompilationUnitRewrite(fMember.getCompilationUnit());
          rewriteVisibility(
              adjustor,
              rewrite.getASTRewrite(),
              rewrite.getRoot(),
              rewrite.createCategorizedGroupDescription(
                  Messages.format(
                      RefactoringCoreMessages.MemberVisibilityAdjustor_change_visibility,
                      getLabel(getKeyword())),
                  SET_VISIBILITY_ADJUSTMENTS),
              fRefactoringStatus);
        }
        monitor.worked(1);
      } finally {
        monitor.done();
      }
    }
  }

  /**
   * Returns the label for the specified java element.
   *
   * @param element the element to get the label for
   * @return the label for the element
   */
  public static String getLabel(final IJavaElement element) {
    Assert.isNotNull(element);
    return JavaElementLabels.getElementLabel(
        element, JavaElementLabels.ALL_FULLY_QUALIFIED | JavaElementLabels.ALL_DEFAULT);
  }

  /**
   * Returns the label for the specified visibility keyword.
   *
   * @param keyword the keyword to get the label for, or <code>null</code> for default visibility
   * @return the label for the keyword
   */
  public static String getLabel(final ModifierKeyword keyword) {
    Assert.isTrue(isVisibilityKeyword(keyword));
    if (keyword == null)
      return RefactoringCoreMessages.MemberVisibilityAdjustor_change_visibility_default;
    else if (ModifierKeyword.PUBLIC_KEYWORD.equals(keyword))
      return RefactoringCoreMessages.MemberVisibilityAdjustor_change_visibility_public;
    else if (ModifierKeyword.PROTECTED_KEYWORD.equals(keyword))
      return RefactoringCoreMessages.MemberVisibilityAdjustor_change_visibility_protected;
    else return RefactoringCoreMessages.MemberVisibilityAdjustor_change_visibility_private;
  }

  /**
   * Returns the message string for the specified member.
   *
   * @param member the member to get the string for
   * @return the string for the member
   */
  public static String getMessage(final IMember member) {
    Assert.isTrue(member instanceof IType || member instanceof IMethod || member instanceof IField);
    if (member instanceof IType)
      return RefactoringCoreMessages.MemberVisibilityAdjustor_change_visibility_type_warning;
    else if (member instanceof IMethod)
      return RefactoringCoreMessages.MemberVisibilityAdjustor_change_visibility_method_warning;
    else return RefactoringCoreMessages.MemberVisibilityAdjustor_change_visibility_field_warning;
  }

  /**
   * Do the specified modifiers represent a lower visibility than the required threshold?
   *
   * @param modifiers the modifiers to test
   * @param threshold the visibility threshold to compare with
   * @return <code>true</code> if the visibility is lower than required, <code>false</code>
   *     otherwise
   */
  public static boolean hasLowerVisibility(final int modifiers, final int threshold) {
    if (Modifier.isPrivate(threshold)) return false;
    else if (Modifier.isPublic(threshold)) return !Modifier.isPublic(modifiers);
    else if (Modifier.isProtected(threshold))
      return !Modifier.isProtected(modifiers) && !Modifier.isPublic(modifiers);
    else return Modifier.isPrivate(modifiers);
  }

  /**
   * Does the specified modifier keyword represent a lower visibility than the required threshold?
   *
   * @param keyword the visibility keyword to test, or <code>null</code> for default visibility
   * @param threshold the visibility threshold keyword to compare with, or <code>null</code> to
   *     compare with default visibility
   * @return <code>true</code> if the visibility is lower than required, <code>false</code>
   *     otherwise
   */
  public static boolean hasLowerVisibility(
      final ModifierKeyword keyword, final ModifierKeyword threshold) {
    Assert.isTrue(isVisibilityKeyword(keyword));
    Assert.isTrue(isVisibilityKeyword(threshold));
    return hasLowerVisibility(
        keyword != null ? keyword.toFlagValue() : Modifier.NONE,
        threshold != null ? threshold.toFlagValue() : Modifier.NONE);
  }

  /**
   * Is the specified severity a refactoring status severity?
   *
   * @param severity the severity to test
   * @return <code>true</code> if it is a refactoring status severity, <code>false</code> otherwise
   */
  private static boolean isStatusSeverity(final int severity) {
    return severity == RefactoringStatus.ERROR
        || severity == RefactoringStatus.FATAL
        || severity == RefactoringStatus.INFO
        || severity == RefactoringStatus.OK
        || severity == RefactoringStatus.WARNING;
  }

  /**
   * Is the specified modifier keyword a visibility keyword?
   *
   * @param keyword the keyword to test, or <code>null</code>
   * @return <code>true</code> if it is a visibility keyword, <code>false</code> otherwise
   */
  private static boolean isVisibilityKeyword(final ModifierKeyword keyword) {
    return keyword == null
        || ModifierKeyword.PUBLIC_KEYWORD.equals(keyword)
        || ModifierKeyword.PROTECTED_KEYWORD.equals(keyword)
        || ModifierKeyword.PRIVATE_KEYWORD.equals(keyword);
  }

  /**
   * Is the specified modifier a visibility modifier?
   *
   * @param modifier the keyword to test
   * @return <code>true</code> if it is a visibility modifier, <code>false</code> otherwise
   */
  private static boolean isVisibilityModifier(final int modifier) {
    return modifier == Modifier.NONE
        || modifier == Modifier.PUBLIC
        || modifier == Modifier.PROTECTED
        || modifier == Modifier.PRIVATE;
  }

  /**
   * Converts a given modifier keyword into a visibility flag.
   *
   * @param keyword the keyword to convert
   * @return the visibility flag
   */
  private static int keywordToVisibility(final ModifierKeyword keyword) {
    int visibility = 0;
    if (keyword == ModifierKeyword.PUBLIC_KEYWORD) visibility = Flags.AccPublic;
    else if (keyword == ModifierKeyword.PRIVATE_KEYWORD) visibility = Flags.AccPrivate;
    else if (keyword == ModifierKeyword.PROTECTED_KEYWORD) visibility = Flags.AccProtected;
    return visibility;
  }

  /**
   * Does the specified member need further visibility adjustment?
   *
   * @param member the member to test
   * @param threshold the visibility threshold to test for
   * @param adjustments the map of members to visibility adjustments
   * @return <code>true</code> if the member needs further adjustment, <code>false</code> otherwise
   */
  public static boolean needsVisibilityAdjustments(
      final IMember member,
      final int threshold,
      final Map<IMember, IncomingMemberVisibilityAdjustment> adjustments) {
    Assert.isNotNull(member);
    Assert.isTrue(isVisibilityModifier(threshold));
    Assert.isNotNull(adjustments);
    final IncomingMemberVisibilityAdjustment adjustment = adjustments.get(member);
    if (adjustment != null) {
      final ModifierKeyword keyword = adjustment.getKeyword();
      return hasLowerVisibility(keyword == null ? Modifier.NONE : keyword.toFlagValue(), threshold);
    }
    return true;
  }

  /**
   * Does the specified member need further visibility adjustment?
   *
   * @param member the member to test
   * @param threshold the visibility threshold to test for, or <code>null</code> for default
   *     visibility
   * @param adjustments the map of members to visibility adjustments
   * @return <code>true</code> if the member needs further adjustment, <code>false</code> otherwise
   */
  public static boolean needsVisibilityAdjustments(
      final IMember member,
      final ModifierKeyword threshold,
      final Map<IMember, IncomingMemberVisibilityAdjustment> adjustments) {
    Assert.isNotNull(member);
    Assert.isNotNull(adjustments);
    final IncomingMemberVisibilityAdjustment adjustment = adjustments.get(member);
    if (adjustment != null) return hasLowerVisibility(adjustment.getKeyword(), threshold);
    return true;
  }

  /** The map of members to visibility adjustments */
  private Map<IMember, IncomingMemberVisibilityAdjustment> fAdjustments =
      new LinkedHashMap<
          IMember,
          IncomingMemberVisibilityAdjustment>(); // LinkedHashMap to preserve order of generated
  // warnings

  /** Should incoming references be adjusted? */
  private boolean fIncoming = true;

  /** Should outgoing references be adjusted? */
  private boolean fOutgoing = true;

  /** The referenced element causing the visibility adjustment */
  private final IMember fReferenced;

  /** The referencing java element */
  private final IJavaElement fReferencing;

  /**
   * The AST rewrite to use for reference visibility adjustments, or <code>null</code> to use a
   * compilation unit rewrite
   */
  private ASTRewrite fRewrite = null;

  /** The map of compilation units to compilation unit rewrites */
  private Map<ICompilationUnit, CompilationUnitRewrite> fRewrites =
      new HashMap<ICompilationUnit, CompilationUnitRewrite>(3);

  /**
   * The root node of the AST rewrite for reference visibility adjustments, or <code>null</code> to
   * use a compilation unit rewrite
   */
  private CompilationUnit fRoot = null;

  /** The incoming search scope */
  private IJavaSearchScope fScope;

  /** The status of the visibility adjustment */
  private RefactoringStatus fStatus = new RefactoringStatus();

  /** The type hierarchy cache */
  private final Map<IType, ITypeHierarchy> fTypeHierarchies = new LRUMap<IType, ITypeHierarchy>(10);

  /** The visibility message severity */
  private int fVisibilitySeverity = RefactoringStatus.WARNING;

  /** The working copy owner, or <code>null</code> to use none */
  private WorkingCopyOwner fOwner = null;

  /**
   * Creates a new java element visibility adjustor.
   *
   * @param referencing the referencing element used to compute the visibility
   * @param referenced the referenced member which causes the visibility changes
   */
  public MemberVisibilityAdjustor(final IJavaElement referencing, final IMember referenced) {
    Assert.isTrue(!(referenced instanceof IInitializer));
    Assert.isTrue(
        referencing instanceof ICompilationUnit
            || referencing instanceof IType
            || referencing instanceof IPackageFragment);
    fScope =
        RefactoringScopeFactory.createReferencedScope(
            new IJavaElement[] {referenced},
            IJavaSearchScope.REFERENCED_PROJECTS
                | IJavaSearchScope.SOURCES
                | IJavaSearchScope.APPLICATION_LIBRARIES);
    fReferencing = referencing;
    fReferenced = referenced;
  }

  /**
   * Adjusts the visibility of the specified member.
   *
   * @param element the "source" point from which to calculate the visibility
   * @param referencedMovedElement the moved element which may be adjusted in visibility
   * @param monitor the progress monitor to use
   * @throws JavaModelException if the visibility adjustment could not be computed
   */
  private void adjustIncomingVisibility(
      final IJavaElement element, IMember referencedMovedElement, final IProgressMonitor monitor)
      throws JavaModelException {
    final ModifierKeyword threshold =
        getVisibilityThreshold(element, referencedMovedElement, monitor);
    int flags = referencedMovedElement.getFlags();
    IType declaring = referencedMovedElement.getDeclaringType();
    if (declaring != null && declaring.isInterface()
        || referencedMovedElement instanceof IField
            && Flags.isEnum(referencedMovedElement.getFlags())) return;
    if (hasLowerVisibility(flags, threshold == null ? Modifier.NONE : threshold.toFlagValue())
        && needsVisibilityAdjustment(referencedMovedElement, threshold))
      fAdjustments.put(
          referencedMovedElement,
          new IncomingMemberVisibilityAdjustment(
              referencedMovedElement,
              threshold,
              RefactoringStatus.createStatus(
                  fVisibilitySeverity,
                  Messages.format(
                      getMessage(referencedMovedElement),
                      new String[] {getLabel(referencedMovedElement), getLabel(threshold)}),
                  JavaStatusContext.create(referencedMovedElement),
                  null,
                  RefactoringStatusEntry.NO_CODE,
                  null)));
  }

  /**
   * Check whether anyone accesses the members of the moved type from the outside. Those may need to
   * have their visibility adjusted.
   *
   * @param member the member
   * @param monitor the progress monitor to use
   * @throws JavaModelException if an error occurs
   */
  private void adjustMemberVisibility(final IMember member, final IProgressMonitor monitor)
      throws JavaModelException {

    if (member instanceof IType) {
      // recursively check accessibility of member type's members
      final IJavaElement[] typeMembers = ((IType) member).getChildren();
      for (int i = 0; i < typeMembers.length; i++) {
        if (!(typeMembers[i] instanceof IInitializer))
          adjustMemberVisibility((IMember) typeMembers[i], monitor);
      }
    }

    if (member.equals(fReferenced) || Modifier.isPublic(member.getFlags())) return;

    final SearchResultGroup[] references = findReferences(member, monitor);
    for (int i = 0; i < references.length; i++) {
      final SearchMatch[] searchResults = references[i].getSearchResults();
      for (int k = 0; k < searchResults.length; k++) {
        final IJavaElement referenceToMember = (IJavaElement) searchResults[k].getElement();
        if (fAdjustments.get(member) == null
            && referenceToMember instanceof IMember
            && !isInsideMovedMember(referenceToMember)) {
          // check whether the member is still visible from the
          // destination. As we are moving a type, the destination is
          // a package or another type.
          adjustIncomingVisibility(fReferencing, member, new SubProgressMonitor(monitor, 1));
        }
      }
    }
  }

  /**
   * Is the specified member inside the moved member?
   *
   * @param element the element
   * @return <code>true</code> if it is inside, <code>false</code> otherwise
   */
  private boolean isInsideMovedMember(final IJavaElement element) {
    IJavaElement current = element;
    while ((current = current.getParent()) != null) if (current.equals(fReferenced)) return true;
    return false;
  }

  /**
   * Finds references to the specified member.
   *
   * @param member the member
   * @param monitor the progress monitor to use
   * @return the search result groups
   * @throws JavaModelException if an error occurs during search
   */
  private SearchResultGroup[] findReferences(final IMember member, final IProgressMonitor monitor)
      throws JavaModelException {
    final RefactoringSearchEngine2 engine =
        new RefactoringSearchEngine2(
            SearchPattern.createPattern(
                member, IJavaSearchConstants.REFERENCES, SearchUtils.GENERICS_AGNOSTIC_MATCH_RULE));
    engine.setOwner(fOwner);
    engine.setFiltering(true, true);
    engine.setScope(RefactoringScopeFactory.create(member));
    engine.searchPattern(new SubProgressMonitor(monitor, 1));
    return (SearchResultGroup[]) engine.getResults();
  }

  /**
   * Adjusts the visibility of the member based on the incoming references represented by the
   * specified search result groups.
   *
   * <p>If there is at least one reference to the moved element from outside the moved element,
   * visibility must be increased such that the moved element (fReferenced) is still visible at the
   * target from all references. This effectively means that the old element (fReferenced) must be
   * visible from the new location (fReferencing).
   *
   * @param groups the search result groups representing the references
   * @param monitor the progress monitor to use
   * @throws JavaModelException if the java elements could not be accessed
   */
  private void adjustIncomingVisibility(
      final SearchResultGroup[] groups, final IProgressMonitor monitor) throws JavaModelException {
    try {
      monitor.beginTask("", groups.length); // $NON-NLS-1$
      monitor.setTaskName(RefactoringCoreMessages.MemberVisibilityAdjustor_checking);
      SearchMatch[] matches = null;
      boolean adjusted = false;
      for (int index = 0; index < groups.length; index++) {
        matches = groups[index].getSearchResults();
        for (int offset = 0; offset < matches.length; offset++) {
          final Object element = matches[offset].getElement();
          if (element instanceof IMember && !isInsideMovedMember((IMember) element)) {
            // found one reference which is not inside the moved
            // element => adjust visibility of the moved element
            adjustIncomingVisibility(fReferencing, fReferenced, monitor);
            adjusted = true; // one adjustment is enough
            break;
          }
        }
        if (adjusted) break;
        monitor.worked(1);
      }
    } finally {
      monitor.done();
    }
  }

  /**
   * Adjusts the visibility of the referenced field found in a compilation unit.
   *
   * @param field the referenced field to adjust
   * @param threshold the visibility threshold, or <code>null</code> for default visibility
   * @throws JavaModelException if an error occurs
   */
  private void adjustOutgoingVisibility(final IField field, final ModifierKeyword threshold)
      throws JavaModelException {
    Assert.isTrue(!field.isBinary() && !field.isReadOnly());
    // bug 100555 (moving inner class to top level class; taking private fields with you)
    final IType declaring = field.getDeclaringType();
    if (declaring != null && declaring.equals(fReferenced)) return;
    if (hasLowerVisibility(field.getFlags(), keywordToVisibility(threshold))
        && needsVisibilityAdjustment(field, threshold))
      adjustOutgoingVisibility(
          field,
          threshold,
          RefactoringCoreMessages.MemberVisibilityAdjustor_change_visibility_field_warning);
  }

  /**
   * Adjusts the visibility of the referenced body declaration.
   *
   * @param member the member where to adjust the visibility
   * @param threshold the visibility keyword representing the required visibility, or <code>null
   *     </code> for default visibility
   * @param template the message template to use
   * @throws JavaModelException if an error occurs
   */
  private void adjustOutgoingVisibility(
      final IMember member, final ModifierKeyword threshold, final String template)
      throws JavaModelException {
    Assert.isTrue(!member.isBinary() && !member.isReadOnly());
    boolean adjust = true;
    final IType declaring = member.getDeclaringType();
    if (declaring != null
        && (JavaModelUtil.isInterfaceOrAnnotation(declaring)
            || (member instanceof IField) && Flags.isEnum(member.getFlags())
            || declaring.equals(fReferenced))) adjust = false;
    if (adjust
        && hasLowerVisibility(member.getFlags(), keywordToVisibility(threshold))
        && needsVisibilityAdjustment(member, threshold))
      fAdjustments.put(
          member,
          new OutgoingMemberVisibilityAdjustment(
              member,
              threshold,
              RefactoringStatus.createStatus(
                  fVisibilitySeverity,
                  Messages.format(
                      template,
                      new String[] {
                        JavaElementLabels.getTextLabel(
                            member,
                            JavaElementLabels.M_PARAMETER_TYPES
                                | JavaElementLabels.ALL_FULLY_QUALIFIED),
                        getLabel(threshold)
                      }),
                  JavaStatusContext.create(member),
                  null,
                  RefactoringStatusEntry.NO_CODE,
                  null)));
  }

  /**
   * Adjusts the visibilities of the referenced element from the search match found in a compilation
   * unit.
   *
   * @param match the search match representing the element declaration
   * @param monitor the progress monitor to use
   * @throws JavaModelException if the visibility could not be determined
   */
  private void adjustOutgoingVisibility(final SearchMatch match, final IProgressMonitor monitor)
      throws JavaModelException {
    final Object element = match.getElement();
    if (element instanceof IMember) {
      final IMember member = (IMember) element;
      if (!member.isBinary() && !member.isReadOnly() && !isInsideMovedMember(member)) {
        adjustOutgoingVisibilityChain(member, monitor);
      }
    }
  }

  private void adjustOutgoingVisibilityChain(final IMember member, final IProgressMonitor monitor)
      throws JavaModelException {

    if (!Modifier.isPublic(member.getFlags())) {
      final ModifierKeyword threshold =
          computeOutgoingVisibilityThreshold(fReferencing, member, monitor);
      if (member instanceof IMethod) {
        adjustOutgoingVisibility(
            member,
            threshold,
            RefactoringCoreMessages.MemberVisibilityAdjustor_change_visibility_method_warning);
      } else if (member instanceof IField) {
        adjustOutgoingVisibility((IField) member, threshold);
      } else if (member instanceof IType) {
        adjustOutgoingVisibility(
            member,
            threshold,
            RefactoringCoreMessages.MemberVisibilityAdjustor_change_visibility_type_warning);
      }
    }

    if (member.getDeclaringType() != null)
      adjustOutgoingVisibilityChain(member.getDeclaringType(), monitor);
  }

  /**
   * Adjusts the visibilities of the outgoing references from the member represented by the
   * specified search result groups.
   *
   * @param groups the search result groups representing the references
   * @param monitor the progress monitor to us
   * @throws JavaModelException if the visibility could not be determined
   */
  private void adjustOutgoingVisibility(
      final SearchResultGroup[] groups, final IProgressMonitor monitor) throws JavaModelException {
    try {
      monitor.beginTask("", groups.length); // $NON-NLS-1$
      monitor.setTaskName(RefactoringCoreMessages.MemberVisibilityAdjustor_checking);
      IJavaElement element = null;
      SearchMatch[] matches = null;
      SearchResultGroup group = null;
      for (int index = 0; index < groups.length; index++) {
        group = groups[index];
        element = JavaCore.create(group.getResource());
        if (element instanceof ICompilationUnit) {
          matches = group.getSearchResults();
          for (int offset = 0; offset < matches.length; offset++)
            adjustOutgoingVisibility(matches[offset], new SubProgressMonitor(monitor, 1));
        } // else if (element != null)
        // fStatus.merge(RefactoringStatus.createStatus(fFailureSeverity,
        // RefactoringCoreMessages.getFormattedString
        // ("MemberVisibilityAdjustor.binary.outgoing.project", new String[] {
        // element.getJavaProject().getElementName(), getLabel
        // (fReferenced)}), null, null, RefactoringStatusEntry.NO_CODE, null)); //$NON-NLS-1$
        // else if (group.getResource() != null)
        // fStatus.merge(RefactoringStatus.createStatus(fFailureSeverity,
        // RefactoringCoreMessages.getFormattedString
        // ("MemberVisibilityAdjustor.binary.outgoing.resource", new String[] {
        // group.getResource().getName(), getLabel
        // (fReferenced)}), null, null, RefactoringStatusEntry.NO_CODE, null)); //$NON-NLS-1$

        // TW: enable when bug 78387 is fixed

        monitor.worked(1);
      }
    } finally {
      monitor.done();
    }
  }

  /**
   * Adjusts the visibilities of the referenced and referencing elements.
   *
   * @param monitor the progress monitor to use
   * @throws JavaModelException if an error occurs during search
   */
  public final void adjustVisibility(final IProgressMonitor monitor) throws JavaModelException {
    try {
      monitor.beginTask("", 7); // $NON-NLS-1$
      monitor.setTaskName(RefactoringCoreMessages.MemberVisibilityAdjustor_checking);
      final RefactoringSearchEngine2 engine =
          new RefactoringSearchEngine2(
              SearchPattern.createPattern(
                  fReferenced,
                  IJavaSearchConstants.REFERENCES,
                  SearchUtils.GENERICS_AGNOSTIC_MATCH_RULE));
      engine.setScope(fScope);
      engine.setStatus(fStatus);
      engine.setOwner(fOwner);
      if (fIncoming) {
        // check calls to the referenced (moved) element, adjust element
        // visibility if necessary.
        engine.searchPattern(new SubProgressMonitor(monitor, 1));
        adjustIncomingVisibility(
            (SearchResultGroup[]) engine.getResults(), new SubProgressMonitor(monitor, 1));
        engine.clearResults();
        // If the moved element is a type: Adjust visibility of members
        // of the type if they are accessed outside of the moved type
        if (fReferenced instanceof IType) {
          final IType type = (IType) fReferenced;
          adjustMemberVisibility(type, new SubProgressMonitor(monitor, 1));
        }
      }
      if (fOutgoing) {
        /*
         * Search for the types, fields, and methods which
         * are called/acted upon inside the referenced element (the one
         * to be moved) and assure that they are also visible from
         * within the referencing element (the destination type (or
         * package if move to new type)).
         */
        engine.searchReferencedTypes(
            fReferenced,
            new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
        engine.searchReferencedFields(
            fReferenced,
            new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
        engine.searchReferencedMethods(
            fReferenced,
            new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
        adjustOutgoingVisibility(
            (SearchResultGroup[]) engine.getResults(), new SubProgressMonitor(monitor, 1));
      }
    } finally {
      monitor.done();
    }
  }

  /**
   * Computes the visibility threshold for the referenced element.
   *
   * @param referencing the referencing element
   * @param referenced the referenced element
   * @param monitor the progress monitor to use
   * @return the visibility keyword corresponding to the threshold, or <code>null</code> for default
   *     visibility
   * @throws JavaModelException if the java elements could not be accessed
   */
  public ModifierKeyword getVisibilityThreshold(
      final IJavaElement referencing, final IMember referenced, final IProgressMonitor monitor)
      throws JavaModelException {
    Assert.isTrue(!(referencing instanceof IInitializer));
    Assert.isTrue(!(referenced instanceof IInitializer));
    ModifierKeyword keyword = ModifierKeyword.PUBLIC_KEYWORD;
    try {
      monitor.beginTask("", 1); // $NON-NLS-1$
      monitor.setTaskName(RefactoringCoreMessages.MemberVisibilityAdjustor_checking);
      final int referencingType = referencing.getElementType();
      final int referencedType = referenced.getElementType();
      switch (referencedType) {
        case IJavaElement.TYPE:
          {
            final IType typeReferenced = (IType) referenced;
            final ICompilationUnit referencedUnit = typeReferenced.getCompilationUnit();
            switch (referencingType) {
              case IJavaElement.TYPE:
                {
                  keyword = thresholdTypeToType((IType) referencing, typeReferenced, monitor);
                  break;
                }
              case IJavaElement.FIELD:
              case IJavaElement.METHOD:
                {
                  final IMember member = (IMember) referencing;
                  if (typeReferenced.equals(member.getDeclaringType()))
                    keyword = ModifierKeyword.PRIVATE_KEYWORD;
                  else if (referencedUnit != null
                      && referencedUnit.equals(member.getCompilationUnit()))
                    keyword = ModifierKeyword.PRIVATE_KEYWORD;
                  else if (typeReferenced
                      .getPackageFragment()
                      .equals(member.getDeclaringType().getPackageFragment())) keyword = null;
                  break;
                }
              case IJavaElement.PACKAGE_FRAGMENT:
                {
                  final IPackageFragment fragment = (IPackageFragment) referencing;
                  if (typeReferenced.getPackageFragment().equals(fragment)) keyword = null;
                  break;
                }
              default:
                Assert.isTrue(false);
            }
            break;
          }
        case IJavaElement.FIELD:
          {
            final IField fieldReferenced = (IField) referenced;
            final ICompilationUnit referencedUnit = fieldReferenced.getCompilationUnit();
            switch (referencingType) {
              case IJavaElement.TYPE:
                {
                  keyword = thresholdTypeToField((IType) referencing, fieldReferenced, monitor);
                  break;
                }
              case IJavaElement.FIELD:
              case IJavaElement.METHOD:
                {
                  final IMember member = (IMember) referencing;
                  if (fieldReferenced.getDeclaringType().equals(member.getDeclaringType()))
                    keyword = ModifierKeyword.PRIVATE_KEYWORD;
                  else if (referencedUnit != null
                      && referencedUnit.equals(member.getCompilationUnit()))
                    keyword = ModifierKeyword.PRIVATE_KEYWORD;
                  else if (fieldReferenced
                      .getDeclaringType()
                      .getPackageFragment()
                      .equals(member.getDeclaringType().getPackageFragment())) keyword = null;
                  break;
                }
              case IJavaElement.PACKAGE_FRAGMENT:
                {
                  final IPackageFragment fragment = (IPackageFragment) referencing;
                  if (fieldReferenced.getDeclaringType().getPackageFragment().equals(fragment))
                    keyword = null;
                  break;
                }
              default:
                Assert.isTrue(false);
            }
            break;
          }
        case IJavaElement.METHOD:
          {
            final IMethod methodReferenced = (IMethod) referenced;
            final ICompilationUnit referencedUnit = methodReferenced.getCompilationUnit();
            switch (referencingType) {
              case IJavaElement.TYPE:
                {
                  keyword = thresholdTypeToMethod((IType) referencing, methodReferenced, monitor);
                  break;
                }
              case IJavaElement.FIELD:
              case IJavaElement.METHOD:
                {
                  final IMember member = (IMember) referencing;
                  if (methodReferenced.getDeclaringType().equals(member.getDeclaringType()))
                    keyword = ModifierKeyword.PRIVATE_KEYWORD;
                  else if (referencedUnit != null
                      && referencedUnit.equals(member.getCompilationUnit()))
                    keyword = ModifierKeyword.PRIVATE_KEYWORD;
                  else if (methodReferenced
                      .getDeclaringType()
                      .getPackageFragment()
                      .equals(member.getDeclaringType().getPackageFragment())) keyword = null;
                  break;
                }
              case IJavaElement.PACKAGE_FRAGMENT:
                {
                  final IPackageFragment fragment = (IPackageFragment) referencing;
                  if (methodReferenced.getDeclaringType().getPackageFragment().equals(fragment))
                    keyword = null;
                  break;
                }
              default:
                Assert.isTrue(false);
            }
            break;
          }
        default:
          Assert.isTrue(false);
      }
    } finally {
      monitor.done();
    }
    return keyword;
  }

  /**
   * Computes the visibility threshold for the referenced element.
   *
   * @param referencing the referencing element
   * @param referenced the referenced element
   * @param monitor the progress monitor to use
   * @return the visibility keyword corresponding to the threshold, or <code>null</code> for default
   *     visibility
   * @throws JavaModelException if the java elements could not be accessed
   */
  private ModifierKeyword computeOutgoingVisibilityThreshold(
      final IJavaElement referencing, final IMember referenced, final IProgressMonitor monitor)
      throws JavaModelException {
    Assert.isTrue(
        referencing instanceof ICompilationUnit
            || referencing instanceof IType
            || referencing instanceof IPackageFragment);
    Assert.isTrue(
        referenced instanceof IType
            || referenced instanceof IField
            || referenced instanceof IMethod);
    ModifierKeyword keyword = ModifierKeyword.PUBLIC_KEYWORD;
    try {
      monitor.beginTask("", 1); // $NON-NLS-1$
      monitor.setTaskName(RefactoringCoreMessages.MemberVisibilityAdjustor_checking);
      final int referencingType = referencing.getElementType();
      final int referencedType = referenced.getElementType();
      switch (referencedType) {
        case IJavaElement.TYPE:
          {
            final IType typeReferenced = (IType) referenced;
            switch (referencingType) {
              case IJavaElement.COMPILATION_UNIT:
                {
                  final ICompilationUnit unit = (ICompilationUnit) referencing;
                  final ICompilationUnit referencedUnit = typeReferenced.getCompilationUnit();
                  if (referencedUnit != null && referencedUnit.equals(unit))
                    keyword = ModifierKeyword.PRIVATE_KEYWORD;
                  else if (referencedUnit != null
                      && referencedUnit.getParent().equals(unit.getParent())) keyword = null;
                  break;
                }
              case IJavaElement.TYPE:
                {
                  keyword = thresholdTypeToType((IType) referencing, typeReferenced, monitor);
                  break;
                }
              case IJavaElement.PACKAGE_FRAGMENT:
                {
                  final IPackageFragment fragment = (IPackageFragment) referencing;
                  if (typeReferenced.getPackageFragment().equals(fragment)) keyword = null;
                  break;
                }
              default:
                Assert.isTrue(false);
            }
            break;
          }
        case IJavaElement.FIELD:
          {
            final IField fieldReferenced = (IField) referenced;
            final ICompilationUnit referencedUnit = fieldReferenced.getCompilationUnit();
            switch (referencingType) {
              case IJavaElement.COMPILATION_UNIT:
                {
                  final ICompilationUnit unit = (ICompilationUnit) referencing;
                  if (referencedUnit != null && referencedUnit.equals(unit))
                    keyword = ModifierKeyword.PRIVATE_KEYWORD;
                  else if (referencedUnit != null
                      && referencedUnit.getParent().equals(unit.getParent())) keyword = null;
                  break;
                }
              case IJavaElement.TYPE:
                {
                  keyword = thresholdTypeToField((IType) referencing, fieldReferenced, monitor);
                  break;
                }
              case IJavaElement.PACKAGE_FRAGMENT:
                {
                  final IPackageFragment fragment = (IPackageFragment) referencing;
                  if (fieldReferenced.getDeclaringType().getPackageFragment().equals(fragment))
                    keyword = null;
                  break;
                }
              default:
                Assert.isTrue(false);
            }
            break;
          }
        case IJavaElement.METHOD:
          {
            final IMethod methodReferenced = (IMethod) referenced;
            final ICompilationUnit referencedUnit = methodReferenced.getCompilationUnit();
            switch (referencingType) {
              case IJavaElement.COMPILATION_UNIT:
                {
                  final ICompilationUnit unit = (ICompilationUnit) referencing;
                  if (referencedUnit != null && referencedUnit.equals(unit))
                    keyword = ModifierKeyword.PRIVATE_KEYWORD;
                  else if (referencedUnit != null
                      && referencedUnit.getParent().equals(unit.getParent())) keyword = null;
                  break;
                }
              case IJavaElement.TYPE:
                {
                  keyword = thresholdTypeToMethod((IType) referencing, methodReferenced, monitor);
                  break;
                }
              case IJavaElement.PACKAGE_FRAGMENT:
                {
                  final IPackageFragment fragment = (IPackageFragment) referencing;
                  if (methodReferenced.getDeclaringType().getPackageFragment().equals(fragment))
                    keyword = null;
                  break;
                }
              default:
                Assert.isTrue(false);
            }
            break;
          }
        default:
          Assert.isTrue(false);
      }
    } finally {
      monitor.done();
    }
    return keyword;
  }

  /**
   * Returns the existing visibility adjustments (element type: Map <IMember,
   * IVisibilityAdjustment>).
   *
   * @return the visibility adjustments
   */
  public final Map<IMember, IncomingMemberVisibilityAdjustment> getAdjustments() {
    return fAdjustments;
  }

  /**
   * Returns a compilation unit rewrite for the specified compilation unit.
   *
   * @param unit the compilation unit to get the rewrite for
   * @return the rewrite for the compilation unit
   */
  private CompilationUnitRewrite getCompilationUnitRewrite(final ICompilationUnit unit) {
    CompilationUnitRewrite rewrite = fRewrites.get(unit);
    if (rewrite == null) {
      if (fOwner == null) rewrite = new CompilationUnitRewrite(unit);
      else rewrite = new CompilationUnitRewrite(fOwner, unit);
      fRewrites.put(unit, rewrite);
    }
    return rewrite;
  }

  /**
   * Returns a cached type hierarchy for the specified type.
   *
   * @param type the type to get the hierarchy for
   * @param monitor the progress monitor to use
   * @return the type hierarchy
   * @throws JavaModelException if the type hierarchy could not be created
   */
  private ITypeHierarchy getTypeHierarchy(final IType type, final IProgressMonitor monitor)
      throws JavaModelException {
    ITypeHierarchy hierarchy = null;
    try {
      monitor.beginTask("", 1); // $NON-NLS-1$
      monitor.setTaskName(RefactoringCoreMessages.MemberVisibilityAdjustor_checking);
      try {
        hierarchy = fTypeHierarchies.get(type);
        if (hierarchy == null) {
          if (fOwner == null) {
            hierarchy =
                type.newSupertypeHierarchy(
                    new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
          } else {
            hierarchy =
                type.newSupertypeHierarchy(
                    fOwner,
                    new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
          }
          fTypeHierarchies.put(type, hierarchy);
        }
      } finally {
        monitor.done();
      }
    } finally {
      monitor.done();
    }
    return hierarchy;
  }

  /**
   * Does the specified member need further visibility adjustment?
   *
   * @param member the member to test
   * @param threshold the visibility threshold to test for
   * @return <code>true</code> if the member needs further adjustment, <code>false</code> otherwise
   */
  private boolean needsVisibilityAdjustment(final IMember member, final ModifierKeyword threshold) {
    Assert.isNotNull(member);
    return needsVisibilityAdjustments(member, threshold, fAdjustments);
  }

  /**
   * Rewrites the computed adjustments for the specified compilation unit.
   *
   * @param unit the compilation unit to rewrite the adjustments
   * @param monitor the progress monitor to use
   * @throws JavaModelException if an error occurs during search
   */
  public final void rewriteVisibility(final ICompilationUnit unit, final IProgressMonitor monitor)
      throws JavaModelException {
    try {
      monitor.beginTask("", fAdjustments.keySet().size()); // $NON-NLS-1$
      monitor.setTaskName(RefactoringCoreMessages.MemberVisibilityAdjustor_adjusting);
      IMember member = null;
      IVisibilityAdjustment adjustment = null;
      for (final Iterator<IMember> iterator = fAdjustments.keySet().iterator();
          iterator.hasNext(); ) {
        member = iterator.next();
        if (unit.equals(member.getCompilationUnit())) {
          adjustment = fAdjustments.get(member);
          if (adjustment != null)
            adjustment.rewriteVisibility(this, new SubProgressMonitor(monitor, 1));
        }
      }
    } finally {
      fTypeHierarchies.clear();
      monitor.done();
    }
  }

  /**
   * Rewrites the computed adjustments.
   *
   * @param monitor the progress monitor to use
   * @throws JavaModelException if an error occurs during search
   */
  public final void rewriteVisibility(final IProgressMonitor monitor) throws JavaModelException {
    try {
      monitor.beginTask("", fAdjustments.keySet().size()); // $NON-NLS-1$
      monitor.setTaskName(RefactoringCoreMessages.MemberVisibilityAdjustor_adjusting);
      IMember member = null;
      IVisibilityAdjustment adjustment = null;
      for (final Iterator<IMember> iterator = fAdjustments.keySet().iterator();
          iterator.hasNext(); ) {
        member = iterator.next();
        adjustment = fAdjustments.get(member);
        if (adjustment != null)
          adjustment.rewriteVisibility(this, new SubProgressMonitor(monitor, 1));
        if (monitor.isCanceled()) throw new OperationCanceledException();
      }
    } finally {
      fTypeHierarchies.clear();
      monitor.done();
    }
  }

  /**
   * Sets the existing visibility adjustments to be taken into account (element type: Map <IMember,
   * IVisibilityAdjustment>).
   *
   * <p>This method must be called before calling {@link
   * MemberVisibilityAdjustor#adjustVisibility(IProgressMonitor)}. The default is to take no
   * existing adjustments into account.
   *
   * @param adjustments the existing adjustments to set
   */
  public final void setAdjustments(
      final Map<IMember, IncomingMemberVisibilityAdjustment> adjustments) {
    Assert.isNotNull(adjustments);
    fAdjustments = adjustments;
  }

  /**
   * Sets the severity of failure messages.
   *
   * <p>This method must be called before calling {@link
   * MemberVisibilityAdjustor#adjustVisibility(IProgressMonitor)}. The default is a status with
   * value {@link RefactoringStatus#ERROR}.
   *
   * @param severity the severity of failure messages
   */
  public final void setFailureSeverity(final int severity) {
    Assert.isTrue(isStatusSeverity(severity));
  }

  /**
   * Determines whether incoming references should be adjusted.
   *
   * <p>This method must be called before calling {@link
   * MemberVisibilityAdjustor#adjustVisibility(IProgressMonitor)}. The default is to adjust incoming
   * references.
   *
   * @param incoming <code>true</code> if incoming references should be adjusted, <code>false</code>
   *     otherwise
   */
  public final void setIncoming(final boolean incoming) {
    fIncoming = incoming;
  }

  /**
   * Determines whether outgoing references should be adjusted.
   *
   * <p>This method must be called before calling {@link
   * MemberVisibilityAdjustor#adjustVisibility(IProgressMonitor)}. The default is to adjust outgoing
   * references.
   *
   * @param outgoing <code>true</code> if outgoing references should be adjusted, <code>false</code>
   *     otherwise
   */
  public final void setOutgoing(final boolean outgoing) {
    fOutgoing = outgoing;
  }

  /**
   * Sets the AST rewrite to use for member visibility adjustments.
   *
   * <p>This method must be called before calling {@link
   * MemberVisibilityAdjustor#adjustVisibility(IProgressMonitor)}. The default is to use a
   * compilation unit rewrite.
   *
   * @param rewrite the AST rewrite to set
   * @param root the root of the AST used in the rewrite
   */
  public final void setRewrite(final ASTRewrite rewrite, final CompilationUnit root) {
    Assert.isTrue(rewrite == null || root != null);
    fRewrite = rewrite;
    fRoot = root;
  }

  /**
   * Sets the compilation unit rewrites used by this adjustor (element type: Map <ICompilationUnit,
   * CompilationUnitRewrite>).
   *
   * <p>This method must be called before calling {@link
   * MemberVisibilityAdjustor#adjustVisibility(IProgressMonitor)}. The default is to use no existing
   * rewrites.
   *
   * @param rewrites the map of compilation units to compilation unit rewrites to set
   */
  public final void setRewrites(final Map<ICompilationUnit, CompilationUnitRewrite> rewrites) {
    Assert.isNotNull(rewrites);
    fRewrites = rewrites;
  }

  /**
   * Sets the incoming search scope used by this adjustor.
   *
   * <p>This method must be called before calling {@link
   * MemberVisibilityAdjustor#adjustVisibility(IProgressMonitor)}. The default is the whole
   * workspace as scope.
   *
   * @param scope the search scope to set
   */
  public final void setScope(final IJavaSearchScope scope) {
    Assert.isNotNull(scope);
    fScope = scope;
  }

  /**
   * Sets the working copy owner used by this adjustor.
   *
   * <p>This method must be called before calling {@link
   * MemberVisibilityAdjustor#adjustVisibility(IProgressMonitor)}. The default is to use none.
   *
   * @param owner the working copy owner, or <code>null</code> to use none
   */
  public final void setOwner(final WorkingCopyOwner owner) {
    fOwner = owner;
  }

  /**
   * Sets the refactoring status used by this adjustor.
   *
   * <p>This method must be called before calling {@link
   * MemberVisibilityAdjustor#adjustVisibility(IProgressMonitor)}. The default is a fresh status
   * with status {@link RefactoringStatus#OK}.
   *
   * @param status the refactoring status to set
   */
  public final void setStatus(final RefactoringStatus status) {
    Assert.isNotNull(status);
    fStatus = status;
  }

  /**
   * Sets the severity of visibility messages.
   *
   * <p>This method must be called before calling {@link
   * MemberVisibilityAdjustor#adjustVisibility(IProgressMonitor)}. The default is a status with
   * value {@link RefactoringStatus#WARNING}.
   *
   * @param severity the severity of visibility messages
   */
  public final void setVisibilitySeverity(final int severity) {
    Assert.isTrue(isStatusSeverity(severity));
    fVisibilitySeverity = severity;
  }

  /**
   * Returns the visibility threshold from a type to a field.
   *
   * @param referencing the referencing type
   * @param referenced the referenced field
   * @param monitor the progress monitor to use
   * @return the visibility keyword corresponding to the threshold, or <code>null</code> for default
   *     visibility
   * @throws JavaModelException if the java elements could not be accessed
   */
  private ModifierKeyword thresholdTypeToField(
      final IType referencing, final IField referenced, final IProgressMonitor monitor)
      throws JavaModelException {
    ModifierKeyword keyword = ModifierKeyword.PUBLIC_KEYWORD;
    final ICompilationUnit referencedUnit = referenced.getCompilationUnit();
    if (referenced.getDeclaringType().equals(referencing))
      keyword = ModifierKeyword.PRIVATE_KEYWORD;
    else {
      final ITypeHierarchy hierarchy =
          getTypeHierarchy(referencing, new SubProgressMonitor(monitor, 1));
      final IType[] types = hierarchy.getSupertypes(referencing);
      IType superType = null;
      for (int index = 0; index < types.length; index++) {
        superType = types[index];
        if (superType.equals(referenced.getDeclaringType())) {
          keyword = ModifierKeyword.PROTECTED_KEYWORD;
          return keyword;
        }
      }
    }
    final ICompilationUnit typeUnit = referencing.getCompilationUnit();
    if (referencedUnit != null && referencedUnit.equals(typeUnit))
      keyword = ModifierKeyword.PRIVATE_KEYWORD;
    else if (referencedUnit != null
        && typeUnit != null
        && referencedUnit.getParent().equals(typeUnit.getParent())) keyword = null;
    return keyword;
  }

  /**
   * Returns the visibility threshold from a type to a method.
   *
   * @param referencing the referencing type
   * @param referenced the referenced method
   * @param monitor the progress monitor to use
   * @return the visibility keyword corresponding to the threshold, or <code>null</code> for default
   *     visibility
   * @throws JavaModelException if the java elements could not be accessed
   */
  private ModifierKeyword thresholdTypeToMethod(
      final IType referencing, final IMethod referenced, final IProgressMonitor monitor)
      throws JavaModelException {
    final ICompilationUnit referencedUnit = referenced.getCompilationUnit();
    ModifierKeyword keyword = ModifierKeyword.PUBLIC_KEYWORD;
    if (referenced.getDeclaringType().equals(referencing))
      keyword = ModifierKeyword.PRIVATE_KEYWORD;
    else {
      final ITypeHierarchy hierarchy =
          getTypeHierarchy(referencing, new SubProgressMonitor(monitor, 1));
      final IType[] types = hierarchy.getSupertypes(referencing);
      IType superType = null;
      for (int index = 0; index < types.length; index++) {
        superType = types[index];
        if (superType.equals(referenced.getDeclaringType())) {
          keyword = ModifierKeyword.PROTECTED_KEYWORD;
          return keyword;
        }
      }
    }
    final ICompilationUnit typeUnit = referencing.getCompilationUnit();
    if (referencedUnit != null && referencedUnit.equals(typeUnit)) {
      if (referenced.getDeclaringType().getDeclaringType() != null) keyword = null;
      else keyword = ModifierKeyword.PRIVATE_KEYWORD;
    } else if (referencedUnit != null && referencedUnit.getParent().equals(typeUnit.getParent()))
      keyword = null;
    return keyword;
  }

  /**
   * Returns the visibility threshold from a type to another type.
   *
   * @param referencing the referencing type
   * @param referenced the referenced type
   * @param monitor the progress monitor to use
   * @return the visibility keyword corresponding to the threshold, or <code>null</code> for default
   *     visibility
   * @throws JavaModelException if the java elements could not be accessed
   */
  private ModifierKeyword thresholdTypeToType(
      final IType referencing, final IType referenced, final IProgressMonitor monitor)
      throws JavaModelException {
    ModifierKeyword keyword = ModifierKeyword.PUBLIC_KEYWORD;
    final ICompilationUnit referencedUnit = referenced.getCompilationUnit();
    if (referencing.equals(referenced.getDeclaringType()))
      keyword = ModifierKeyword.PRIVATE_KEYWORD;
    else {
      final ITypeHierarchy hierarchy =
          getTypeHierarchy(referencing, new SubProgressMonitor(monitor, 1));
      final IType[] types = hierarchy.getSupertypes(referencing);
      IType superType = null;
      for (int index = 0; index < types.length; index++) {
        superType = types[index];
        if (superType.equals(referenced)) {
          keyword = null;
          return keyword;
        }
      }
    }
    final ICompilationUnit typeUnit = referencing.getCompilationUnit();
    if (referencedUnit != null && referencedUnit.equals(typeUnit)) {
      if (referenced.getDeclaringType() != null) keyword = null;
      else keyword = ModifierKeyword.PRIVATE_KEYWORD;
    } else if (referencedUnit != null
        && typeUnit != null
        && referencedUnit.getParent().equals(typeUnit.getParent())) keyword = null;
    return keyword;
  }
}
