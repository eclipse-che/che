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

import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class AttributeFilterTest {

    private static final String TYPE = "type";
    private static final String NAME = "name";

    private static final String MIXABLE_TYPE = "mixableType";
    private static final String MIXABLE_NAME = "mixableName";

    private static final String ATTRIBUTE_ID    = "attrId";
    private static final String ATTRIBUTE_VALUE = "value";

    @Mock
    private ProjectManager projectManager;
    @Mock
    private FolderEntry projectFolder;
    @Mock
    private FolderEntry moduleFolder;

    private ProjectConfigDto config;

    private AttributeFilter filter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(projectFolder.getChild(anyString())).thenReturn(moduleFolder);

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(NAME, Arrays.asList(ATTRIBUTE_VALUE));

        config = DtoFactory.newDto(ProjectConfigDto.class)
                           .withMixins(Arrays.asList(MIXABLE_TYPE))
                           .withAttributes(attributes)
                           .withType(TYPE)
                           .withName(NAME);

        filter = new AttributeFilter(projectManager);
    }

    @Test
    public void persistentAttributesShouldBeAddedToProject() throws Exception {
        prepareProjectTypeRegistry();

        filter.addPersistedAttributesToProject(config, projectFolder);

        List<String> values = config.getAttributes().get(ATTRIBUTE_ID);

        assertThat(values.get(0), is(equalTo(ATTRIBUTE_VALUE)));
        assertThat(values.size(), is(equalTo(1)));
        assertThat(config.getMixins().get(0), is(equalTo(MIXABLE_TYPE)));
    }

    private void prepareProjectTypeRegistry() {
        PersistedProjectType projectType = new PersistedProjectType();

        ProjectTypeDef mixableType = new ProjectTypeDef(MIXABLE_TYPE, MIXABLE_NAME, false, true, true) {
        };

        Set<ProjectTypeDef> projectTypes = new HashSet<>(asList(projectType, mixableType));

        ProjectTypeRegistry projectTypeRegistry = new ProjectTypeRegistry(projectTypes);

        when(projectManager.getProjectTypeRegistry()).thenReturn(projectTypeRegistry);
    }

    @Test
    public void runtimeAttributesShouldBeAddedToProject() throws Exception {
        prepareProjectTypeRegistry();

        filter.addRuntimeAttributesToProject(config, projectFolder);

        List<String> values = config.getAttributes().get(NAME);

        assertThat(values.get(0), is(equalTo(ATTRIBUTE_VALUE)));
        assertThat(values.size(), is(equalTo(1)));
        assertThat(config.getMixins().get(0), is(equalTo(MIXABLE_TYPE)));
    }

    private class PersistedProjectType extends ProjectTypeDef {
        PersistedProjectType() {
            super(TYPE, NAME, true, false, true);

            addConstantDefinition(ATTRIBUTE_ID, NAME, ATTRIBUTE_VALUE);
            addVariableDefinition(NAME, ATTRIBUTE_ID, false, new AttributeValue(ATTRIBUTE_VALUE));
        }
    }
}