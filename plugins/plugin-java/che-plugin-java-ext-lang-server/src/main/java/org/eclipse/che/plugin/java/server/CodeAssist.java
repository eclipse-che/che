/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.java.server;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.inject.Singleton;

import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.Change;
import org.eclipse.che.ide.ext.java.shared.dto.ConflictImportDTO;
import org.eclipse.che.ide.ext.java.shared.dto.OrganizeImportResult;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;
import org.eclipse.che.ide.ext.java.shared.dto.ProposalApplyResult;
import org.eclipse.che.ide.ext.java.shared.dto.ProposalPresentation;
import org.eclipse.che.ide.ext.java.shared.dto.Proposals;
import org.eclipse.che.ide.ext.java.shared.dto.Region;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeInfo;
import org.eclipse.che.jdt.JavadocFinder;
import org.eclipse.che.jdt.javadoc.HTMLPrinter;
import org.eclipse.che.jdt.javaeditor.HasLinkedModel;
import org.eclipse.che.jdt.javaeditor.TextViewer;
import org.eclipse.che.jdt.ui.CheActionAcces;
import org.eclipse.che.jface.text.contentassist.ICompletionProposal;
import org.eclipse.che.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.che.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.che.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.che.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.DocumentAdapter;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation;
import org.eclipse.jdt.internal.corext.format.DocumentChangeListener;
import org.eclipse.jdt.internal.corext.refactoring.changes.MoveCompilationUnitChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.RenameCompilationUnitChange;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.correction.JavaCorrectionProcessor;
import org.eclipse.jdt.internal.ui.text.java.JavaAllCompletionProposalComputer;
import org.eclipse.jdt.internal.ui.text.java.RelevanceSorter;
import org.eclipse.jdt.internal.ui.text.java.TemplateCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.correction.ChangeCorrectionProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class CodeAssist {
    private static final Logger LOG = LoggerFactory.getLogger(CodeAssist.class);
    private final Cache<String, CodeAssistContext> cache;

    public CodeAssist() {
        //todo configure expire time
        cache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES).removalListener(
                new RemovalListener<String, CodeAssistContext>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, CodeAssistContext> notification) {
                        if (notification.getValue() != null) {
                            notification.getValue().clean();
                        }
                    }
                }).build();
    }

    public Proposals computeProposals(IJavaProject project, String fqn, int offset, final String content) throws JavaModelException {

        WorkingCopyOwner copyOwner = new WorkingCopyOwner() {
            @Override
            public IBuffer createBuffer(ICompilationUnit workingCopy) {
                return new org.eclipse.jdt.internal.ui.javaeditor.DocumentAdapter(workingCopy, workingCopy.getPath(), content);
            }
        };
        ICompilationUnit compilationUnit;

        IType type = project.findType(fqn);
        if (type == null) {
            return null;
        }
        if (type.isBinary()) {
            compilationUnit = type.getClassFile().getWorkingCopy(copyOwner, null);
        } else {
            compilationUnit = type.getCompilationUnit().getWorkingCopy(copyOwner, null);
        }

        IBuffer buffer = compilationUnit.getBuffer();
        IDocument document;
        if (buffer instanceof org.eclipse.jdt.internal.ui.javaeditor.DocumentAdapter) {
            document = ((org.eclipse.jdt.internal.ui.javaeditor.DocumentAdapter)buffer).getDocument();
        } else {
            document = new DocumentAdapter(buffer);
        }
        TextViewer viewer = new TextViewer(document, new Point(offset, 0));
        JavaContentAssistInvocationContext context =
                new JavaContentAssistInvocationContext(viewer, offset, compilationUnit);

        List<ICompletionProposal> proposals = new ArrayList<>();
        proposals.addAll(new JavaAllCompletionProposalComputer().computeCompletionProposals(context, null));
        proposals.addAll(new TemplateCompletionProposalComputer().computeCompletionProposals(context, null));

        Collections.sort(proposals, new RelevanceSorter());

        return convertProposals(offset, compilationUnit, viewer, proposals);
    }

    private Proposals convertProposals(int offset, ICompilationUnit compilationUnit, TextViewer viewer,
                                       List<ICompletionProposal> proposals) {
        Proposals result = DtoFactory.getInstance().createDto(Proposals.class);
        String sessionId = UUID.randomUUID().toString();
        result.setSessionId(sessionId);

        ArrayList<ProposalPresentation> presentations = new ArrayList<>();
        for (int i = 0; i < proposals.size(); i++) {
            ProposalPresentation presentation = DtoFactory.getInstance().createDto(ProposalPresentation.class);
            ICompletionProposal proposal = proposals.get(i);
            presentation.setIndex(i);
            presentation.setDisplayString(proposal.getDisplayString());
            String image = proposal.getImage() == null ? null : proposal.getImage().getImg();
            presentation.setImage(image);
            if (proposal instanceof ICompletionProposalExtension4) {
                presentation.setAutoInsertable(((ICompletionProposalExtension4)proposal).isAutoInsertable());
            }
            if (proposal instanceof CheActionAcces) {
                String actionId = ((CheActionAcces)proposal).getActionId();
                if (actionId != null) {
                    presentation.setActionId(actionId);
                }
            }
            presentations.add(presentation);
        }
        result.setProposals(presentations);
        cache.put(sessionId, new CodeAssistContext(viewer, offset, proposals, compilationUnit));
        return result;
    }

    public ProposalApplyResult applyCompletion(String sessionId, int index, boolean insert) {
        CodeAssistContext context = cache.getIfPresent(sessionId);
        if (context != null) {
            try {
                return context.apply(index, insert);
            } finally {
                cache.invalidate(sessionId);
            }
        } else {
            throw new IllegalArgumentException("CodeAssist context doesn't exist or time of completion was expired");
        }
    }

    @SuppressWarnings("unchecked")
    public Proposals computeAssistProposals(IJavaProject project, String fqn, int offset, List<Problem> problems) throws CoreException {
        ICompilationUnit compilationUnit;

        IType type = project.findType(fqn);
        if (type == null) {
            return null;
        }
        if (type.isBinary()) {
            throw new JavaModelException(
                    new JavaModelStatus(IJavaModelStatusConstants.CORE_EXCEPTION, "Can't calculate Quick Assist on binary file"));
        } else {
            compilationUnit = type.getCompilationUnit();
        }

        IBuffer buffer = compilationUnit.getBuffer();

        ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
        bufferManager.connect(compilationUnit.getPath(), LocationKind.IFILE, new NullProgressMonitor());
        ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(compilationUnit.getPath(), LocationKind.IFILE);
        IDocument document = textFileBuffer.getDocument();
        TextViewer viewer = new TextViewer(document, new Point(offset, 0));
        AssistContext context = new AssistContext(compilationUnit, offset, 0);
        ArrayList proposals = new ArrayList<>();
        JavaCorrectionProcessor.collectProposals(context, problems, true, true, proposals);
        return convertProposals(offset, compilationUnit, viewer, proposals);
    }

    public String getJavaDoc(String sessionId, int index) {
        CodeAssistContext context = cache.getIfPresent(sessionId);
        if (context != null) {
            return context.getJavadoc(index);
        } else {

            throw new IllegalArgumentException("CodeAssist context doesn't exist or time of completion was expired");
        }
    }

    /**
     * Organizes the imports of a compilation unit.
     *
     * @param project
     *         current java project
     * @param fqn
     *         fully qualified name of the java file
     * @return list of imports which have conflicts
     */
    public OrganizeImportResult organizeImports(IJavaProject project, String fqn) throws CoreException, BadLocationException {
        ICompilationUnit compilationUnit = prepareCompilationUnit(project, fqn);
        return createOrganizeImportOperation(compilationUnit, null);
    }

    /**
     * Applies chosen imports after resolving conflicts.
     *
     * @param project
     *        current java project
     * @param fqn
     *         fully qualified name of the java file
     * @param  chosen
     *          list of chosen imports as result of resolving conflicts which needed to add to all imports.
     */
    public List<Change> applyChosenImports(IJavaProject project, String fqn, List<String> chosen) throws CoreException, BadLocationException {
        ICompilationUnit compilationUnit = prepareCompilationUnit(project, fqn);
        OrganizeImportResult result = createOrganizeImportOperation(compilationUnit, chosen);
        return result.getChanges();
    }

    private OrganizeImportResult createOrganizeImportOperation(ICompilationUnit compilationUnit,
                                                               List<String> chosen) throws CoreException {
        CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings(compilationUnit.getJavaProject());

        OrganizeImportsOperation operation = new OrganizeImportsOperation(compilationUnit,
                                                                          null,
                                                                          settings.importIgnoreLowercase,
                                                                          !compilationUnit.isWorkingCopy(),
                                                                          true,
                                                                          chosen,
                                                                          null);

        NullProgressMonitor monitor = new NullProgressMonitor();
        TextEdit edit = operation.createTextEdit(monitor);
        OrganizeImportResult result = DtoFactory.newDto(OrganizeImportResult.class);
        TypeNameMatch[][] choices = operation.getChoices();
        //Apply organize import declarations if operation doesn't have conflicts (choices.length == 0)
        //or all conflicts were resolved (!chosen.isEmpty())
        if ((chosen != null && !chosen.isEmpty()) || choices == null || choices.length == 0) {
            IBuffer buffer = compilationUnit.getBuffer();
            IDocument document = new Document(buffer.getContents());
            DocumentChangeListener documentChangeListener = new DocumentChangeListener(document);
            try {
                edit.apply(document);
            } catch (BadLocationException e) {
                LOG.debug("Applying Organize import text edits goes wrong:", e);
            }
            result.setChanges(documentChangeListener.getChanges());
            return result;
        }

        result.setConflicts(createListOfDTOMatches(choices));
        return result;
    }

    private List<ConflictImportDTO> createListOfDTOMatches(TypeNameMatch[][] choices) {
        List<ConflictImportDTO> typeMatches = new ArrayList<>();
        for (int i = 0; i < choices.length; i++) {
            List<String> nameMatches = new ArrayList<>();
            TypeNameMatch[] choice = choices[i];
            for (int j = 0; j < choice.length; j++) {
                nameMatches.add(choice[j].getFullyQualifiedName());
            }
            typeMatches.add(DtoFactory.newDto(ConflictImportDTO.class).withTypeMatches(nameMatches));
        }
        return typeMatches;
    }

    private ICompilationUnit prepareCompilationUnit(IJavaProject project, String fqn) throws JavaModelException {
        ICompilationUnit compilationUnit;

        IType type = project.findType(fqn);
        if (type == null) {
            throw new JavaModelException(
                    new JavaModelStatus(IJavaModelStatusConstants.CORE_EXCEPTION, "Can't find a file: " + fqn));
        }
        if (type.isBinary()) {
            throw new JavaModelException(
                    new JavaModelStatus(IJavaModelStatusConstants.CORE_EXCEPTION, "Can't organize imports on binary file"));
        } else {
            compilationUnit = type.getCompilationUnit();
        }
        return compilationUnit;
    }

    private class CodeAssistContext {
        private TextViewer                viewer;
        private int                       offset;
        private List<ICompletionProposal> proposals;
        private ICompilationUnit          cUnit;

        public CodeAssistContext(TextViewer viewer, int offset,
                                 List<ICompletionProposal> proposals, ICompilationUnit cUnit) {
            this.viewer = viewer;
            this.offset = offset;
            this.proposals = proposals;
            this.cUnit = cUnit;
        }

        public void clean() {
            if (cUnit != null) {
                try {
                    cUnit.discardWorkingCopy();
                } catch (JavaModelException e) {
                    //ignore
                }
                try {
                    FileBuffers.getTextFileBufferManager().disconnect(cUnit.getPath(), LocationKind.IFILE, new NullProgressMonitor());
                } catch (CoreException e) {
                    LOG.error("Can't disconnect from file buffer: " + cUnit.getPath(), e);
                }
            }
        }

        public ProposalApplyResult apply(int index, boolean insert) {
            IDocument document = viewer.getDocument();
            final List<Change> changes = new ArrayList<>();
            document.addDocumentListener(new IDocumentListener() {
                @Override
                public void documentAboutToBeChanged(DocumentEvent event) {
                }

                @Override
                public void documentChanged(DocumentEvent event) {
                    changes.add(DtoFactory.newDto(Change.class)
                                          .withLength(event.getLength())
                                          .withOffset(event.getOffset())
                                          .withText(event.getText()));
                }
            });
            try {
                char trigger = (char)0;
                int stateMask = insert ? 0 : SWT.CTRL;
                ICompletionProposal completionProposal = proposals.get(index);
                ProposalApplyResult result = DtoFactory.newDto(ProposalApplyResult.class);
                if (completionProposal instanceof ChangeCorrectionProposal) {
                    result.setChangeInfo(prepareChangeInfo((ChangeCorrectionProposal)completionProposal));
                }
                if (completionProposal instanceof ICompletionProposalExtension2) {
                    ICompletionProposalExtension2 completionProposalExtension2 = (ICompletionProposalExtension2)completionProposal;
                    completionProposalExtension2.apply(viewer, trigger, stateMask, offset);
                } else if (completionProposal instanceof ICompletionProposalExtension) {
                    ICompletionProposalExtension completionProposalExtension = (ICompletionProposalExtension)completionProposal;
                    completionProposalExtension.apply(document, trigger, offset);
                } else {
                    completionProposal.apply(document);
                }

                result.setChanges(changes);
                Point selection = completionProposal.getSelection(document);
                if (selection != null) {
                    result.setSelection(DtoFactory.newDto(Region.class).withOffset(selection.x).withLength(selection.y));
                }
                if (completionProposal instanceof HasLinkedModel) {
                    result.setLinkedModeModel(((HasLinkedModel)completionProposal).getLinkedModel());
                }
                return result;


            } catch (IndexOutOfBoundsException | CoreException e) {
                throw new IllegalArgumentException("Can't find completion: " + index, e);
            }
        }

        public String getJavadoc(int index) {
            ICompletionProposal proposal = proposals.get(index);
            String result;
            if (proposal instanceof ICompletionProposalExtension5) {
                Object info = ((ICompletionProposalExtension5)proposal).getAdditionalProposalInfo(null);
                if (info != null) {
                    result = info.toString();
                } else {
                    StringBuffer buffer = new StringBuffer();
                    HTMLPrinter.insertPageProlog(buffer, 0, JavadocFinder.getStyleSheet());
                    HTMLPrinter.addParagraph(buffer, "No documentation found.");
                    HTMLPrinter.addPageEpilog(buffer);
                    result = buffer.toString();
                }
            } else {
                result = proposal.getAdditionalProposalInfo();
            }
            return result;
        }

        private ChangeInfo prepareChangeInfo(ChangeCorrectionProposal changeCorrectionProposal) throws CoreException {
            org.eclipse.ltk.core.refactoring.Change change = changeCorrectionProposal.getChange();
            if (change == null) {
                return null;
            }
            ChangeInfo changeInfo = DtoFactory.newDto(ChangeInfo.class);
            String changeName = change.getName();
            if (changeName.startsWith("Rename") && change instanceof RenameCompilationUnitChange) {
                prepareRenameCompilationUnitChange(changeInfo, change);
            } else if (changeName.startsWith("Move")) {
                prepareMoveChange(changeInfo, change);
            }

            return changeInfo;
        }

        private void prepareMoveChange(ChangeInfo changeInfo, org.eclipse.ltk.core.refactoring.Change ch) {
            changeInfo.setName(ChangeInfo.ChangeName.MOVE);
            for (org.eclipse.ltk.core.refactoring.Change change : ((CompositeChange)ch).getChildren()) {
                if (change instanceof MoveCompilationUnitChange) {
                    MoveCompilationUnitChange moveChange = (MoveCompilationUnitChange)change;
                    String className = moveChange.getCu().getPath().lastSegment();
                    changeInfo.setPath(moveChange.getDestinationPackage().getPath().append(className).toString());
                    changeInfo.setOldPath(((CompilationUnit)change.getModifiedElement()).getPath().toString());
                }
            }
        }

        private void prepareRenameCompilationUnitChange(ChangeInfo changeInfo, org.eclipse.ltk.core.refactoring.Change change) {
            changeInfo.setName(ChangeInfo.ChangeName.RENAME_COMPILATION_UNIT);
            RenameCompilationUnitChange renameChange = (RenameCompilationUnitChange)change;
            changeInfo.setPath(renameChange.getResourcePath().removeLastSegments(1).append(renameChange.getNewName()).toString());
            changeInfo.setOldPath(renameChange.getResourcePath().toString());
        }
    }
}
