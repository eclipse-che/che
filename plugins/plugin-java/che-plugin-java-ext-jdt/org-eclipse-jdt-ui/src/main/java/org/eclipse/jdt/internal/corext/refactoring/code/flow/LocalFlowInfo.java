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

import org.eclipse.jdt.core.dom.IVariableBinding;

class LocalFlowInfo extends FlowInfo {

	private int fVariableId;

	public LocalFlowInfo(IVariableBinding binding, int localAccessMode, FlowContext context) {
		super(NO_RETURN);
		fVariableId= binding.getVariableId();
		if (context.considerAccessMode()) {
			createAccessModeArray(context);
			fAccessModes[fVariableId - context.getStartingIndex()]= localAccessMode;
			context.manageLocal(binding);
		}
	}

	public LocalFlowInfo(LocalFlowInfo info, int localAccessMode, FlowContext context) {
		super(NO_RETURN);
		fVariableId= info.fVariableId;
		if (context.considerAccessMode()) {
			createAccessModeArray(context);
			fAccessModes[fVariableId - context.getStartingIndex()]= localAccessMode;
		}
	}

	public void setWriteAccess(FlowContext context) {
		if (context.considerAccessMode()) {
			fAccessModes[fVariableId - context.getStartingIndex()]= FlowInfo.WRITE;
		}
	}
}

