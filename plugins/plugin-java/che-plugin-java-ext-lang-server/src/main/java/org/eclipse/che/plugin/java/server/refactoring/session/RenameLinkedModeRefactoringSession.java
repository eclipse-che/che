/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.server.refactoring.session;

import static org.eclipse.che.plugin.java.server.dto.DtoServerImpls.LinkedModeModelImpl;
import static org.eclipse.che.plugin.java.server.dto.DtoServerImpls.LinkedPositionGroupImpl;
import static org.eclipse.che.plugin.java.server.dto.DtoServerImpls.RegionImpl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.eclipse.che.ide.ext.java.shared.dto.LinkedModeModel;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeCreationResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeInfo;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringResult;
import org.eclipse.che.plugin.java.server.refactoring.DtoConverter;
import org.eclipse.che.plugin.java.server.refactoring.RefactoringException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenamingNameSuggestor;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jdt.ui.refactoring.RenameSupport;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * The class contains methods for the refactoring that uses the linked editor.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
public class RenameLinkedModeRefactoringSession extends RefactoringSession {
  private final IJavaElement element;
  private final ICompilationUnit compilationUnit;
  private final int offset;
  private String fOriginalName;

  public RenameLinkedModeRefactoringSession(
      IJavaElement element, ICompilationUnit compilationUnit, int offset) {
    super(null);
    this.element = element;
    this.compilationUnit = compilationUnit;
    this.offset = offset;
  }

  @Override
  public ChangeCreationResult createChange() throws RefactoringException {
    return super.createChange();
  }

  @Override
  public Change getChange() {
    return super.getChange();
  }

  @Override
  public RefactoringResult apply() {
    throw new UnsupportedOperationException(
        "apply isn't supported on RenameLinkedModeRefactoringSession");
  }

  public LinkedModeModel getModel() {
    CompilationUnit root =
        SharedASTProvider.getAST(compilationUnit, SharedASTProvider.WAIT_YES, null);

    LinkedPositionGroupImpl group = new LinkedPositionGroupImpl();
    ASTNode selectedNode = NodeFinder.perform(root, offset, 0);
    if (!(selectedNode instanceof SimpleName)) {
      return null;
    }
    SimpleName nameNode = (SimpleName) selectedNode;

    fOriginalName = nameNode.getIdentifier();
    final int pos = nameNode.getStartPosition();
    ASTNode[] sameNodes = LinkedNodeFinder.findByNode(root, nameNode);

    // TODO: copied from LinkedNamesAssistProposal#apply(..):
    // sort for iteration order, starting with the node @ offset
    Arrays.sort(
        sameNodes,
        new Comparator<ASTNode>() {
          public int compare(ASTNode o1, ASTNode o2) {
            return rank(o1) - rank(o2);
          }

          /**
           * Returns the absolute rank of an <code>ASTNode</code>. Nodes preceding <code>pos</code>
           * are ranked last.
           *
           * @param node the node to compute the rank for
           * @return the rank of the node with respect to the invocation offset
           */
          private int rank(ASTNode node) {
            int relativeRank = node.getStartPosition() + node.getLength() - pos;
            if (relativeRank < 0) return Integer.MAX_VALUE + relativeRank;
            else return relativeRank;
          }
        });
    for (int i = 0; i < sameNodes.length; i++) {
      ASTNode elem = sameNodes[i];
      RegionImpl position = new RegionImpl();
      position.setOffset(elem.getStartPosition());
      position.setLength(elem.getLength());
      group.addPositions(position);
    }
    LinkedModeModelImpl model = new LinkedModeModelImpl();
    model.addGroups(group);
    return model;
  }

  /**
   * Make rename operation.
   *
   * @param newName the name which will be applied
   * @return result of the rename operation
   * @throws CoreException if an error occurs while creating the refactoring instance
   * @throws InvocationTargetException if an error occurred while executing the operation.
   * @throws InterruptedException if the operation has been canceled by the user.
   */
  public RefactoringResult doRename(String newName)
      throws CoreException, InvocationTargetException, InterruptedException {
    if (fOriginalName.equals(newName)) {
      return DtoConverter.toRefactoringResultDto(new RefactoringStatus());
    }
    RenameSupport renameSupport = undoAndCreateRenameSupport(newName);
    if (renameSupport == null)
      return DtoConverter.toRefactoringResultDto(
          RefactoringStatus.createFatalErrorStatus("Can't create rename refactoring"));

    RefactoringResult refactoringResult =
        DtoConverter.toRefactoringResultDto(renameSupport.perform());

    PerformChangeOperation operation = renameSupport.getfPerformChangeOperation();
    if (operation == null) {
      return refactoringResult;
    }
    CompositeChange operationChange = (CompositeChange) operation.getUndoChange();
    Change[] changes = operationChange.getChildren();

    List<ChangeInfo> changesInfo = new ArrayList<>();
    prepareChangesInfo(changes, changesInfo);

    refactoringResult.setChanges(changesInfo);

    return refactoringResult;
  }

  private RenameSupport undoAndCreateRenameSupport(String newName) throws CoreException {

    if (newName.length() == 0) return null;

    RenameJavaElementDescriptor descriptor = createRenameDescriptor(element, newName);
    return RenameSupport.create(descriptor);
  }

