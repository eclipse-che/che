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

class ForFlowInfo extends FlowInfo {

	public void mergeInitializer(FlowInfo info, FlowContext context) {
		if (info == null)
			return;
		mergeAccessModeSequential(info, context);
	}

	public void mergeCondition(FlowInfo info, FlowContext context) {
		if (info == null)
			return;
		mergeAccessModeSequential(info, context);
	}

	public void mergeIncrement(FlowInfo info, FlowContext context) {
		if (info == null)
			return;

		info.mergeEmptyCondition(context);
		mergeAccessModeSequential(info, context);
	}

	public void mergeAction(FlowInfo info, FlowContext context) {
		if (info == null)
			return;

		info.mergeEmptyCondition(context);

		mergeSequential(info, context);
	}
}

