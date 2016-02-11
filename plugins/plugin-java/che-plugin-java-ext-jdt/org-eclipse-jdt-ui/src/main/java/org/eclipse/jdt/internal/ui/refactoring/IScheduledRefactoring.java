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
package org.eclipse.jdt.internal.ui.refactoring;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

public interface IScheduledRefactoring {

	/**
	 * The scheduling rule used to perform the
	 * refactoring.
	 *
	 * @return {@link ISchedulingRule} not null
	 */
	public ISchedulingRule getSchedulingRule();

}
