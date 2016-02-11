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
package org.eclipse.jdt.internal.corext.refactoring.code.flow;

class DoWhileFlowInfo extends FlowInfo {

	private boolean fActionBranches;

	public void mergeAction(FlowInfo info, FlowContext context) {
		if (info == null)
			return;

		fActionBranches= info.branches();

		assign(info);

		if (fActionBranches && fReturnKind == VALUE_RETURN) {
			fReturnKind= PARTIAL_RETURN;
		}

	}

	public void mergeCondition(FlowInfo info, FlowContext context) {
		if (fActionBranches || fReturnKind == VALUE_RETURN || fReturnKind == VOID_RETURN || info == null)
			return;
		mergeAccessModeSequential(info, context);
	}
}

