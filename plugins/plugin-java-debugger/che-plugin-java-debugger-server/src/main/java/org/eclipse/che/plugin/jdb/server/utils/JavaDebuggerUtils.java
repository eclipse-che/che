/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.jdb.server.utils;

import com.sun.istack.internal.NotNull;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.jdt.core.search.SearchEngine.createWorkspaceScope;

/**
 * Class uses for find and handle important information from the Java Model.
 *
 * @author Alexander Andrienko
 */
public class JavaDebuggerUtils {

    /**
     * Returns Location for current debugger resource.
     *
     * @param location
     *         location type from JVM
     * @throws DebuggerException
     *         in case {@link org.eclipse.jdt.core.JavaModelException} or if Java {@link org.eclipse.jdt.core.IType}
     *         was not find
     */
    public Location getLocation(com.sun.jdi.Location location) throws DebuggerException {
        String fqn = location.declaringType().name();

        List<IType> types;
        try {
            Pair<char[][], char[][]> fqnPair = prepareFqnToSearch(fqn);

            types = findTypeByFqn(fqnPair.first, fqnPair.second, createWorkspaceScope());
        } catch (JavaModelException e) {
            throw new DebuggerException("Can't find class models by fqn: " + fqn, e);
        }

        if (types.isEmpty()) {
            throw new DebuggerException("Type with fully qualified name: " + fqn + " was not found");
        }

        IType type = types.get(0);//TODO we need handle few result! It's temporary solution.
        String typeProjectPath = type.getJavaProject().getPath().toOSString();
        if (type.isBinary()) {
            IClassFile classFile = type.getClassFile();
            int libId = classFile.getAncestor(IPackageFragmentRoot.PACKAGE_FRAGMENT_ROOT).hashCode();
            return new LocationImpl(fqn, location.lineNumber(), null, true, libId, typeProjectPath);
        } else {
            ICompilationUnit compilationUnit = type.getCompilationUnit();
            typeProjectPath = type.getJavaProject().getPath().toOSString();
            String resourcePath = compilationUnit.getPath().toOSString();
            return new LocationImpl(fqn, location.lineNumber(), resourcePath, false, -1, typeProjectPath);
        }
    }

    private Pair<char[][], char[][]> prepareFqnToSearch(@NotNull String fqn) {
        String outerClassFqn = extractOuterClassFqn(fqn);
        int lastDotIndex = outerClassFqn.trim().lastIndexOf('.');

        char[][] packages;
        char[][] names;
        if (lastDotIndex == -1) {
            packages = new char[0][];
            names = new char[][]{outerClassFqn.toCharArray()};
        } else {
            String packageLine = fqn.substring(0, lastDotIndex);
            packages = new char[][]{packageLine.toCharArray()};

            String nameLine = fqn.substring(lastDotIndex + 1, outerClassFqn.length());
            names = new char[][]{nameLine.toCharArray()};
        }
        return new Pair<>(packages, names);
    }

    private String extractOuterClassFqn(String fqn) {
        //handle fqn in case nested classes
        if (fqn.contains("$")) {
            return fqn.substring(0, fqn.indexOf("$"));
        }
        //handle fqn in case lambda expressions
        if (fqn.contains("$$")) {
            return fqn.substring(0, fqn.indexOf("$$"));
        }
        return fqn;
    }

    private List<IType> findTypeByFqn(char[][] packages, char[][] names, IJavaSearchScope scope) throws JavaModelException {
        List<IType> result = new ArrayList<>();

        SearchEngine searchEngine = new SearchEngine();

        searchEngine.searchAllTypeNames(packages,
                                        names,
                                        scope,
                                        new TypeNameMatchRequestor() {
                                            @Override
                                            public void acceptTypeNameMatch(TypeNameMatch typeNameMatch) {
                                                result.add(typeNameMatch.getType());
                                            }
                                        },
                                        IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
                                        new NullProgressMonitor());
        return result;
    }
}
