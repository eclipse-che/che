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

package org.eclipse.jdt.internal.corext.refactoring.util;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.ui.text.correction.JavadocTagsSubProcessor;
import org.eclipse.text.edits.TextEditGroup;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


public class JavadocUtil {

	private JavadocUtil() {
		// static-only
	}

	//TODO: is a copy of ChangeSignatureRefactoring.DeclarationUpdate#createParamTag(..)
	public static TagElement createParamTag(String parameterName, AST ast, IJavaProject javaProject) {
		TagElement paramNode= ast.newTagElement();
		paramNode.setTagName(TagElement.TAG_PARAM);

		SimpleName simpleName= ast.newSimpleName(parameterName);
		paramNode.fragments().add(simpleName);

		TextElement textElement= ast.newTextElement();
		String text= StubUtility.getTodoTaskTag(javaProject);
		if (text != null)
			textElement.setText(text); //TODO: use template with {@todo} ...
		paramNode.fragments().add(textElement);

		return paramNode;
	}

	/**
	 * Decide whether to add a "param" javadoc tag or not.
	 * @param methodDeclaration the method declaration
	 * @return method has javadoc && (method had no parameter before || there is already an @param tag)
	 */
	public static boolean shouldAddParamJavadoc(MethodDeclaration methodDeclaration) {
		Javadoc javadoc= methodDeclaration.getJavadoc();
		if (javadoc == null)
			return false;
		if (methodDeclaration.parameters().size() == 0)
			return true;
		List<TagElement> tags= javadoc.tags();
		for (Iterator<TagElement> iter= tags.iterator(); iter.hasNext();) {
			TagElement element= iter.next();
			if (TagElement.TAG_PARAM.equals(element.getTagName()))
				return true;
		}
		return false;
	}


	/**
	 * Adds a "param" javadoc tag for a new last parameter if necessary.
	 * @param parameterName
	 * @param methodDeclaration
	 * @param astRewrite
	 * @param javaProject
	 * @param groupDescription
	 */
	public static void addParamJavadoc(String parameterName, MethodDeclaration methodDeclaration,
			ASTRewrite astRewrite, IJavaProject javaProject, TextEditGroup groupDescription) {
		if (! shouldAddParamJavadoc(methodDeclaration))
			return;

		ListRewrite tagsRewrite= astRewrite.getListRewrite(methodDeclaration.getJavadoc(), Javadoc.TAGS_PROPERTY);
		HashSet<String> leadingNames= new HashSet<String>();
		for (Iterator<SingleVariableDeclaration> iter= methodDeclaration.parameters().iterator(); iter.hasNext();) {
			SingleVariableDeclaration curr= iter.next();
			leadingNames.add(curr.getName().getIdentifier());
		}
		TagElement parameterTag= createParamTag(parameterName, astRewrite.getAST(), javaProject);
		JavadocTagsSubProcessor.insertTag(tagsRewrite, parameterTag, leadingNames, groupDescription);
	}

}
