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
package org.eclipse.jdt.internal.corext.codemanipulation;

public class CodeGenerationSettings {

	public boolean createComments= true;
	public boolean useKeywordThis= false;

	public boolean importIgnoreLowercase= true;
	public boolean overrideAnnotation= false;

	public int tabWidth;
	public int indentWidth;



	public void setSettings(CodeGenerationSettings settings) {
		settings.createComments= createComments;
		settings.useKeywordThis= useKeywordThis;
		settings.importIgnoreLowercase= importIgnoreLowercase;
		settings.overrideAnnotation= overrideAnnotation;
		settings.tabWidth= tabWidth;
		settings.indentWidth= indentWidth;
	}


}

