/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.ui.text.java;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.che.jface.text.contentassist.ICompletionProposal;
import org.eclipse.che.jface.text.contentassist.IContextInformation;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.CollectionsUtil;
import org.eclipse.jdt.internal.corext.util.TypeFilter;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.AnnotationAtttributeProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.AnonymousTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.AnonymousTypeProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.FieldProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.GetterSetterCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaFieldWithCastedReceiverCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyPackageCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.MethodDeclarationCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.MethodProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.OverrideCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.ProposalContextInformation;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocInlineTagCompletionProposal;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocLinkTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

/**
 * Java UI implementation of <code>CompletionRequestor</code>. Produces {@link
 * IJavaCompletionProposal}s from the proposal descriptors received via the <code>
 * CompletionRequestor</code> interface.
 *
 * <p>The lifecycle of a <code>CompletionProposalCollector</code> instance is very simple:
 *
 * <pre>
 * ICompilationUnit unit= ...
 * int offset= ...
 *
 * CompletionProposalCollector collector= new CompletionProposalCollector(unit);
 * unit.codeComplete(offset, collector);
 * IJavaCompletionProposal[] proposals= collector.getJavaCompletionProposals();
 * String errorMessage= collector.getErrorMessage();
 *
 * &#x2f;&#x2f; display &#x2f; process proposals
 * </pre>
 *
 * Note that after a code completion operation, the collector will store any received proposals,
 * which may require a considerable amount of memory, so the collector should not be kept as a
 * reference after a completion operation.
 *
 * <p>Clients may instantiate or subclass.
 *
 * @since 3.1
 */
public class CompletionProposalCollector extends CompletionRequestor {

