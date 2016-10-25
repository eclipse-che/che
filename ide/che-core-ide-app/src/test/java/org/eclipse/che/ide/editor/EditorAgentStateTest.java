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
package org.eclipse.che.ide.editor;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

import com.google.common.base.Optional;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.editor.synchronization.EditorContentSynchronizer;
import org.eclipse.che.ide.part.editor.multipart.EditorMultiPartStackPresenter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Evgen Vidolob
 */
@RunWith(MockitoJUnitRunner.class)
public class EditorAgentStateTest {


    @Mock
    private EventBus                            eventBus;
    @Mock
    private FileTypeRegistry                    fileTypeRegistry;
    @Mock
    private EditorRegistry                      editorRegistry;
    @Mock
    private WorkspaceAgent                      workspaceAgent;
    @Mock
    private CoreLocalizationConstant            coreLocalizationConstant;
    @Mock
    private EditorMultiPartStackPresenter       editorMultiPartStack;
    @Mock
    private Provider<EditorContentSynchronizer> editorContentSynchronizerProvider;
    @Mock
    private EditorPartStack                     editorPartStack;
    @Mock
    private EditorPartStack                     splitPartStack;
    @Mock
    private File                                file;
    @Mock
    private FileType                            fileType;
    @Mock
    private EditorProvider                      editorProvider;
    @Mock
    private EditorPartPresenter                 editorPartPresenter;
    @Mock
    private AppContext                          appContext;
    @Mock
    private Container                           wsRoot;
    @Mock
    private Promise<Optional<File>>             filePromise;
    @Mock
    private EditorInput                         editorInput;


    @InjectMocks
    private EditorAgentImpl editorAgent;


    @Before
    public void setUp() throws Exception {
        when(fileTypeRegistry.getFileTypeByFile(Matchers.<VirtualFile>any())).thenReturn(fileType);
        when(editorRegistry.getEditor(fileType)).thenReturn(editorProvider);
        when(editorProvider.getEditor()).thenReturn(editorPartPresenter);
        when(appContext.getWorkspaceRoot()).thenReturn(wsRoot);

    }


    @Test
    @Ignore
    public void shouldRestoreNonSplitState() throws Exception {
        JsonObject state = defaultNonSplitState();
        when(editorRegistry.findEditorProviderById(anyString())).thenReturn(editorProvider);
        when(wsRoot.getFile(anyString())).thenReturn(filePromise);
        when(editorMultiPartStack.createFirstPartStack()).thenReturn(editorPartStack);
        when(filePromise.then(Matchers.<Operation<Optional<File>>>any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                Object callback = arguments[0];
                if (callback instanceof Operation) {
                    ((Operation)callback).apply(Optional.of(file));
                }
                return null;
            }
        });

        editorAgent.loadState(state);

