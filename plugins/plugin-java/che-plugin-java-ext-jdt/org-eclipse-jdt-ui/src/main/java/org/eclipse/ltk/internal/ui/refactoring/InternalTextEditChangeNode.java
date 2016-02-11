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
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode.ChildNode;

import java.util.List;

public abstract class InternalTextEditChangeNode extends AbstractChangeNode {

	protected InternalTextEditChangeNode(PreviewNode parent, Change change) {
		super(parent, change);
	}

	public int getActive() {
		return getCompositeChangeActive();
	}

	boolean hasOneGroupCategory(List categories) {
		return ((TextEditBasedChange)getChange()).hasOneGroupCategory(categories);
	}

	protected TextEditBasedChange getTextEditBasedChange() {
		return (TextEditBasedChange)getChange();
	}

	final PreviewNode[] doCreateChildren() {
		return createChildNodes();
	}

	protected static TextEditChangeNode internalGetTextEditChangeNode(PreviewNode node) {
		PreviewNode element= node.getParent();
		while(!(element instanceof TextEditChangeNode) && element != null) {
			element= element.getParent();
		}
		return (TextEditChangeNode)element;
	}

	protected abstract ChildNode[] createChildNodes();
}