  /** Tells whether this class is in debug mode. */
  private static final boolean DEBUG =
      "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jdt.ui/debug/ResultCollector"));
  // $NON-NLS-1$//$NON-NLS-2$

  /** Triggers for method proposals without parameters. Do not modify. */
  protected static final char[] METHOD_TRIGGERS = new char[] {';', ',', '.', '\t', '[', ' '};
  /** Triggers for method proposals. Do not modify. */
  protected static final char[] METHOD_WITH_ARGUMENTS_TRIGGERS = new char[] {'(', '-', ' '};
  /** Triggers for types. Do not modify. */
  protected static final char[] TYPE_TRIGGERS = new char[] {'.', '\t', '[', '(', ' '};
  /** Triggers for variables. Do not modify. */
  protected static final char[] VAR_TRIGGER = new char[] {'\t', ' ', '=', ';', '.'};

  private final CompletionProposalLabelProvider fLabelProvider =
      new CompletionProposalLabelProvider();
  private final ImageDescriptorRegistry fRegistry = JavaPlugin.getImageDescriptorRegistry();

  private final List<IJavaCompletionProposal> fJavaProposals = new ArrayList<>();
  private final List<IJavaCompletionProposal> fKeywords = new ArrayList<>();
  private final Set<String> fSuggestedMethodNames = new HashSet<>();

  private final ICompilationUnit fCompilationUnit;
  private final IJavaProject fJavaProject;
  private int fUserReplacementLength;

  private CompletionContext fContext;
  private IProblem fLastProblem;

  /* performance instrumentation */
  private long fStartTime;
  private long fUITime;

  /**
   * The UI invocation context or <code>null</code>.
   *
   * @since 3.2
   */
  private JavaContentAssistInvocationContext fInvocationContext;

  /**
   * Creates a new instance ready to collect proposals. If the passed <code>ICompilationUnit</code>
   * is not contained in an {@link org.eclipse.jdt.core.IJavaProject}, no javadoc will be available
   * as {@link ICompletionProposal#getAdditionalProposalInfo() additional info} on the created
   * proposals.
   *
   * @param cu the compilation unit that the result collector will operate on
   */
  public CompletionProposalCollector(ICompilationUnit cu) {
    this(cu.getJavaProject(), cu, false);
  }

  /**
   * Creates a new instance ready to collect proposals. Note that proposals for anonymous types and
   * method declarations are not created when using this constructor, as those need to know the
   * compilation unit that they are created on. Use {@link
   * CompletionProposalCollector#CompletionProposalCollector(org.eclipse.jdt.core.ICompilationUnit)}
   * instead to get all proposals.
   *
   * <p>If the passed Java project is <code>null</code>, no javadoc will be available as {@link
   * ICompletionProposal#getAdditionalProposalInfo() additional info} on the created (e.g. method
   * and type) proposals.
   *
   * @param project the project that the result collector will operate on, or <code>null</code>
   */
  public CompletionProposalCollector(IJavaProject project) {
    this(project, null, false);
  }

  private CompletionProposalCollector(
      IJavaProject project, ICompilationUnit cu, boolean ignoreAll) {
    super(ignoreAll);
    fJavaProject = project;
    fCompilationUnit = cu;

    fUserReplacementLength = -1;
    if (!ignoreAll) {
      setRequireExtendedContext(true);
    }
  }

  /**
   * Creates a new instance ready to collect proposals. If the passed <code>ICompilationUnit</code>
   * is not contained in an {@link org.eclipse.jdt.core.IJavaProject}, no javadoc will be available
   * as {@link ICompletionProposal#getAdditionalProposalInfo() additional info} on the created
   * proposals.
   *
   * @param cu the compilation unit that the result collector will operate on
   * @param ignoreAll <code>true</code> to ignore all kinds of completion proposals
   * @since 3.4
   */
  public CompletionProposalCollector(ICompilationUnit cu, boolean ignoreAll) {
    this(cu.getJavaProject(), cu, ignoreAll);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.CompletionRequestor#setIgnored(int, boolean)
   */
  @Override
  public void setIgnored(int completionProposalKind, boolean ignore) {
    super.setIgnored(completionProposalKind, ignore);
    if (completionProposalKind == CompletionProposal.METHOD_DECLARATION && !ignore) {
      setRequireExtendedContext(true);
    }
  }

  /**
   * Sets the invocation context.
   *
   * <p>Subclasses may extend.
   *
   * @param context the invocation context
   * @see #getInvocationContext()
   * @since 3.2
   */
  public void setInvocationContext(JavaContentAssistInvocationContext context) {
    Assert.isNotNull(context);
    fInvocationContext = context;
    context.setCollector(this);
  }

  /**
   * Returns the invocation context. If none has been set via {@link
   * #setInvocationContext(JavaContentAssistInvocationContext)}, a new one is created.
   *
   * @return invocationContext the invocation context
   * @since 3.2
   */
  protected final JavaContentAssistInvocationContext getInvocationContext() {
    if (fInvocationContext == null) {
      ICompilationUnit cu = getCompilationUnit();
      if (cu != null) setInvocationContext(new JavaContentAssistInvocationContext(cu));
      else setInvocationContext(new JavaContentAssistInvocationContext(fJavaProject));
    }
    return fInvocationContext;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Subclasses may replace, but usually should not need to. Consider replacing {@linkplain
   * #createJavaCompletionProposal(org.eclipse.jdt.core.CompletionProposal)
   * createJavaCompletionProposal} instead.
   */
  @Override
  public void accept(CompletionProposal proposal) {
    long start = DEBUG ? System.currentTimeMillis() : 0;
    try {
      if (isFiltered(proposal)) return;

      if (proposal.getKind() == CompletionProposal.POTENTIAL_METHOD_DECLARATION) {
        acceptPotentialMethodDeclaration(proposal);
      } else {
        IJavaCompletionProposal javaProposal = createJavaCompletionProposal(proposal);
        if (javaProposal != null) {
          fJavaProposals.add(javaProposal);
          if (proposal.getKind() == CompletionProposal.KEYWORD) fKeywords.add(javaProposal);
        }
      }
    } catch (IllegalArgumentException e) {
      // all signature processing method may throw IAEs
      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84657
      // don't abort, but log and show all the valid proposals
      JavaPlugin.log(
          new Status(
              IStatus.ERROR,
              JavaPlugin.getPluginId(),
              IStatus.OK,
              "Exception when processing proposal for: " + String.valueOf(proposal.getCompletion()),
              e)); // $NON-NLS-1$
    }

    if (DEBUG) fUITime += System.currentTimeMillis() - start;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Subclasses may extend, but usually should not need to.
   *
   * @see #getContext()
   */
  @Override
  public void acceptContext(CompletionContext context) {
    fContext = context;
    fLabelProvider.setContext(context);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Subclasses may extend, but must call the super implementation.
   */
  @Override
  public void beginReporting() {
    if (DEBUG) {
      fStartTime = System.currentTimeMillis();
      fUITime = 0;
    }

    fLastProblem = null;
    fJavaProposals.clear();
    fKeywords.clear();
    fSuggestedMethodNames.clear();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Subclasses may extend, but must call the super implementation.
   */
  @Override
  public void completionFailure(IProblem problem) {
    fLastProblem = problem;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Subclasses may extend, but must call the super implementation.
   */
  @Override
  public void endReporting() {
    if (DEBUG) {
      long total = System.currentTimeMillis() - fStartTime;
      System.err.println("Core Collector (core):\t" + (total - fUITime)); // $NON-NLS-1$
      System.err.println("Core Collector (ui):\t" + fUITime); // $NON-NLS-1$
    }
  }

  /**
   * Returns an error message about any error that may have occurred during code completion, or the
   * empty string if none.
   *
   * <p>Subclasses may replace or extend.
   *
   * @return an error message or the empty string
   */
  public String getErrorMessage() {
    if (fLastProblem != null) return fLastProblem.getMessage();
    return ""; // $NON-NLS-1$
  }

  /**
   * Returns the unsorted list of received proposals.
   *
   * @return the unsorted list of received proposals
   */
  public final IJavaCompletionProposal[] getJavaCompletionProposals() {
    return CollectionsUtil.toArray(fJavaProposals, IJavaCompletionProposal.class);
  }

  /**
   * Returns the unsorted list of received keyword proposals.
   *
   * @return the unsorted list of received keyword proposals
   */
  public final IJavaCompletionProposal[] getKeywordCompletionProposals() {
    return CollectionsUtil.toArray(fKeywords, IJavaCompletionProposal.class);
  }

  /**
   * If the replacement length is set, it overrides the length returned from the content assist
   * infrastructure. Use this setting if code assist is called with a none empty selection.
   *
   * @param length the new replacement length, relative to the code assist offset. Must be equal to
   *     or greater than zero.
   */
  public final void setReplacementLength(int length) {
    Assert.isLegal(length >= 0);
    fUserReplacementLength = length;
  }

  /**
   * Computes the relevance for a given <code>CompletionProposal</code>.
   *
   * <p>Subclasses may replace, but usually should not need to.
   *
   * @param proposal the proposal to compute the relevance for
   * @return the relevance for <code>proposal</code>
   */
  protected int computeRelevance(CompletionProposal proposal) {
    final int baseRelevance = proposal.getRelevance() * 16;
    switch (proposal.getKind()) {
      case CompletionProposal.PACKAGE_REF:
        return baseRelevance + 0;
      case CompletionProposal.LABEL_REF:
        return baseRelevance + 1;
      case CompletionProposal.KEYWORD:
        return baseRelevance + 2;
      case CompletionProposal.TYPE_REF:
      case CompletionProposal.ANONYMOUS_CLASS_DECLARATION:
      case CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION:
        return baseRelevance + 3;
      case CompletionProposal.METHOD_REF:
      case CompletionProposal.CONSTRUCTOR_INVOCATION:
      case CompletionProposal.METHOD_NAME_REFERENCE:
      case CompletionProposal.METHOD_DECLARATION:
      case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
        return baseRelevance + 4;
      case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
        return baseRelevance + 4 /* + 99 */;
      case CompletionProposal.FIELD_REF:
        return baseRelevance + 5;
      case CompletionProposal.LOCAL_VARIABLE_REF:
      case CompletionProposal.VARIABLE_DECLARATION:
        return baseRelevance + 6;
      default:
        return baseRelevance;
    }
  }

  /**
   * Creates a new java completion proposal from a core proposal. This may involve computing the
   * display label and setting up some context.
   *
   * <p>This method is called for every proposal that will be displayed to the user, which may be
   * hundreds. Implementations should therefore defer as much work as possible: Labels should be
   * computed lazily to leverage virtual table usage, and any information only needed when
   * <em>applying</em> a proposal should not be computed yet.
   *
   * <p>Implementations may return <code>null</code> if a proposal should not be included in the
   * list presented to the user.
   *
   * <p>Subclasses may extend or replace this method.
   *
   * @param proposal the core completion proposal to create a UI proposal for
   * @return the created java completion proposal, or <code>null</code> if no proposal should be
   *     displayed
   */
  protected IJavaCompletionProposal createJavaCompletionProposal(CompletionProposal proposal) {
    switch (proposal.getKind()) {
      case CompletionProposal.KEYWORD:
        return createKeywordProposal(proposal);
      case CompletionProposal.PACKAGE_REF:
        return createPackageProposal(proposal);
      case CompletionProposal.TYPE_REF:
        return createTypeProposal(proposal);
      case CompletionProposal.JAVADOC_TYPE_REF:
        return createJavadocLinkTypeProposal(proposal);
      case CompletionProposal.FIELD_REF:
      case CompletionProposal.JAVADOC_FIELD_REF:
      case CompletionProposal.JAVADOC_VALUE_REF:
        return createFieldProposal(proposal);
      case CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER:
        return createFieldWithCastedReceiverProposal(proposal);
      case CompletionProposal.METHOD_REF:
      case CompletionProposal.CONSTRUCTOR_INVOCATION:
      case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
      case CompletionProposal.METHOD_NAME_REFERENCE:
      case CompletionProposal.JAVADOC_METHOD_REF:
        return createMethodReferenceProposal(proposal);
      case CompletionProposal.METHOD_DECLARATION:
        return createMethodDeclarationProposal(proposal);
      case CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION:
        return createAnonymousTypeProposal(proposal, getInvocationContext());
      case CompletionProposal.ANONYMOUS_CLASS_DECLARATION:
        return createAnonymousTypeProposal(proposal, null);
      case CompletionProposal.LABEL_REF:
        return createLabelProposal(proposal);
      case CompletionProposal.LOCAL_VARIABLE_REF:
      case CompletionProposal.VARIABLE_DECLARATION:
        return createLocalVariableProposal(proposal);
      case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
        return createAnnotationAttributeReferenceProposal(proposal);
      case CompletionProposal.JAVADOC_BLOCK_TAG:
      case CompletionProposal.JAVADOC_PARAM_REF:
        return createJavadocSimpleProposal(proposal);
      case CompletionProposal.JAVADOC_INLINE_TAG:
        return createJavadocInlineTagProposal(proposal);
      case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
      default:
        return null;
    }
  }

  /**
   * Creates the context information for a given method reference proposal. The passed proposal must
   * be of kind {@link org.eclipse.jdt.core.CompletionProposal#METHOD_REF}.
   *
   * @param methodProposal the method proposal for which to create context information
   * @return the context information for <code>methodProposal</code>
   */
  protected final IContextInformation createMethodContextInformation(
      CompletionProposal methodProposal) {
    Assert.isTrue(methodProposal.getKind() == CompletionProposal.METHOD_REF);
    return new ProposalContextInformation(methodProposal);
  }

  /**
   * Returns the compilation unit that the receiver operates on, or <code>null</code> if the <code>
   * IJavaProject</code> constructor was used to create the receiver.
   *
   * @return the compilation unit that the receiver operates on, or <code>null</code>
   */
  protected final ICompilationUnit getCompilationUnit() {
    return fCompilationUnit;
  }

  /**
   * Returns the <code>CompletionContext</code> for this completion operation.
   *
   * @return the <code>CompletionContext</code> for this completion operation
   * @see
   *     org.eclipse.jdt.core.CompletionRequestor#acceptContext(org.eclipse.jdt.core.CompletionContext)
   */
  protected final CompletionContext getContext() {
    return fContext;
  }

  /**
   * Returns a cached image for the given descriptor.
   *
   * @param descriptor the image descriptor to get an image for, may be <code>null</code>
   * @return the image corresponding to <code>descriptor</code>
   */
  protected final Image getImage(ImageDescriptor descriptor) {
    return (descriptor == null) ? null : fRegistry.get(descriptor);
  }

  /**
   * Returns the proposal label provider used by the receiver.
   *
   * @return the proposal label provider used by the receiver
   */
  protected final CompletionProposalLabelProvider getLabelProvider() {
    return fLabelProvider;
  }

  /**
   * Returns the replacement length of a given completion proposal. The replacement length is
   * usually the difference between the return values of <code>proposal.getReplaceEnd</code> and
   * <code>proposal.getReplaceStart</code>, but this behavior may be overridden by calling {@link
   * #setReplacementLength(int)}.
   *
   * @param proposal the completion proposal to get the replacement length for
   * @return the replacement length for <code>proposal</code>
   */
  protected final int getLength(CompletionProposal proposal) {
    int start = proposal.getReplaceStart();
    int end = proposal.getReplaceEnd();
    int length;
    if (fUserReplacementLength == -1) {
      length = end - start;
    } else {
      length = fUserReplacementLength;
      // extend length to begin at start
      int behindCompletion = proposal.getCompletionLocation() + 1;
      if (start < behindCompletion) {
        length += behindCompletion - start;
      }
    }
    return length;
  }

  /**
   * Returns <code>true</code> if <code>proposal</code> is filtered, e.g. should not be proposed to
   * the user, <code>false</code> if it is valid.
   *
   * <p>Subclasses may extends this method. The default implementation filters proposals set to be
   * ignored via {@linkplain org.eclipse.jdt.core.CompletionRequestor#setIgnored(int, boolean)
   * setIgnored} and types set to be ignored in the preferences.
   *
   * @param proposal the proposal to filter
   * @return <code>true</code> to filter <code>proposal</code>, <code>false</code> to let it pass
   */
  protected boolean isFiltered(CompletionProposal proposal) {
    if (isIgnored(proposal.getKind())) return true;
    char[] declaringType = getDeclaringType(proposal);
    return declaringType != null && TypeFilter.isFiltered(declaringType);
  }

  /**
   * Returns the type signature of the declaring type of a <code>CompletionProposal</code>, or
   * <code>null</code> for proposals that do not have a declaring type. The return value is
   * <em>not</em> <code>null</code> for proposals of the following kinds:
   *
   * <ul>
   *   <li>METHOD_DECLARATION
   *   <li>METHOD_NAME_REFERENCE
   *   <li>METHOD_REF
   *   <li>ANNOTATION_ATTRIBUTE_REF
   *   <li>POTENTIAL_METHOD_DECLARATION
   *   <li>ANONYMOUS_CLASS_DECLARATION
   *   <li>FIELD_REF
   *   <li>PACKAGE_REF (returns the package, but no type)
   *   <li>TYPE_REF
   * </ul>
   *
   * @param proposal the completion proposal to get the declaring type for
   * @return the type signature of the declaring type, or <code>null</code> if there is none
   * @see org.eclipse.jdt.core.Signature#toCharArray(char[])
   */
  protected final char[] getDeclaringType(CompletionProposal proposal) {
    switch (proposal.getKind()) {
      case CompletionProposal.METHOD_DECLARATION:
      case CompletionProposal.METHOD_NAME_REFERENCE:
      case CompletionProposal.JAVADOC_METHOD_REF:
      case CompletionProposal.METHOD_REF:
      case CompletionProposal.CONSTRUCTOR_INVOCATION:
      case CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION:
      case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
      case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
      case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
      case CompletionProposal.ANONYMOUS_CLASS_DECLARATION:
      case CompletionProposal.FIELD_REF:
      case CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER:
      case CompletionProposal.JAVADOC_FIELD_REF:
      case CompletionProposal.JAVADOC_VALUE_REF:
        char[] declaration = proposal.getDeclarationSignature();
        // special methods may not have a declaring type: methods defined on arrays etc.
        // Currently known: class literals don't have a declaring type - use Object
        if (declaration == null) return "java.lang.Object".toCharArray(); // $NON-NLS-1$
        return Signature.toCharArray(declaration);
      case CompletionProposal.PACKAGE_REF:
        return proposal.getDeclarationSignature();
      case CompletionProposal.JAVADOC_TYPE_REF:
      case CompletionProposal.TYPE_REF:
        return Signature.toCharArray(proposal.getSignature());
      case CompletionProposal.LOCAL_VARIABLE_REF:
      case CompletionProposal.VARIABLE_DECLARATION:
      case CompletionProposal.KEYWORD:
      case CompletionProposal.LABEL_REF:
      case CompletionProposal.JAVADOC_BLOCK_TAG:
      case CompletionProposal.JAVADOC_INLINE_TAG:
      case CompletionProposal.JAVADOC_PARAM_REF:
        return null;
      default:
        Assert.isTrue(false);
        return null;
    }
  }

  private void acceptPotentialMethodDeclaration(CompletionProposal proposal) {
    try {
      IJavaElement enclosingElement = null;
      //			if (getContext().isExtended()) {
      //				enclosingElement= getContext().getEnclosingElement();
      //			} else if (fCompilationUnit != null) {
      // kept for backward compatibility: CU is not reconciled at this moment, information is
      // missing (bug 70005)
      enclosingElement = fCompilationUnit.getElementAt(proposal.getCompletionLocation() + 1);
      //			}
      if (enclosingElement == null) return;
      IType type = (IType) enclosingElement.getAncestor(IJavaElement.TYPE);
      if (type != null) {
        String prefix = String.valueOf(proposal.getName());
        int completionStart = proposal.getReplaceStart();
        int completionEnd = proposal.getReplaceEnd();
        int relevance = computeRelevance(proposal);

        GetterSetterCompletionProposal.evaluateProposals(
            type,
            prefix,
            completionStart,
            completionEnd - completionStart,
            relevance + 2,
            fSuggestedMethodNames,
            fJavaProposals);
        MethodDeclarationCompletionProposal.evaluateProposals(
            type,
            prefix,
            completionStart,
            completionEnd - completionStart,
            relevance,
            fSuggestedMethodNames,
            fJavaProposals);
      }
    } catch (CoreException e) {
      JavaPlugin.log(e);
    }
  }

  private IJavaCompletionProposal createAnnotationAttributeReferenceProposal(
      CompletionProposal proposal) {
    StyledString displayString = fLabelProvider.createLabelWithTypeAndDeclaration(proposal);
    ImageDescriptor descriptor = fLabelProvider.createMethodImageDescriptor(proposal);
    String completion = String.valueOf(proposal.getCompletion());
    JavaCompletionProposal javaProposal =
        new JavaCompletionProposal(
            completion,
            proposal.getReplaceStart(),
            getLength(proposal),
            getImage(descriptor),
            displayString,
            computeRelevance(proposal));
    if (fJavaProject != null)
      javaProposal.setProposalInfo(new AnnotationAtttributeProposalInfo(fJavaProject, proposal));
    return javaProposal;
  }

  private IJavaCompletionProposal createAnonymousTypeProposal(
      CompletionProposal proposal, JavaContentAssistInvocationContext invocationContext) {
    if (fCompilationUnit == null || fJavaProject == null) return null;

    char[] declarationKey = proposal.getDeclarationKey();
    if (declarationKey == null) return null;

    try {
      IJavaElement element = fJavaProject.findElement(new String(declarationKey), null);
      if (!(element instanceof IType)) return null;

      IType type = (IType) element;

      String completion = String.valueOf(proposal.getCompletion());
      int start = proposal.getReplaceStart();
      int length = getLength(proposal);
      int relevance = computeRelevance(proposal);

      StyledString label = fLabelProvider.createAnonymousTypeLabel(proposal);

      JavaCompletionProposal javaProposal =
          new AnonymousTypeCompletionProposal(
              fJavaProject,
              fCompilationUnit,
              invocationContext,
              start,
              length,
              completion,
              label,
              String.valueOf(proposal.getDeclarationSignature()),
              type,
              relevance);
      javaProposal.setProposalInfo(new AnonymousTypeProposalInfo(fJavaProject, proposal));
      return javaProposal;
    } catch (JavaModelException e) {
      return null;
    }
  }

  private IJavaCompletionProposal createFieldProposal(CompletionProposal proposal) {
    String completion = String.valueOf(proposal.getCompletion());
    int start = proposal.getReplaceStart();
    int length = getLength(proposal);
    StyledString label = fLabelProvider.createStyledLabel(proposal);
    Image image = getImage(fLabelProvider.createFieldImageDescriptor(proposal));
    int relevance = computeRelevance(proposal);

    JavaCompletionProposal javaProposal =
        new JavaCompletionProposal(
            completion,
            start,
            length,
            image,
            label,
            relevance,
            getContext().isInJavadoc(),
            getInvocationContext());
    if (fJavaProject != null)
      javaProposal.setProposalInfo(new FieldProposalInfo(fJavaProject, proposal));

    javaProposal.setTriggerCharacters(VAR_TRIGGER);

    return javaProposal;
  }

  /**
   * Creates the Java completion proposal for the JDT Core {@link
   * org.eclipse.jdt.core.CompletionProposal#FIELD_REF_WITH_CASTED_RECEIVER} proposal.
   *
   * @param proposal the JDT Core proposal
   * @return the Java completion proposal
   * @since 3.4
   */
  private IJavaCompletionProposal createFieldWithCastedReceiverProposal(
      CompletionProposal proposal) {
    String completion = String.valueOf(proposal.getCompletion());
    completion =
        CodeFormatterUtil.format(
            CodeFormatter.K_EXPRESSION, completion, 0, "\n", fJavaProject); // $NON-NLS-1$
    int start = proposal.getReplaceStart();
    int length = getLength(proposal);
    StyledString label = fLabelProvider.createStyledLabel(proposal);
    Image image = getImage(fLabelProvider.createFieldImageDescriptor(proposal));
    int relevance = computeRelevance(proposal);

    JavaCompletionProposal javaProposal =
        new JavaFieldWithCastedReceiverCompletionProposal(
            completion,
            start,
            length,
            image,
            label,
            relevance,
            getContext().isInJavadoc(),
            getInvocationContext(),
            proposal);
    if (fJavaProject != null)
      javaProposal.setProposalInfo(new FieldProposalInfo(fJavaProject, proposal));

    javaProposal.setTriggerCharacters(VAR_TRIGGER);

    return javaProposal;
  }

  private IJavaCompletionProposal createJavadocSimpleProposal(CompletionProposal javadocProposal) {
    // TODO do better with javadoc proposals
    //		String completion= String.valueOf(proposal.getCompletion());
    //		int start= proposal.getReplaceStart();
    //		int length= getLength(proposal);
    //		String label= fLabelProvider.createSimpleLabel(proposal);
    //		Image image= getImage(fLabelProvider.createImageDescriptor(proposal));
    //		int relevance= computeRelevance(proposal);
    //
    //		JavaCompletionProposal javaProposal= new JavaCompletionProposal(completion, start, length,
    // image, label, relevance);
    //		if (fJavaProject != null)
    //			javaProposal.setProposalInfo(new FieldProposalInfo(fJavaProject, proposal));
    //
    //		javaProposal.setTriggerCharacters(VAR_TRIGGER);
    //
    //		return javaProposal;
    LazyJavaCompletionProposal proposal =
        new LazyJavaCompletionProposal(javadocProposal, getInvocationContext());
    //		adaptLength(proposal, javadocProposal);
    return proposal;
  }

  private IJavaCompletionProposal createJavadocInlineTagProposal(
      CompletionProposal javadocProposal) {
    LazyJavaCompletionProposal proposal =
        new JavadocInlineTagCompletionProposal(javadocProposal, getInvocationContext());
    adaptLength(proposal, javadocProposal);
    return proposal;
  }

  private IJavaCompletionProposal createKeywordProposal(CompletionProposal proposal) {
    String completion = String.valueOf(proposal.getCompletion());
    int start = proposal.getReplaceStart();
    int length = getLength(proposal);
    StyledString label = fLabelProvider.createSimpleLabel(proposal);
    int relevance = computeRelevance(proposal);
    return new JavaCompletionProposal(completion, start, length, null, label, relevance);
  }

  private IJavaCompletionProposal createLabelProposal(CompletionProposal proposal) {
    String completion = String.valueOf(proposal.getCompletion());
    int start = proposal.getReplaceStart();
    int length = getLength(proposal);
    StyledString label = fLabelProvider.createSimpleLabel(proposal);
    int relevance = computeRelevance(proposal);

    return new JavaCompletionProposal(completion, start, length, null, label, relevance);
  }

  private IJavaCompletionProposal createLocalVariableProposal(CompletionProposal proposal) {
    String completion = String.valueOf(proposal.getCompletion());
    int start = proposal.getReplaceStart();
    int length = getLength(proposal);
    Image image = getImage(fLabelProvider.createLocalImageDescriptor(proposal));
    StyledString label = fLabelProvider.createSimpleLabelWithType(proposal);
    int relevance = computeRelevance(proposal);
    final JavaCompletionProposal javaProposal =
        new JavaCompletionProposal(completion, start, length, image, label, relevance);
    javaProposal.setTriggerCharacters(VAR_TRIGGER);
    return javaProposal;
  }

  private IJavaCompletionProposal createMethodDeclarationProposal(CompletionProposal proposal) {
    if (fCompilationUnit == null || fJavaProject == null) return null;

    String name = String.valueOf(proposal.getName());
    String[] paramTypes = Signature.getParameterTypes(String.valueOf(proposal.getSignature()));
    for (int index = 0; index < paramTypes.length; index++)
      paramTypes[index] = Signature.toString(paramTypes[index]);
    int start = proposal.getReplaceStart();
    int length = getLength(proposal);

    StyledString label = fLabelProvider.createOverrideMethodProposalLabel(proposal);

    JavaCompletionProposal javaProposal =
        new OverrideCompletionProposal(
            fJavaProject,
            fCompilationUnit,
            name,
            paramTypes,
            start,
            length,
            label,
            String.valueOf(proposal.getCompletion()));
    javaProposal.setImage(getImage(fLabelProvider.createMethodImageDescriptor(proposal)));
    javaProposal.setProposalInfo(new MethodProposalInfo(fJavaProject, proposal));
    javaProposal.setRelevance(computeRelevance(proposal));

    fSuggestedMethodNames.add(new String(name));
    return javaProposal;
  }

  private IJavaCompletionProposal createMethodReferenceProposal(CompletionProposal methodProposal) {
    LazyJavaCompletionProposal proposal =
        new JavaMethodCompletionProposal(methodProposal, getInvocationContext());
    adaptLength(proposal, methodProposal);
    return proposal;
  }

  private void adaptLength(LazyJavaCompletionProposal proposal, CompletionProposal coreProposal) {
    if (fUserReplacementLength != -1) {
      proposal.setReplacementLength(getLength(coreProposal));
    }
  }

  private IJavaCompletionProposal createPackageProposal(CompletionProposal proposal) {
    return new LazyPackageCompletionProposal(proposal, getInvocationContext());
  }

  private IJavaCompletionProposal createTypeProposal(CompletionProposal typeProposal) {
    LazyJavaCompletionProposal proposal =
        new LazyJavaTypeCompletionProposal(typeProposal, getInvocationContext());
    adaptLength(proposal, typeProposal);
    return proposal;
  }

  private IJavaCompletionProposal createJavadocLinkTypeProposal(CompletionProposal typeProposal) {
    LazyJavaCompletionProposal proposal =
        new JavadocLinkTypeCompletionProposal(typeProposal, getInvocationContext());
    adaptLength(proposal, typeProposal);
    return proposal;
  }
}
