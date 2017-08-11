/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.statepersistance;

import elemental.json.Json;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.component.StateComponent;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.util.HashSet;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test covers {@link AppStateManager} functionality.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class AppStateManagerTest {

    private static final String WS_ID = "ws_id";

    @Mock
    private StateComponent     component1;
    @Mock
    private StateComponent     component2;
    @Mock
    private Promise<Void>      promise;
    @Mock
    private PreferencesManager preferencesManager;
    @Mock
    private JsonFactory        jsonFactory;
    @Mock
    private JsonObject         pref;
    @Mock
    private PromiseProvider    promiseProvider;

    @Mock
    private Promise<Void> sequentialRestore;

    @Captor
    private ArgumentCaptor<Function<Void, Promise<Void>>> sequentialRestoreThenFunction;

    @Captor
    private ArgumentCaptor<String> preferenceArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> jsonArgumentCaptor;

    private AppStateManager appStateManager;

    @Before
    public void setUp() {
        Set<StateComponent> components = new HashSet<>();
        components.add(component1);
        components.add(component2);

        when(component1.getId()).thenReturn("component1");
        when(component2.getId()).thenReturn("component2");
        when(preferencesManager.flushPreferences()).thenReturn(promise);
        when(preferencesManager.getValue(AppStateManager.PREFERENCE_PROPERTY_NAME)).thenReturn("");
        when(jsonFactory.parse(anyString())).thenReturn(pref = Json.createObject());
        appStateManager = new AppStateManager(components, preferencesManager, jsonFactory, promiseProvider);
    }

    @Test
    public void shouldStoreStateInPreferences() throws Exception {
        appStateManager.persistWorkspaceState(WS_ID);
        verify(preferencesManager).flushPreferences();
    }

    @Test
    public void shouldCallGetStateOnStateComponent() throws Exception {
        appStateManager.persistWorkspaceState(WS_ID);
        verify(component1, atLeastOnce()).getState();
        verify(component2, atLeastOnce()).getState();
    }

    @Test
    public void shouldStoreStateByWsId() throws Exception {
        appStateManager.persistWorkspaceState(WS_ID);
        verify(preferencesManager).setValue(preferenceArgumentCaptor.capture(), jsonArgumentCaptor.capture());
        assertThat(preferenceArgumentCaptor.getValue()).isNotNull();
        assertThat(preferenceArgumentCaptor.getValue()).isNotNull();
        JsonObject object = Json.parse(jsonArgumentCaptor.getValue());
        assertThat(object.hasKey(WS_ID)).isTrue();
    }

    @Test
    public void shouldSaveStateInFile() throws Exception {
        JsonObject object = Json.createObject();
        object.put("key1", "value1");
        when(component1.getState()).thenReturn(object);

        appStateManager.persistWorkspaceState(WS_ID);

        verify(component1).getState();
        verify(preferencesManager).setValue(anyString(), jsonArgumentCaptor.capture());
        assertThat(jsonArgumentCaptor.getValue()).isNotNull().isNotEmpty();

        String value = jsonArgumentCaptor.getValue();
        JsonObject jsonObject = Json.parse(value).getObject(WS_ID);
        JsonObject workspace = jsonObject.getObject("workspace");
        assertThat(workspace).isNotNull();

        JsonObject jsonObject1 = workspace.getObject("component1");
        assertThat(jsonObject1.jsEquals(object)).isTrue();

    }

    @Test
    public void restoreShouldReadFromPreferences() throws Exception {
        pref.put(WS_ID, Json.createObject());
        appStateManager.restoreWorkspaceState(WS_ID);

        verify(preferencesManager).getValue(AppStateManager.PREFERENCE_PROPERTY_NAME);
    }

    @Test
    public void restoreShouldCallLoadState() throws Exception {
        JsonObject ws = Json.createObject();
        pref.put(WS_ID, ws);
        JsonObject workspace = Json.createObject();
        ws.put("workspace", workspace);
        JsonObject comp1 = Json.createObject();
        workspace.put("component1", comp1);
        comp1.put("key1", "value1");

        when(promiseProvider.resolve(any(Void.class))).thenReturn(sequentialRestore);

        appStateManager.restoreWorkspaceState(WS_ID);

        verify(sequentialRestore).thenPromise(sequentialRestoreThenFunction.capture());
        sequentialRestoreThenFunction.getValue().apply(null);

        ArgumentCaptor<JsonObject> stateCaptor = ArgumentCaptor.forClass(JsonObject.class);
        verify(component1).loadState(stateCaptor.capture());

        JsonObject jsonObject = stateCaptor.getValue();
        assertThat(jsonObject.hasKey("key1")).isTrue();
        assertThat(jsonObject.getString("key1")).isEqualTo("value1");
    }

}
