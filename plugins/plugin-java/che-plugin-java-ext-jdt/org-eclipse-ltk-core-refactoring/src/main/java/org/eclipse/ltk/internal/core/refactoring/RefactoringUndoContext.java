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
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.core.commands.operations.IUndoContext;

import org.eclipse.core.resources.ResourcesPlugin;

public class RefactoringUndoContext implements IUndoContext {

	public String getLabel() {
		return RefactoringCoreMessages.RefactoringUndoContext_label;
	}

	public boolean matches(IUndoContext context) {
		if (this == context)
			return true;
		IUndoContext workspaceContext= (IUndoContext)ResourcesPlugin.getWorkspace().getAdapter(IUndoContext.class);
		if (workspaceContext == null)
			return false;
		return workspaceContext.matches(context);
	}
}
