/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Sebastian Davids
 * <sdavids@gmx.de> - Bug 37432 getInvertEqualsProposal Benjamin Muskalla <b.muskalla@gmx.net> - Bug
 * 36350 convertToStringBufferPropsal Chris West (Faux) <eclipse@goeswhere.com> - [quick assist]
 * "Use 'StringBuilder' for string concatenation" could fix existing misuses -
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=282755 Lukas Hanke <hanke@yatta.de> - Bug 241696
 * [quick fix] quickfix to iterate over a collection -
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=241696 Eugene Lucash <e.lucash@gmail.com> - [quick
 * assist] Add key binding for Extract method Quick Assist - https://bugs.eclipse.org/424166 Lukas
 * Hanke <hanke@yatta.de> - Bug 430818 [1.8][quick fix] Quick fix for "for loop" is not shown for
 * bare local variable/argument/field - https://bugs.eclipse.org/bugs/show_bug.cgi?id=430818
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.PrefixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.dom.DimensionRewrite;
import org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder;
import org.eclipse.jdt.internal.corext.dom.ModifierRewrite;
import org.eclipse.jdt.internal.corext.dom.ScopeAnalyzer;
import org.eclipse.jdt.internal.corext.dom.Selection;
import org.eclipse.jdt.internal.corext.dom.SelectionAnalyzer;
import org.eclipse.jdt.internal.corext.dom.TokenScanner;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.ControlStatementsFix;
import org.eclipse.jdt.internal.corext.fix.ConvertLoopFix;
import org.eclipse.jdt.internal.corext.fix.IProposableFix;
import org.eclipse.jdt.internal.corext.fix.LambdaExpressionsFix;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.jdt.internal.corext.fix.TypeParametersFix;
import org.eclipse.jdt.internal.corext.fix.VariableDeclarationFix;
import org.eclipse.jdt.internal.corext.refactoring.code.ConvertAnonymousToNestedRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractConstantRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractTempRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineTempRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.code.PromoteTempToFieldRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.util.TightSourceRangeComputer;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.fix.ControlStatementsCleanUp;
import org.eclipse.jdt.internal.ui.fix.ConvertLoopCleanUp;
import org.eclipse.jdt.internal.ui.fix.ExpressionsCleanUp;
import org.eclipse.jdt.internal.ui.fix.LambdaExpressionsCleanUp;
import org.eclipse.jdt.internal.ui.fix.TypeParametersCleanUp;
import org.eclipse.jdt.internal.ui.fix.VariableDeclarationCleanUp;
import org.eclipse.jdt.internal.ui.text.correction.proposals.AssignToVariableAssistProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.FixCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.GenerateForLoopAssistProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.LinkedCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.LinkedNamesAssistProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.NewDefiningMethodProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.RefactoringCorrectionProposal;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.jdt.ui.text.java.correction.ChangeCorrectionProposal;
import org.eclipse.jdt.ui.text.java.correction.ICommandAccess;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.swt.graphics.Image;

/** */
public class QuickAssistProcessor implements IQuickAssistProcessor {

  public static final String SPLIT_JOIN_VARIABLE_DECLARATION_ID =
      "org.eclipse.jdt.ui.correction.splitJoinVariableDeclaration.assist"; // $NON-NLS-1$
  public static final String CONVERT_FOR_LOOP_ID =
      "org.eclipse.jdt.ui.correction.convertForLoop.assist"; // $NON-NLS-1$
  public static final String ASSIGN_TO_LOCAL_ID =
      "org.eclipse.jdt.ui.correction.assignToLocal.assist"; // $NON-NLS-1$
  public static final String ASSIGN_TO_FIELD_ID =
      "org.eclipse.jdt.ui.correction.assignToField.assist"; // $NON-NLS-1$
  public static final String ASSIGN_PARAM_TO_FIELD_ID =
      "org.eclipse.jdt.ui.correction.assignParamToField.assist"; // $NON-NLS-1$
  public static final String ADD_BLOCK_ID =
      "org.eclipse.jdt.ui.correction.addBlock.assist"; // $NON-NLS-1$
  public static final String EXTRACT_LOCAL_ID =
      "org.eclipse.jdt.ui.correction.extractLocal.assist"; // $NON-NLS-1$
  public static final String EXTRACT_LOCAL_NOT_REPLACE_ID =
      "org.eclipse.jdt.ui.correction.extractLocalNotReplaceOccurrences.assist"; // $NON-NLS-1$
  public static final String EXTRACT_CONSTANT_ID =
      "org.eclipse.jdt.ui.correction.extractConstant.assist"; // $NON-NLS-1$
  public static final String INLINE_LOCAL_ID =
      "org.eclipse.jdt.ui.correction.inlineLocal.assist"; // $NON-NLS-1$
  public static final String CONVERT_LOCAL_TO_FIELD_ID =
      "org.eclipse.jdt.ui.correction.convertLocalToField.assist"; // $NON-NLS-1$
  public static final String CONVERT_ANONYMOUS_TO_LOCAL_ID =
      "org.eclipse.jdt.ui.correction.convertAnonymousToLocal.assist"; // $NON-NLS-1$
  public static final String CONVERT_TO_STRING_BUFFER_ID =
      "org.eclipse.jdt.ui.correction.convertToStringBuffer.assist"; // $NON-NLS-1$
  public static final String CONVERT_TO_MESSAGE_FORMAT_ID =
      "org.eclipse.jdt.ui.correction.convertToMessageFormat.assist"; // $NON-NLS-1$;
  public static final String EXTRACT_METHOD_INPLACE_ID =
      "org.eclipse.jdt.ui.correction.extractMethodInplace.assist"; // $NON-NLS-1$;

  public QuickAssistProcessor() {
    super();
  }

  public boolean hasAssists(IInvocationContext context) throws CoreException {
    ASTNode coveringNode = context.getCoveringNode();
    if (coveringNode != null) {
      ArrayList<ASTNode> coveredNodes =
          AdvancedQuickAssistProcessor.getFullyCoveredNodes(context, coveringNode);
      return getCatchClauseToThrowsProposals(context, coveringNode, null)
          || getPickoutTypeFromMulticatchProposals(context, coveringNode, coveredNodes, null)
          || getConvertToMultiCatchProposals(context, coveringNode, null)
          || getUnrollMultiCatchProposals(context, coveringNode, null)
          || getRenameLocalProposals(context, coveringNode, null, null)
          || getRenameRefactoringProposal(context, coveringNode, null, null)
          || getAssignToVariableProposals(context, coveringNode, null, null)
          || getUnWrapProposals(context, coveringNode, null)
          || getAssignParamToFieldProposals(context, coveringNode, null)
          || getJoinVariableProposals(context, coveringNode, null)
          || getAddFinallyProposals(context, coveringNode, null)
          || getAddElseProposals(context, coveringNode, null)
          || getSplitVariableProposals(context, coveringNode, null)
          || getAddBlockProposals(context, coveringNode, null)
          || getArrayInitializerToArrayCreation(context, coveringNode, null)
          || getCreateInSuperClassProposals(context, coveringNode, null)
          || getInvertEqualsProposal(context, coveringNode, null)
          || getConvertForLoopProposal(context, coveringNode, null)
          || getConvertIterableLoopProposal(context, coveringNode, null)
          || getConvertEnhancedForLoopProposal(context, coveringNode, null)
          || getGenerateForLoopProposals(context, coveringNode, null, null)
          || getExtractVariableProposal(context, false, null)
          || getExtractMethodProposal(context, coveringNode, false, null)
          || getInlineLocalProposal(context, coveringNode, null)
          || getConvertLocalToFieldProposal(context, coveringNode, null)
          || getConvertAnonymousToNestedProposal(context, coveringNode, null)
          || getConvertAnonymousClassCreationsToLambdaProposals(context, coveringNode, null)
          || getConvertLambdaToAnonymousClassCreationsProposals(context, coveringNode, null)
          || getChangeLambdaBodyToBlockProposal(context, coveringNode, null)
          || getChangeLambdaBodyToExpressionProposal(context, coveringNode, null)
          || getRemoveBlockProposals(context, coveringNode, null)
          || getMakeVariableDeclarationFinalProposals(context, null)
          || getMissingCaseStatementProposals(context, coveringNode, null)
          || getConvertStringConcatenationProposals(context, null)
          || getInferDiamondArgumentsProposal(context, coveringNode, null, null);
    }
    return false;
  }

  public IJavaCompletionProposal[] getAssists(
      IInvocationContext context, IProblemLocation[] locations) throws CoreException {
    ASTNode coveringNode = context.getCoveringNode();
    if (coveringNode != null) {
      ArrayList<ASTNode> coveredNodes =
          AdvancedQuickAssistProcessor.getFullyCoveredNodes(context, coveringNode);
      ArrayList<ICommandAccess> resultingCollections = new ArrayList<ICommandAccess>();
      boolean noErrorsAtLocation = noErrorsAtLocation(locations);

      // quick assists that show up also if there is an error/warning
      getRenameLocalProposals(context, coveringNode, locations, resultingCollections);
      getRenameRefactoringProposal(context, coveringNode, locations, resultingCollections);
      getAssignToVariableProposals(context, coveringNode, locations, resultingCollections);
      getAssignParamToFieldProposals(context, coveringNode, resultingCollections);
      getInferDiamondArgumentsProposal(context, coveringNode, locations, resultingCollections);
      getGenerateForLoopProposals(context, coveringNode, locations, resultingCollections);

      if (noErrorsAtLocation) {
        boolean problemsAtLocation = locations.length != 0;
        getCatchClauseToThrowsProposals(context, coveringNode, resultingCollections);
        getPickoutTypeFromMulticatchProposals(
            context, coveringNode, coveredNodes, resultingCollections);
        getConvertToMultiCatchProposals(context, coveringNode, resultingCollections);
        getUnrollMultiCatchProposals(context, coveringNode, resultingCollections);
        getUnWrapProposals(context, coveringNode, resultingCollections);
        getJoinVariableProposals(context, coveringNode, resultingCollections);
        getSplitVariableProposals(context, coveringNode, resultingCollections);
        getAddFinallyProposals(context, coveringNode, resultingCollections);
        getAddElseProposals(context, coveringNode, resultingCollections);
        getAddBlockProposals(context, coveringNode, resultingCollections);
        getInvertEqualsProposal(context, coveringNode, resultingCollections);
        getArrayInitializerToArrayCreation(context, coveringNode, resultingCollections);
        getCreateInSuperClassProposals(context, coveringNode, resultingCollections);
        getExtractVariableProposal(context, problemsAtLocation, resultingCollections);
        getExtractMethodProposal(context, coveringNode, problemsAtLocation, resultingCollections);
        getInlineLocalProposal(context, coveringNode, resultingCollections);
        getConvertLocalToFieldProposal(context, coveringNode, resultingCollections);
        getConvertAnonymousToNestedProposal(context, coveringNode, resultingCollections);
        getConvertAnonymousClassCreationsToLambdaProposals(
            context, coveringNode, resultingCollections);
        getConvertLambdaToAnonymousClassCreationsProposals(
            context, coveringNode, resultingCollections);
        getChangeLambdaBodyToBlockProposal(context, coveringNode, resultingCollections);
        getChangeLambdaBodyToExpressionProposal(context, coveringNode, resultingCollections);
        if (!getConvertForLoopProposal(context, coveringNode, resultingCollections))
          getConvertIterableLoopProposal(context, coveringNode, resultingCollections);
        getConvertEnhancedForLoopProposal(context, coveringNode, resultingCollections);
        getRemoveBlockProposals(context, coveringNode, resultingCollections);
        getMakeVariableDeclarationFinalProposals(context, resultingCollections);
        getConvertStringConcatenationProposals(context, resultingCollections);
        getMissingCaseStatementProposals(context, coveringNode, resultingCollections);
      }
      return resultingCollections.toArray(new IJavaCompletionProposal[resultingCollections.size()]);
    }
    return null;
  }

  static boolean noErrorsAtLocation(IProblemLocation[] locations) {
    if (locations != null) {
      for (int i = 0; i < locations.length; i++) {
        IProblemLocation location = locations[i];
        if (location.isError()) {
          if (IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER.equals(location.getMarkerType())
              && JavaCore.getOptionForConfigurableSeverity(location.getProblemId()) != null) {
            // continue (only drop out for severe (non-optional) errors)
          } else {
            return false;
          }
        }
      }
    }
    return true;
  }

  private static int getIndex(int offset, List<Statement> statements) {
    for (int i = 0; i < statements.size(); i++) {
      Statement s = statements.get(i);
      if (offset <= s.getStartPosition()) {
        return i;
      }
      if (offset < s.getStartPosition() + s.getLength()) {
        return -1;
      }
    }
    return statements.size();
  }

  private static boolean getExtractMethodProposal(
      IInvocationContext context,
      ASTNode coveringNode,
      boolean problemsAtLocation,
      Collection<ICommandAccess> proposals)
      throws CoreException {
    if (!(coveringNode instanceof Expression)
        && !(coveringNode instanceof Statement)
        && !(coveringNode instanceof Block)) {
      return false;
    }
    if (coveringNode instanceof Block) {
      List<Statement> statements = ((Block) coveringNode).statements();
      int startIndex = getIndex(context.getSelectionOffset(), statements);
      if (startIndex == -1) return false;
      int endIndex =
          getIndex(context.getSelectionOffset() + context.getSelectionLength(), statements);
      if (endIndex == -1 || endIndex <= startIndex) return false;
    }

    if (proposals == null) {
      return true;
    }

    final ICompilationUnit cu = context.getCompilationUnit();
    final ExtractMethodRefactoring extractMethodRefactoring =
        new ExtractMethodRefactoring(
            context.getASTRoot(), context.getSelectionOffset(), context.getSelectionLength());
    extractMethodRefactoring.setMethodName("extracted"); // $NON-NLS-1$
    if (extractMethodRefactoring.checkInitialConditions(new NullProgressMonitor()).isOK()) {
      String label = CorrectionMessages.QuickAssistProcessor_extractmethod_description;
      LinkedProposalModel linkedProposalModel = new LinkedProposalModel();
      extractMethodRefactoring.setLinkedProposalModel(linkedProposalModel);

      Image image = JavaPluginImages.get(JavaPluginImages.DESC_MISC_PUBLIC);
      int relevance =
          problemsAtLocation
              ? IProposalRelevance.EXTRACT_METHOD_ERROR
              : IProposalRelevance.EXTRACT_METHOD;
      RefactoringCorrectionProposal proposal =
          new RefactoringCorrectionProposal(label, cu, extractMethodRefactoring, relevance, image);
      proposal.setCommandId(EXTRACT_METHOD_INPLACE_ID);
      proposal.setLinkedProposalModel(linkedProposalModel);
      proposals.add(proposal);
    }
    return true;
  }

  private static boolean getExtractVariableProposal(
      IInvocationContext context, boolean problemsAtLocation, Collection<ICommandAccess> proposals)
      throws CoreException {

    ASTNode node = context.getCoveredNode();

    if (!(node instanceof Expression)) {
      if (context.getSelectionLength() != 0) {
        return false;
      }
      node = context.getCoveringNode();
      if (!(node instanceof Expression)) {
        return false;
      }
    }
    final Expression expression = (Expression) node;

    ITypeBinding binding = expression.resolveTypeBinding();
    if (binding == null || Bindings.isVoidType(binding)) {
      return false;
    }
    if (proposals == null) {
      return true;
    }

    final ICompilationUnit cu = context.getCompilationUnit();
    ExtractTempRefactoring extractTempRefactoring =
        new ExtractTempRefactoring(
            context.getASTRoot(), context.getSelectionOffset(), context.getSelectionLength());
    if (extractTempRefactoring.checkInitialConditions(new NullProgressMonitor()).isOK()) {
      LinkedProposalModel linkedProposalModel = new LinkedProposalModel();
      extractTempRefactoring.setLinkedProposalModel(linkedProposalModel);
      extractTempRefactoring.setCheckResultForCompileProblems(false);

      String label = CorrectionMessages.QuickAssistProcessor_extract_to_local_all_description;
      Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_LOCAL);
      int relevance;
      if (context.getSelectionLength() == 0) {
        relevance = IProposalRelevance.EXTRACT_LOCAL_ALL_ZERO_SELECTION;
      } else if (problemsAtLocation) {
        relevance = IProposalRelevance.EXTRACT_LOCAL_ALL_ERROR;
      } else {
        relevance = IProposalRelevance.EXTRACT_LOCAL_ALL;
      }
      RefactoringCorrectionProposal proposal =
          new RefactoringCorrectionProposal(label, cu, extractTempRefactoring, relevance, image) {
            @Override
            protected void init(Refactoring refactoring) throws CoreException {
              ExtractTempRefactoring etr = (ExtractTempRefactoring) refactoring;
              etr.setTempName(etr.guessTempName()); // expensive
            }
          };
      proposal.setCommandId(EXTRACT_LOCAL_ID);
      proposal.setLinkedProposalModel(linkedProposalModel);
      proposals.add(proposal);
    }

