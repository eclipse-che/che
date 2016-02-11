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

class IfFlowInfo extends FlowInfo {

	public void mergeCondition(FlowInfo info, FlowContext context) {
		if (info == null)
			return;
		mergeAccessModeSequential(info, context);
	}

	public void merge(FlowInfo thenPart, FlowInfo elsePart, FlowContext context) {
		if (thenPart == null && elsePart == null)
			return;

		GenericConditionalFlowInfo cond= new GenericConditionalFlowInfo();
		if (thenPart != null)
			cond.merge(thenPart, context);

		if (elsePart != null)
			cond.merge(elsePart, context);

		if (thenPart == null || elsePart == null)
			cond.mergeEmptyCondition(context);

		mergeSequential(cond, context);
	}
}

