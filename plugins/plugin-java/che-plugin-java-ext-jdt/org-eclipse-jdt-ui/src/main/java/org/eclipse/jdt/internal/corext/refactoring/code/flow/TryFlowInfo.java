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

class TryFlowInfo extends FlowInfo {

	public TryFlowInfo() {
		super();
	}

	public void mergeTry(FlowInfo info, FlowContext context) {
		if (info == null)
			return;

		assign(info);
	}

	public void mergeCatch(FlowInfo info, FlowContext context) {
		if (info == null)
			return;

		mergeConditional(info, context);
	}

	public void mergeFinally(FlowInfo info, FlowContext context) {
		if (info == null)
			return;

		mergeSequential(info, context);
	}
}

