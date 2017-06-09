/*******************************************************************************
 * Copyright (c) 2012-2017 RedHat, Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   RedHat, Inc - initial test implementation
 *******************************************************************************/
package org.eclipse.che.plugin.testing.classpath.maven.server;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;
import org.eclipse.che.plugin.java.server.rest.ClasspathServiceInterface;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for the Maven implementation for the test classpath provider.
 *
 * @author David Festal
 */
public class MavenTestClasspathProviderTest {
    @Mock
    private ClasspathServiceInterface  classpathService;
    @Mock
    private IWorkspaceRoot             workspaceRoot;

    private MavenTestClasspathProvider classpathProvider;

    private static DtoFactory          dtoFactory = DtoFactory.getInstance();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        classpathProvider = new MavenTestClasspathProvider(classpathService);
    }

    public static class ClasspathEntry {
        String               fullPath;
        boolean              external;
        int                  kind;
        List<ClasspathEntry> resolvedEntries;
        String               fileSystemPath;

        public ClasspathEntryDto dto() {
            return dtoFactory.createDto(ClasspathEntryDto.class)
                             .withEntryKind(kind)
                             .withPath(fullPath)
                             .withExpandedEntries(resolvedEntries.stream().map(ClasspathEntry::dto).collect(Collectors.toList()));
        }
    }

    private ClasspathEntry externalLib(String fullPath) {
        ClasspathEntry cp = new ClasspathEntry();
        cp.external = true;
        cp.fullPath = fullPath;
        cp.fileSystemPath = fullPath;
        cp.kind = IClasspathEntry.CPE_LIBRARY;
        cp.resolvedEntries = Collections.emptyList();
        return cp;
    }

    private ClasspathEntry internalLib(String fullPath, String fileSystemPath) {
        ClasspathEntry cp = new ClasspathEntry();
        cp.external = false;
        cp.fullPath = fullPath;
        cp.fileSystemPath = fileSystemPath;
        cp.kind = IClasspathEntry.CPE_LIBRARY;
        cp.resolvedEntries = Collections.emptyList();
        return cp;
    }

    private ClasspathEntry source(String fullPath) {
        ClasspathEntry cp = new ClasspathEntry();
        cp.external = false;
        cp.fullPath = fullPath;
        cp.fileSystemPath = null;
        cp.kind = IClasspathEntry.CPE_SOURCE;
        cp.resolvedEntries = Collections.emptyList();
        return cp;
    }

    private ClasspathEntry container(String containerPath, List<ClasspathEntry> resolvedEntries) {
        ClasspathEntry cp = new ClasspathEntry();
        cp.external = false;
        cp.fullPath = null;
        cp.fileSystemPath = null;
        cp.kind = IClasspathEntry.CPE_CONTAINER;
        cp.resolvedEntries = resolvedEntries;
        return cp;
    }

    private void buildMocks(List<ClasspathEntry> entries) throws JavaModelException {
        when(classpathService.getClasspath(anyString()))
                                                        .thenReturn(entries.stream().map(ClasspathEntry::dto).collect(Collectors.toList()));

        for (ClasspathEntry entry : entries) {
            if (!entry.external && entry.kind == IClasspathEntry.CPE_LIBRARY) {
                IPath resourceLocation = new Path(entry.fileSystemPath);
                IResource result = mock(IResource.class);
                when(result.getLocation())
                                          .thenReturn(resourceLocation);

                when(workspaceRoot.findMember(new Path(entry.fullPath)))
                                                                        .thenReturn(result);
            }
        }
    }

    @Test
    public void testTypicalMavenProjectClasspath() throws JavaModelException, MalformedURLException {
        List<ClasspathEntry> entries =
                                     asList(
                                            externalLib("/home/user/.m2/repository/com/google/guava/guava/20.0/guava-20.0.jar"),
                                            internalLib("exampleProject/lib/internal.jar", "/some/fileSystemPath/internal.jar"),
                                            container("org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER",
                                                      asList(externalLib("/home/user/.m2/repository/com/google/collections/google-collections/1.0/google-collections-1.0.jar"),
                                                             externalLib("/home/user/.m2/repository/com/google/gwt/gwt-servlet/2.8.0/gwt-servlet-2.8.0.jar"))),
                                            source("exampleProject/src/main/java"),
                                            source("exampleProject/src/test/java"));
        buildMocks(entries);
        URL[] classpath = classpathProvider.getProjectClasspath("/projects/exampleProject", "exampleProject", workspaceRoot);
        assertArrayEquals(new URL[]{
                                    new URL("file:/home/user/.m2/repository/com/google/guava/guava/20.0/guava-20.0.jar"),
                                    new URL("file:/some/fileSystemPath/internal.jar"),
                                    new URL("file:/home/user/.m2/repository/com/google/collections/google-collections/1.0/google-collections-1.0.jar"),
                                    new URL("file:/home/user/.m2/repository/com/google/gwt/gwt-servlet/2.8.0/gwt-servlet-2.8.0.jar"),
                                    new URL("file:/projects/exampleProject/target/classes"),
                                    new URL("file:/projects/exampleProject/target/test-classes")
        }, classpath);
    }
}
