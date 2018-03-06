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
package org.eclipse.che.ide.ext.git.client;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.MergeResult;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Folder;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Base test for git extension.
 *
 * @author Andrey Plotnikov
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class BaseTest {
  public static final String PROJECT_PATH = "/test";
  public static final String FILE_1_PATH = "/test/a/file_1";
  public static final String FILE_2_PATH = "/test/a/file_2";
  public static final String FOLDER_1_PATH = "/test/a";
  public static final String FOLDER_2_PATH = "/test/a";
  public static final boolean SELECTED_ITEM = true;
  public static final boolean UNSELECTED_ITEM = false;
  public static final boolean ENABLE_BUTTON = true;
  public static final boolean DISABLE_BUTTON = false;
  public static final boolean ENABLE_FIELD = true;
  public static final boolean DISABLE_FIELD = false;
  public static final boolean ACTIVE_BRANCH = true;
  public static final String EMPTY_TEXT = "";
  public static final String PROJECT_NAME = "test";
  public static final String REMOTE_NAME = "codenvy";
  public static final String LOCALE_URI = "http://codenvy.com/git/workspace/test";
  public static final String REMOTE_URI = "git@github.com:codenvy/test.git";
  public static final String REPOSITORY_NAME = "origin";
  public static final String LOCAL_BRANCH = "localBranch";
  public static final String REMOTE_BRANCH = "remoteBranch";
  public static final String WS_ID = "id";
  @Mock protected Project project;
  @Mock protected AppContext appContext;
  @Mock protected GitServiceClient service;
  @Mock protected GitLocalizationConstant constant;
  @Mock protected GitOutputConsole console;
  @Mock protected GitOutputConsoleFactory gitOutputConsoleFactory;
  @Mock protected ProcessesPanelPresenter processesPanelPresenter;
  @Mock protected GitResources resources;
  @Mock protected EventBus eventBus;
  @Mock protected NotificationManager notificationManager;
  @Mock protected DtoFactory dtoFactory;
  @Mock protected DtoUnmarshallerFactory dtoUnmarshallerFactory;
  @Mock protected DialogFactory dialogFactory;
  @Mock protected PromiseError promiseError;
  @Mock protected Throwable throwable;

  @Mock protected File file_1;
  @Mock protected File file_2;
  @Mock protected Folder folder_1;
  @Mock protected Folder folder_2;

  @Mock protected Promise<Status> statusPromise;
  @Captor protected ArgumentCaptor<Operation<Status>> statusPromiseCaptor;
  @Captor protected ArgumentCaptor<Operation<PromiseError>> promiseErrorCaptor;

  @Mock protected Promise<Void> voidPromise;
  @Captor protected ArgumentCaptor<Operation<Void>> voidPromiseCaptor;

  @Mock protected Promise<List<Branch>> branchListPromise;
  @Captor protected ArgumentCaptor<Operation<List<Branch>>> branchListCaptor;

  @Mock protected Promise<PushResponse> pushPromise;
  @Captor protected ArgumentCaptor<Operation<PushResponse>> pushPromiseCaptor;

  @Mock protected Promise<Branch> branchPromise;
  @Captor protected ArgumentCaptor<Operation<Branch>> branchCaptor;

  @Mock protected Promise<Resource[]> synchronizePromise;
  @Captor protected ArgumentCaptor<Operation<Resource[]>> synchronizeCaptor;

  @Mock protected Promise<Revision> revisionPromise;
  @Captor protected ArgumentCaptor<Operation<Revision>> revisionCaptor;

  @Mock protected Promise<List<Remote>> remoteListPromise;
  @Captor protected ArgumentCaptor<Operation<List<Remote>>> remoteListCaptor;

  @Mock protected Promise<MergeResult> mergeResultPromise;
  @Captor protected ArgumentCaptor<Operation<MergeResult>> mergeResultCaptor;

  @Mock protected Promise<String> stringPromise;
  @Captor protected ArgumentCaptor<Operation<String>> stringCaptor;

  @Mock protected Promise<LogResponse> logPromise;
  @Captor protected ArgumentCaptor<Operation<LogResponse>> logCaptor;

  @Mock protected Promise<ShowFileContentResponse> showPromise;
  @Captor protected ArgumentCaptor<Operation<ShowFileContentResponse>> showCaptor;

  @Before
  public void disarm() {
    when(project.getName()).thenReturn(PROJECT_NAME);
    when(project.getLocation()).thenReturn(Path.valueOf(PROJECT_PATH));

    when(gitOutputConsoleFactory.create(anyString())).thenReturn(console);

    when(file_1.getLocation()).thenReturn(Path.valueOf(FILE_1_PATH));
    when(file_2.getLocation()).thenReturn(Path.valueOf(FILE_2_PATH));

    when(folder_1.getLocation()).thenReturn(Path.valueOf(FOLDER_1_PATH));
    when(folder_2.getLocation()).thenReturn(Path.valueOf(FOLDER_2_PATH));

    when(promiseError.getMessage()).thenReturn("error");
    when(promiseError.getCause()).thenReturn(throwable);
    when(throwable.getMessage()).thenReturn("error");
  }
}
