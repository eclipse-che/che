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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.api.project.server.type.AbstractAttribute;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.ProjectTypeConstraintException;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.commons.lang.NameGenerator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.testng.Assert.*;

public class ProjectTypesTest extends BaseProjectTypeTest {

    @Test(expectedExceptions = NotFoundException.class)
    public void testGetMixinsShouldReturnNotFoundException() throws Exception {
        final String notFoundMixin = generate("notFoundMixin-", 5);
        Set<ProjectTypeDef> pts = new HashSet<>();
        pts.add(new PrimaryType());
        pts.add(new PersistedMixin());
        pts.add(new NotPersistedMixin());
        ProjectTypeRegistry reg = new ProjectTypeRegistry(pts);
        new ProjectTypes(generate("projectPath-", 5),
                         PrimaryType.PRIMARY_ID,
                         Arrays.asList(notFoundMixin, PersistedMixin.PERSISTED_MIXIN_ID, NotPersistedMixin.NOT_PERSISTED_MIXIN_ID),
                         reg);
    }

    @Test(expectedExceptions = ProjectTypeConstraintException.class)
    public void testGetMixinsShouldReturnProjectTypeConstraintException() throws Exception {
        String otherPrimaryId = generate("projectType-", 3);
        Set<ProjectTypeDef> pts = new HashSet<>();
        pts.add(new PrimaryType());
        pts.add(new PrimaryType(otherPrimaryId, generate("projectType-", 5)));
        pts.add(new PersistedMixin());
        ProjectTypeRegistry reg = new ProjectTypeRegistry(pts);
        new ProjectTypes(generate("projectPath-", 5),
                         PrimaryType.PRIMARY_ID,
                         Arrays.asList( PersistedMixin.PERSISTED_MIXIN_ID, otherPrimaryId),
                         reg);
    }

    @Test
    public void testGetMixinsShouldNotReturnNotPersistedMixin() throws Exception {
        Set<ProjectTypeDef> pts = new HashSet<>();
        pts.add(new PrimaryType());
        pts.add(new PersistedMixin());
        pts.add(new NotPersistedMixin());
        ProjectTypeRegistry reg = new ProjectTypeRegistry(pts);
        ProjectTypes projectTypes = new ProjectTypes(generate("projectPath-", 5),
                                                     PrimaryType.PRIMARY_ID,
                                                     Arrays.asList(PersistedMixin.PERSISTED_MIXIN_ID,
                                                                   NotPersistedMixin.NOT_PERSISTED_MIXIN_ID),
                                                     reg);

        assertFalse(projectTypes.getMixins().containsKey(NotPersistedMixin.NOT_PERSISTED_MIXIN_ID));
    }

    @Test
    public void testGetMixins() throws Exception {
        Set<ProjectTypeDef> pts = new HashSet<>();
        pts.add(new PrimaryType());
        pts.add(new PersistedMixin());
        ProjectTypeRegistry reg = new ProjectTypeRegistry(pts);
        ProjectTypes projectTypes = new ProjectTypes(generate("projectPath-", 5),
                                                     PrimaryType.PRIMARY_ID,
                                                     Collections.singletonList(PersistedMixin.PERSISTED_MIXIN_ID),
                                                     reg);
        assertNotNull(projectTypes.getMixins());
        assertEquals(projectTypes.getMixins().size(), 1);
        assertTrue(projectTypes.getMixins().containsKey(PersistedMixin.PERSISTED_MIXIN_ID));
    }
}
