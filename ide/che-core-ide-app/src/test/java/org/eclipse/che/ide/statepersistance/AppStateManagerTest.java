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
package org.eclipse.che.ide.statepersistance;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.inject.Provider;
import elemental.json.Json;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.api.statepersistance.AppStateServiceClient;
import org.eclipse.che.ide.api.statepersistance.StateComponent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

/**
 * Test covers {@link AppStateManager} functionality.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class AppStateManagerTest {

  private static final String COMPONENT_ONE_ID = "component1";
  private static final String COMPONENT_TWO_ID = "component2";

  @Mock private StateComponentRegistry stateComponentRegistry;
  @Mock private Provider<StateComponentRegistry> stateComponentRegistryProvider;
  @Mock private Provider<PerspectiveManager> perspectiveManagerProvider;
  @Mock private StateComponent component1;
  @Mock private StateComponent component2;
  @Mock private Provider<StateComponent> component1Provider;
  @Mock private Provider<StateComponent> component2Provider;
  @Mock private Promise<Void> promise;
  @Mock private Promise<String> getStatePromise;
  @Mock private JsonFactory jsonFactory;
  @Mock private AppStateServiceClient appStateService;
  @Mock private PromiseProvider promiseProvider;

  @Mock private Promise<Void> sequentialRestore;

  @Captor private ArgumentCaptor<Function<Void, Promise<Void>>> sequentialRestoreThenFunction;

  @Captor private ArgumentCaptor<String> saveStateArgumentCaptor;

  private AppStateManager appStateManager;

  @Before
  public void setUp() {
    when(appStateService.loadState()).thenReturn(getStatePromise);
    when(getStatePromise.thenPromise((Function<String, Promise<Void>>) any())).thenReturn(promise);

    List<StateComponent> components = new ArrayList<>();
    components.add(component1);
    components.add(component2);

    when(stateComponentRegistry.getComponents()).thenReturn(components);
    when(stateComponentRegistry.getComponentById(anyString())).thenReturn(Optional.of(component1));
    when(stateComponentRegistryProvider.get()).thenReturn(stateComponentRegistry);

    when(component1Provider.get()).thenReturn(component1);
    when(component2Provider.get()).thenReturn(component2);

    when(component1.getId()).thenReturn(COMPONENT_ONE_ID);
    when(component2.getId()).thenReturn(COMPONENT_TWO_ID);
    when(jsonFactory.parse(anyString())).thenReturn(Json.createObject());
    appStateManager =
        new AppStateManager(
            perspectiveManagerProvider,
            stateComponentRegistryProvider,
            jsonFactory,
            promiseProvider,
            appStateService);
    appStateManager.readState();
  }

  @Test
  public void shouldStoreState() throws Exception {
    appStateManager.persistState();

    verify(appStateService).saveState(saveStateArgumentCaptor.capture());
    assertThat(saveStateArgumentCaptor.getValue()).isNotNull();
  }

  @Test
  public void shouldCallGetStateOnStateComponent() throws Exception {
    appStateManager.persistState();
    verify(component1, atLeastOnce()).getState();
    verify(component2, atLeastOnce()).getState();
  }

  @Test
  public void shouldSaveStateInFile() throws Exception {
    JsonObject firstComponentState = Json.createObject();
    firstComponentState.put("key1", "value1");
    when(component1.getState()).thenReturn(firstComponentState);

    JsonObject secondComponentState = Json.createObject();
    secondComponentState.put("key2", "value2");
    when(component2.getState()).thenReturn(secondComponentState);

    appStateManager.persistState();

    verify(component1).getState();
    verify(component2).getState();
    verify(appStateService).saveState(saveStateArgumentCaptor.capture());

    String json = saveStateArgumentCaptor.getValue();
    assertThat(json).isNotNull();
    JsonObject appState = Json.parse(json);
    assertThat(appState).isNotNull();

    JsonObject jsonObject1 = appState.getObject(COMPONENT_ONE_ID);
    assertThat(jsonObject1.jsEquals(firstComponentState)).isTrue();

    JsonObject jsonObject2 = appState.getObject(COMPONENT_TWO_ID);
    assertThat(jsonObject2.jsEquals(secondComponentState)).isTrue();
  }

  @Test
  public void restoreShouldCallLoadState() throws Exception {
    JsonObject appState = Json.createObject();
    JsonObject firstComponent = Json.createObject();
    appState.put("component1", firstComponent);
    firstComponent.put("key1", "value1");
    when(promiseProvider.resolve(nullable(Void.class))).thenReturn(sequentialRestore);

    appStateManager.restoreState(appState);

    verify(sequentialRestore).thenPromise(sequentialRestoreThenFunction.capture());
    sequentialRestoreThenFunction.getValue().apply(null);

    ArgumentCaptor<JsonObject> stateCaptor = ArgumentCaptor.forClass(JsonObject.class);
    verify(component1).loadState(stateCaptor.capture());

    JsonObject jsonObject = stateCaptor.getValue();
    assertThat(jsonObject.hasKey("key1")).isTrue();
    assertThat(jsonObject.getString("key1")).isEqualTo("value1");
  }
}
