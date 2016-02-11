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

import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.project.shared.dto.ProjectTemplateDescriptor;
import org.eclipse.che.dto.server.DtoFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URL;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

/**
 * @author Vitaly Parfonov
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectTemplateDescriptionLoaderTest {

    @Mock
    private ProjectTemplateRegistry templateRegistry;

    @Captor
    private ArgumentCaptor<List<String>>                    tagsCaptor;
    @Captor
    private ArgumentCaptor<ProjectTemplateDescriptor> templateCaptor;

    @Before
    public void setUp() {
        URL resource = getClass().getClassLoader().getResource("che-templates");

        //noinspection ConstantConditions
        new ProjectTemplateDescriptionLoader(resource.getPath(), "location", templateRegistry);
    }

    @Test
    public void templatesShouldBeRegistered() {
        verify(templateRegistry).register(tagsCaptor.capture(), templateCaptor.capture());

        List<String> tags = tagsCaptor.getValue();

        assertThat(tags.get(0), is(equalTo("test")));
        assertThat(tags.size(), is(equalTo(1)));

        ProjectTemplateDescriptor descriptor = templateCaptor.getValue();

        assertThat(descriptor.getDisplayName(), is(equalTo("Test")));
        assertThat(descriptor.getProjectType(), is(equalTo("test-type")));
        assertThat(descriptor.getCategory(), is(equalTo("Samples")));
    }

    @Test
    public void templatesShouldNotBeRegisteredWhenPathToTemplateJsonIsIncorrect() {
        reset(templateRegistry);
        new ProjectTemplateDescriptionLoader("incorrect path", "location", templateRegistry);

        verify(templateRegistry, never()).register(tagsCaptor.capture(), templateCaptor.capture());
    }

    @Test
    public void locationShouldBeReplacedWhenRegisterTemplates() throws Exception {
        URL resource = getClass().getClassLoader().getResource("che-templates/embed_type.json");

        //noinspection ConstantConditions
        final List<ProjectTemplateDescriptor> templates = DtoFactory.getInstance().createListDtoFromJson(resource.openStream(),
                                                                                                         ProjectTemplateDescriptor.class);
        ProjectTemplateDescriptor descriptor = templates.get(0);

        assertThat(descriptor.getSource().getLocation(), is(equalTo("${project.template_location_dir}")));

        //noinspection ConstantConditions
        new ProjectTemplateDescriptionLoader(resource.getPath(), "location", templateRegistry);

        verify(templateRegistry).register(tagsCaptor.capture(), templateCaptor.capture());

        ProjectTemplateDescriptor descriptorWithNewLocation = templateCaptor.getValue();

        assertThat(descriptorWithNewLocation.getSource().getLocation(), is(equalTo("location")));

        assertEquals(1, descriptor.getCommands().size());
        CommandDto commandDto = descriptor.getCommands().get(0);
        assertThat(commandDto.getCommandLine(), is(equalTo("echo \"hello\"")));
        assertThat(commandDto.getType(), is(equalTo("custom")));
        assertThat(commandDto.getName(), is(equalTo("customCommand")));

    }
}
