/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.environment.compose;

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeRecipe;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeService;
import org.mockito.InjectMocks;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test deserialization field {@link ComposeService#build} in the {@link ComposeEnvironmentFactory}.
 *
 * @author Mario Loriedo
 */
@Listeners(MockitoTestNGListener.class)
public class BuildContextTest {

  @InjectMocks private ComposeEnvironmentFactory factory;

  @Test
  public void shouldParseBuildArgsWhenProvided() throws Exception {
    // given
    String recipeContent =
        "services:\n"
            + " dev-machine:\n"
            + "  build:\n"
            + "   context: .\n"
            + "   args:\n"
            + "    buildno: 1\n"
            + "    password: secret\n";

    Map<String, String> expected =
        new HashMap<String, String>() {
          {
            put("buildno", "1");
            put("password", "secret");
          }
        };

    // when
    ComposeRecipe composeRecipe = factory.doParse(recipeContent);

    // then
    assertEquals(composeRecipe.getServices().get("dev-machine").getBuild().getArgs(), expected);
  }

  @Test
  public void shouldNotParseBuildArgsWhenNotProvided() throws Exception {
    // given
    String recipeContent = "services:\n" + " dev-machine:\n" + "  build:\n" + "   context: .\n";

    // when
    ComposeRecipe composeRecipe = factory.doParse(recipeContent);

    // then
    assertEquals(
        Collections.emptyMap(),
        composeRecipe.getServices().get("dev-machine").getBuild().getArgs());
  }
}
