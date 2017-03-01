/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.testing.classpath.maven.server;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;
import org.eclipse.che.plugin.java.server.rest.ClasspathService;
import org.eclipse.che.plugin.testing.classpath.server.TestClasspathProvider;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaModelException;

import com.google.inject.Inject;

/**
 * Maven implementation for the test classpath provider.
 *
 * @author Mirage Abeysekara
 * @author David Festal
 * 
 */
public class MavenTestClasspathProvider implements TestClasspathProvider {
	private ClasspathService classpathService;
	
	@Inject
	public MavenTestClasspathProvider(ClasspathService classpathService) {
		this.classpathService = classpathService;
	}
	
	/**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader getClassLoader(String projectPath, boolean updateClasspath) throws Exception {
        try {
            return new URLClassLoader(getProjectClasspath(projectPath).toArray(URL[]::new), null);
        } catch (JavaModelException e) {
            throw new Exception("Failed to build the classpath for testing project: " + projectPath, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProjectType() {
        return "maven";
    }

    private Stream<ClasspathEntryDto> toResolvedClassPath(Stream<ClasspathEntryDto> rawClasspath) {
    	return rawClasspath.flatMap(dto -> {
    		if (dto.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
    			return toResolvedClassPath(dto.getExpandedEntries().stream());
    		} else {
    			return Stream.of(dto);
    		}
    	});
    }
    
    private Stream<URL> getProjectClasspath(String projectPath) throws JavaModelException {
    	String relativeProject = projectPath.substring(ResourcesPlugin.getPathToWorkspace().length());
		IContainer root = ResourcesPlugin.getWorkspace().getRoot();
    	return toResolvedClassPath(classpathService.getClasspath(relativeProject).stream())
		.map(dto -> {
			try {
				String dtoPath = dto.getPath();
			    IResource res = root.findMember(new Path(dtoPath));
			    File path;
			    switch(dto.getEntryKind()) {
			    case IClasspathEntry.CPE_LIBRARY:
				    if (res == null) {
				        path = new File(dtoPath);
				    } else {
				    	path = new File(root.getLocation().toFile(), dtoPath);
				    }
			        break;
			    case IClasspathEntry.CPE_SOURCE:
			    	IPath projectRelativePath = new Path(dtoPath).removeFirstSegments(1);
			    	String projectRelativePathStr = projectRelativePath.toString();
			    	switch(projectRelativePathStr) {
			    	case "src/main/java":
			    		path = Paths.get(projectPath, "target", "classes").toFile();
			    		break;
			    	case "src/test/java":
			    		path = Paths.get(projectPath, "target", "test-classes").toFile();
			    		break;
			    	default:
				    	path = new File(root.getLocation().toFile(), dtoPath);
			    	}
				    break;
				default:
			        path = new File(dtoPath);
			    }
				return path.toURI().toURL();
			} catch (MalformedURLException e) {
				return null;
			}
		})
		.filter(url -> url != null)
		.distinct();
    }
}
