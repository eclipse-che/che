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
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.CodenvyJsonProvider;
import org.eclipse.che.api.project.shared.dto.ProjectTemplateDescriptor;
import org.eclipse.che.api.vfs.server.ContentStream;
import org.eclipse.che.api.vfs.server.ContentStreamWriter;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.tools.DependencySupplierImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.mockito.Mock;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static javax.ws.rs.HttpMethod.GET;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Vitaly Parfonov
 */
public class ProjectTemplateServiceTest {

    @Mock
    private ProjectTemplateDescriptor descriptor1;
    @Mock
    private ProjectTemplateDescriptor descriptor2;

    private ResourceLauncher launcher;

    @BeforeTest
    public void setUp() {
        ProjectTemplateRegistry templateRegistry = new ProjectTemplateRegistry();

        templateRegistry.register(asList("test"), descriptor1);
        templateRegistry.register(asList("test2"), descriptor2);

        DependencySupplierImpl dependencies = new DependencySupplierImpl();
        dependencies.addComponent(ProjectTemplateRegistry.class, templateRegistry);

        ResourceBinder resources = new ResourceBinderImpl();
        ProviderBinder providers = new ApplicationProviderBinder();
        EverrestProcessor processor = new EverrestProcessor(resources, providers, dependencies, new EverrestConfiguration(), null);
        launcher = new ResourceLauncher(processor);

        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return java.util.Collections.<Class<?>>singleton(ProjectTemplateService.class);
            }

            @Override
            public Set<Object> getSingletons() {
                return new HashSet<>(asList(new CodenvyJsonProvider(new HashSet<>(asList(ContentStream.class))),
                                            new ContentStreamWriter(),
                                            new ApiExceptionMapper()));
            }
        });

        ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, ProviderBinder.getInstance()));
    }

    @Test
    public void templateByTagShouldBeReturned() throws Exception {
        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project-template?tag=test",
                                                      "http://localhost:8080/api", null, null, null);
        assertThat(response.getStatus(), is(equalTo(200)));
        //noinspection unchecked
        List<ProjectTemplateDescriptor> result = (List<ProjectTemplateDescriptor>)response.getEntity();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void allTemplatesShouldBeReturned() throws Exception {
        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project-template/all",
                                                      "http://localhost:8080/api", null, null, null);
        assertThat(response.getStatus(), is(equalTo(200)));
        //noinspection unchecked
        List<ProjectTemplateDescriptor> result = (List<ProjectTemplateDescriptor>)response.getEntity();

        assertNotNull(result);
        assertEquals(2, result.size());
    }
}
