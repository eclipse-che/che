/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.statepersistance;

import elemental.json.Json;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.component.StateComponent;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test covers {@link AppStateManager} functionality.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class AppStateManagerTest {

    private static final String WS_ID = "ws_id";

    @Mock
    private StateComponent component1;
    @Mock
    private StateComponent component2;
    @Mock
    private Promise<Void> promise;
    @Mock
    private Promise<String> contentPromise;
    @Mock
    private PreferencesManager preferencesManager;
    @Mock
    private JsonFactory jsonFactory;
    @Mock
    private JsonObject pref;

    @Captor
    private ArgumentCaptor<String> preferenceArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> jsonArgumentCaptor;

    private AppStateManager appStateManager;

    @Before
    public void setUp() {
        Map<String, StateComponent> components = new HashMap<>();
        components.put("component1", component1);
        components.put("component2", component2);

        when(preferencesManager.flushPreferences()).thenReturn(promise);
        when(preferencesManager.getValue(AppStateManager.PREFERENCE_PROPERTY_NAME)).thenReturn("");
        when(jsonFactory.parse(anyString())).thenReturn(pref = Json.createObject());
        appStateManager = new AppStateManager(components, preferencesManager, jsonFactory);
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

        appStateManager.restoreWorkspaceState(WS_ID);

        ArgumentCaptor<JsonObject> stateCaptor = ArgumentCaptor.forClass(JsonObject.class);
        verify(component1).loadState(stateCaptor.capture());

        JsonObject jsonObject = stateCaptor.getValue();
        assertThat(jsonObject.hasKey("key1")).isTrue();
        assertThat(jsonObject.getString("key1")).isEqualTo("value1");
    }
}
