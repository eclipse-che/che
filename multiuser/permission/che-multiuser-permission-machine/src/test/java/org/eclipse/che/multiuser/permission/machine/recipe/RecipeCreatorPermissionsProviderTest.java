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
package org.eclipse.che.multiuser.permission.machine.recipe;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.recipe.OldRecipeImpl;
import org.eclipse.che.api.recipe.RecipePersistedEvent;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.multiuser.api.permission.server.PermissionsManager;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link RecipeCreatorPermissionsProvider}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class RecipeCreatorPermissionsProviderTest {

  @Mock private EventService eventService;

  @Mock private PermissionsManager permManager;

  @InjectMocks private RecipeCreatorPermissionsProvider permProvider;

  @AfterMethod
  public void resetContext() {
    EnvironmentContext.reset();
  }

  @Test
  public void shouldAddPermissions() throws Exception {
    final EnvironmentContext ctx = new EnvironmentContext();
    ctx.setSubject(new SubjectImpl("test-user-name", "test-user-id", "test-token", false));
    EnvironmentContext.setCurrent(ctx);
    final OldRecipeImpl recipe = createRecipe();

    permProvider.onEvent(new RecipePersistedEvent(recipe));

    final ArgumentCaptor<RecipePermissionsImpl> captor =
        ArgumentCaptor.forClass(RecipePermissionsImpl.class);
    verify(permManager).storePermission(captor.capture());
    final RecipePermissionsImpl perm = captor.getValue();
    assertEquals(perm.getInstanceId(), recipe.getId());
    assertEquals(perm.getUserId(), "test-user-id");
    assertEquals(perm.getDomainId(), RecipeDomain.DOMAIN_ID);
    assertEquals(perm.getActions(), RecipeDomain.getActions());
  }

  @Test
  public void shouldNotAddPermissionsIfThereIsNoUserInEnvironmentContext() throws Exception {
    permProvider.onEvent(new RecipePersistedEvent(createRecipe()));

    verify(permManager, never()).storePermission(any());
  }

  @Test
  public void shouldSubscribe() {
    permProvider.subscribe();

    verify(eventService).subscribe(permProvider, RecipePersistedEvent.class);
  }

  @Test
  public void shouldUnsubscribe() {
    permProvider.unsubscribe();

    verify(eventService).unsubscribe(permProvider, RecipePersistedEvent.class);
  }

  private static OldRecipeImpl createRecipe() {
    return new OldRecipeImpl(
        "test", "DEBIAN_JDK8", "test", "test", "script", asList("debian", "test"), "description");
  }
}