  /**
   * Creates a rename descriptor.
   *
   * @param javaElement element to rename
   * @param newName new name
   * @return a rename descriptor with current settings as used in the refactoring dialogs
   * @throws JavaModelException if an error occurs while accessing the element
   */
  private RenameJavaElementDescriptor createRenameDescriptor(
      IJavaElement javaElement, String newName) throws JavaModelException {
    String contributionId;
    // see RefactoringExecutionStarter#createRenameSupport(..):
    int elementType = javaElement.getElementType();
    switch (elementType) {
      case IJavaElement.JAVA_PROJECT:
        contributionId = IJavaRefactorings.RENAME_JAVA_PROJECT;
        break;
      case IJavaElement.PACKAGE_FRAGMENT_ROOT:
        contributionId = IJavaRefactorings.RENAME_SOURCE_FOLDER;
        break;
      case IJavaElement.PACKAGE_FRAGMENT:
        contributionId = IJavaRefactorings.RENAME_PACKAGE;
        break;
      case IJavaElement.COMPILATION_UNIT:
        contributionId = IJavaRefactorings.RENAME_COMPILATION_UNIT;
        break;
      case IJavaElement.TYPE:
        contributionId = IJavaRefactorings.RENAME_TYPE;
        break;
      case IJavaElement.METHOD:
        final IMethod method = (IMethod) javaElement;
        if (method.isConstructor())
          return createRenameDescriptor(method.getDeclaringType(), newName);
        else contributionId = IJavaRefactorings.RENAME_METHOD;
        break;
      case IJavaElement.FIELD:
        IField field = (IField) javaElement;
        if (field.isEnumConstant()) contributionId = IJavaRefactorings.RENAME_ENUM_CONSTANT;
        else contributionId = IJavaRefactorings.RENAME_FIELD;
        break;
      case IJavaElement.TYPE_PARAMETER:
        contributionId = IJavaRefactorings.RENAME_TYPE_PARAMETER;
        break;
      case IJavaElement.LOCAL_VARIABLE:
        contributionId = IJavaRefactorings.RENAME_LOCAL_VARIABLE;
        break;
      default:
        return null;
    }

    RenameJavaElementDescriptor descriptor =
        (RenameJavaElementDescriptor)
            RefactoringCore.getRefactoringContribution(contributionId).createDescriptor();
    descriptor.setJavaElement(javaElement);
    descriptor.setNewName(newName);
    if (elementType != IJavaElement.PACKAGE_FRAGMENT_ROOT) descriptor.setUpdateReferences(true);

    //        IDialogSettings javaSettings= JavaPlugin.getDefault().getDialogSettings();
    //        IDialogSettings refactoringSettings=
    // javaSettings.getSection(RefactoringWizardPage.REFACTORING_SETTINGS); //TODO: undocumented API
    //        if (refactoringSettings == null) {
    //            refactoringSettings=
    // javaSettings.addNewSection(RefactoringWizardPage.REFACTORING_SETTINGS);
    //        }

    switch (elementType) {
      case IJavaElement.METHOD:
      case IJavaElement.FIELD:
        descriptor.setDeprecateDelegate(
            /*refactoringSettings.getBoolean(DelegateUIHelper.DELEGATE_DEPRECATION)*/ false);
        descriptor.setKeepOriginal(
            /*refactoringSettings.getBoolean(DelegateUIHelper.DELEGATE_UPDATING)*/ false);
    }
    switch (elementType) {
      case IJavaElement.TYPE:
        //			case IJavaElement.COMPILATION_UNIT: // TODO
        descriptor.setUpdateSimilarDeclarations(
            /*refactoringSettings.getBoolean(RenameRefactoringWizard.TYPE_UPDATE_SIMILAR_ELEMENTS)*/ false);
        int strategy;
        try {
          strategy =
              1; // refactoringSettings.getInt(RenameRefactoringWizard.TYPE_SIMILAR_MATCH_STRATEGY);
        } catch (NumberFormatException e) {
          strategy = RenamingNameSuggestor.STRATEGY_EXACT;
        }
        descriptor.setMatchStrategy(strategy);
    }
    switch (elementType) {
      case IJavaElement.PACKAGE_FRAGMENT:
        descriptor.setUpdateHierarchy(
            /*refactoringSettings.getBoolean(RenameRefactoringWizard.PACKAGE_RENAME_SUBPACKAGES)*/ true);
    }
    switch (elementType) {
      case IJavaElement.PACKAGE_FRAGMENT:
      case IJavaElement.TYPE:
        String
            fileNamePatterns = /*refactoringSettings.get(RenameRefactoringWizard.QUALIFIED_NAMES_PATTERNS)*/
                "*";
        if (fileNamePatterns != null && fileNamePatterns.length() != 0) {
          descriptor.setFileNamePatterns(fileNamePatterns);
          boolean
              updateQualifiedNames = /*refactoringSettings.getBoolean(RenameRefactoringWizard.UPDATE_QUALIFIED_NAMES)*/
                  false;
          descriptor.setUpdateQualifiedNames(updateQualifiedNames);
          //                    fShowPreview|= updateQualifiedNames;
        }
    }
    switch (elementType) {
      case IJavaElement.PACKAGE_FRAGMENT:
      case IJavaElement.TYPE:
      case IJavaElement.FIELD:
        boolean updateTextualOccurrences =
            false; // refactoringSettings.getBoolean(RenameRefactoringWizard.UPDATE_TEXTUAL_MATCHES);
        descriptor.setUpdateTextualOccurrences(updateTextualOccurrences);
        //                fShowPreview|= updateTextualOccurrences;
    }
    switch (elementType) {
      case IJavaElement.FIELD:
        descriptor.setRenameGetters(
            /*refactoringSettings.getBoolean(RenameRefactoringWizard.FIELD_RENAME_GETTER)*/ false);
        descriptor.setRenameSetters(
            /*refactoringSettings.getBoolean(RenameRefactoringWizard.FIELD_RENAME_SETTER)*/ false);
    }
    return descriptor;
  }
}
