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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;
import org.eclipse.che.plugin.java.server.rest.ClasspathService;
import org.eclipse.che.plugin.testing.classpath.server.TestClasspathProvider;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaModelException;

import com.google.inject.Inject;

/**
 * Maven implementation for the test classpath provider.
 *
 * @author Mirage Abeysekara
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
        List<URL> classUrls;
        try {
            if (updateClasspath) {
                buildClasspath(projectPath);
            }
            classUrls = getProjectClasspath(projectPath);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Failed to build Maven classpath.", e);
        }
        return new URLClassLoader(classUrls.toArray(new URL[classUrls.size()]), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProjectType() {
        return "maven";
    }

    private boolean buildClasspath(String projectPath) throws IOException, InterruptedException {
        final CommandLine commandLineClassPath = new CommandLine("mvn", "clean", "dependency:build-classpath",
                "-Dmdep.outputFile=target/test.classpath.maven");
        Process processBuildClassPath = new ProcessBuilder().redirectErrorStream(true).directory(new File(projectPath))
                .command(commandLineClassPath.toShellCommand()).start();
        ProcessUtil.process(processBuildClassPath, LineConsumer.DEV_NULL, LineConsumer.DEV_NULL);
        processBuildClassPath.waitFor();
        final CommandLine commandLineTestCompile = new CommandLine("mvn", "test-compile");
        Process processTestCompile = new ProcessBuilder().redirectErrorStream(true).directory(new File(projectPath))
                .command(commandLineTestCompile.toShellCommand()).start();
        ProcessUtil.process(processTestCompile, LineConsumer.DEV_NULL, LineConsumer.DEV_NULL);
        return processTestCompile.waitFor() == 0;

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
    
    private List<URL> getProjectClasspath(String projectPath) throws IOException {
    	String relativeProject = projectPath.substring(ResourcesPlugin.getPathToWorkspace().length());
    	try {
    		IContainer root = ResourcesPlugin.getWorkspace().getRoot();
        	return toResolvedClassPath(classpathService.getClasspath(relativeProject).stream())
			.map(dto -> {
				try {
					String dtoPath = dto.getPath();
				    File path;
				    switch(dto.getEntryKind()) {
				    case IClasspathEntry.CPE_LIBRARY:
					    IResource res = root.findMember(new Path(dtoPath));
					    if (res == null) {
					        path = new File(dtoPath);
					        break;
					    }
				    case IClasspathEntry.CPE_SOURCE:
				        path = new File(root.getLocation().toFile(), dtoPath);
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
			.collect(Collectors.toList());
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Collections.emptyList();
/*    	
    	List<URL> classUrls = new ArrayList<>();
        File cpFile = Paths.get(projectPath, "target", "test.classpath.maven").toFile();
        FileReader fileReader = new FileReader(cpFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = bufferedReader.readLine();
        String[] paths = line.split(":");
        for (String path : paths) {
            classUrls.add(new File(path).toURI().toURL());
        }
        bufferedReader.close();
        fileReader.close();
    	
        classUrls.add(Paths.get(projectPath, "target", "classes").toUri().toURL());
        classUrls.add(Paths.get(projectPath, "target", "test-classes").toUri().toURL());
        return classUrls;
*/
    }
}
