/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.refactoring.rename;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.text.edits.ReplaceEdit;

import org.eclipse.ltk.core.refactoring.GroupCategorySet;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.core.search.MethodDeclarationMatch;
import org.eclipse.jdt.core.search.SearchMatch;

import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.SearchResultGroup;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.delegates.DelegateMethodCreator;
import org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.util.TextChangeManager;
import org.eclipse.jdt.internal.corext.util.Messages;

import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;

public class RenameNonVirtualMethodProcessor extends RenameMethodProcessor {

	/**
	 * Creates a new rename method processor.
	 * <p>
	 * This constructor is only invoked by <code>RenameTypeProcessor</code>.
	 * </p>
	 *
	 * @param method the method
	 * @param manager the change manager
	 * @param categorySet the group category set
	 */
	RenameNonVirtualMethodProcessor(IMethod method, TextChangeManager manager, GroupCategorySet categorySet) {
		super(method, manager, categorySet);
	}

	/**
	 * Creates a new rename method processor.
	 * @param method the method, or <code>null</code> if invoked by scripting
	 */
	public RenameNonVirtualMethodProcessor(IMethod method) {
		super(method);
	}

	/**
	 * Creates a new rename method processor from scripting arguments
	 *
	 * @param method the method, or <code>null</code> if invoked by scripting
	 * @param arguments the arguments
	 * @param status the resulting status
	 */
	public RenameNonVirtualMethodProcessor(IMethod method, JavaRefactoringArguments arguments, RefactoringStatus status) {
		this(method);
		RefactoringStatus initializeStatus= initialize(arguments);
		status.merge(initializeStatus);
	}

	@Override
	public boolean isApplicable() throws CoreException {
		return RefactoringAvailabilityTester.isRenameNonVirtualMethodAvailable(getMethod());
	}

	//----------- preconditions --------------

	@Override
	protected RefactoringStatus doCheckFinalConditions(IProgressMonitor pm, CheckConditionsContext checkContext) throws CoreException {
		try{
			pm.beginTask("", 3); //$NON-NLS-1$
			RefactoringStatus result= new RefactoringStatus();
			result.merge(super.doCheckFinalConditions(new SubProgressMonitor(pm, 1), checkContext));
			if (result.hasFatalError())
				return result;

			final IMethod method= getMethod();
			final IType declaring= method.getDeclaringType();
			final String name= getNewElementName();
			IMethod[] hierarchyMethods= hierarchyDeclaresMethodName(
				new SubProgressMonitor(pm, 1), declaring.newTypeHierarchy(new SubProgressMonitor(pm, 1)), method, name);

			for (int i= 0; i < hierarchyMethods.length; i++) {
				IMethod hierarchyMethod= hierarchyMethods[i];
				RefactoringStatusContext context= JavaStatusContext.create(hierarchyMethod);
				if (Checks.compareParamTypes(method.getParameterTypes(), hierarchyMethod.getParameterTypes())) {
					String message= Messages.format(
						RefactoringCoreMessages.RenamePrivateMethodRefactoring_hierarchy_defines,
						new String[]{BasicElementLabels.getJavaElementName(declaring.getFullyQualifiedName('.')), BasicElementLabels.getJavaElementName(name)});
					result.addError(message, context);
				}else {
					String message= Messages.format(
						RefactoringCoreMessages.RenamePrivateMethodRefactoring_hierarchy_defines2,
						new String[]{BasicElementLabels.getJavaElementName(declaring.getFullyQualifiedName('.')), BasicElementLabels.getJavaElementName(name)});
					result.addWarning(message, context);
				}
			}
			return result;
		} finally{
			pm.done();
		}
	}

	/*
	 * @see RenameMethodProcessor#addOccurrences(org.eclipse.jdt.internal.corext.refactoring.util.TextChangeManager, org.eclipse.core.runtime.IProgressMonitor, RefactoringStatus)
	 */
	@Override
	protected void addOccurrences(TextChangeManager manager, IProgressMonitor pm, RefactoringStatus status) throws CoreException {
		pm.beginTask("", 1); //$NON-NLS-1$
		// declaration update must be registered first
		addDeclarationUpdate(manager);
		if (getUpdateReferences())
			addReferenceUpdates(manager, pm);
		pm.worked(1);
	}

	private ICompilationUnit getDeclaringCU() {
		return getMethod().getCompilationUnit();
	}

	private void addDeclarationUpdate(TextChangeManager manager) throws CoreException {

		if (getDelegateUpdating()) {
			// create the delegate
			CompilationUnitRewrite rewrite= new CompilationUnitRewrite(getDeclaringCU());
			rewrite.setResolveBindings(true);
			MethodDeclaration methodDeclaration= ASTNodeSearchUtil.getMethodDeclarationNode(getMethod(), rewrite.getRoot());
			DelegateMethodCreator creator= new DelegateMethodCreator();
			creator.setDeclaration(methodDeclaration);
			creator.setDeclareDeprecated(getDeprecateDelegates());
			creator.setSourceRewrite(rewrite);
			creator.setCopy(true);
			creator.setNewElementName(getNewElementName());
			creator.prepareDelegate();
			creator.createEdit();
			CompilationUnitChange cuChange= rewrite.createChange(true);
			if (cuChange != null) {
				cuChange.setKeepPreviewEdits(true);
				manager.manage(getDeclaringCU(), cuChange);
			}
		}

		String editName= RefactoringCoreMessages.RenameMethodRefactoring_update_declaration;
		ISourceRange nameRange= getMethod().getNameRange();
		ReplaceEdit replaceEdit= new ReplaceEdit(nameRange.getOffset(), nameRange.getLength(), getNewElementName());
		addTextEdit(manager.get(getDeclaringCU()), editName, replaceEdit);
	}

	private void addReferenceUpdates(TextChangeManager manager, IProgressMonitor pm) {
		SearchResultGroup[] grouped= getOccurrences();
		for (int i= 0; i < grouped.length; i++) {
			SearchResultGroup group= grouped[i];
			SearchMatch[] results= group.getSearchResults();
			ICompilationUnit cu= group.getCompilationUnit();
			TextChange change= manager.get(cu);
			for (int j= 0; j < results.length; j++){
				SearchMatch match= results[j];
				if (!(match instanceof MethodDeclarationMatch)) {
					ReplaceEdit replaceEdit= createReplaceEdit(match, cu);
					String editName= RefactoringCoreMessages.RenamePrivateMethodRefactoring_update;
					addTextEdit(change, editName, replaceEdit);
				}
			}
		}
		pm.done();
	}

	public String getDelegateUpdatingTitle(boolean plural) {
		if (plural)
			return RefactoringCoreMessages.DelegateMethodCreator_keep_original_renamed_plural;
		else
			return RefactoringCoreMessages.DelegateMethodCreator_keep_original_renamed_singular;
	}
}
