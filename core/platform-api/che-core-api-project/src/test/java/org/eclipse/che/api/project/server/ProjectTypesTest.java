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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.testng.Assert.*;

/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p>
 * Contributors:
 * Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
public class ProjectTypesTest {


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


    @Test(expectedExceptions = ProjectTypeConstraintException.class)
    public void testGetMixinsShouldReturnProjectTypeConstraintExceptionInCaseNotPersistedMixin() throws Exception {
        Set<ProjectTypeDef> pts = new HashSet<>();
        pts.add(new PrimaryType());
        pts.add(new PersistedMixin());
        pts.add(new NotPersistedMixin());
        ProjectTypeRegistry reg = new ProjectTypeRegistry(pts);
        new ProjectTypes(generate("projectPath-", 5),
                         PrimaryType.PRIMARY_ID,
                         Arrays.asList(PersistedMixin.PERSISTED_MIXIN_ID, NotPersistedMixin.NOT_PERSISTED_MIXIN_ID),
                         reg);
    }

    @Test
    public void testGetMixins() throws Exception {
        String otherMixinId = generate("projectMixin-", 3);
        Set<ProjectTypeDef> pts = new HashSet<>();
        pts.add(new PrimaryType());
        pts.add(new PersistedMixin());
        pts.add(new PersistedMixin(otherMixinId, generate("projectMixinName-", 3)));
        ProjectTypeRegistry reg = new ProjectTypeRegistry(pts);
        ProjectTypes projectTypes = new ProjectTypes(generate("projectPath-", 5),
                                                     PrimaryType.PRIMARY_ID,
                                                     Arrays.asList( PersistedMixin.PERSISTED_MIXIN_ID, otherMixinId),
                                                     reg);
        assertNotNull(projectTypes.getMixins());
        assertEquals(projectTypes.getMixins().size(), 2);
        assertTrue(projectTypes.getMixins().containsKey(PersistedMixin.PERSISTED_MIXIN_ID));
    }

    @Test
    public void testGetAttributeDefs() throws Exception {

    }

    @Test
    public void testGetPrimary() throws Exception {

    }

    @Test
    public void testGetAll() throws Exception {

    }


    private class PrimaryType extends ProjectTypeDef {

        final static String PRIMARY_ID = "primaryId";

        final static String PRIMARY_NAME = "primaryName";

        protected PrimaryType() {
            this(PRIMARY_ID, PRIMARY_NAME);
        }

        protected PrimaryType(String id, String displayName) {
            super(id, displayName, true, false, true);
        }
    }

    private class PersistedMixin extends ProjectTypeDef {

        final static String PERSISTED_MIXIN_ID = "persistedMixinId";
        final static String PERSISTED_MIXIN_NAME = "persistedMixinName";

        protected PersistedMixin() {
            this(PERSISTED_MIXIN_ID, PERSISTED_MIXIN_NAME);
        }

        protected PersistedMixin(String id, String displayName) {
            super(id, displayName, false, true, true);
        }
    }

    private class NotPersistedMixin extends ProjectTypeDef {

        final static String NOT_PERSISTED_MIXIN_ID = "notPersistedMixinId";
        final static String NOT_PERSISTED_MIXIN_NAME = "notPersistedMixinName";

        protected NotPersistedMixin() {
            this(NOT_PERSISTED_MIXIN_ID, NOT_PERSISTED_MIXIN_NAME);
        }

        protected NotPersistedMixin(String id, String displayName) {
            super(id, displayName, false, true, false);
        }
    }


}