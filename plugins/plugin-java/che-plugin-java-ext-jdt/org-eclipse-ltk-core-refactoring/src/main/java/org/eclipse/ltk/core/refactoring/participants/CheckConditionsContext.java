/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IFile;

import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * A context that is shared between the refactoring processor and all its
 * associated participants during condition checking.
 * <p>
 * The context manages a set of {@link IConditionChecker}objects to collect
 * condition checks that should be perform across all participants and the
 * processor. For example validating if a file can be changed (see
 * {@link org.eclipse.core.resources.IWorkspace#validateEdit(org.eclipse.core.resources.IFile[], java.lang.Object)}
 * should only be called once for all files modified by the processor and all
 * participants.
 * </p>
 * <p>
 * Note: this class is not intended to be extended by clients.
 * </p>
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CheckConditionsContext {

	private Map fCheckers= new HashMap();

	/**
	 * Returns the condition checker of the given type.
	 *
	 * @param clazz the type of the condition checker
	 *
	 * @return the condition checker or <code>null</code> if
	 *  no checker is registered for the given type
	 */
	public IConditionChecker getChecker(Class clazz) {
		return (IConditionChecker)fCheckers.get(clazz);
	}

	/**
	 * Adds the given condition checker. An exception will be
	 * thrown if a checker of the same type already exists in
	 * this context.
	 *
	 * @param checker the checker to add
	 * @throws CoreException if a checker of the same type already
	 *  exists
	 */
	public void add(IConditionChecker checker) throws CoreException {
		Object old= fCheckers.put(checker.getClass(), checker);
		if (old != null) {
			fCheckers.put(checker.getClass(), old);
			throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(),
				IRefactoringCoreStatusCodes.CHECKER_ALREADY_EXISTS_IN_CONTEXT,
				Messages.format(RefactoringCoreMessages.CheckConditionContext_error_checker_exists, checker.getClass().toString()),
				null));
		}
	}

	/**
	 * Checks the condition of all registered condition checkers and returns a
	 * merge status result.
	 *
	 * @param pm a progress monitor or <code>null</code> if no progress
	 *  reporting is desired
	 *
	 * @return the combined status result
	 *
	 * @throws CoreException if an error occurs during condition checking
	 */
	public RefactoringStatus check(IProgressMonitor pm) throws CoreException {
		if (pm == null)
			pm= new NullProgressMonitor();
		RefactoringStatus result= new RefactoringStatus();
		mergeResourceOperationAndValidateEdit();
		List values= new ArrayList(fCheckers.values());
		Collections.sort(values, new Comparator() {
			public int compare(Object o1, Object o2) {
				// Note there can only be one ResourceOperationChecker. So it
				// is save to not test the case that both objects are
				// ResourceOperationChecker
				if (o1 instanceof ResourceChangeChecker)
					return -1;
				if (o2 instanceof ResourceChangeChecker)
					return 1;
				return 0;
			}
		});
		pm.beginTask("", values.size()); //$NON-NLS-1$
		for (Iterator iter= values.iterator(); iter.hasNext();) {
			IConditionChecker checker= (IConditionChecker)iter.next();
			result.merge(checker.check(new SubProgressMonitor(pm, 1)));
			if (pm.isCanceled())
				throw new OperationCanceledException();
		}
		return result;
	}

	private void mergeResourceOperationAndValidateEdit() throws CoreException {
		ValidateEditChecker validateEditChecker= (ValidateEditChecker) getChecker(ValidateEditChecker.class);
		if (validateEditChecker == null)
			return;
		ResourceChangeChecker resourceChangeChecker= (ResourceChangeChecker) getChecker(ResourceChangeChecker.class);
		if (resourceChangeChecker == null)
			return;

		IFile[] changedFiles= resourceChangeChecker.getChangedFiles();
		validateEditChecker.addFiles(changedFiles);
	}
}
