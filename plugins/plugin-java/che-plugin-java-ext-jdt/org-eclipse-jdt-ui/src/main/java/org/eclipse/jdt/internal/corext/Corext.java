/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext;

import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * Facade for JavaPlugin to not contaminate corext classes.
 */
public class Corext {

	public static String getPluginId() {
		return JavaPlugin.getPluginId();
	}
}
