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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;

import java.util.ArrayList;
import java.util.List;

public class CompositeChangeNode extends AbstractChangeNode {

	private final RefactoringPreviewChangeFilter fFilter;

	public CompositeChangeNode(PreviewNode parent, RefactoringPreviewChangeFilter filter, CompositeChange change) {
		super(parent, change);
		fFilter = filter;
	}

	public int getActive() {
		return getCompositeChangeActive();
	}

	public ImageDescriptor getImageDescriptor() {
		return RefactoringPluginImages.DESC_OBJS_COMPOSITE_CHANGE;
	}

	PreviewNode[] doCreateChildren() {
		List children = new ArrayList();
		getFlattendedChildren(children, this, (CompositeChange)getChange());
		return (PreviewNode[])children.toArray(new PreviewNode[children.size()]);
	}

	private void getFlattendedChildren(List result, CompositeChangeNode parent, CompositeChange focus) {
		Change[] changes = focus.getChildren();
		for (int i = 0; i < changes.length; i++) {
			Change change = changes[i];
			if (fFilter == null || fFilter.select(change)) {
				if (change instanceof CompositeChange && ((CompositeChange)change).isSynthetic()) {
					getFlattendedChildren(result, parent, (CompositeChange) change);
				} else {
					result.add(createNode(parent, change));
				}
			}
		}
	}
}