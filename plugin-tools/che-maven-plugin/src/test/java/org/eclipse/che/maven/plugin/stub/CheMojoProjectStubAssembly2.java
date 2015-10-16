/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.maven.plugin.stub;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.mockito.Mockito;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.doReturn;

/**
 * Stub for sample assembly1
 * @author Florent Benoit
 */
public class CheMojoProjectStubAssembly2 extends CheMojoProjectStub {

    /** {@inheritDoc} */
    public File getBasedir() {
        return new File(super.getBasedir() + "/src/test/projects/assembly2");
    }


    /**
     * Add links to the dependency artifacts
     * @return
     */
    @Override
    public Set<Artifact> getDependencyArtifacts() {


        List<Dependency> dependencyList = getModel().getDependencies();
        Set<Artifact> set = dependencyList.stream()
                                          .map(dependency -> {
                                              Artifact artifact = Mockito.mock(Artifact.class);

                                              File superDir = super.getBasedir();
                                              File dependencyFile = new File(superDir, "target" + File.separator + "test-dependencies" +
                                                                                       File.separator + "junit.jar");

                                              // add junit jar
                                              doReturn(dependencyFile).when(artifact).getFile();

                                              return artifact;
                                          })
                                          .collect(Collectors.toSet());

        return set;

    }
}

