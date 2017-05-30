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
package org.eclipse.che.plugin.docker.machine;

import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Check if MachineSource object used for Docker is working as expected.
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class DockerMachineSourceTest {

    @Mock
    private MachineSource machineSource;

    @DataProvider(name = "image-ids")
    public Object[][] messageProvider() {

        return new String[][]{
                {"docker-registry.company.com:5000/my-repository:some-tag", "docker-registry.company.com:5000", "my-repository", "some-tag",
                 null},
                {"my-repository", null, "my-repository", null, null},
                {"my-repository:tag", null, "my-repository", "tag", null},
                {"docker-registry.company.com:5000/my-repository", "docker-registry.company.com:5000", "my-repository", null, null},
                {"docker-registry.company.com:5000/my-repository:mytag@digest123", "docker-registry.company.com:5000", "my-repository",
                 "mytag", "digest123"},
                {"ubuntu@sha256:45b23dee08af5e43a7fea6c4cf9c25ccf269ee113168c19722f87876677c5cb2", null, "ubuntu", null,
                 "sha256:45b23dee08af5e43a7fea6c4cf9c25ccf269ee113168c19722f87876677c5cb2"},
                {"docker-registry:5000/ubuntu@sha256:45b23dee08af5e43a7fea6c4cf9c25ccf269ee113168c19722f87876677c5cb2",
                 "docker-registry:5000", "ubuntu", null, "sha256:45b23dee08af5e43a7fea6c4cf9c25ccf269ee113168c19722f87876677c5cb2"},
                };
    }


    /**
     * Check that all the constructor are valid and not throwing exception based on data provider
     */
    @Test(dataProvider = "image-ids")
    public void testConstructors(String location, String registry, String repository, String tag, String digest) throws MachineException {
        DockerMachineSource source1 = new DockerMachineSource(repository).withTag(tag).withRegistry(registry).withDigest(digest);
        assertEquals(source1.getLocation(), location);

        DockerMachineSource source2 = new DockerMachineSource(source1);
        assertEquals(source2.getLocation(), location);
        assertEquals(source2.getRegistry(), registry);
        assertEquals(source2.getRepository(), repository);
        assertEquals(source2.getTag(), tag);
        assertEquals(source2.getDigest(), digest);


        DockerMachineSource source3 = new DockerMachineSource(repository);
        source3.setTag(tag);
        source3.setRegistry(registry);
        source3.setDigest(digest);
        assertEquals(source3.getLocation(), location);


    }


    /**
     * Check valid source type
     */
    @Test(expectedExceptions = MachineException.class)
    public void testInvalidSourceType() throws MachineException {
        when(machineSource.getType()).thenReturn("invalid");
        new DockerMachineSource(machineSource);
    }

    /**
     * Check invalid format
     */
    @Test(expectedExceptions = MachineException.class)
    public void testInvalidFormat() throws MachineException {
        when(machineSource.getType()).thenReturn("image");
        when(machineSource.getLocation()).thenReturn("@image");
        new DockerMachineSource(machineSource);
    }

}
