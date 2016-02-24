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
package org.eclipse.che.api.project.server.type;


import org.eclipse.che.api.project.server.BaseProjectTypeTest;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


public class ProjectTypeUtilsTest extends BaseProjectTypeTest {

    @Test
    public void testEnsure() throws Exception {
        Set<ProjectTypeDef> pts = new HashSet<>();
        pts.add(new PrimaryType());
        pts.add(new PersistedMixin());
        pts.add(new NotPersistedMixin());
        ProjectTypeRegistry reg = new ProjectTypeRegistry(pts);
        Map<String, List<String>> attr = new HashMap<>();
        attr.put(PersistedMixin.PERSISTED_MIXIN_ATTRIBUTE_ID, Arrays.asList(""));
        attr.put(NotPersistedMixin.NOT_PERSISTED_MIXIN_ATTRIBUTE_ID, Arrays.asList(""));
        ProjectConfigDto configDto = DtoFactory.newDto(ProjectConfigDto.class)
                                               .withMixins(Arrays.asList(PersistedMixin.PERSISTED_MIXIN_ID,
                                                                         NotPersistedMixin.NOT_PERSISTED_MIXIN_ID))
                                               .withType(PrimaryType.PRIMARY_ID).withAttributes(attr);
        assertEquals(configDto.getMixins().size(), 2);
        assertEquals(configDto.getAttributes().size(), 2);
        final ProjectConfigDto ensure = ProjectTypeUtils.ensure(configDto, reg);
        assertNotNull(ensure.getMixins());
        assertEquals(ensure.getMixins().size(), 1);
        assertNotNull(ensure.getAttributes());
        assertEquals(ensure.getAttributes().size(), 1);



    }
}