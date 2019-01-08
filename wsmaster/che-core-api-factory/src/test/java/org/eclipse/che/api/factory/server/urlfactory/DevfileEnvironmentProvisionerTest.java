/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.factory.server.urlfactory;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_TOOL_TYPE;
import static org.eclipse.che.api.factory.server.urlfactory.DevfileEnvironmentProvisioner.DEFAULT_RECIPE_CONTENT_TYPE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Optional;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class DevfileEnvironmentProvisionerTest {

  public static final String LOCAL_FILENAME = "local.yaml";
  public static final String TOOL_NAME = "foo";

  @Mock private URLFetcher urlFetcher;

  @InjectMocks private DevfileEnvironmentProvisioner provisioner;

  @Test(
      expectedExceptions = BadRequestException.class,
      expectedExceptionsMessageRegExp =
          "This kind of URL's does not support '" + KUBERNETES_TOOL_TYPE + "' type tools.")
  public void shouldThrowExceptionWhenRecipeToolIsPresentAndNoURLComposerGiven() throws Exception {
    Tool tool =
        new Tool().withType(KUBERNETES_TOOL_TYPE).withLocal(LOCAL_FILENAME).withName(TOOL_NAME);
    Devfile devfile = new Devfile();
    devfile.setTools(singletonList(tool));

    provisioner.tryProvision(devfile, null);
  }

  @Test(
      expectedExceptions = BadRequestException.class,
      expectedExceptionsMessageRegExp =
          "The local file '"
              + LOCAL_FILENAME
              + "' defined in tool  '"
              + TOOL_NAME
              + "' is unreachable or empty.")
  public void shouldThrowExceptionWhenRecipeContentIsNull() throws Exception {
    Tool tool =
        new Tool().withType(OPENSHIFT_TOOL_TYPE).withLocal(LOCAL_FILENAME).withName(TOOL_NAME);
    Devfile devfile = new Devfile();
    devfile.setTools(singletonList(tool));
    when(urlFetcher.fetch(anyString())).thenReturn(null);

    provisioner.tryProvision(devfile, s -> "http://foo.bar/local.yaml");
  }

  @Test
  public void shouldReturnEnvironmentWithCorrectRecipeTypeAndContentFromK8SList() throws Exception {
    final String content = "apiVersion: v1\n kind: List";
    Tool tool =
        new Tool().withType(KUBERNETES_TOOL_TYPE).withLocal(LOCAL_FILENAME).withName(TOOL_NAME);
    Devfile devfile = new Devfile();
    devfile.setTools(singletonList(tool));
    when(urlFetcher.fetch(anyString())).thenReturn(content);

    Optional<EnvironmentImpl> result =
        provisioner.tryProvision(devfile, s -> "http://foo.bar/local.yaml");

    assertTrue(result.isPresent());
    RecipeImpl recipe = result.get().getRecipe();
    assertNotNull(recipe);
    assertEquals(recipe.getType(), KUBERNETES_TOOL_TYPE);
    assertEquals(recipe.getContentType(), DEFAULT_RECIPE_CONTENT_TYPE);
    assertEquals(recipe.getContent(), content);
  }

  @Test
  public void shouldReturnEnvironmentWithCorrectRecipeTypeAndContentFromOSList() throws Exception {
    final String content = "apiVersion: v1\n kind: List";
    Tool tool =
        new Tool().withType(OPENSHIFT_TOOL_TYPE).withLocal(LOCAL_FILENAME).withName(TOOL_NAME);
    Devfile devfile = new Devfile();
    devfile.setTools(singletonList(tool));
    when(urlFetcher.fetch(anyString())).thenReturn(content);

    Optional<EnvironmentImpl> result =
        provisioner.tryProvision(devfile, s -> "http://foo.bar/local.yaml");

    assertTrue(result.isPresent());
    RecipeImpl recipe = result.get().getRecipe();
    assertNotNull(recipe);
    assertEquals(recipe.getType(), OPENSHIFT_TOOL_TYPE);
    assertEquals(recipe.getContentType(), DEFAULT_RECIPE_CONTENT_TYPE);
    assertEquals(recipe.getContent(), content);
  }
}
