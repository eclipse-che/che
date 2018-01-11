/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.project.server.type.ProjectTypeResolver;
import org.eclipse.che.api.project.server.type.ProjectTypes;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class ProjectTypesTest {

  private static final String PRIMARY_ID = "primaryId";

  private static final String PRIMARY_NAME = "primaryName";

  private static final String PERSISTED_MIXIN_ID = "persistedMixinId";
  private static final String PERSISTED_MIXIN_NAME = "persistedMixinName";
  private static final String PERSISTED_MIXIN_ATTRIBUTE_NAME = "persistedMixinAttributeName";
  private static final String PERSISTED_MIXIN_ATTRIBUTE_ID =
      PERSISTED_MIXIN_ID + ":" + PERSISTED_MIXIN_ATTRIBUTE_NAME;

  private static final String NOT_PERSISTED_MIXIN_ID = "notPersistedMixinId";
  private static final String NOT_PERSISTED_MIXIN_NAME = "notPersistedMixinName";
  private static final String NOT_PERSISTED_MIXIN_ATTRIBUTE_NAME = "notPersistedMixinAttributeName";
  private static final String NOT_PERSISTED_MIXIN_ATTRIBUTE_ID =
      NOT_PERSISTED_MIXIN_ID + ":" + NOT_PERSISTED_MIXIN_ATTRIBUTE_NAME;

  @Mock private ProjectTypeDef primaryType;
  @Mock private ProjectTypeDef persistedMixin;
  @Mock private ProjectTypeDef notPersistedMixin;
  @Mock private ProjectTypeRegistry registry;
  @Mock private ProjectTypeResolver resolver;

  @BeforeMethod
  public void configurePrimaryType() throws Exception {
    when(primaryType.isPrimaryable()).thenReturn(true);
    when(primaryType.isMixable()).thenReturn(false);
    when(primaryType.isPersisted()).thenReturn(true);
    when(primaryType.getId()).thenReturn(PRIMARY_ID);
    when(primaryType.getDisplayName()).thenReturn(PRIMARY_NAME);
  }

  @BeforeMethod
  public void configurePersistedMixinType() throws Exception {
    when(persistedMixin.isPrimaryable()).thenReturn(false);
    when(persistedMixin.isMixable()).thenReturn(true);
    when(persistedMixin.isPersisted()).thenReturn(true);
    when(persistedMixin.getId()).thenReturn(PERSISTED_MIXIN_ID);
    when(persistedMixin.getDisplayName()).thenReturn(PERSISTED_MIXIN_NAME);
  }

  @BeforeMethod
  public void configureNotPersistedMixinType() throws Exception {
    when(notPersistedMixin.isPrimaryable()).thenReturn(false);
    when(notPersistedMixin.isMixable()).thenReturn(true);
    when(notPersistedMixin.isPersisted()).thenReturn(false);
    when(notPersistedMixin.getId()).thenReturn(NOT_PERSISTED_MIXIN_ID);
    when(notPersistedMixin.getDisplayName()).thenReturn(NOT_PERSISTED_MIXIN_NAME);
  }

  @BeforeMethod
  public void configureProjectTypeRegistry() throws Exception {
    when(registry.getProjectType(PRIMARY_ID)).thenReturn(primaryType);
    when(registry.getProjectType(PERSISTED_MIXIN_ID)).thenReturn(persistedMixin);
    when(registry.getProjectType(NOT_PERSISTED_MIXIN_ID)).thenReturn(notPersistedMixin);
  }

  @Test
  public void testGetMixinsShouldNotReturnNotPersistedMixin() throws Exception {
    List<String> mixinTypes = Arrays.asList(PERSISTED_MIXIN_ID, NOT_PERSISTED_MIXIN_ID);
    ProjectTypes projectTypes = new ProjectTypes(PRIMARY_ID, mixinTypes, registry, resolver);

    assertFalse(projectTypes.getMixins().containsKey(NOT_PERSISTED_MIXIN_ID));
  }

  @Test
  public void testGetMixins() throws Exception {
    List<String> mixinTypes = singletonList(PERSISTED_MIXIN_ID);
    ProjectTypes projectTypes = new ProjectTypes(PRIMARY_ID, mixinTypes, registry, resolver);

    assertNotNull(projectTypes.getMixins());
    assertEquals(projectTypes.getMixins().size(), 1);
    assertTrue(projectTypes.getMixins().containsKey(PERSISTED_MIXIN_ID));
  }
}
