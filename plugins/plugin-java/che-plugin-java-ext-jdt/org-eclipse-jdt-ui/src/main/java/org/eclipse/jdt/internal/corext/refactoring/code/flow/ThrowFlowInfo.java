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
package org.eclipse.jdt.internal.corext.refactoring.code.flow;


class ThrowFlowInfo extends FlowInfo {

	public ThrowFlowInfo() {
		super(THROW);
	}

	public void merge(FlowInfo info, FlowContext context) {
		if (info == null)
			return;

		assignAccessMode(info);
	}

}


