/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.jdt.refactoring.session;

import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameSettings;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.corext.refactoring.tagging.IDelegateUpdating;
import org.eclipse.jdt.internal.corext.refactoring.tagging.INameUpdating;
import org.eclipse.jdt.internal.corext.refactoring.tagging.IQualifiedNameUpdating;
import org.eclipse.jdt.internal.corext.refactoring.tagging.IReferenceUpdating;
import org.eclipse.jdt.internal.corext.refactoring.tagging.ISimilarDeclarationUpdating;
import org.eclipse.jdt.internal.corext.refactoring.tagging.ITextUpdating;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

/**
 * @author Evgen Vidolob
 */
public class RenameSession extends RefactoringSession {
    public RenameSession(RenameRefactoring refactoring) {
        super(refactoring);
    }

    public void setSettings(RenameSettings settings) {
        IDelegateUpdating delegateUpdating = (IDelegateUpdating)refactoring.getAdapter(IDelegateUpdating.class);
        if(delegateUpdating != null && delegateUpdating.canEnableDelegateUpdating()){
            delegateUpdating.setDelegateUpdating(settings.isDelegateUpdating());
            delegateUpdating.setDeprecateDelegates(settings.isDeprecateDelegates());
        }
        IQualifiedNameUpdating nameUpdating = (IQualifiedNameUpdating)refactoring.getAdapter(IQualifiedNameUpdating.class);
        if(nameUpdating != null && nameUpdating.canEnableQualifiedNameUpdating()){
            nameUpdating.setUpdateQualifiedNames(settings.isUpdateQualifiedNames());
            if(settings.isUpdateQualifiedNames()){
                nameUpdating.setFilePatterns(settings.getFilePatterns());
            }
        }

        IReferenceUpdating referenceUpdating = (IReferenceUpdating)refactoring.getAdapter(IReferenceUpdating.class);
        if(referenceUpdating != null){
            referenceUpdating.setUpdateReferences(settings.isUpdateReferences());
        }

        ISimilarDeclarationUpdating similarDeclarationUpdating =
                (ISimilarDeclarationUpdating)refactoring.getAdapter(ISimilarDeclarationUpdating.class);
        if(similarDeclarationUpdating != null){
            similarDeclarationUpdating.setUpdateSimilarDeclarations(settings.isUpdateSimilarDeclarations());
            if(settings.isUpdateSimilarDeclarations()) {
                similarDeclarationUpdating.setMatchStrategy(settings.getMachStrategy());
            }
        }

        ITextUpdating textUpdating = (ITextUpdating)refactoring.getAdapter(ITextUpdating.class);
        if(textUpdating != null && textUpdating.canEnableTextUpdating()){
            textUpdating.setUpdateTextualMatches(settings.isUpdateTextualMatches());
        }


    }

    public RefactoringStatus validateNewName(String newName) {
        INameUpdating updating = getNameUpdating();
        updating.setNewElementName(newName);
        try {
            return updating.checkNewElementName(newName);
        } catch (CoreException e){
            JavaPlugin.log(e);
            return RefactoringStatus.createFatalErrorStatus("An unexpected exception occurred. See the error log for more details.");
        }
    }

    private INameUpdating getNameUpdating() {
        return (INameUpdating)refactoring.getAdapter(INameUpdating.class);
    }
}
