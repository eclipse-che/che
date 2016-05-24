/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.plugin.java.server.refactoring.session;

import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeCreationResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeInfo;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringResult;
import org.eclipse.che.plugin.java.server.refactoring.DtoConverter;
import org.eclipse.che.plugin.java.server.refactoring.RefactoringException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationStateChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.MoveCompilationUnitChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.RenameCompilationUnitChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.RenamePackageChange;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.UndoTextFileChange;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.ui.refactoring.AbstractChangeNode;
import org.eclipse.ltk.internal.ui.refactoring.FinishResult;
import org.eclipse.ltk.internal.ui.refactoring.PreviewNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The base class of the refactoring that describes all operation.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
public abstract class RefactoringSession {

    protected Refactoring       refactoring;
    protected PreviewNode       previewNode;
    private   RefactoringStatus conditionCheckingStatus;
    private   Change            change;

    public RefactoringSession(Refactoring refactoring) {
        this.refactoring = refactoring;
    }

    public ChangeCreationResult createChange() throws RefactoringException {
        Change change = createChange(new CreateChangeOperation(
                new CheckConditionsOperation(refactoring, CheckConditionsOperation.FINAL_CONDITIONS),
                RefactoringStatus.FATAL), true);
        // Status has been updated since we have passed true
        RefactoringStatus status = conditionCheckingStatus;
        // Creating the change has been canceled
        if (change == null && status == null) {
            internalSetChange(change);
            throw new RefactoringException("Creating the change has been canceled");
        }

        // Set change if we don't have fatal errors.
        if (!status.hasFatalError()) {
            internalSetChange(change);
        }

        ChangeCreationResult result = DtoFactory.newDto(ChangeCreationResult.class);
        result.setStatus(DtoConverter.toRefactoringStatusDto(status));
        result.setCanShowPreviewPage(status.isOK());
        return result;
    }

    private void internalSetChange(Change change) {
        this.change = change;
    }

    private Change createChange(CreateChangeOperation operation, boolean updateStatus) throws RefactoringException {
        CoreException exception = null;
        try {
            ResourcesPlugin.getWorkspace().run(operation, new NullProgressMonitor());
        } catch (CoreException e) {
            exception = e;
        }

        if (updateStatus) {
            RefactoringStatus status = null;
            if (exception != null) {
                status = new RefactoringStatus();
                String msg = exception.getMessage();
                if (msg != null) {
                    status.addFatalError(Messages.format("{0}. See the error log for more details.", msg));
                } else {
                    status.addFatalError(
                            "An unexpected exception occurred while creating a change object. See the error log for more details.");
                }
                JavaPlugin.log(exception);
            } else {
                status = operation.getConditionCheckingStatus();
            }
            setConditionCheckingStatus(status);
        } else {
            if (exception != null)
                throw new RefactoringException(exception);
        }
        Change change = operation.getChange();
        return change;
    }

    public void setConditionCheckingStatus(RefactoringStatus conditionCheckingStatus) {
        this.conditionCheckingStatus = conditionCheckingStatus;
    }

    public Change getChange() {
        return change;
    }

    /**
     * @return instance of {@link org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus}.
     * That describes status of the refactoring operation.
     */
    public RefactoringResult apply() {
        PerformChangeOperation operation = new PerformChangeOperation(change);
        FinishResult result = internalPerformFinish(operation);
        if (result.isException()) {
            return DtoConverter.toRefactoringResultDto(RefactoringStatus.createErrorStatus("Refactoring failed with Exception."));
        }

        CompositeChange operationChange = (CompositeChange)operation.getUndoChange();
        Change[] changes = operationChange.getChildren();

        RefactoringStatus validationStatus = operation.getValidationStatus();
        if (validationStatus != null) {
            List<ChangeInfo> changesInfo = new ArrayList<>();

            prepareChangesInfo(changes, changesInfo);

            RefactoringResult status = DtoConverter.toRefactoringResultDto(validationStatus);
            status.setChanges(changesInfo);

            return status;
        }

        return DtoConverter.toRefactoringResultDto(new RefactoringStatus());
    }

