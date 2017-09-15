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
package org.eclipse.che.ide.ext.java.client.refactoring.service;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.MimeType.TEXT_PLAIN;
import static org.eclipse.che.ide.resource.Path.valueOf;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;
import static org.eclipse.che.ide.util.PathEncoder.encodePath;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeCreationResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeEnabledState;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangePreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateMoveRefactoring;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.LinkedRenameRefactoringApply;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.MoveSettings;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringChange;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringPreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringSession;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameRefactoringSession;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameSettings;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ReorgDestination;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ValidateNewName;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 * @author Vlad Zhukovskyi
 */
@Singleton
final class RefactoringServiceClientImpl implements RefactoringServiceClient {

  private final AsyncRequestFactory asyncRequestFactory;
  private final DtoUnmarshallerFactory unmarshallerFactory;
  private final AppContext appContext;
  private final String pathToService;
  private final MessageLoader loader;

  @Inject
  public RefactoringServiceClientImpl(
      AsyncRequestFactory asyncRequestFactory,
      DtoUnmarshallerFactory unmarshallerFactory,
      AppContext appContext,
      LoaderFactory loaderFactory) {
    this.asyncRequestFactory = asyncRequestFactory;
    this.unmarshallerFactory = unmarshallerFactory;
    this.appContext = appContext;
    this.loader = loaderFactory.newLoader();
    this.pathToService = "/java/refactoring/";
  }

  /** {@inheritDoc} */
  @Override
  public Promise<String> createMoveRefactoring(final CreateMoveRefactoring moveRefactoring) {
    return asyncRequestFactory
        .createPostRequest(
            appContext.getWsAgentServerApiEndpoint() + pathToService + "move/create",
            moveRefactoring)
        .header(ACCEPT, TEXT_PLAIN)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loader)
        .send(new StringUnmarshaller());
  }

  /** {@inheritDoc} */
  @Override
  public Promise<RenameRefactoringSession> createRenameRefactoring(
      final CreateRenameRefactoring settings) {
    final String url = appContext.getWsAgentServerApiEndpoint() + pathToService + "rename/create";
    return asyncRequestFactory
        .createPostRequest(url, settings)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loader)
        .send(unmarshallerFactory.newUnmarshaller(RenameRefactoringSession.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<RefactoringResult> applyLinkedModeRename(
      final LinkedRenameRefactoringApply refactoringApply) {
    final String url =
        appContext.getWsAgentServerApiEndpoint() + pathToService + "rename/linked/apply";
    return asyncRequestFactory
        .createPostRequest(url, refactoringApply)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loader)
        .send(unmarshallerFactory.newUnmarshaller(RefactoringResult.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<RefactoringStatus> setDestination(final ReorgDestination destination) {
    final String url = appContext.getWsAgentServerApiEndpoint() + pathToService + "set/destination";

    return asyncRequestFactory
        .createPostRequest(url, destination)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loader)
        .send(unmarshallerFactory.newUnmarshaller(RefactoringStatus.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<Void> setMoveSettings(final MoveSettings settings) {
    final String url =
        appContext.getWsAgentServerApiEndpoint() + pathToService + "set/move/setting";

    return asyncRequestFactory
        .createPostRequest(url, settings)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loader)
        .send();
  }

  /** {@inheritDoc} */
  @Override
  public Promise<ChangeCreationResult> createChange(final RefactoringSession session) {
    final String url = appContext.getWsAgentServerApiEndpoint() + pathToService + "create/change";

    return asyncRequestFactory
        .createPostRequest(url, session)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loader)
        .send(unmarshallerFactory.newUnmarshaller(ChangeCreationResult.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<RefactoringPreview> getRefactoringPreview(final RefactoringSession session) {
    final String url = appContext.getWsAgentServerApiEndpoint() + pathToService + "get/preview";

    return asyncRequestFactory
        .createPostRequest(url, session)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loader)
        .send(unmarshallerFactory.newUnmarshaller(RefactoringPreview.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<RefactoringResult> applyRefactoring(final RefactoringSession session) {
    final String url = appContext.getWsAgentServerApiEndpoint() + pathToService + "apply";

    return asyncRequestFactory
        .createPostRequest(url, session)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loader)
        .send(unmarshallerFactory.newUnmarshaller(RefactoringResult.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<Void> changeChangeEnabledState(final ChangeEnabledState state) {
    final String url = appContext.getWsAgentServerApiEndpoint() + pathToService + "change/enabled";

    return asyncRequestFactory
        .createPostRequest(url, state)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loader)
        .send();
  }

  /** {@inheritDoc} */
  @Override
  public Promise<ChangePreview> getChangePreview(final RefactoringChange change) {
    final String url = appContext.getWsAgentServerApiEndpoint() + pathToService + "change/preview";

    return asyncRequestFactory
        .createPostRequest(url, change)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loader)
        .send(unmarshallerFactory.newUnmarshaller(ChangePreview.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<RefactoringStatus> validateNewName(final ValidateNewName newName) {
    final String url =
        appContext.getWsAgentServerApiEndpoint() + pathToService + "rename/validate/name";

    return asyncRequestFactory
        .createPostRequest(url, newName)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loader)
        .send(unmarshallerFactory.newUnmarshaller(RefactoringStatus.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<Void> setRenameSettings(final RenameSettings settings) {
    final String url =
        appContext.getWsAgentServerApiEndpoint() + pathToService + "set/rename/settings";

    return asyncRequestFactory
        .createPostRequest(url, settings)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loader)
        .send();
  }

  @Override
  public Promise<Void> reindexProject(String projectPath) {
    final String url =
        appContext.getWsAgentServerApiEndpoint()
            + pathToService
            + "reindex?projectpath="
            + encodePath(valueOf(projectPath));

    return asyncRequestFactory.createGetRequest(url).loader(loader).send();
  }
}