    ExtractTempRefactoring extractTempRefactoringSelectedOnly =
        new ExtractTempRefactoring(
            context.getASTRoot(), context.getSelectionOffset(), context.getSelectionLength());
    extractTempRefactoringSelectedOnly.setReplaceAllOccurrences(false);
    if (extractTempRefactoringSelectedOnly
        .checkInitialConditions(new NullProgressMonitor())
        .isOK()) {
      LinkedProposalModel linkedProposalModel = new LinkedProposalModel();
      extractTempRefactoringSelectedOnly.setLinkedProposalModel(linkedProposalModel);
      extractTempRefactoringSelectedOnly.setCheckResultForCompileProblems(false);

      String label = CorrectionMessages.QuickAssistProcessor_extract_to_local_description;
      Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_LOCAL);
      int relevance;
      if (context.getSelectionLength() == 0) {
        relevance = IProposalRelevance.EXTRACT_LOCAL_ZERO_SELECTION;
      } else if (problemsAtLocation) {
        relevance = IProposalRelevance.EXTRACT_LOCAL_ERROR;
      } else {
        relevance = IProposalRelevance.EXTRACT_LOCAL;
      }
      RefactoringCorrectionProposal proposal =
          new RefactoringCorrectionProposal(
              label, cu, extractTempRefactoringSelectedOnly, relevance, image) {
            @Override
            protected void init(Refactoring refactoring) throws CoreException {
              ExtractTempRefactoring etr = (ExtractTempRefactoring) refactoring;
              etr.setTempName(etr.guessTempName()); // expensive
            }
          };
      proposal.setCommandId(EXTRACT_LOCAL_NOT_REPLACE_ID);
      proposal.setLinkedProposalModel(linkedProposalModel);
      proposals.add(proposal);
    }

    ExtractConstantRefactoring extractConstRefactoring =
        new ExtractConstantRefactoring(
            context.getASTRoot(), context.getSelectionOffset(), context.getSelectionLength());
    if (extractConstRefactoring.checkInitialConditions(new NullProgressMonitor()).isOK()) {
      LinkedProposalModel linkedProposalModel = new LinkedProposalModel();
      extractConstRefactoring.setLinkedProposalModel(linkedProposalModel);
      extractConstRefactoring.setCheckResultForCompileProblems(false);

      String label = CorrectionMessages.QuickAssistProcessor_extract_to_constant_description;
      Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_LOCAL);
      int relevance;
      if (context.getSelectionLength() == 0) {
        relevance = IProposalRelevance.EXTRACT_CONSTANT_ZERO_SELECTION;
      } else if (problemsAtLocation) {
        relevance = IProposalRelevance.EXTRACT_CONSTANT_ERROR;
      } else {
        relevance = IProposalRelevance.EXTRACT_CONSTANT;
      }
      RefactoringCorrectionProposal proposal =
          new RefactoringCorrectionProposal(label, cu, extractConstRefactoring, relevance, image) {
            @Override
            protected void init(Refactoring refactoring) throws CoreException {
              ExtractConstantRefactoring etr = (ExtractConstantRefactoring) refactoring;
              etr.setConstantName(etr.guessConstantName()); // expensive
            }
          };
      proposal.setCommandId(EXTRACT_CONSTANT_ID);
      proposal.setLinkedProposalModel(linkedProposalModel);
      proposals.add(proposal);
    }
    return false;
  }

  private static boolean getConvertAnonymousToNestedProposal(
      IInvocationContext context, final ASTNode node, Collection<ICommandAccess> proposals)
      throws CoreException {
    if (!(node instanceof Name)) return false;

    ASTNode normalized = ASTNodes.getNormalizedNode(node);
    if (normalized.getLocationInParent() != ClassInstanceCreation.TYPE_PROPERTY) return false;

    final AnonymousClassDeclaration anonymTypeDecl =
        ((ClassInstanceCreation) normalized.getParent()).getAnonymousClassDeclaration();
    if (anonymTypeDecl == null || anonymTypeDecl.resolveBinding() == null) {
      return false;
    }

    if (proposals == null) {
      return true;
    }

    final ICompilationUnit cu = context.getCompilationUnit();
    final ConvertAnonymousToNestedRefactoring refactoring =
        new ConvertAnonymousToNestedRefactoring(anonymTypeDecl);

    String extTypeName = ASTNodes.getSimpleNameIdentifier((Name) node);
    ITypeBinding anonymTypeBinding = anonymTypeDecl.resolveBinding();
    String className;
    if (anonymTypeBinding.getInterfaces().length == 0) {
      className =
          Messages.format(
              CorrectionMessages.QuickAssistProcessor_name_extension_from_interface, extTypeName);
    } else {
      className =
          Messages.format(
              CorrectionMessages.QuickAssistProcessor_name_extension_from_class, extTypeName);
    }
    String[][] existingTypes = ((IType) anonymTypeBinding.getJavaElement()).resolveType(className);
    int i = 1;
    while (existingTypes != null) {
      i++;
      existingTypes = ((IType) anonymTypeBinding.getJavaElement()).resolveType(className + i);
    }
    refactoring.setClassName(i == 1 ? className : className + i);

    if (refactoring.checkInitialConditions(new NullProgressMonitor()).isOK()) {
      LinkedProposalModel linkedProposalModel = new LinkedProposalModel();
      refactoring.setLinkedProposalModel(linkedProposalModel);

      String label = CorrectionMessages.QuickAssistProcessor_convert_anonym_to_nested;
      Image image =
          JavaPlugin.getImageDescriptorRegistry()
              .get(
                  JavaElementImageProvider.getTypeImageDescriptor(
                      true, false, Flags.AccPrivate, false));
      RefactoringCorrectionProposal proposal =
          new RefactoringCorrectionProposal(
              label, cu, refactoring, IProposalRelevance.CONVERT_ANONYMOUS_TO_NESTED, image);
      proposal.setLinkedProposalModel(linkedProposalModel);
      proposal.setCommandId(CONVERT_ANONYMOUS_TO_LOCAL_ID);
      proposals.add(proposal);
    }
    return false;
  }

  private static boolean getConvertAnonymousClassCreationsToLambdaProposals(
      IInvocationContext context,
      ASTNode covering,
      Collection<ICommandAccess> resultingCollections) {
    while (covering instanceof Name
        || covering instanceof Type
        || covering instanceof Dimension
        || covering.getParent() instanceof MethodDeclaration
        || covering.getLocationInParent() == AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY) {
      covering = covering.getParent();
    }

    ClassInstanceCreation cic;
    if (covering instanceof ClassInstanceCreation) {
      cic = (ClassInstanceCreation) covering;
    } else if (covering.getLocationInParent()
        == ClassInstanceCreation.ANONYMOUS_CLASS_DECLARATION_PROPERTY) {
      cic = (ClassInstanceCreation) covering.getParent();
    } else if (covering instanceof Name) {
      ASTNode normalized = ASTNodes.getNormalizedNode(covering);
      if (normalized.getLocationInParent() != ClassInstanceCreation.TYPE_PROPERTY) return false;
      cic = (ClassInstanceCreation) normalized.getParent();
    } else {
      return false;
    }

    IProposableFix fix = LambdaExpressionsFix.createConvertToLambdaFix(cic);
    if (fix == null) return false;

    if (resultingCollections == null) return true;

    // add correction proposal
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    Map<String, String> options = new Hashtable<String, String>();
    options.put(CleanUpConstants.CONVERT_FUNCTIONAL_INTERFACES, CleanUpOptions.TRUE);
    options.put(CleanUpConstants.USE_LAMBDA, CleanUpOptions.TRUE);
    FixCorrectionProposal proposal =
        new FixCorrectionProposal(
            fix,
            new ExpressionsCleanUp(options),
            IProposalRelevance.CONVERT_TO_LAMBDA_EXPRESSION,
            image,
            context);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getConvertLambdaToAnonymousClassCreationsProposals(
      IInvocationContext context,
      ASTNode covering,
      Collection<ICommandAccess> resultingCollections) {
    LambdaExpression lambda;
    if (covering instanceof LambdaExpression) {
      lambda = (LambdaExpression) covering;
    } else if (covering.getLocationInParent() == LambdaExpression.BODY_PROPERTY) {
      lambda = (LambdaExpression) covering.getParent();
    } else {
      return false;
    }

    IProposableFix fix = LambdaExpressionsFix.createConvertToAnonymousClassCreationsFix(lambda);
    if (fix == null) return false;

    if (resultingCollections == null) return true;

    // add correction proposal
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    Map<String, String> options = new Hashtable<String, String>();
    options.put(CleanUpConstants.CONVERT_FUNCTIONAL_INTERFACES, CleanUpOptions.TRUE);
    options.put(CleanUpConstants.USE_ANONYMOUS_CLASS_CREATION, CleanUpOptions.TRUE);
    FixCorrectionProposal proposal =
        new FixCorrectionProposal(
            fix,
            new LambdaExpressionsCleanUp(options),
            IProposalRelevance.CONVERT_TO_ANONYMOUS_CLASS_CREATION,
            image,
            context);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getChangeLambdaBodyToBlockProposal(
      IInvocationContext context,
      ASTNode covering,
      Collection<ICommandAccess> resultingCollections) {
    LambdaExpression lambda;
    if (covering instanceof LambdaExpression) {
      lambda = (LambdaExpression) covering;
    } else if (covering.getLocationInParent() == LambdaExpression.BODY_PROPERTY) {
      lambda = (LambdaExpression) covering.getParent();
    } else {
      return false;
    }

    if (!(lambda.getBody() instanceof Expression)) return false;
    if (lambda.resolveMethodBinding() == null) return false;

    if (resultingCollections == null) return true;

    AST ast = lambda.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);

    Statement statementInBlockBody;
    Expression bodyExpr = (Expression) rewrite.createMoveTarget(lambda.getBody());
    if (ast.resolveWellKnownType("void")
        .isEqualTo(lambda.resolveMethodBinding().getReturnType())) { // $NON-NLS-1$
      ExpressionStatement expressionStatement = ast.newExpressionStatement(bodyExpr);
      statementInBlockBody = expressionStatement;
    } else {
      ReturnStatement returnStatement = ast.newReturnStatement();
      returnStatement.setExpression(bodyExpr);
      statementInBlockBody = returnStatement;
    }
    Block blockBody = ast.newBlock();
    blockBody.statements().add(statementInBlockBody);

    rewrite.set(lambda, LambdaExpression.BODY_PROPERTY, blockBody, null);

    // add proposal
    String label = CorrectionMessages.QuickAssistProcessor_change_lambda_body_to_block;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.CHANGE_LAMBDA_BODY_TO_BLOCK,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getChangeLambdaBodyToExpressionProposal(
      IInvocationContext context,
      ASTNode covering,
      Collection<ICommandAccess> resultingCollections) {
    LambdaExpression lambda;
    if (covering instanceof LambdaExpression) {
      lambda = (LambdaExpression) covering;
    } else if (covering.getLocationInParent() == LambdaExpression.BODY_PROPERTY) {
      lambda = (LambdaExpression) covering.getParent();
    } else {
      return false;
    }

    if (!(lambda.getBody() instanceof Block)) return false;

    Block lambdaBody = (Block) lambda.getBody();
    if (lambdaBody.statements().size() != 1) return false;

    Expression exprBody;
    Statement singleStatement = (Statement) lambdaBody.statements().get(0);
    if (singleStatement instanceof ReturnStatement) {
      Expression returnExpr = ((ReturnStatement) singleStatement).getExpression();
      if (returnExpr == null) return false;
      exprBody = returnExpr;
    } else if (singleStatement instanceof ExpressionStatement) {
      Expression expression = ((ExpressionStatement) singleStatement).getExpression();
      if (isValidExpressionBody(expression)) {
        exprBody = expression;
      } else {
        return false;
      }
    } else {
      return false;
    }

    if (resultingCollections == null) return true;

    AST ast = lambda.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);

    Expression movedBody = (Expression) rewrite.createMoveTarget(exprBody);
    rewrite.set(lambda, LambdaExpression.BODY_PROPERTY, movedBody, null);

    // add proposal
    String label = CorrectionMessages.QuickAssistProcessor_change_lambda_body_to_expression;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.CHANGE_LAMBDA_BODY_TO_EXPRESSION,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean isValidExpressionBody(Expression expression) {
    boolean isValidExpressionBody =
        expression instanceof Assignment
            || expression instanceof ClassInstanceCreation
            || expression instanceof MethodInvocation
            || expression instanceof PostfixExpression
            || expression instanceof SuperMethodInvocation;
    if (expression instanceof PrefixExpression) {
      Operator operator = ((PrefixExpression) expression).getOperator();
      if (operator == Operator.INCREMENT || operator == Operator.DECREMENT) {
        isValidExpressionBody = true;
      }
    }
    return isValidExpressionBody;
  }

  public static boolean getInferDiamondArgumentsProposal(
      IInvocationContext context,
      ASTNode node,
      IProblemLocation[] locations,
      Collection<ICommandAccess> resultingCollections) {
    // don't add if already added as quick fix
    if (containsMatchingProblem(locations, IProblem.DiamondNotBelow17)) return false;
    ParameterizedType createdType = null;

    if (node instanceof Name) {
      Name name = ASTNodes.getTopMostName((Name) node);
      if (name.getLocationInParent() == SimpleType.NAME_PROPERTY
          || name.getLocationInParent() == NameQualifiedType.NAME_PROPERTY) {
        ASTNode type = name.getParent();
        if (type.getLocationInParent() == ParameterizedType.TYPE_PROPERTY) {
          createdType = (ParameterizedType) type.getParent();
          if (createdType.getLocationInParent() != ClassInstanceCreation.TYPE_PROPERTY) {
            return false;
          }
        }
      }
    } else if (node instanceof ParameterizedType) {
      createdType = (ParameterizedType) node;
      if (createdType.getLocationInParent() != ClassInstanceCreation.TYPE_PROPERTY) {
        return false;
      }
    } else if (node instanceof ClassInstanceCreation) {
      ClassInstanceCreation creation = (ClassInstanceCreation) node;
      Type type = creation.getType();
      if (type instanceof ParameterizedType) {
        createdType = (ParameterizedType) type;
      }
    }

    IProposableFix fix =
        TypeParametersFix.createInsertInferredTypeArgumentsFix(context.getASTRoot(), createdType);
    if (fix != null && resultingCollections != null) {
      Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
      int relevance =
          locations == null
              ? IProposalRelevance.INSERT_INFERRED_TYPE_ARGUMENTS
              : IProposalRelevance.INSERT_INFERRED_TYPE_ARGUMENTS_ERROR; // if error -> higher than
      // ReorgCorrectionsSubProcessor.getNeedHigherComplianceProposals()
      Map<String, String> options = new HashMap<String, String>();
      options.put(CleanUpConstants.USE_TYPE_ARGUMENTS, CleanUpOptions.TRUE);
      options.put(CleanUpConstants.INSERT_INFERRED_TYPE_ARGUMENTS, CleanUpOptions.TRUE);
      FixCorrectionProposal proposal =
          new FixCorrectionProposal(
              fix, new TypeParametersCleanUp(options), relevance, image, context);
      resultingCollections.add(proposal);
    }
    return true;
  }

  private static boolean getJoinVariableProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    ASTNode parent = node.getParent();

    VariableDeclarationFragment fragment = null;
    boolean onFirstAccess = false;
    if (node instanceof SimpleName
        && node.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY) {
      onFirstAccess = true;
      SimpleName name = (SimpleName) node;
      IBinding binding = name.resolveBinding();
      if (!(binding instanceof IVariableBinding)) {
        return false;
      }
      ASTNode declaring = context.getASTRoot().findDeclaringNode(binding);
      if (declaring instanceof VariableDeclarationFragment) {
        fragment = (VariableDeclarationFragment) declaring;
      } else {
        return false;
      }
    } else if (parent instanceof VariableDeclarationFragment) {
      fragment = (VariableDeclarationFragment) parent;
    } else {
      return false;
    }

    IVariableBinding binding = fragment.resolveBinding();
    Expression initializer = fragment.getInitializer();
    if ((initializer != null && initializer.getNodeType() != ASTNode.NULL_LITERAL)
        || binding == null
        || binding.isField()) {
      return false;
    }

    if (!(fragment.getParent() instanceof VariableDeclarationStatement)) {
      return false;
    }
    VariableDeclarationStatement statement = (VariableDeclarationStatement) fragment.getParent();

    SimpleName[] names = LinkedNodeFinder.findByBinding(statement.getParent(), binding);
    if (names.length <= 1 || names[0] != fragment.getName()) {
      return false;
    }
    SimpleName firstAccess = names[1];
    if (onFirstAccess) {
      if (firstAccess != node) {
        return false;
      }
    } else {
      if (firstAccess.getLocationInParent() != Assignment.LEFT_HAND_SIDE_PROPERTY) {
        return false;
      }
    }
    Assignment assignment = (Assignment) firstAccess.getParent();
    if (assignment.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
      return false;
    }
    ExpressionStatement assignParent = (ExpressionStatement) assignment.getParent();

    if (resultingCollections == null) {
      return true;
    }

    AST ast = statement.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);
    TightSourceRangeComputer sourceRangeComputer = new TightSourceRangeComputer();
    sourceRangeComputer.addTightSourceNode(assignParent);
    rewrite.setTargetSourceRangeComputer(sourceRangeComputer);

    String label = CorrectionMessages.QuickAssistProcessor_joindeclaration_description;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_LOCAL);
    LinkedCorrectionProposal proposal =
        new LinkedCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.JOIN_VARIABLE_DECLARATION,
            image);
    proposal.setCommandId(SPLIT_JOIN_VARIABLE_DECLARATION_ID);

    Expression placeholder = (Expression) rewrite.createMoveTarget(assignment.getRightHandSide());
    rewrite.set(fragment, VariableDeclarationFragment.INITIALIZER_PROPERTY, placeholder, null);

    if (onFirstAccess) {
      // replace assignment with variable declaration
      rewrite.replace(assignParent, rewrite.createMoveTarget(statement), null);
    } else {
      // different scopes -> remove assignments, set variable initializer
      if (ASTNodes.isControlStatementBody(assignParent.getLocationInParent())) {
        Block block = ast.newBlock();
        rewrite.replace(assignParent, block, null);
      } else {
        rewrite.remove(assignParent, null);
      }
    }

    proposal.setEndPosition(rewrite.track(fragment.getName()));
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getSplitVariableProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    VariableDeclarationFragment fragment;
    if (node instanceof VariableDeclarationFragment) {
      fragment = (VariableDeclarationFragment) node;
    } else if (node.getLocationInParent() == VariableDeclarationFragment.NAME_PROPERTY) {
      fragment = (VariableDeclarationFragment) node.getParent();
    } else {
      return false;
    }

    if (fragment.getInitializer() == null) {
      return false;
    }

    Statement statement;
    ASTNode fragParent = fragment.getParent();
    if (fragParent instanceof VariableDeclarationStatement) {
      statement = (VariableDeclarationStatement) fragParent;
    } else if (fragParent instanceof VariableDeclarationExpression) {
      if (fragParent.getLocationInParent() == TryStatement.RESOURCES_PROPERTY) {
        return false;
      }
      statement = (Statement) fragParent.getParent();
    } else {
      return false;
    }
    // statement is ForStatement or VariableDeclarationStatement

    ASTNode statementParent = statement.getParent();
    StructuralPropertyDescriptor property = statement.getLocationInParent();
    if (!property.isChildListProperty()) {
      return false;
    }

    List<? extends ASTNode> list =
        ASTNodes.getChildListProperty(statementParent, (ChildListPropertyDescriptor) property);

    if (resultingCollections == null) {
      return true;
    }

    AST ast = statement.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);

    String label = CorrectionMessages.QuickAssistProcessor_splitdeclaration_description;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_LOCAL);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.SPLIT_VARIABLE_DECLARATION,
            image);
    boolean commandConflict = false;
    for (Iterator<ICommandAccess> iterator = resultingCollections.iterator();
        iterator.hasNext(); ) {
      Object completionProposal = iterator.next();
      if (completionProposal instanceof ChangeCorrectionProposal) {
        if (SPLIT_JOIN_VARIABLE_DECLARATION_ID.equals(
            ((ChangeCorrectionProposal) completionProposal).getCommandId())) {
          commandConflict = true;
        }
      }
    }
    if (!commandConflict) {
      proposal.setCommandId(SPLIT_JOIN_VARIABLE_DECLARATION_ID);
    }

    Statement newStatement;
    int insertIndex = list.indexOf(statement);

    Expression placeholder = (Expression) rewrite.createMoveTarget(fragment.getInitializer());
    ITypeBinding binding = fragment.getInitializer().resolveTypeBinding();
    if (placeholder instanceof ArrayInitializer && binding != null && binding.isArray()) {
      ArrayCreation creation = ast.newArrayCreation();
      creation.setInitializer((ArrayInitializer) placeholder);
      final ITypeBinding componentType = binding.getElementType();
      Type type = null;
      if (componentType.isPrimitive())
        type = ast.newPrimitiveType(PrimitiveType.toCode(componentType.getName()));
      else type = ast.newSimpleType(ast.newSimpleName(componentType.getName()));
      creation.setType(ast.newArrayType(type, binding.getDimensions()));
      placeholder = creation;
    }
    Assignment assignment = ast.newAssignment();
    assignment.setRightHandSide(placeholder);
    assignment.setLeftHandSide(ast.newSimpleName(fragment.getName().getIdentifier()));

    if (statement instanceof VariableDeclarationStatement) {
      newStatement = ast.newExpressionStatement(assignment);
      insertIndex += 1; // add after declaration
    } else {
      rewrite.replace(fragment.getParent(), assignment, null);
      VariableDeclarationFragment newFrag = ast.newVariableDeclarationFragment();
      newFrag.setName(ast.newSimpleName(fragment.getName().getIdentifier()));
      newFrag
          .extraDimensions()
          .addAll(DimensionRewrite.copyDimensions(fragment.extraDimensions(), rewrite));

      VariableDeclarationExpression oldVarDecl = (VariableDeclarationExpression) fragParent;

      VariableDeclarationStatement newVarDec = ast.newVariableDeclarationStatement(newFrag);
      newVarDec.setType((Type) rewrite.createCopyTarget(oldVarDecl.getType()));
      newVarDec.modifiers().addAll(ASTNodeFactory.newModifiers(ast, oldVarDecl.getModifiers()));
      newStatement = newVarDec;
    }

    ListRewrite listRewriter =
        rewrite.getListRewrite(statementParent, (ChildListPropertyDescriptor) property);
    listRewriter.insertAt(newStatement, insertIndex, null);

    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getConvertStringConcatenationProposals(
      IInvocationContext context, Collection<ICommandAccess> resultingCollections) {
    ASTNode node = context.getCoveringNode();
    BodyDeclaration parentDecl = ASTResolving.findParentBodyDeclaration(node);
    if (!(parentDecl instanceof MethodDeclaration || parentDecl instanceof Initializer))
      return false;

    AST ast = node.getAST();
    ITypeBinding stringBinding = ast.resolveWellKnownType("java.lang.String"); // $NON-NLS-1$

    if (node instanceof Expression && !(node instanceof InfixExpression)) {
      node = node.getParent();
    }
    if (node instanceof VariableDeclarationFragment) {
      node = ((VariableDeclarationFragment) node).getInitializer();
    } else if (node instanceof Assignment) {
      node = ((Assignment) node).getRightHandSide();
    }

    InfixExpression oldInfixExpression = null;
    while (node instanceof InfixExpression) {
      InfixExpression curr = (InfixExpression) node;
      if (curr.resolveTypeBinding() == stringBinding
          && curr.getOperator() == InfixExpression.Operator.PLUS) {
        oldInfixExpression = curr; // is a infix expression we can use
      } else {
        break;
      }
      node = node.getParent();
    }
    if (oldInfixExpression == null) return false;

    if (resultingCollections == null) {
      return true;
    }

    LinkedCorrectionProposal stringBufferProposal =
        getConvertToStringBufferProposal(context, ast, oldInfixExpression);
    resultingCollections.add(stringBufferProposal);

    ASTRewriteCorrectionProposal messageFormatProposal =
        getConvertToMessageFormatProposal(context, ast, oldInfixExpression);
    if (messageFormatProposal != null) resultingCollections.add(messageFormatProposal);

    return true;
  }

  private static LinkedCorrectionProposal getConvertToStringBufferProposal(
      IInvocationContext context, AST ast, InfixExpression oldInfixExpression) {
    String bufferOrBuilderName;
    ICompilationUnit cu = context.getCompilationUnit();
    if (JavaModelUtil.is50OrHigher(cu.getJavaProject())) {
      bufferOrBuilderName = "StringBuilder"; // $NON-NLS-1$
    } else {
      bufferOrBuilderName = "StringBuffer"; // $NON-NLS-1$
    }

    ASTRewrite rewrite = ASTRewrite.create(ast);

    SimpleName existingBuffer = getEnclosingAppendBuffer(oldInfixExpression);

    String mechanismName =
        BasicElementLabels.getJavaElementName(
            existingBuffer == null ? bufferOrBuilderName : existingBuffer.getIdentifier());
    String label =
        Messages.format(
            CorrectionMessages.QuickAssistProcessor_convert_to_string_buffer_description,
            mechanismName);
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    LinkedCorrectionProposal proposal =
        new LinkedCorrectionProposal(
            label, cu, rewrite, IProposalRelevance.CONVERT_TO_STRING_BUFFER, image);
    proposal.setCommandId(CONVERT_TO_STRING_BUFFER_ID);

    Statement insertAfter;
    String bufferName;

    String groupID = "nameId"; // $NON-NLS-1$
    ListRewrite listRewrite;

    Statement enclosingStatement = ASTResolving.findParentStatement(oldInfixExpression);

    if (existingBuffer != null) {
      if (ASTNodes.isControlStatementBody(enclosingStatement.getLocationInParent())) {
        Block newBlock = ast.newBlock();
        listRewrite = rewrite.getListRewrite(newBlock, Block.STATEMENTS_PROPERTY);
        insertAfter = null;
        rewrite.replace(enclosingStatement, newBlock, null);
      } else {
        listRewrite =
            rewrite.getListRewrite(
                enclosingStatement.getParent(),
                (ChildListPropertyDescriptor) enclosingStatement.getLocationInParent());
        insertAfter = enclosingStatement;
      }

      bufferName = existingBuffer.getIdentifier();

    } else {
      // create buffer
      VariableDeclarationFragment frag = ast.newVariableDeclarationFragment();
      // check if name is already in use and provide alternative
      List<String> fExcludedVariableNames =
          Arrays.asList(ASTResolving.getUsedVariableNames(oldInfixExpression));

      SimpleType bufferType = ast.newSimpleType(ast.newName(bufferOrBuilderName));
      ClassInstanceCreation newBufferExpression = ast.newClassInstanceCreation();

      String[] newBufferNames =
          StubUtility.getVariableNameSuggestions(
              NamingConventions.VK_LOCAL,
              cu.getJavaProject(),
              bufferOrBuilderName,
              0,
              fExcludedVariableNames,
              true);
      bufferName = newBufferNames[0];

      SimpleName bufferNameDeclaration = ast.newSimpleName(bufferName);
      frag.setName(bufferNameDeclaration);

      proposal.addLinkedPosition(rewrite.track(bufferNameDeclaration), true, groupID);
      for (int i = 0; i < newBufferNames.length; i++) {
        proposal.addLinkedPositionProposal(groupID, newBufferNames[i], null);
      }

      newBufferExpression.setType(bufferType);
      frag.setInitializer(newBufferExpression);

      VariableDeclarationStatement bufferDeclaration = ast.newVariableDeclarationStatement(frag);
      bufferDeclaration.setType(ast.newSimpleType(ast.newName(bufferOrBuilderName)));
      insertAfter = bufferDeclaration;

      Statement statement = ASTResolving.findParentStatement(oldInfixExpression);
      if (ASTNodes.isControlStatementBody(statement.getLocationInParent())) {
        Block newBlock = ast.newBlock();
        listRewrite = rewrite.getListRewrite(newBlock, Block.STATEMENTS_PROPERTY);
        listRewrite.insertFirst(bufferDeclaration, null);
        listRewrite.insertLast(rewrite.createMoveTarget(statement), null);
        rewrite.replace(statement, newBlock, null);
      } else {
        listRewrite =
            rewrite.getListRewrite(
                statement.getParent(),
                (ChildListPropertyDescriptor) statement.getLocationInParent());
        listRewrite.insertBefore(bufferDeclaration, statement, null);
      }
    }

    List<Expression> operands = new ArrayList<Expression>();
    collectInfixPlusOperands(oldInfixExpression, operands);

    Statement lastAppend = insertAfter;
    for (Iterator<Expression> iter = operands.iterator(); iter.hasNext(); ) {
      Expression operand = iter.next();

      MethodInvocation appendIncovationExpression = ast.newMethodInvocation();
      appendIncovationExpression.setName(ast.newSimpleName("append")); // $NON-NLS-1$
      SimpleName bufferNameReference = ast.newSimpleName(bufferName);

      // If there was an existing name, don't offer to rename it
      if (existingBuffer == null) {
        proposal.addLinkedPosition(rewrite.track(bufferNameReference), true, groupID);
      }

      appendIncovationExpression.setExpression(bufferNameReference);
      appendIncovationExpression.arguments().add(rewrite.createCopyTarget(operand));

      ExpressionStatement appendExpressionStatement =
          ast.newExpressionStatement(appendIncovationExpression);
      if (lastAppend == null) {
        listRewrite.insertFirst(appendExpressionStatement, null);
      } else {
        listRewrite.insertAfter(appendExpressionStatement, lastAppend, null);
      }
      lastAppend = appendExpressionStatement;
    }

    if (existingBuffer != null) {
      proposal.setEndPosition(rewrite.track(lastAppend));
      if (insertAfter != null) {
        rewrite.remove(enclosingStatement, null);
      }
    } else {
      // replace old expression with toString
      MethodInvocation bufferToString = ast.newMethodInvocation();
      bufferToString.setName(ast.newSimpleName("toString")); // $NON-NLS-1$
      SimpleName bufferNameReference = ast.newSimpleName(bufferName);
      bufferToString.setExpression(bufferNameReference);
      proposal.addLinkedPosition(rewrite.track(bufferNameReference), true, groupID);

      rewrite.replace(oldInfixExpression, bufferToString, null);
      proposal.setEndPosition(rewrite.track(bufferToString));
    }

    return proposal;
  }

  private static void collectInfixPlusOperands(Expression expression, List<Expression> collector) {
    if (expression instanceof InfixExpression
        && ((InfixExpression) expression).getOperator() == InfixExpression.Operator.PLUS) {
      InfixExpression infixExpression = (InfixExpression) expression;

      collectInfixPlusOperands(infixExpression.getLeftOperand(), collector);
      collectInfixPlusOperands(infixExpression.getRightOperand(), collector);
      List<Expression> extendedOperands = infixExpression.extendedOperands();
      for (Iterator<Expression> iter = extendedOperands.iterator(); iter.hasNext(); ) {
        collectInfixPlusOperands(iter.next(), collector);
      }

    } else {
      collector.add(expression);
    }
  }

  /**
   * Checks
   *
   * <ul>
   *   <li>whether the given infix expression is the argument of a StringBuilder#append() or
   *       StringBuffer#append() invocation, and
   *   <li>the append method is called on a simple variable, and
   *   <li>the invocation occurs in a statement (not as nested expression)
   * </ul>
   *
   * @param infixExpression the infix expression
   * @return the name of the variable we were appending to, or <code>null</code> if not matching
   */
  private static SimpleName getEnclosingAppendBuffer(InfixExpression infixExpression) {
    if (infixExpression.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
      MethodInvocation methodInvocation = (MethodInvocation) infixExpression.getParent();

      // ..not in an expression.. (e.g. not sb.append("high" + 5).append(6);)
      if (methodInvocation.getParent() instanceof Statement) {

        // ..of a function called append:
        if ("append".equals(methodInvocation.getName().getIdentifier())) { // $NON-NLS-1$
          Expression expression = methodInvocation.getExpression();

          // ..and the append is being called on a Simple object:
          if (expression instanceof SimpleName) {
            IBinding binding = ((SimpleName) expression).resolveBinding();
            if (binding instanceof IVariableBinding) {
              String typeName = ((IVariableBinding) binding).getType().getQualifiedName();

              // And the object's type is a StringBuilder or StringBuffer:
              if ("java.lang.StringBuilder".equals(typeName)
                  || "java.lang.StringBuffer".equals(typeName)) { // $NON-NLS-1$ //$NON-NLS-2$
                return (SimpleName) expression;
              }
            }
          }
        }
      }
    }
    return null;
  }

  private static ASTRewriteCorrectionProposal getConvertToMessageFormatProposal(
      IInvocationContext context, AST ast, InfixExpression oldInfixExpression) {

    ICompilationUnit cu = context.getCompilationUnit();
    boolean is50OrHigher = JavaModelUtil.is50OrHigher(cu.getJavaProject());

    ASTRewrite rewrite = ASTRewrite.create(ast);
    CompilationUnit root = context.getASTRoot();
    ImportRewrite importRewrite = StubUtility.createImportRewrite(root, true);
    ContextSensitiveImportRewriteContext importContext =
        new ContextSensitiveImportRewriteContext(
            root, oldInfixExpression.getStartPosition(), importRewrite);

    // collect operands
    List<Expression> operands = new ArrayList<Expression>();
    collectInfixPlusOperands(oldInfixExpression, operands);

    List<Expression> formatArguments = new ArrayList<Expression>();
    String formatString = ""; // $NON-NLS-1$
    int i = 0;
    for (Iterator<Expression> iterator = operands.iterator(); iterator.hasNext(); ) {
      Expression operand = iterator.next();

      if (operand instanceof StringLiteral) {
        String value = ((StringLiteral) operand).getEscapedValue();
        value = value.substring(1, value.length() - 1);
        value = value.replaceAll("'", "''"); // $NON-NLS-1$ //$NON-NLS-2$
        formatString += value;
      } else {
        formatString += "{" + i + "}"; // $NON-NLS-1$ //$NON-NLS-2$

        Expression argument;
        if (is50OrHigher) {
          argument = (Expression) rewrite.createCopyTarget(operand);
        } else {
          ITypeBinding binding = operand.resolveTypeBinding();
          if (binding == null) return null;

          argument = (Expression) rewrite.createCopyTarget(operand);

          if (binding.isPrimitive()) {
            ITypeBinding boxedBinding = Bindings.getBoxedTypeBinding(binding, ast);
            if (boxedBinding != binding) {
              Type boxedType = importRewrite.addImport(boxedBinding, ast, importContext);
              ClassInstanceCreation cic = ast.newClassInstanceCreation();
              cic.setType(boxedType);
              cic.arguments().add(argument);
              argument = cic;
            }
          }
        }

        formatArguments.add(argument);
        i++;
      }
    }

    if (formatArguments.size() == 0) return null;

    String label = CorrectionMessages.QuickAssistProcessor_convert_to_message_format;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);

    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label, cu, rewrite, IProposalRelevance.CONVERT_TO_MESSAGE_FORMAT, image);
    proposal.setCommandId(CONVERT_TO_MESSAGE_FORMAT_ID);

    proposal.setImportRewrite(importRewrite);

    String messageType =
        importRewrite.addImport("java.text.MessageFormat", importContext); // $NON-NLS-1$

    MethodInvocation formatInvocation = ast.newMethodInvocation();
    formatInvocation.setExpression(ast.newName(messageType));
    formatInvocation.setName(ast.newSimpleName("format")); // $NON-NLS-1$

    List<Expression> arguments = formatInvocation.arguments();

    StringLiteral formatStringArgument = ast.newStringLiteral();
    formatStringArgument.setEscapedValue("\"" + formatString + "\""); // $NON-NLS-1$ //$NON-NLS-2$
    arguments.add(formatStringArgument);

    if (is50OrHigher) {
      for (Iterator<Expression> iterator = formatArguments.iterator(); iterator.hasNext(); ) {
        arguments.add(iterator.next());
      }
    } else {
      ArrayCreation objectArrayCreation = ast.newArrayCreation();

      Type objectType = ast.newSimpleType(ast.newSimpleName("Object")); // $NON-NLS-1$
      ArrayType arrayType = ast.newArrayType(objectType);
      objectArrayCreation.setType(arrayType);

      ArrayInitializer arrayInitializer = ast.newArrayInitializer();

      List<Expression> initializerExpressions = arrayInitializer.expressions();
      for (Iterator<Expression> iterator = formatArguments.iterator(); iterator.hasNext(); ) {
        initializerExpressions.add(iterator.next());
      }
      objectArrayCreation.setInitializer(arrayInitializer);

      arguments.add(objectArrayCreation);
    }

    rewrite.replace(oldInfixExpression, formatInvocation, null);

    return proposal;
  }

  public static boolean getAssignToVariableProposals(
      IInvocationContext context,
      ASTNode node,
      IProblemLocation[] locations,
      Collection<ICommandAccess> resultingCollections) {
    Statement statement = ASTResolving.findParentStatement(node);
    if (!(statement instanceof ExpressionStatement)) {
      return false;
    }
    ExpressionStatement expressionStatement = (ExpressionStatement) statement;

    Expression expression = expressionStatement.getExpression();
    if (expression.getNodeType() == ASTNode.ASSIGNMENT) {
      return false; // too confusing and not helpful
    }

    ITypeBinding typeBinding = expression.resolveTypeBinding();
    typeBinding = Bindings.normalizeTypeBinding(typeBinding);
    if (typeBinding == null) {
      return false;
    }
    if (resultingCollections == null) {
      return true;
    }

    // don't add if already added as quick fix
    if (containsMatchingProblem(locations, IProblem.UnusedObjectAllocation)) return false;

    ICompilationUnit cu = context.getCompilationUnit();

    AssignToVariableAssistProposal localProposal =
        new AssignToVariableAssistProposal(
            cu,
            AssignToVariableAssistProposal.LOCAL,
            expressionStatement,
            typeBinding,
            IProposalRelevance.ASSIGN_TO_LOCAL);
    localProposal.setCommandId(ASSIGN_TO_LOCAL_ID);
    resultingCollections.add(localProposal);

    ASTNode type = ASTResolving.findParentType(expression);
    if (type != null) {
      AssignToVariableAssistProposal fieldProposal =
          new AssignToVariableAssistProposal(
              cu,
              AssignToVariableAssistProposal.FIELD,
              expressionStatement,
              typeBinding,
              IProposalRelevance.ASSIGN_TO_FIELD);
      fieldProposal.setCommandId(ASSIGN_TO_FIELD_ID);
      resultingCollections.add(fieldProposal);
    }
    return true;
  }

  private static boolean containsMatchingProblem(IProblemLocation[] locations, int problemId) {
    if (locations != null) {
      for (int i = 0; i < locations.length; i++) {
        IProblemLocation location = locations[i];
        if (IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER.equals(location.getMarkerType())
            && location.getProblemId() == problemId) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean getAssignParamToFieldProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    node = ASTNodes.getNormalizedNode(node);
    ASTNode parent = node.getParent();
    if (!(parent instanceof SingleVariableDeclaration)
        || !(parent.getParent() instanceof MethodDeclaration)) {
      return false;
    }
    SingleVariableDeclaration paramDecl = (SingleVariableDeclaration) parent;
    IVariableBinding binding = paramDecl.resolveBinding();

    MethodDeclaration methodDecl = (MethodDeclaration) parent.getParent();
    if (binding == null || methodDecl.getBody() == null) {
      return false;
    }
    ITypeBinding typeBinding = binding.getType();
    if (typeBinding == null) {
      return false;
    }

    if (resultingCollections == null) {
      return true;
    }

    ITypeBinding parentType = Bindings.getBindingOfParentType(node);
    if (parentType != null) {
      if (parentType.isInterface()) {
        return false;
      }
      // assign to existing fields
      CompilationUnit root = context.getASTRoot();
      IVariableBinding[] declaredFields = parentType.getDeclaredFields();
      boolean isStaticContext = ASTResolving.isInStaticContext(node);
      for (int i = 0; i < declaredFields.length; i++) {
        IVariableBinding curr = declaredFields[i];
        if (isStaticContext == Modifier.isStatic(curr.getModifiers())
            && typeBinding.isAssignmentCompatible(curr.getType())) {
          ASTNode fieldDeclFrag = root.findDeclaringNode(curr);
          if (fieldDeclFrag instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) fieldDeclFrag;
            if (fragment.getInitializer() == null) {
              resultingCollections.add(
                  new AssignToVariableAssistProposal(
                      context.getCompilationUnit(),
                      paramDecl,
                      fragment,
                      typeBinding,
                      IProposalRelevance.ASSIGN_PARAM_TO_EXISTING_FIELD));
            }
          }
        }
      }
    }

    AssignToVariableAssistProposal fieldProposal =
        new AssignToVariableAssistProposal(
            context.getCompilationUnit(),
            paramDecl,
            null,
            typeBinding,
            IProposalRelevance.ASSIGN_PARAM_TO_NEW_FIELD);
    fieldProposal.setCommandId(ASSIGN_PARAM_TO_FIELD_ID);
    resultingCollections.add(fieldProposal);
    return true;
  }

  private static boolean getAddFinallyProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    TryStatement tryStatement = ASTResolving.findParentTryStatement(node);
    if (tryStatement == null || tryStatement.getFinally() != null) {
      return false;
    }
    Statement statement = ASTResolving.findParentStatement(node);
    if (tryStatement != statement && tryStatement.getBody() != statement) {
      return false; // an node inside a catch or finally block
    }

    if (resultingCollections == null) {
      return true;
    }

    AST ast = tryStatement.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);
    Block finallyBody = ast.newBlock();

    rewrite.set(tryStatement, TryStatement.FINALLY_PROPERTY, finallyBody, null);

    String label = CorrectionMessages.QuickAssistProcessor_addfinallyblock_description;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_ADD);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.ADD_FINALLY_BLOCK,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getAddElseProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    if (!(node instanceof IfStatement)) {
      return false;
    }
    IfStatement ifStatement = (IfStatement) node;
    if (ifStatement.getElseStatement() != null) {
      return false;
    }

    if (resultingCollections == null) {
      return true;
    }

    AST ast = node.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);
    Block body = ast.newBlock();

    rewrite.set(ifStatement, IfStatement.ELSE_STATEMENT_PROPERTY, body, null);

    String label = CorrectionMessages.QuickAssistProcessor_addelseblock_description;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_ADD);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label, context.getCompilationUnit(), rewrite, IProposalRelevance.ADD_ELSE_BLOCK, image);
    resultingCollections.add(proposal);
    return true;
  }

  public static boolean getCatchClauseToThrowsProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    CatchClause catchClause = (CatchClause) ASTResolving.findAncestor(node, ASTNode.CATCH_CLAUSE);
    if (catchClause == null) {
      return false;
    }

    Statement statement = ASTResolving.findParentStatement(node);
    if (statement != catchClause.getParent() && statement != catchClause.getBody()) {
      return false; // selection is in a statement inside the body
    }

    Type type = catchClause.getException().getType();
    if (!type.isSimpleType() && !type.isUnionType() && !type.isNameQualifiedType()) {
      return false;
    }

    BodyDeclaration bodyDeclaration = ASTResolving.findParentBodyDeclaration(catchClause);
    if (!(bodyDeclaration instanceof MethodDeclaration)
        && !(bodyDeclaration instanceof Initializer)) {
      return false;
    }

    if (resultingCollections == null) {
      return true;
    }

    AST ast = bodyDeclaration.getAST();
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_OBJS_EXCEPTION);

    Type selectedMultiCatchType = null;
    if (type.isUnionType() && node instanceof Name) {
      Name topMostName = ASTNodes.getTopMostName((Name) node);
      ASTNode parent = topMostName.getParent();
      if (parent instanceof SimpleType) {
        selectedMultiCatchType = (SimpleType) parent;
      } else if (parent instanceof NameQualifiedType) {
        selectedMultiCatchType = (NameQualifiedType) parent;
      }
    }

    if (bodyDeclaration instanceof MethodDeclaration) {
      MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;

      ASTRewrite rewrite = ASTRewrite.create(ast);
      if (selectedMultiCatchType != null) {
        removeException(rewrite, (UnionType) type, selectedMultiCatchType);
        addExceptionToThrows(ast, methodDeclaration, rewrite, selectedMultiCatchType);
        String label = CorrectionMessages.QuickAssistProcessor_exceptiontothrows_description;
        ASTRewriteCorrectionProposal proposal =
            new ASTRewriteCorrectionProposal(
                label,
                context.getCompilationUnit(),
                rewrite,
                IProposalRelevance.REPLACE_EXCEPTION_WITH_THROWS,
                image);
        resultingCollections.add(proposal);
      } else {
        removeCatchBlock(rewrite, catchClause);
        if (type.isUnionType()) {
          UnionType unionType = (UnionType) type;
          List<Type> types = unionType.types();
          for (Type elementType : types) {
            if (!(elementType instanceof SimpleType || elementType instanceof NameQualifiedType))
              return false;
            addExceptionToThrows(ast, methodDeclaration, rewrite, elementType);
          }
        } else {
          addExceptionToThrows(ast, methodDeclaration, rewrite, type);
        }
        String label = CorrectionMessages.QuickAssistProcessor_catchclausetothrows_description;
        ASTRewriteCorrectionProposal proposal =
            new ASTRewriteCorrectionProposal(
                label,
                context.getCompilationUnit(),
                rewrite,
                IProposalRelevance.REPLACE_CATCH_CLAUSE_WITH_THROWS,
                image);
        resultingCollections.add(proposal);
      }
    }
    { // for initializers or method declarations
      ASTRewrite rewrite = ASTRewrite.create(ast);
      if (selectedMultiCatchType != null) {
        removeException(rewrite, (UnionType) type, selectedMultiCatchType);
        String label = CorrectionMessages.QuickAssistProcessor_removeexception_description;
        ASTRewriteCorrectionProposal proposal =
            new ASTRewriteCorrectionProposal(
                label,
                context.getCompilationUnit(),
                rewrite,
                IProposalRelevance.REMOVE_EXCEPTION,
                image);
        resultingCollections.add(proposal);
      } else {
        removeCatchBlock(rewrite, catchClause);
        String label = CorrectionMessages.QuickAssistProcessor_removecatchclause_description;
        ASTRewriteCorrectionProposal proposal =
            new ASTRewriteCorrectionProposal(
                label,
                context.getCompilationUnit(),
                rewrite,
                IProposalRelevance.REMOVE_CATCH_CLAUSE,
                image);
        resultingCollections.add(proposal);
      }
    }

    return true;
  }

  private static void removeException(ASTRewrite rewrite, UnionType unionType, Type exception) {
    ListRewrite listRewrite = rewrite.getListRewrite(unionType, UnionType.TYPES_PROPERTY);
    List<Type> types = unionType.types();
    for (Iterator<Type> iterator = types.iterator(); iterator.hasNext(); ) {
      Type type = iterator.next();
      if (type.equals(exception)) {
        listRewrite.remove(type, null);
      }
    }
  }

  private static void addExceptionToThrows(
      AST ast, MethodDeclaration methodDeclaration, ASTRewrite rewrite, Type type2) {
    ITypeBinding binding = type2.resolveBinding();
    if (binding == null || isNotYetThrown(binding, methodDeclaration.thrownExceptionTypes())) {
      Type newType = (Type) ASTNode.copySubtree(ast, type2);

      ListRewrite listRewriter =
          rewrite.getListRewrite(
              methodDeclaration, MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY);
      listRewriter.insertLast(newType, null);
    }
  }

  private static void removeCatchBlock(ASTRewrite rewrite, CatchClause catchClause) {
    TryStatement tryStatement = (TryStatement) catchClause.getParent();
    if (tryStatement.catchClauses().size() > 1
        || tryStatement.getFinally() != null
        || !tryStatement.resources().isEmpty()) {
      rewrite.remove(catchClause, null);
    } else {
      Block block = tryStatement.getBody();
      List<Statement> statements = block.statements();
      int nStatements = statements.size();
      if (nStatements == 1) {
        ASTNode first = statements.get(0);
        rewrite.replace(tryStatement, rewrite.createCopyTarget(first), null);
      } else if (nStatements > 1) {
        ListRewrite listRewrite = rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
        ASTNode first = statements.get(0);
        ASTNode last = statements.get(statements.size() - 1);
        ASTNode newStatement = listRewrite.createCopyTarget(first, last);
        if (ASTNodes.isControlStatementBody(tryStatement.getLocationInParent())) {
          Block newBlock = rewrite.getAST().newBlock();
          newBlock.statements().add(newStatement);
          newStatement = newBlock;
        }
        rewrite.replace(tryStatement, newStatement, null);
      } else {
        rewrite.remove(tryStatement, null);
      }
    }
  }

  private static boolean isNotYetThrown(ITypeBinding binding, List<Type> thrownExceptions) {
    for (int i = 0; i < thrownExceptions.size(); i++) {
      Type name = thrownExceptions.get(i);
      ITypeBinding elem = name.resolveBinding();
      if (elem != null) {
        if (Bindings.isSuperType(elem, binding)) { // existing exception is base class of new
          return false;
        }
      }
    }
    return true;
  }

  private static boolean getPickoutTypeFromMulticatchProposals(
      IInvocationContext context,
      ASTNode node,
      ArrayList<ASTNode> coveredNodes,
      Collection<ICommandAccess> resultingCollections) {
    CatchClause catchClause = (CatchClause) ASTResolving.findAncestor(node, ASTNode.CATCH_CLAUSE);
    if (catchClause == null) {
      return false;
    }

    Statement statement = ASTResolving.findParentStatement(node);
    if (statement != catchClause.getParent() && statement != catchClause.getBody()) {
      return false; // selection is in a statement inside the body
    }

    Type type = catchClause.getException().getType();
    if (!type.isUnionType()) {
      return false;
    }

    Type selectedMultiCatchType = null;
    if (type.isUnionType() && node instanceof Name) {
      Name topMostName = ASTNodes.getTopMostName((Name) node);
      ASTNode parent = topMostName.getParent();
      if (parent instanceof SimpleType || parent instanceof NameQualifiedType) {
        selectedMultiCatchType = (Type) parent;
      }
    }

    boolean multipleExceptions = coveredNodes.size() > 1;
    if ((selectedMultiCatchType == null) && (!(node instanceof UnionType) || !multipleExceptions)) {
      return false;
    }

    if (!multipleExceptions) {
      coveredNodes.add(selectedMultiCatchType);
    }

    BodyDeclaration bodyDeclaration = ASTResolving.findParentBodyDeclaration(catchClause);
    if (!(bodyDeclaration instanceof MethodDeclaration)
        && !(bodyDeclaration instanceof Initializer)) {
      return false;
    }

    if (resultingCollections == null) {
      return true;
    }

    AST ast = bodyDeclaration.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);

    CatchClause newCatchClause = ast.newCatchClause();
    SingleVariableDeclaration newSingleVariableDeclaration = ast.newSingleVariableDeclaration();
    UnionType newUnionType = ast.newUnionType();
    List<Type> types = newUnionType.types();
    for (int i = 0; i < coveredNodes.size(); i++) {
      ASTNode typeNode = coveredNodes.get(i);
      types.add((Type) rewrite.createCopyTarget(typeNode));
      rewrite.remove(typeNode, null);
    }
    newSingleVariableDeclaration.setType(newUnionType);
    newSingleVariableDeclaration.setName(
        (SimpleName) rewrite.createCopyTarget(catchClause.getException().getName()));
    newCatchClause.setException(newSingleVariableDeclaration);

    setCatchClauseBody(newCatchClause, rewrite, catchClause);

    TryStatement tryStatement = (TryStatement) catchClause.getParent();
    ListRewrite listRewrite =
        rewrite.getListRewrite(tryStatement, TryStatement.CATCH_CLAUSES_PROPERTY);
    listRewrite.insertAfter(newCatchClause, catchClause, null);

    Image image = JavaPluginImages.get(JavaPluginImages.IMG_OBJS_EXCEPTION);
    String label =
        !multipleExceptions
            ? CorrectionMessages.QuickAssistProcessor_move_exception_to_separate_catch_block
            : CorrectionMessages.QuickAssistProcessor_move_exceptions_to_separate_catch_block;
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.MOVE_EXCEPTION_TO_SEPERATE_CATCH_BLOCK,
            image);

    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getConvertToMultiCatchProposals(
      IInvocationContext context,
      ASTNode covering,
      Collection<ICommandAccess> resultingCollections) {
    if (!JavaModelUtil.is17OrHigher(context.getCompilationUnit().getJavaProject())) return false;

    CatchClause catchClause =
        (CatchClause) ASTResolving.findAncestor(covering, ASTNode.CATCH_CLAUSE);
    if (catchClause == null) {
      return false;
    }

    Statement statement = ASTResolving.findParentStatement(covering);
    if (statement != catchClause.getParent() && statement != catchClause.getBody()) {
      return false; // selection is in a statement inside the body
    }

    Type type1 = catchClause.getException().getType();
    Type selectedMultiCatchType = null;
    if (type1.isUnionType() && covering instanceof Name) {
      Name topMostName = ASTNodes.getTopMostName((Name) covering);
      ASTNode parent = topMostName.getParent();
      if (parent instanceof SimpleType || parent instanceof NameQualifiedType) {
        selectedMultiCatchType = (Type) parent;
      }
    }
    if (selectedMultiCatchType != null) return false;

    TryStatement tryStatement = (TryStatement) catchClause.getParent();
    List<CatchClause> catchClauses = tryStatement.catchClauses();
    if (catchClauses.size() <= 1) return false;

    String commonSource = null;
    try {
      IBuffer buffer = context.getCompilationUnit().getBuffer();
      for (Iterator<CatchClause> iterator = catchClauses.iterator(); iterator.hasNext(); ) {
        CatchClause catchClause1 = iterator.next();
        Block body = catchClause1.getBody();
        String source = buffer.getText(body.getStartPosition(), body.getLength());
        if (commonSource == null) {
          commonSource = source;
        } else {
          if (!commonSource.equals(source)) return false;
        }
      }
    } catch (JavaModelException e) {
      return false;
    }

    if (resultingCollections == null) return true;

    AST ast = covering.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);
    TightSourceRangeComputer sourceRangeComputer = new TightSourceRangeComputer();
    sourceRangeComputer.addTightSourceNode(catchClauses.get(catchClauses.size() - 1));
    rewrite.setTargetSourceRangeComputer(sourceRangeComputer);

    CatchClause firstCatchClause = catchClauses.get(0);

    UnionType newUnionType = ast.newUnionType();
    List<Type> types = newUnionType.types();
    for (Iterator<CatchClause> iterator = catchClauses.iterator(); iterator.hasNext(); ) {
      CatchClause catchClause1 = iterator.next();
      Type type = catchClause1.getException().getType();
      if (type instanceof UnionType) {
        List<Type> types2 = ((UnionType) type).types();
        for (Iterator<Type> iterator2 = types2.iterator(); iterator2.hasNext(); ) {
          types.add((Type) rewrite.createCopyTarget(iterator2.next()));
        }
      } else {
        types.add((Type) rewrite.createCopyTarget(type));
      }
    }

    SingleVariableDeclaration newExceptionDeclaration = ast.newSingleVariableDeclaration();
    newExceptionDeclaration.setType(newUnionType);
    newExceptionDeclaration.setName(
        (SimpleName) rewrite.createCopyTarget(firstCatchClause.getException().getName()));
    rewrite.replace(firstCatchClause.getException(), newExceptionDeclaration, null);

    for (int i = 1; i < catchClauses.size(); i++) {
      rewrite.remove(catchClauses.get(i), null);
    }

    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    String label = CorrectionMessages.QuickAssistProcessor_convert_to_single_multicatch_block;
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.COMBINE_CATCH_BLOCKS,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getUnrollMultiCatchProposals(
      IInvocationContext context,
      ASTNode covering,
      Collection<ICommandAccess> resultingCollections) {
    if (!JavaModelUtil.is17OrHigher(context.getCompilationUnit().getJavaProject())) return false;

    CatchClause catchClause =
        (CatchClause) ASTResolving.findAncestor(covering, ASTNode.CATCH_CLAUSE);
    if (catchClause == null) {
      return false;
    }

    Statement statement = ASTResolving.findParentStatement(covering);
    if (statement != catchClause.getParent() && statement != catchClause.getBody()) {
      return false; // selection is in a statement inside the body
    }

    Type type1 = catchClause.getException().getType();
    Type selectedMultiCatchType = null;
    if (type1.isUnionType() && covering instanceof Name) {
      Name topMostName = ASTNodes.getTopMostName((Name) covering);
      ASTNode parent = topMostName.getParent();
      if (parent instanceof SimpleType || parent instanceof NameQualifiedType) {
        selectedMultiCatchType = (Type) parent;
      }
    }
    if (selectedMultiCatchType != null) return false;

    SingleVariableDeclaration singleVariableDeclaration = catchClause.getException();
    Type type = singleVariableDeclaration.getType();
    if (!(type instanceof UnionType)) return false;

    if (resultingCollections == null) return true;

    AST ast = covering.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);

    TryStatement tryStatement = (TryStatement) catchClause.getParent();
    ListRewrite listRewrite =
        rewrite.getListRewrite(tryStatement, TryStatement.CATCH_CLAUSES_PROPERTY);

    UnionType unionType = (UnionType) type;
    List<Type> types = unionType.types();
    for (int i = types.size() - 1; i >= 0; i--) {
      Type type2 = types.get(i);
      CatchClause newCatchClause = ast.newCatchClause();

      SingleVariableDeclaration newSingleVariableDeclaration = ast.newSingleVariableDeclaration();
      newSingleVariableDeclaration.setType((Type) rewrite.createCopyTarget(type2));
      newSingleVariableDeclaration.setName(
          (SimpleName) rewrite.createCopyTarget(singleVariableDeclaration.getName()));
      newCatchClause.setException(newSingleVariableDeclaration);
      setCatchClauseBody(newCatchClause, rewrite, catchClause);
      listRewrite.insertAfter(newCatchClause, catchClause, null);
    }
    rewrite.remove(catchClause, null);

    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    String label =
        CorrectionMessages.QuickAssistProcessor_convert_to_multiple_singletype_catch_blocks;
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.USE_SEPARATE_CATCH_BLOCKS,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static void setCatchClauseBody(
      CatchClause newCatchClause, ASTRewrite rewrite, CatchClause catchClause) {
    // Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=350285

    //		newCatchClause.setBody((Block) rewrite.createCopyTarget(catchClause.getBody()));

    // newCatchClause#setBody() destroys the formatting, hence copy statement by statement.
    List<Statement> statements = catchClause.getBody().statements();
    for (Iterator<Statement> iterator2 = statements.iterator(); iterator2.hasNext(); ) {
      newCatchClause.getBody().statements().add(rewrite.createCopyTarget(iterator2.next()));
    }
  }

  private static boolean getRenameLocalProposals(
      IInvocationContext context,
      ASTNode node,
      IProblemLocation[] locations,
      Collection<ICommandAccess> resultingCollections) {
    if (!(node instanceof SimpleName)) {
      return false;
    }
    SimpleName name = (SimpleName) node;
    IBinding binding = name.resolveBinding();
    if (binding != null && binding.getKind() == IBinding.PACKAGE) {
      return false;
    }

    if (containsQuickFixableRenameLocal(locations)) {
      return false;
    }

    if (resultingCollections == null) {
      return true;
    }

    LinkedNamesAssistProposal proposal = new LinkedNamesAssistProposal(context, name);
    if (locations.length != 0) {
      proposal.setRelevance(IProposalRelevance.LINKED_NAMES_ASSIST_ERROR);
    }

    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getRenameRefactoringProposal(
      IInvocationContext context,
      ASTNode node,
      IProblemLocation[] locations,
      Collection<ICommandAccess> resultingCollections)
      throws CoreException {
    // TODO RENAME
    //		if (!(context instanceof AssistContext)) {
    //			return false;
    //		}
    //		IEditorPart editor= ((AssistContext)context).getEditor();
    //		if (!(editor instanceof JavaEditor))
    //			return false;
    //
    //		if (!(node instanceof SimpleName)) {
    //			return false;
    //		}
    //		SimpleName name= (SimpleName) node;
    //		IBinding binding= name.resolveBinding();
    //		if (binding == null) {
    //			return false;
    //		}
    //
    //		IJavaElement javaElement= binding.getJavaElement();
    //		if (javaElement == null ||
    // !RefactoringAvailabilityTester.isRenameElementAvailable(javaElement)) {
    //			return false;
    //		}
    //
    //		if (resultingCollections == null) {
    //			return true;
    //		}
    //
    //		RenameRefactoringProposal proposal= new RenameRefactoringProposal((JavaEditor) editor);
    //		if (locations.length != 0) {
    //			proposal.setRelevance(IProposalRelevance.RENAME_REFACTORING_ERROR);
    //		} else if (containsQuickFixableRenameLocal(locations)) {
    //			proposal.setRelevance(IProposalRelevance.RENAME_REFACTORING_QUICK_FIX);
    //		}
    //
    //
    //		resultingCollections.add(proposal);
    //		return true;
    return false;
  }

  private static boolean containsQuickFixableRenameLocal(IProblemLocation[] locations) {
    if (locations != null) {
      for (int i = 0; i < locations.length; i++) {
        IProblemLocation location = locations[i];
        if (IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER.equals(location.getMarkerType())) {
          switch (location.getProblemId()) {
            case IProblem.LocalVariableHidingLocalVariable:
            case IProblem.LocalVariableHidingField:
            case IProblem.FieldHidingLocalVariable:
            case IProblem.FieldHidingField:
            case IProblem.ArgumentHidingLocalVariable:
            case IProblem.ArgumentHidingField:
              return true;
          }
        }
      }
    }
    return false;
  }

  public static ASTNode getCopyOfInner(
      ASTRewrite rewrite, ASTNode statement, boolean toControlStatementBody) {
    if (statement.getNodeType() == ASTNode.BLOCK) {
      Block block = (Block) statement;
      List<Statement> innerStatements = block.statements();
      int nStatements = innerStatements.size();
      if (nStatements == 1) {
        return rewrite.createCopyTarget(innerStatements.get(0));
      } else if (nStatements > 1) {
        if (toControlStatementBody) {
          return rewrite.createCopyTarget(block);
        }
        ListRewrite listRewrite = rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
        ASTNode first = innerStatements.get(0);
        ASTNode last = innerStatements.get(nStatements - 1);
        return listRewrite.createCopyTarget(first, last);
      }
      return null;
    } else {
      return rewrite.createCopyTarget(statement);
    }
  }

  private static boolean getUnWrapProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    ASTNode outer = node;

    Block block = null;
    if (outer.getNodeType() == ASTNode.BLOCK) {
      block = (Block) outer;
      outer = block.getParent();
    }

    ASTNode body = null;
    String label = null;
    if (outer instanceof IfStatement) {
      IfStatement ifStatement = (IfStatement) outer;
      Statement elseBlock = ifStatement.getElseStatement();
      if (elseBlock == null
          || elseBlock instanceof Block && ((Block) elseBlock).statements().isEmpty()) {
        body = ifStatement.getThenStatement();
      }
      label = CorrectionMessages.QuickAssistProcessor_unwrap_ifstatement;
    } else if (outer instanceof WhileStatement) {
      body = ((WhileStatement) outer).getBody();
      label = CorrectionMessages.QuickAssistProcessor_unwrap_whilestatement;
    } else if (outer instanceof ForStatement) {
      body = ((ForStatement) outer).getBody();
      label = CorrectionMessages.QuickAssistProcessor_unwrap_forstatement;
    } else if (outer instanceof EnhancedForStatement) {
      body = ((EnhancedForStatement) outer).getBody();
      label = CorrectionMessages.QuickAssistProcessor_unwrap_forstatement;
    } else if (outer instanceof SynchronizedStatement) {
      body = ((SynchronizedStatement) outer).getBody();
      label = CorrectionMessages.QuickAssistProcessor_unwrap_synchronizedstatement;
    } else if (outer instanceof SimpleName && outer.getParent() instanceof LabeledStatement) {
      LabeledStatement labeledStatement = (LabeledStatement) outer.getParent();
      outer = labeledStatement;
      body = labeledStatement.getBody();
      label = CorrectionMessages.QuickAssistProcessor_unwrap_labeledstatement;
    } else if (outer instanceof LabeledStatement) {
      body = ((LabeledStatement) outer).getBody();
      label = CorrectionMessages.QuickAssistProcessor_unwrap_labeledstatement;
    } else if (outer instanceof DoStatement) {
      body = ((DoStatement) outer).getBody();
      label = CorrectionMessages.QuickAssistProcessor_unwrap_dostatement;
    } else if (outer instanceof TryStatement) {
      TryStatement tryStatement = (TryStatement) outer;
      if (tryStatement.catchClauses().isEmpty() && tryStatement.resources().isEmpty()) {
        body = tryStatement.getBody();
      }
      label = CorrectionMessages.QuickAssistProcessor_unwrap_trystatement;
    } else if (outer instanceof AnonymousClassDeclaration) {
      List<BodyDeclaration> decls = ((AnonymousClassDeclaration) outer).bodyDeclarations();
      for (int i = 0; i < decls.size(); i++) {
        BodyDeclaration elem = decls.get(i);
        if (elem instanceof MethodDeclaration) {
          Block curr = ((MethodDeclaration) elem).getBody();
          if (curr != null && !curr.statements().isEmpty()) {
            if (body != null) {
              return false;
            }
            body = curr;
          }
        } else if (elem instanceof TypeDeclaration) {
          return false;
        }
      }
      label = CorrectionMessages.QuickAssistProcessor_unwrap_anonymous;
      outer = ASTResolving.findParentStatement(outer);
      if (outer == null) {
        return false; // private Object o= new Object() { ... };
      }
    } else if (outer instanceof Block) {
      //	-> a block in a block
      body = block;
      outer = block;
      label = CorrectionMessages.QuickAssistProcessor_unwrap_block;
    } else if (outer instanceof ParenthesizedExpression) {
      // ParenthesizedExpression expression= (ParenthesizedExpression) outer;
      // body= expression.getExpression();
      // label= CorrectionMessages.getString("QuickAssistProcessor.unwrap.parenthesis");
      // //$NON-NLS-1$
    } else if (outer instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) outer;
      if (invocation.arguments().size() == 1) {
        body = (ASTNode) invocation.arguments().get(0);
        if (invocation.getParent().getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
          int kind = body.getNodeType();
          if (kind != ASTNode.ASSIGNMENT
              && kind != ASTNode.PREFIX_EXPRESSION
              && kind != ASTNode.POSTFIX_EXPRESSION
              && kind != ASTNode.METHOD_INVOCATION
              && kind != ASTNode.SUPER_METHOD_INVOCATION) {
            body = null;
          }
        }
        label = CorrectionMessages.QuickAssistProcessor_unwrap_methodinvocation;
      }
    }
    if (body == null) {
      return false;
    }
    ASTRewrite rewrite = ASTRewrite.create(outer.getAST());
    ASTNode inner =
        getCopyOfInner(rewrite, body, ASTNodes.isControlStatementBody(outer.getLocationInParent()));
    if (inner == null) {
      return false;
    }
    if (resultingCollections == null) {
      return true;
    }

    rewrite.replace(outer, inner, null);
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_OBJS_EXCEPTION);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.UNWRAP_STATEMENTS,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean isControlStatementWithBlock(ASTNode node) {
    switch (node.getNodeType()) {
      case ASTNode.IF_STATEMENT:
      case ASTNode.WHILE_STATEMENT:
      case ASTNode.FOR_STATEMENT:
      case ASTNode.ENHANCED_FOR_STATEMENT:
      case ASTNode.DO_STATEMENT:
        return true;
      default:
        return false;
    }
  }

  private static boolean getRemoveBlockProposals(
      IInvocationContext context,
      ASTNode coveringNode,
      Collection<ICommandAccess> resultingCollections) {
    IProposableFix[] fixes =
        ControlStatementsFix.createRemoveBlockFix(context.getASTRoot(), coveringNode);
    if (fixes != null) {
      if (resultingCollections == null) {
        return true;
      }
      Map<String, String> options = new Hashtable<String, String>();
      options.put(CleanUpConstants.CONTROL_STATEMENTS_USE_BLOCKS, CleanUpOptions.TRUE);
      options.put(CleanUpConstants.CONTROL_STATMENTS_USE_BLOCKS_NEVER, CleanUpOptions.TRUE);
      ICleanUp cleanUp = new ControlStatementsCleanUp(options);
      for (int i = 0; i < fixes.length; i++) {
        IProposableFix fix = fixes[i];
        Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
        FixCorrectionProposal proposal =
            new FixCorrectionProposal(
                fix, cleanUp, IProposalRelevance.REMOVE_BLOCK_FIX, image, context);
        resultingCollections.add(proposal);
      }
      return true;
    }
    return false;
  }

  private static boolean getAddBlockProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    if (!(node instanceof Statement)) {
      return false;
    }

    /*
     * only show the quick assist when the selection is of the control statement keywords (if, else, while,...)
     * but not inside the statement or the if expression.
     */
    if (!isControlStatementWithBlock(node) && isControlStatementWithBlock(node.getParent())) {
      int statementStart = node.getStartPosition();
      int statementEnd = statementStart + node.getLength();

      int offset = context.getSelectionOffset();
      int length = context.getSelectionLength();
      if (length == 0) {
        if (offset != statementEnd) { // cursor at end
          return false;
        }
      } else {
        if (offset > statementStart || offset + length < statementEnd) { // statement selected
          return false;
        }
      }
      node = node.getParent();
    }

    StructuralPropertyDescriptor childProperty = null;
    ASTNode child = null;
    switch (node.getNodeType()) {
      case ASTNode.IF_STATEMENT:
        ASTNode then = ((IfStatement) node).getThenStatement();
        ASTNode elseStatement = ((IfStatement) node).getElseStatement();
        if ((then instanceof Block) && (elseStatement instanceof Block || elseStatement == null)) {
          break;
        }
        int thenEnd = then.getStartPosition() + then.getLength();
        int selectionEnd = context.getSelectionOffset() + context.getSelectionLength();
        if (!(then instanceof Block)) {
          if (selectionEnd <= thenEnd) {
            childProperty = IfStatement.THEN_STATEMENT_PROPERTY;
            child = then;
            break;
          } else if (elseStatement != null && selectionEnd < elseStatement.getStartPosition()) {
            // find out if we are before or after the 'else' keyword
            try {
              TokenScanner scanner = new TokenScanner(context.getCompilationUnit());
              int elseTokenStart = scanner.getNextStartOffset(thenEnd, true);
              if (selectionEnd < elseTokenStart) {
                childProperty = IfStatement.THEN_STATEMENT_PROPERTY;
                child = then;
                break;
              }
            } catch (CoreException e) {
              // ignore
            }
          }
        }
        if (elseStatement != null
            && !(elseStatement instanceof Block)
            && context.getSelectionOffset() >= thenEnd) {
          childProperty = IfStatement.ELSE_STATEMENT_PROPERTY;
          child = elseStatement;
        }
        break;
      case ASTNode.WHILE_STATEMENT:
        ASTNode whileBody = ((WhileStatement) node).getBody();
        if (!(whileBody instanceof Block)) {
          childProperty = WhileStatement.BODY_PROPERTY;
          child = whileBody;
        }
        break;
      case ASTNode.FOR_STATEMENT:
        ASTNode forBody = ((ForStatement) node).getBody();
        if (!(forBody instanceof Block)) {
          childProperty = ForStatement.BODY_PROPERTY;
          child = forBody;
        }
        break;
      case ASTNode.ENHANCED_FOR_STATEMENT:
        ASTNode enhancedForBody = ((EnhancedForStatement) node).getBody();
        if (!(enhancedForBody instanceof Block)) {
          childProperty = EnhancedForStatement.BODY_PROPERTY;
          child = enhancedForBody;
        }
        break;
      case ASTNode.DO_STATEMENT:
        ASTNode doBody = ((DoStatement) node).getBody();
        if (!(doBody instanceof Block)) {
          childProperty = DoStatement.BODY_PROPERTY;
          child = doBody;
        }
        break;
      default:
    }
    if (child == null) {
      return false;
    }

    if (resultingCollections == null) {
      return true;
    }
    AST ast = node.getAST();
    {
      ASTRewrite rewrite = ASTRewrite.create(ast);

      ASTNode childPlaceholder = rewrite.createMoveTarget(child);
      Block replacingBody = ast.newBlock();
      replacingBody.statements().add(childPlaceholder);
      rewrite.set(node, childProperty, replacingBody, null);

      String label;
      if (childProperty == IfStatement.THEN_STATEMENT_PROPERTY) {
        label = CorrectionMessages.QuickAssistProcessor_replacethenwithblock_description;
      } else if (childProperty == IfStatement.ELSE_STATEMENT_PROPERTY) {
        label = CorrectionMessages.QuickAssistProcessor_replaceelsewithblock_description;
      } else {
        label = CorrectionMessages.QuickAssistProcessor_replacebodywithblock_description;
      }

      Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
      LinkedCorrectionProposal proposal =
          new LinkedCorrectionProposal(
              label, context.getCompilationUnit(), rewrite, IProposalRelevance.ADD_BLOCK, image);
      proposal.setCommandId(ADD_BLOCK_ID);
      proposal.setEndPosition(rewrite.track(child));
      resultingCollections.add(proposal);
    }

    if (node.getNodeType() == ASTNode.IF_STATEMENT) {
      ASTRewrite rewrite = ASTRewrite.create(ast);

      while (node.getLocationInParent() == IfStatement.ELSE_STATEMENT_PROPERTY) {
        node = node.getParent();
      }

      boolean missingBlockFound = false;
      boolean foundElse = false;

      IfStatement ifStatement;
      Statement thenStatment;
      Statement elseStatment;
      do {
        ifStatement = (IfStatement) node;
        thenStatment = ifStatement.getThenStatement();
        elseStatment = ifStatement.getElseStatement();

        if (!(thenStatment instanceof Block)) {
          ASTNode childPlaceholder1 = rewrite.createMoveTarget(thenStatment);
          Block replacingBody1 = ast.newBlock();
          replacingBody1.statements().add(childPlaceholder1);
          rewrite.set(ifStatement, IfStatement.THEN_STATEMENT_PROPERTY, replacingBody1, null);
          if (thenStatment != child) {
            missingBlockFound = true;
          }
        }
        if (elseStatment != null) {
          foundElse = true;
        }
        node = elseStatment;
      } while (elseStatment instanceof IfStatement);

      if (elseStatment != null && !(elseStatment instanceof Block)) {
        ASTNode childPlaceholder2 = rewrite.createMoveTarget(elseStatment);

        Block replacingBody2 = ast.newBlock();
        replacingBody2.statements().add(childPlaceholder2);
        rewrite.set(ifStatement, IfStatement.ELSE_STATEMENT_PROPERTY, replacingBody2, null);
        if (elseStatment != child) {
          missingBlockFound = true;
        }
      }

      if (missingBlockFound && foundElse) {
        String label = CorrectionMessages.QuickAssistProcessor_replacethenelsewithblock_description;
        Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
        ASTRewriteCorrectionProposal proposal =
            new ASTRewriteCorrectionProposal(
                label,
                context.getCompilationUnit(),
                rewrite,
                IProposalRelevance.CHANGE_IF_ELSE_TO_BLOCK,
                image);
        resultingCollections.add(proposal);
      }
    }
    return true;
  }

  private static boolean getInvertEqualsProposal(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    if (!(node instanceof MethodInvocation)) {
      node = node.getParent();
      if (!(node instanceof MethodInvocation)) {
        return false;
      }
    }
    MethodInvocation method = (MethodInvocation) node;
    String identifier = method.getName().getIdentifier();
    if (!"equals".equals(identifier)
        && !"equalsIgnoreCase".equals(identifier)) { // $NON-NLS-1$ //$NON-NLS-2$
      return false;
    }
    List<Expression> arguments = method.arguments();
    if (arguments.size() != 1) { // overloaded equals w/ more than 1 argument
      return false;
    }
    Expression right = arguments.get(0);
    ITypeBinding binding = right.resolveTypeBinding();
    if (binding != null
        && !(binding.isClass()
            || binding.isInterface()
            || binding.isEnum())) { // overloaded equals w/ non-class/interface argument or null
      return false;
    }
    if (resultingCollections == null) {
      return true;
    }

    Expression left = method.getExpression();

    AST ast = method.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);
    if (left == null) { // equals(x) -> x.equals(this)
      MethodInvocation replacement = ast.newMethodInvocation();
      replacement.setName((SimpleName) rewrite.createCopyTarget(method.getName()));
      replacement.arguments().add(ast.newThisExpression());
      replacement.setExpression((Expression) rewrite.createCopyTarget(right));
      rewrite.replace(method, replacement, null);
    } else if (right instanceof ThisExpression) { // x.equals(this) -> equals(x)
      MethodInvocation replacement = ast.newMethodInvocation();
      replacement.setName((SimpleName) rewrite.createCopyTarget(method.getName()));
      replacement.arguments().add(rewrite.createCopyTarget(left));
      rewrite.replace(method, replacement, null);
    } else {
      ASTNode leftExpression = left;
      while (leftExpression instanceof ParenthesizedExpression) {
        leftExpression = ((ParenthesizedExpression) left).getExpression();
      }
      rewrite.replace(right, rewrite.createCopyTarget(leftExpression), null);

      if (right instanceof CastExpression
          || right instanceof Assignment
          || right instanceof ConditionalExpression
          || right instanceof InfixExpression) {
        ParenthesizedExpression paren = ast.newParenthesizedExpression();
        paren.setExpression((Expression) rewrite.createCopyTarget(right));
        rewrite.replace(left, paren, null);
      } else {
        rewrite.replace(left, rewrite.createCopyTarget(right), null);
      }
    }

    String label = CorrectionMessages.QuickAssistProcessor_invertequals_description;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);

    LinkedCorrectionProposal proposal =
        new LinkedCorrectionProposal(
            label, context.getCompilationUnit(), rewrite, IProposalRelevance.INVERT_EQUALS, image);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getArrayInitializerToArrayCreation(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    if (!(node instanceof ArrayInitializer)) {
      return false;
    }
    ArrayInitializer initializer = (ArrayInitializer) node;

    ASTNode parent = initializer.getParent();
    while (parent instanceof ArrayInitializer) {
      initializer = (ArrayInitializer) parent;
      parent = parent.getParent();
    }
    ITypeBinding typeBinding = initializer.resolveTypeBinding();
    if (!(parent instanceof VariableDeclaration) || typeBinding == null || !typeBinding.isArray()) {
      return false;
    }
    if (resultingCollections == null) {
      return true;
    }

    AST ast = node.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);

    String label = CorrectionMessages.QuickAssistProcessor_typetoarrayInitializer_description;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);

    LinkedCorrectionProposal proposal =
        new LinkedCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.ADD_TYPE_TO_ARRAY_INITIALIZER,
            image);

    ImportRewrite imports = proposal.createImportRewrite(context.getASTRoot());
    ImportRewriteContext importRewriteContext =
        new ContextSensitiveImportRewriteContext(node, imports);
    String typeName = imports.addImport(typeBinding, importRewriteContext);

    ArrayCreation creation = ast.newArrayCreation();
    creation.setInitializer((ArrayInitializer) rewrite.createMoveTarget(initializer));
    creation.setType((ArrayType) ASTNodeFactory.newType(ast, typeName));

    rewrite.replace(initializer, creation, null);

    resultingCollections.add(proposal);
    return true;
  }

  public static boolean getCreateInSuperClassProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections)
      throws CoreException {
    if (!(node instanceof SimpleName) || !(node.getParent() instanceof MethodDeclaration)) {
      return false;
    }
    MethodDeclaration decl = (MethodDeclaration) node.getParent();
    if (decl.getName() != node
        || decl.resolveBinding() == null
        || Modifier.isPrivate(decl.getModifiers())) {
      return false;
    }

    ICompilationUnit cu = context.getCompilationUnit();
    CompilationUnit astRoot = context.getASTRoot();

    IMethodBinding binding = decl.resolveBinding();
    ITypeBinding[] paramTypes = binding.getParameterTypes();

    ITypeBinding[] superTypes = Bindings.getAllSuperTypes(binding.getDeclaringClass());
    if (resultingCollections == null) {
      for (int i = 0; i < superTypes.length; i++) {
        ITypeBinding curr = superTypes[i];
        if (curr.isFromSource() && Bindings.findOverriddenMethodInType(curr, binding) == null) {
          return true;
        }
      }
      return false;
    }
    List<SingleVariableDeclaration> params = decl.parameters();
    String[] paramNames = new String[paramTypes.length];
    for (int i = 0; i < params.size(); i++) {
      SingleVariableDeclaration param = params.get(i);
      paramNames[i] = param.getName().getIdentifier();
    }

    for (int i = 0; i < superTypes.length; i++) {
      ITypeBinding curr = superTypes[i];
      if (curr.isFromSource()) {
        IMethodBinding method = Bindings.findOverriddenMethodInType(curr, binding);
        if (method == null) {
          ITypeBinding typeDecl = curr.getTypeDeclaration();
          ICompilationUnit targetCU =
              ASTResolving.findCompilationUnitForBinding(cu, astRoot, typeDecl);
          if (targetCU != null) {
            String label =
                Messages.format(
                    CorrectionMessages.QuickAssistProcessor_createmethodinsuper_description,
                    new String[] {
                      BasicElementLabels.getJavaElementName(curr.getName()),
                      BasicElementLabels.getJavaElementName(binding.getName())
                    });
            resultingCollections.add(
                new NewDefiningMethodProposal(
                    label,
                    targetCU,
                    astRoot,
                    typeDecl,
                    binding,
                    paramNames,
                    IProposalRelevance.CREATE_METHOD_IN_SUPER));
          }
        }
      }
    }
    return true;
  }

  private static boolean getConvertEnhancedForLoopProposal(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    EnhancedForStatement enhancedForStatement =
        getEnclosingHeader(
            node,
            EnhancedForStatement.class,
            EnhancedForStatement.PARAMETER_PROPERTY,
            EnhancedForStatement.EXPRESSION_PROPERTY);
    if (enhancedForStatement == null) {
      return false;
    }
    SingleVariableDeclaration parameter = enhancedForStatement.getParameter();
    IVariableBinding parameterBinding = parameter.resolveBinding();
    if (parameterBinding == null) {
      return false;
    }
    Expression initializer = enhancedForStatement.getExpression();
    ITypeBinding initializerTypeBinding = initializer.resolveTypeBinding();
    if (initializerTypeBinding == null) {
      return false;
    }

    if (resultingCollections == null) {
      return true;
    }

    Statement topLabelStatement = enhancedForStatement;
    while (topLabelStatement.getLocationInParent() == LabeledStatement.BODY_PROPERTY) {
      topLabelStatement = (Statement) topLabelStatement.getParent();
    }

    IJavaProject project = context.getCompilationUnit().getJavaProject();
    AST ast = node.getAST();
    Statement enhancedForBody = enhancedForStatement.getBody();
    Collection<String> usedVarNames =
        Arrays.asList(ASTResolving.getUsedVariableNames(enhancedForBody));

    boolean initializerIsArray = initializerTypeBinding.isArray();
    ITypeBinding initializerListType =
        Bindings.findTypeInHierarchy(initializerTypeBinding, "java.util.List"); // $NON-NLS-1$
    ITypeBinding initializerIterableType =
        Bindings.findTypeInHierarchy(initializerTypeBinding, "java.lang.Iterable"); // $NON-NLS-1$

    if (initializerIterableType != null) {
      String label = CorrectionMessages.QuickAssistProcessor_convert_to_iterator_for_loop;
      Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);

      String iterNameKey = "iterName"; // $NON-NLS-1$
      ASTRewrite rewrite = ASTRewrite.create(ast);
      LinkedCorrectionProposal proposal =
          new LinkedCorrectionProposal(
              label,
              context.getCompilationUnit(),
              rewrite,
              IProposalRelevance.CONVERT_TO_ITERATOR_FOR_LOOP,
              image);

      // convert 'for' statement
      ForStatement forStatement = ast.newForStatement();

      // create initializer
      MethodInvocation iterInitializer = ast.newMethodInvocation();
      iterInitializer.setName(ast.newSimpleName("iterator")); // $NON-NLS-1$
      ImportRewrite imports = proposal.createImportRewrite(context.getASTRoot());
      ImportRewriteContext importRewriteContext =
          new ContextSensitiveImportRewriteContext(node, imports);
      Type iterType =
          ast.newSimpleType(
              ast.newName(
                  imports.addImport("java.util.Iterator", importRewriteContext))); // $NON-NLS-1$
      if (initializerIterableType.getTypeArguments().length == 1) {
        Type iterTypeArgument =
            imports.addImport(
                Bindings.normalizeTypeBinding(initializerIterableType.getTypeArguments()[0]),
                ast,
                importRewriteContext);
        ParameterizedType parameterizedIterType = ast.newParameterizedType(iterType);
        parameterizedIterType.typeArguments().add(iterTypeArgument);
        iterType = parameterizedIterType;
      }
      String[] iterNames =
          StubUtility.getVariableNameSuggestions(
              NamingConventions.VK_LOCAL, project, iterType, iterInitializer, usedVarNames);
      String iterName = iterNames[0];
      SimpleName initializerIterName = ast.newSimpleName(iterName);

      VariableDeclarationFragment iterFragment = ast.newVariableDeclarationFragment();
      iterFragment.setName(initializerIterName);
      proposal.addLinkedPosition(rewrite.track(initializerIterName), 0, iterNameKey);
      for (int i = 0; i < iterNames.length; i++) {
        proposal.addLinkedPositionProposal(iterNameKey, iterNames[i], null);
      }

      Expression initializerExpression = (Expression) rewrite.createCopyTarget(initializer);
      iterInitializer.setExpression(initializerExpression);
      iterFragment.setInitializer(iterInitializer);

      VariableDeclarationExpression iterVariable =
          ast.newVariableDeclarationExpression(iterFragment);
      iterVariable.setType(iterType);
      forStatement.initializers().add(iterVariable);

      // create condition
      MethodInvocation condition = ast.newMethodInvocation();
      condition.setName(ast.newSimpleName("hasNext")); // $NON-NLS-1$
      SimpleName conditionExpression = ast.newSimpleName(iterName);
      proposal.addLinkedPosition(
          rewrite.track(conditionExpression), LinkedPositionGroup.NO_STOP, iterNameKey);
      condition.setExpression(conditionExpression);
      forStatement.setExpression(condition);

      // create 'for' body element variable
      VariableDeclarationFragment elementFragment = ast.newVariableDeclarationFragment();
      elementFragment
          .extraDimensions()
          .addAll(DimensionRewrite.copyDimensions(parameter.extraDimensions(), rewrite));
      elementFragment.setName((SimpleName) rewrite.createCopyTarget(parameter.getName()));

      SimpleName elementIterName = ast.newSimpleName(iterName);
      proposal.addLinkedPosition(
          rewrite.track(elementIterName), LinkedPositionGroup.NO_STOP, iterNameKey);

      MethodInvocation getMethodInvocation = ast.newMethodInvocation();
      getMethodInvocation.setName(ast.newSimpleName("next")); // $NON-NLS-1$
      getMethodInvocation.setExpression(elementIterName);
      elementFragment.setInitializer(getMethodInvocation);

      VariableDeclarationStatement elementVariable =
          ast.newVariableDeclarationStatement(elementFragment);
      ModifierRewrite.create(rewrite, elementVariable).copyAllModifiers(parameter, null);
      elementVariable.setType((Type) rewrite.createCopyTarget(parameter.getType()));

      Block newBody = ast.newBlock();
      List<Statement> statements = newBody.statements();
      statements.add(elementVariable);
      if (enhancedForBody instanceof Block) {
        List<Statement> oldStatements = ((Block) enhancedForBody).statements();
        if (oldStatements.size() > 0) {
          ListRewrite statementsRewrite =
              rewrite.getListRewrite(enhancedForBody, Block.STATEMENTS_PROPERTY);
          Statement oldStatementsCopy =
              (Statement)
                  statementsRewrite.createCopyTarget(
                      oldStatements.get(0), oldStatements.get(oldStatements.size() - 1));
          statements.add(oldStatementsCopy);
        }
      } else {
        statements.add((Statement) rewrite.createCopyTarget(enhancedForBody));
      }

      forStatement.setBody(newBody);
      rewrite.replace(enhancedForStatement, forStatement, null);

      resultingCollections.add(proposal);
    }

    if (initializerIsArray || initializerListType != null) {
      String label = CorrectionMessages.QuickAssistProcessor_convert_to_indexed_for_loop;
      Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);

      String varNameKey = "varName"; // $NON-NLS-1$
      String indexNameKey = "indexName"; // $NON-NLS-1$
      ASTRewrite rewrite = ASTRewrite.create(ast);
      LinkedCorrectionProposal proposal =
          new LinkedCorrectionProposal(
              label,
              context.getCompilationUnit(),
              rewrite,
              IProposalRelevance.CONVERT_TO_INDEXED_FOR_LOOP,
              image);

      // create temp variable from initializer if necessary
      String varName;
      boolean varNameGenerated;
      if (initializer instanceof SimpleName) {
        varName = ((SimpleName) initializer).getIdentifier();
        varNameGenerated = false;
      } else {
        VariableDeclarationFragment varFragment = ast.newVariableDeclarationFragment();
        String[] varNames =
            StubUtility.getVariableNameSuggestions(
                NamingConventions.VK_LOCAL,
                project,
                initializerTypeBinding,
                initializer,
                usedVarNames);
        varName = varNames[0];
        usedVarNames = new ArrayList<String>(usedVarNames);
        usedVarNames.add(varName);
        varNameGenerated = true;
        SimpleName varNameNode = ast.newSimpleName(varName);
        varFragment.setName(varNameNode);
        proposal.addLinkedPosition(rewrite.track(varNameNode), 0, varNameKey);
        for (int i = 0; i < varNames.length; i++) {
          proposal.addLinkedPositionProposal(varNameKey, varNames[i], null);
        }

        varFragment.setInitializer((Expression) rewrite.createCopyTarget(initializer));

        VariableDeclarationStatement varDeclaration =
            ast.newVariableDeclarationStatement(varFragment);
        Type varType;
        if (initializerIsArray) {
          Type copiedType =
              DimensionRewrite.copyTypeAndAddDimensions(
                  parameter.getType(), parameter.extraDimensions(), rewrite);
          varType = ASTNodeFactory.newArrayType(copiedType);
        } else {
          ImportRewrite imports = proposal.createImportRewrite(context.getASTRoot());
          ImportRewriteContext importRewriteContext =
              new ContextSensitiveImportRewriteContext(node, imports);
          varType =
              imports.addImport(
                  Bindings.normalizeForDeclarationUse(initializerTypeBinding, ast),
                  ast,
                  importRewriteContext);
        }
        varDeclaration.setType(varType);

        if (!(topLabelStatement.getParent() instanceof Block)) {
          Block block = ast.newBlock();
          List<Statement> statements = block.statements();
          statements.add(varDeclaration);
          statements.add((Statement) rewrite.createCopyTarget(topLabelStatement));
          rewrite.replace(topLabelStatement, block, null);
        } else {
          rewrite
              .getListRewrite(topLabelStatement.getParent(), Block.STATEMENTS_PROPERTY)
              .insertBefore(varDeclaration, topLabelStatement, null);
        }
      }

      // convert 'for' statement
      ForStatement forStatement = ast.newForStatement();

      // create initializer
      VariableDeclarationFragment indexFragment = ast.newVariableDeclarationFragment();
      NumberLiteral indexInitializer = ast.newNumberLiteral();
      indexFragment.setInitializer(indexInitializer);
      PrimitiveType indexType = ast.newPrimitiveType(PrimitiveType.INT);
      String[] indexNames =
          StubUtility.getVariableNameSuggestions(
              NamingConventions.VK_LOCAL, project, indexType, indexInitializer, usedVarNames);
      String indexName = indexNames[0];
      SimpleName initializerIndexName = ast.newSimpleName(indexName);
      indexFragment.setName(initializerIndexName);
      proposal.addLinkedPosition(rewrite.track(initializerIndexName), 0, indexNameKey);
      for (int i = 0; i < indexNames.length; i++) {
        proposal.addLinkedPositionProposal(indexNameKey, indexNames[i], null);
      }
      VariableDeclarationExpression indexVariable =
          ast.newVariableDeclarationExpression(indexFragment);
      indexVariable.setType(indexType);
      forStatement.initializers().add(indexVariable);

      // create condition
      InfixExpression condition = ast.newInfixExpression();
      condition.setOperator(InfixExpression.Operator.LESS);
      SimpleName conditionLeft = ast.newSimpleName(indexName);
      proposal.addLinkedPosition(
          rewrite.track(conditionLeft), LinkedPositionGroup.NO_STOP, indexNameKey);
      condition.setLeftOperand(conditionLeft);
      SimpleName conditionRightName = ast.newSimpleName(varName);
      if (varNameGenerated) {
        proposal.addLinkedPosition(
            rewrite.track(conditionRightName), LinkedPositionGroup.NO_STOP, varNameKey);
      }
      Expression conditionRight;
      if (initializerIsArray) {
        conditionRight =
            ast.newQualifiedName(conditionRightName, ast.newSimpleName("length")); // $NON-NLS-1$
      } else {
        MethodInvocation sizeMethodInvocation = ast.newMethodInvocation();
        sizeMethodInvocation.setName(ast.newSimpleName("size")); // $NON-NLS-1$
        sizeMethodInvocation.setExpression(conditionRightName);
        conditionRight = sizeMethodInvocation;
      }
      condition.setRightOperand(conditionRight);
      forStatement.setExpression(condition);

      // create updater
      SimpleName indexUpdaterName = ast.newSimpleName(indexName);
      proposal.addLinkedPosition(
          rewrite.track(indexUpdaterName), LinkedPositionGroup.NO_STOP, indexNameKey);
      PostfixExpression indexUpdater = ast.newPostfixExpression();
      indexUpdater.setOperator(PostfixExpression.Operator.INCREMENT);
      indexUpdater.setOperand(indexUpdaterName);
      forStatement.updaters().add(indexUpdater);

      // create 'for' body element variable
      VariableDeclarationFragment elementFragment = ast.newVariableDeclarationFragment();
      elementFragment
          .extraDimensions()
          .addAll(DimensionRewrite.copyDimensions(parameter.extraDimensions(), rewrite));
      elementFragment.setName((SimpleName) rewrite.createCopyTarget(parameter.getName()));

      SimpleName elementVarName = ast.newSimpleName(varName);
      if (varNameGenerated) {
        proposal.addLinkedPosition(
            rewrite.track(elementVarName), LinkedPositionGroup.NO_STOP, varNameKey);
      }
      SimpleName elementIndexName = ast.newSimpleName(indexName);
      proposal.addLinkedPosition(
          rewrite.track(elementIndexName), LinkedPositionGroup.NO_STOP, indexNameKey);

      Expression elementAccess;
      if (initializerIsArray) {
        ArrayAccess elementArrayAccess = ast.newArrayAccess();
        elementArrayAccess.setArray(elementVarName);
        elementArrayAccess.setIndex(elementIndexName);
        elementAccess = elementArrayAccess;
      } else {
        MethodInvocation getMethodInvocation = ast.newMethodInvocation();
        getMethodInvocation.setName(ast.newSimpleName("get")); // $NON-NLS-1$
        getMethodInvocation.setExpression(elementVarName);
        getMethodInvocation.arguments().add(elementIndexName);
        elementAccess = getMethodInvocation;
      }
      elementFragment.setInitializer(elementAccess);

      VariableDeclarationStatement elementVariable =
          ast.newVariableDeclarationStatement(elementFragment);
      ModifierRewrite.create(rewrite, elementVariable).copyAllModifiers(parameter, null);
      elementVariable.setType((Type) rewrite.createCopyTarget(parameter.getType()));

      Block newBody = ast.newBlock();
      List<Statement> statements = newBody.statements();
      statements.add(elementVariable);
      if (enhancedForBody instanceof Block) {
        List<Statement> oldStatements = ((Block) enhancedForBody).statements();
        if (oldStatements.size() > 0) {
          ListRewrite statementsRewrite =
              rewrite.getListRewrite(enhancedForBody, Block.STATEMENTS_PROPERTY);
          Statement oldStatementsCopy =
              (Statement)
                  statementsRewrite.createCopyTarget(
                      oldStatements.get(0), oldStatements.get(oldStatements.size() - 1));
          statements.add(oldStatementsCopy);
        }
      } else {
        statements.add((Statement) rewrite.createCopyTarget(enhancedForBody));
      }

      forStatement.setBody(newBody);
      rewrite.replace(enhancedForStatement, forStatement, null);

      resultingCollections.add(proposal);
    }

    return true;
  }

  private static boolean getConvertForLoopProposal(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    ForStatement forStatement = getEnclosingForStatementHeader(node);
    if (forStatement == null) return false;

    if (resultingCollections == null) return true;

    IProposableFix fix =
        ConvertLoopFix.createConvertForLoopToEnhancedFix(context.getASTRoot(), forStatement);
    if (fix == null) return false;

    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    Map<String, String> options = new HashMap<String, String>();
    options.put(
        CleanUpConstants.CONTROL_STATMENTS_CONVERT_FOR_LOOP_TO_ENHANCED, CleanUpOptions.TRUE);
    ICleanUp cleanUp = new ConvertLoopCleanUp(options);
    FixCorrectionProposal proposal =
        new FixCorrectionProposal(
            fix, cleanUp, IProposalRelevance.CONVERT_FOR_LOOP_TO_ENHANCED, image, context);
    proposal.setCommandId(CONVERT_FOR_LOOP_ID);

    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getConvertIterableLoopProposal(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    ForStatement forStatement = getEnclosingForStatementHeader(node);
    if (forStatement == null) return false;

    if (resultingCollections == null) return true;

    IProposableFix fix =
        ConvertLoopFix.createConvertIterableLoopToEnhancedFix(context.getASTRoot(), forStatement);
    if (fix == null) return false;

    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    Map<String, String> options = new HashMap<String, String>();
    options.put(
        CleanUpConstants.CONTROL_STATMENTS_CONVERT_FOR_LOOP_TO_ENHANCED, CleanUpOptions.TRUE);
    ICleanUp cleanUp = new ConvertLoopCleanUp(options);
    FixCorrectionProposal proposal =
        new FixCorrectionProposal(
            fix, cleanUp, IProposalRelevance.CONVERT_ITERABLE_LOOP_TO_ENHANCED, image, context);
    proposal.setCommandId(CONVERT_FOR_LOOP_ID);

    resultingCollections.add(proposal);
    return true;
  }

  public static boolean getGenerateForLoopProposals(
      IInvocationContext context,
      ASTNode coveringNode,
      IProblemLocation[] locations,
      Collection<ICommandAccess> resultingCollections) {
    //		if (containsMatchingProblem(locations, IProblem.ParsingErrorInsertToComplete))
    //			return false;

    Statement statement = ASTResolving.findParentStatement(coveringNode);
    ICompilationUnit cu = context.getCompilationUnit();
    ITypeBinding expressionType = null;
    Expression expression = null;
    int relevanceBoost = 0;

    if (statement instanceof ExpressionStatement) {
      expressionType = extractExpressionType((ExpressionStatement) statement);
      expression = ((ExpressionStatement) statement).getExpression();

    } else if (statement instanceof VariableDeclarationStatement
        && ((VariableDeclarationStatement) statement).fragments().size() == 1
        && ((VariableDeclarationStatement) statement)
            .fragments()
            .get(0)
            .toString()
            .equals("$missing$")) { // $NON-NLS-1$
      // variable name is resolved to the type in a variable declaration statement, see
      // https://bugs.eclipse.org/430336
      Type type = ((VariableDeclarationStatement) statement).getType();
      if (type instanceof SimpleType) {
        SimpleType simpleType = (SimpleType) type;
        expressionType = extractExpressionType(simpleType);
        expression = simpleType.getName();
        relevanceBoost +=
            6; // need to overrule the bad UnresolvedElementsSubProcessor#addNewTypeProposals(..)
      }
    } else {
      return false;
    }

    if (expressionType == null) return false;

    if (Bindings.findTypeInHierarchy(expressionType, "java.lang.Iterable") != null) { // $NON-NLS-1$
      if (resultingCollections == null) return true;
      GenerateForLoopAssistProposal proposal =
          new GenerateForLoopAssistProposal(
              cu,
              expressionType,
              statement,
              expression,
              GenerateForLoopAssistProposal.GENERATE_ITERATOR_FOR);
      proposal.setRelevance(proposal.getRelevance() + relevanceBoost);
      resultingCollections.add(proposal);
      if (Bindings.findTypeInHierarchy(expressionType, "java.util.List") != null) { // $NON-NLS-1$
        proposal =
            new GenerateForLoopAssistProposal(
                cu,
                expressionType,
                statement,
                expression,
                GenerateForLoopAssistProposal.GENERATE_ITERATE_LIST);
        proposal.setRelevance(proposal.getRelevance() + relevanceBoost);
        resultingCollections.add(proposal);
      }
    } else if (expressionType.isArray()) {
      if (resultingCollections == null) return true;
      GenerateForLoopAssistProposal proposal =
          new GenerateForLoopAssistProposal(
              cu,
              expressionType,
              statement,
              expression,
              GenerateForLoopAssistProposal.GENERATE_ITERATE_ARRAY);
      proposal.setRelevance(proposal.getRelevance() + relevanceBoost);
      resultingCollections.add(proposal);
    } else {
      return false;
    }

    if (JavaModelUtil.is50OrHigher(cu.getJavaProject())) {
      GenerateForLoopAssistProposal proposal =
          new GenerateForLoopAssistProposal(
              cu,
              expressionType,
              statement,
              expression,
              GenerateForLoopAssistProposal.GENERATE_FOREACH);
      proposal.setRelevance(proposal.getRelevance() + relevanceBoost);
      resultingCollections.add(proposal);
    }

    return true;
  }

  private static ITypeBinding extractExpressionType(ExpressionStatement statement) {
    Expression expression = statement.getExpression();
    if (expression.getNodeType() == ASTNode.METHOD_INVOCATION
        || expression.getNodeType() == ASTNode.FIELD_ACCESS) {
      return expression.resolveTypeBinding();
    }
    return null;
  }

  private static ITypeBinding extractExpressionType(SimpleType variableIdentifier) {
    ScopeAnalyzer analyzer = new ScopeAnalyzer((CompilationUnit) variableIdentifier.getRoot());
    // get the name of the variable to search the type for
    Name name = null;
    if (variableIdentifier.getName() instanceof SimpleName) {
      name = variableIdentifier.getName();
    } else if (variableIdentifier.getName() instanceof QualifiedName) {
      name = ((QualifiedName) variableIdentifier.getName()).getName();
    }

    // analyze variables which are available in the scope at the position of the quick assist
    // invokation
    IBinding[] declarationsInScope =
        analyzer.getDeclarationsInScope(name.getStartPosition(), ScopeAnalyzer.VARIABLES);
    for (int i = 0; i < declarationsInScope.length; i++) {
      IBinding currentVariable = declarationsInScope[i];
      if (((IVariableBinding) currentVariable).getName().equals(name.getFullyQualifiedName())) {
        return ((IVariableBinding) currentVariable).getType();
      }
    }
    return null;
  }

  private static ForStatement getEnclosingForStatementHeader(ASTNode node) {
    return getEnclosingHeader(
        node,
        ForStatement.class,
        ForStatement.INITIALIZERS_PROPERTY,
        ForStatement.EXPRESSION_PROPERTY,
        ForStatement.UPDATERS_PROPERTY);
  }

  private static <T extends ASTNode> T getEnclosingHeader(
      ASTNode node, Class<T> headerType, StructuralPropertyDescriptor... headerProperties) {
    if (headerType.isInstance(node)) return headerType.cast(node);

    while (node != null) {
      ASTNode parent = node.getParent();
      if (headerType.isInstance(parent)) {
        StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
        for (StructuralPropertyDescriptor property : headerProperties) {
          if (locationInParent == property) return headerType.cast(parent);
        }
        return null;
      }
      node = parent;
    }
    return null;
  }

  private static boolean getMakeVariableDeclarationFinalProposals(
      IInvocationContext context, Collection<ICommandAccess> resultingCollections) {
    SelectionAnalyzer analyzer =
        new SelectionAnalyzer(
            Selection.createFromStartLength(
                context.getSelectionOffset(), context.getSelectionLength()),
            false);
    context.getASTRoot().accept(analyzer);
    ASTNode[] selectedNodes = analyzer.getSelectedNodes();
    if (selectedNodes.length == 0) return false;

    IProposableFix fix =
        VariableDeclarationFix.createChangeModifierToFinalFix(context.getASTRoot(), selectedNodes);
    if (fix == null) return false;

    if (resultingCollections == null) return true;

    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    Map<String, String> options = new Hashtable<String, String>();
    options.put(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL, CleanUpOptions.TRUE);
    options.put(
        CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL_LOCAL_VARIABLES, CleanUpOptions.TRUE);
    options.put(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL_PARAMETERS, CleanUpOptions.TRUE);
    options.put(
        CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL_PRIVATE_FIELDS, CleanUpOptions.TRUE);
    VariableDeclarationCleanUp cleanUp = new VariableDeclarationCleanUp(options);
    FixCorrectionProposal proposal =
        new FixCorrectionProposal(
            fix, cleanUp, IProposalRelevance.MAKE_VARIABLE_DECLARATION_FINAL, image, context);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getInlineLocalProposal(
      IInvocationContext context, final ASTNode node, Collection<ICommandAccess> proposals)
      throws CoreException {
    if (!(node instanceof SimpleName)) return false;

    SimpleName name = (SimpleName) node;
    IBinding binding = name.resolveBinding();
    if (!(binding instanceof IVariableBinding)) return false;
    IVariableBinding varBinding = (IVariableBinding) binding;
    if (varBinding.isField() || varBinding.isParameter()) return false;
    ASTNode decl = context.getASTRoot().findDeclaringNode(varBinding);
    if (!(decl instanceof VariableDeclarationFragment)
        || decl.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY)
      return false;

    if (proposals == null) {
      return true;
    }

    InlineTempRefactoring refactoring = new InlineTempRefactoring((VariableDeclaration) decl);
    if (refactoring.checkInitialConditions(new NullProgressMonitor()).isOK()) {
      String label = CorrectionMessages.QuickAssistProcessor_inline_local_description;
      Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
      RefactoringCorrectionProposal proposal =
          new RefactoringCorrectionProposal(
              label,
              context.getCompilationUnit(),
              refactoring,
              IProposalRelevance.INLINE_LOCAL,
              image);
      proposal.setCommandId(INLINE_LOCAL_ID);
      proposals.add(proposal);
    }
    return true;
  }

  private static boolean getMissingCaseStatementProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> proposals) {
    if (node instanceof SwitchCase) {
      node = node.getParent();
    }
    if (!(node instanceof SwitchStatement)) return false;

    SwitchStatement switchStatement = (SwitchStatement) node;
    ITypeBinding expressionBinding = switchStatement.getExpression().resolveTypeBinding();
    if (expressionBinding == null || !expressionBinding.isEnum()) return false;

    ArrayList<String> missingEnumCases = new ArrayList<String>();
    boolean hasDefault =
        LocalCorrectionsSubProcessor.evaluateMissingSwitchCases(
            expressionBinding, switchStatement.statements(), missingEnumCases);
    if (missingEnumCases.size() == 0 && hasDefault) return false;

    if (proposals == null) return true;

    LocalCorrectionsSubProcessor.createMissingCaseProposals(
        context, switchStatement, missingEnumCases, proposals);
    return true;
  }

  private static boolean getConvertLocalToFieldProposal(
      IInvocationContext context, final ASTNode node, Collection<ICommandAccess> proposals)
      throws CoreException {
    if (!(node instanceof SimpleName)) return false;

    SimpleName name = (SimpleName) node;
    IBinding binding = name.resolveBinding();
    if (!(binding instanceof IVariableBinding)) return false;
    IVariableBinding varBinding = (IVariableBinding) binding;
    if (varBinding.isField() || varBinding.isParameter()) return false;
    ASTNode decl = context.getASTRoot().findDeclaringNode(varBinding);
    if (decl == null
        || decl.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY)
      return false;

    if (proposals == null) {
      return true;
    }

    PromoteTempToFieldRefactoring refactoring =
        new PromoteTempToFieldRefactoring((VariableDeclaration) decl);
    if (refactoring.checkInitialConditions(new NullProgressMonitor()).isOK()) {
      String label = CorrectionMessages.QuickAssistProcessor_convert_local_to_field_description;
      Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
      LinkedProposalModel linkedProposalModel = new LinkedProposalModel();
      refactoring.setLinkedProposalModel(linkedProposalModel);

      RefactoringCorrectionProposal proposal =
          new RefactoringCorrectionProposal(
              label,
              context.getCompilationUnit(),
              refactoring,
              IProposalRelevance.CONVERT_LOCAL_TO_FIELD,
              image);
      proposal.setLinkedProposalModel(linkedProposalModel);
      proposal.setCommandId(CONVERT_LOCAL_TO_FIELD_ID);
      proposals.add(proposal);
    }
    return true;
  }
}