    /**
     * Prepare the information about changes which were applied.
     *
     * @param changes array of the applied changes
     * @param changesInfo prepared list of {@link ChangeInfo}
     */
    public void prepareChangesInfo(Change[] changes, List<ChangeInfo> changesInfo) {
        for (Change ch : changes) {
            if (ch instanceof DynamicValidationStateChange) {
                prepareChangesInfo(((DynamicValidationStateChange)ch).getChildren(), changesInfo);
            } else {
                ChangeInfo changeInfo = DtoFactory.newDto(ChangeInfo.class);
                String refactoringName = ch.getName();
                if (ch instanceof UndoTextFileChange) {
                    changeInfo.setName(ChangeInfo.ChangeName.UPDATE);
                    changeInfo.setPath(((CompilationUnit)ch.getModifiedElement()).getPath().toString());
                }
                if (refactoringName.startsWith("Rename")) {
                    if (ch instanceof RenameCompilationUnitChange) {
                        prepareRenameCompilationUnitChange(changeInfo, ch);
                    } else if (ch instanceof RenamePackageChange) {
                        prepareRenamePackageChange(changesInfo, changeInfo, ch);
                    }
                }
                if (refactoringName.startsWith("Move")) {
                    prepareMoveChange(changeInfo, ch);
                }

                changesInfo.add(changeInfo);
            }
        }
    }

    private void prepareMoveChange(ChangeInfo changeInfo, Change ch) {
        changeInfo.setName(ChangeInfo.ChangeName.MOVE);
        if (ch instanceof MoveCompilationUnitChange) {
            MoveCompilationUnitChange moveChange = (MoveCompilationUnitChange)ch;
            String className = moveChange.getCu().getPath().lastSegment();
            changeInfo.setOldPath(moveChange.getDestinationPackage().getPath().append(className).toString());
            changeInfo.setPath(((CompilationUnit)ch.getModifiedElement()).getPath().toString());
        }
    }

    private void prepareRenamePackageChange(List<ChangeInfo> changesInfo, ChangeInfo changeInfo, Change ch) {
        changeInfo.setName(ChangeInfo.ChangeName.RENAME_PACKAGE);
        RenamePackageChange renameChange = (RenamePackageChange)ch;

        IPath oldPackageName = new Path(renameChange.getOldName().replace('.', IPath.SEPARATOR));
        IPath newPackageName = new Path(renameChange.getNewName().replace('.', IPath.SEPARATOR));

        changeInfo.setOldPath(renameChange.getResourcePath()
                                          .removeLastSegments(oldPackageName.segmentCount())
                                          .append(newPackageName).toString());
        changeInfo.setPath(renameChange.getResourcePath().toString());

        Set<IResource> compilationUnits = renameChange.getFCompilationUnitStamps().keySet();
        for (IResource iResource : compilationUnits) {
            ChangeInfo change = DtoFactory.newDto(ChangeInfo.class);
            change.setName(ChangeInfo.ChangeName.UPDATE);

            IPath fullPathOldPath = iResource.getFullPath();
            IPath newPath = renameChange.getResourcePath().append(fullPathOldPath.toFile().getName());

            change.setOldPath(fullPathOldPath.toString());
            change.setPath(newPath.toString());

            changesInfo.add(change);
        }
    }

    private void prepareRenameCompilationUnitChange(ChangeInfo changeInfo, Change ch) {
        changeInfo.setName(ChangeInfo.ChangeName.RENAME_COMPILATION_UNIT);
        changeInfo.setPath(((CompilationUnit)ch.getModifiedElement()).getPath().toString());
        RenameCompilationUnitChange renameChange = (RenameCompilationUnitChange)ch;
        changeInfo.setOldPath(renameChange.getResourcePath().removeLastSegments(1).append(renameChange.getNewName()).toString());
    }

    private FinishResult internalPerformFinish(PerformChangeOperation op) {
        op.setUndoManager(RefactoringCore.getUndoManager(), refactoring.getName());
        try {
            ResourcesPlugin.getWorkspace().run(op, new NullProgressMonitor());
        } catch (CoreException e) {
            JavaPlugin.log(e);
            return FinishResult.createException();
        }
        return FinishResult.createOK();
    }

    public PreviewNode getChangePreview() {
        CompositeChange compositeChange;
        if (change instanceof CompositeChange) {
            compositeChange = (CompositeChange)change;
        } else {
            compositeChange = new CompositeChange("Dummy Change"); //$NON-NLS-1$
            compositeChange.add(change);
        }
        previewNode = AbstractChangeNode.createNode(null, compositeChange);
        return previewNode;
    }

    public void updateChangeEnabled(String changeId, boolean enabled) throws RefactoringException {
        PreviewNode node = findNode(previewNode, changeId);
        if (node == null) {
            throw new RefactoringException("Can't find refactoring change to update enabled state.");
        }

        node.setEnabled(enabled);
    }

    private PreviewNode findNode(PreviewNode node, String id) {
        if (node.getId().equals(id)) {
            return node;
        }

        PreviewNode[] children = node.getChildren();
        if (children != null) {
            PreviewNode found;
            for (PreviewNode child : children) {
                found = findNode(child, id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    public PreviewNode getChangePreview(String changeId) {
        return findNode(previewNode, changeId);
    }

    /**
     * Disposes this refactoring session.
     */
    public void dispose() {
        if (change != null) {
            change.dispose();
        }
    }
}
