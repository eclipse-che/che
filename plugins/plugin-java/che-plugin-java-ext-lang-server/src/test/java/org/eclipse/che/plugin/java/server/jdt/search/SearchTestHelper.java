/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.java.server.jdt.search;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.ui.search.JavaSearchQuery;
import org.eclipse.jdt.internal.ui.search.JavaSearchResult;
import org.eclipse.jdt.internal.ui.search.JavaSearchScopeFactory;
import org.eclipse.jdt.ui.search.ElementQuerySpecification;
import org.eclipse.jdt.ui.search.PatternQuerySpecification;
import org.eclipse.search.NewSearchUI;

public class SearchTestHelper {
    static int countMethodRefs(String TypeName, String methodName, String[] parameterTypes) throws JavaModelException {
        JavaSearchQuery query = runMethodRefQuery(TypeName, methodName, parameterTypes);
        JavaSearchResult result = (JavaSearchResult)query.getSearchResult();
        return result.getMatchCount();
    }

    static int countMethodRefs(String methodName) {
        JavaSearchQuery query = runMethodRefQuery(methodName);
        JavaSearchResult result = (JavaSearchResult)query.getSearchResult();
        return result.getMatchCount();
    }


    static JavaSearchQuery runMethodRefQuery(String TypeName, String methodName, String[] parameterTypes) throws JavaModelException {
        IMethod method = getMethod(TypeName, methodName, parameterTypes);
        JavaSearchQuery query = new JavaSearchQuery(new ElementQuerySpecification(method, IJavaSearchConstants.REFERENCES,
                                                                                  JavaSearchScopeFactory.getInstance()
                                                                                                        .createWorkspaceScope(true),
                                                                                  "workspace scope"));
        NewSearchUI.runQueryInForeground(null, query);
        return query;
    }


    static JavaSearchQuery runTypeRefQuery(String typeName) throws JavaModelException {
        IType type = getType(typeName);
        JavaSearchQuery query = new JavaSearchQuery(new ElementQuerySpecification(type, IJavaSearchConstants.REFERENCES,
                                                                                  JavaSearchScopeFactory.getInstance()
                                                                                                        .createWorkspaceScope(true),
                                                                                  "workspace scope"));
        NewSearchUI.runQueryInForeground(null, query);
        return query;
    }

    static JavaSearchQuery runMethodRefQuery(String methodName) {
        JavaSearchQuery
                query = new JavaSearchQuery(
                new PatternQuerySpecification(methodName, IJavaSearchConstants.METHOD, true, IJavaSearchConstants.REFERENCES,
                                              JavaSearchScopeFactory.getInstance().createWorkspaceScope(true), "workspace scope"));
        NewSearchUI.runQueryInForeground(null, query);
        return query;
    }

    static IMethod getMethod(String TypeName, String methodName, String[] parameterTypes) throws JavaModelException {
        IType type = getType(TypeName);
        if (type == null)
            return null;
        IMethod method = type.getMethod(methodName, parameterTypes);
        return method;
    }

    static IType getType(String TypeName) throws JavaModelException {
        IJavaProject p = JUnitSourceSetup.getProject();
        IType type = p.findType(TypeName);
        return type;
    }
}
