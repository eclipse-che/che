/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ProgressMonitorWrapper;

public class NotCancelableProgressMonitor extends ProgressMonitorWrapper {
	public NotCancelableProgressMonitor(IProgressMonitor monitor) {
		super(monitor);
	}
	public void setCanceled(boolean b) {
		// ignore set cancel
	}
	public boolean isCanceled() {
		return false;
	}
}