        verify(editorRegistry).findEditorProviderById("cheDefaultEditor");
        verify(editorProvider).getEditor();
        verify(editorMultiPartStack).createFirstPartStack();
        verify(editorPartStack).addPart(editorPartPresenter);

    }


    @Test
    @Ignore
    public void shouldRestoreSplitState() {
        JsonObject state = defaultSplitState();
        when(editorRegistry.findEditorProviderById(anyString())).thenReturn(editorProvider);
        when(wsRoot.getFile(anyString())).thenReturn(filePromise);
        when(editorMultiPartStack.createFirstPartStack()).thenReturn(editorPartStack);
        when(editorMultiPartStack.split(Matchers.<EditorPartStack>anyObject(), Matchers.<Constraints>anyObject(), anyDouble()))
                .thenReturn(splitPartStack);
        when(filePromise.then(Matchers.<Operation<Optional<File>>>any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                Object callback = arguments[0];
                if (callback instanceof Operation) {
                    ((Operation)callback).apply(Optional.of(file));
                }
                return null;
            }
        });

        editorAgent.loadState(state);

        verify(editorMultiPartStack).createFirstPartStack();
        ArgumentCaptor<Constraints> constraints = ArgumentCaptor.forClass(Constraints.class);
        verify(editorMultiPartStack, times(3)).split(Matchers.<EditorPartStack>anyObject(), constraints.capture(), anyDouble());
    }

    private JsonObject defaultSplitState() {


        String json = "{\n" +
                      "  \"FILES\": {\n" +
                      "    \"DIRECTION\": \"VERTICALLY\",\n" +
                      "    \"SPLIT_FIRST\": {\n" +
                      "      \"FILES\": [\n" +
                      "        {\n" +
                      "          \"PATH\": \"/foo/bar.42\",\n" +
                      "          \"EDITOR_PROVIDER\": \"cheDefaultEditor\",\n" +
                      "          \"CURSOR_OFFSET\": 357,\n" +
                      "          \"TOP_VISIBLE_LINE\": 0,\n" +
                      "          \"ACTIVE\": true\n" +
                      "        }\n" +
                      "      ]\n" +
                      "    },\n" +
                      "    \"SPLIT_SECOND\": {\n" +
                      "      \"DIRECTION\": \"HORIZONTALLY\",\n" +
                      "      \"SPLIT_FIRST\": {\n" +
                      "        \"FILES\": [\n" +
                      "          {\n" +
                      "            \"PATH\": \"/foo/bar.42\",\n" +
                      "            \"EDITOR_PROVIDER\": \"cheDefaultEditor\",\n" +
                      "            \"CURSOR_OFFSET\": 0,\n" +
                      "            \"TOP_VISIBLE_LINE\": 0,\n" +
                      "            \"ACTIVE\": true\n" +
                      "          }\n" +
                      "        ]\n" +
                      "      },\n" +
                      "      \"SPLIT_SECOND\": {\n" +
                      "        \"DIRECTION\": \"VERTICALLY\",\n" +
                      "        \"SPLIT_FIRST\": {\n" +
                      "          \"FILES\": [\n" +
                      "            {\n" +
                      "              \"PATH\": \"/foo/bar.42\",\n" +
                      "              \"EDITOR_PROVIDER\": \"cheDefaultEditor\",\n" +
                      "              \"CURSOR_OFFSET\": 0,\n" +
                      "              \"TOP_VISIBLE_LINE\": 0,\n" +
                      "              \"ACTIVE\": true\n" +
                      "            }\n" +
                      "          ]\n" +
                      "        },\n" +
                      "        \"SPLIT_SECOND\": {\n" +
                      "          \"FILES\": [\n" +
                      "            {\n" +
                      "              \"PATH\": \"/foo/bar.42\",\n" +
                      "              \"EDITOR_PROVIDER\": \"cheDefaultEditor\",\n" +
                      "              \"CURSOR_OFFSET\": 0,\n" +
                      "              \"TOP_VISIBLE_LINE\": 0,\n" +
                      "              \"ACTIVE\": true\n" +
                      "            }\n" +
                      "          ]\n" +
                      "        },\n" +
                      "        \"SIZE\": 334\n" +
                      "      },\n" +
                      "      \"SIZE\": 302\n" +
                      "    },\n" +
                      "    \"SIZE\": 673\n" +
                      "  }\n" +
                      "}";

        return Json.parse(json);
    }

    private JsonObject defaultNonSplitState() {
        JsonObject object = Json.createObject();
        JsonArray files = Json.createArray();
        object.put("FILES", files);
        JsonObject file1 = getDefaultFile();

        files.set(0, file1);
        JsonObject state = Json.createObject();
        state.put("FILES", object);
        return state;
    }

    private JsonObject getDefaultFile() {
        JsonObject file1 = Json.createObject();
        file1.put("PATH", "/foo/bar.42");
        file1.put("EDITOR_PROVIDER", "cheDefaultEditor");
        file1.put("CURSOR_OFFSET", 42);
        file1.put("TOP_VISIBLE_LINE", 21);
        return file1;
    }
}
