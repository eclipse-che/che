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
package org.eclipse.che.plugin.languageserver.ide.location;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.List;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerResources;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.OpenFileInEditorHelper;
import org.eclipse.lsp4j.Location;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Evgen Vidolob */
public class OpenLocationPresenter extends BasePresenter
    implements OpenLocationView.ActionDelegate {

  private final LanguageServerResources resources;
  private final OpenLocationView view;
  private final WorkspaceAgent workspaceAgent;
  private final OpenFileInEditorHelper helper;
  private final NotificationManager notificationManager;
  private final String title;
  public final TextDocumentServiceClient textDocumentService;

  @Inject
  public OpenLocationPresenter(
      LanguageServerResources resources,
      OpenLocationView view,
      WorkspaceAgent workspaceAgent,
      OpenFileInEditorHelper helper,
      NotificationManager notificationManager,
      TextDocumentServiceClient textDocumentService,
      @Assisted String title) {
    this.resources = resources;
    this.view = view;
    this.workspaceAgent = workspaceAgent;
    this.helper = helper;
    this.notificationManager = notificationManager;
    this.textDocumentService = textDocumentService;
    this.title = title;
    view.setDelegate(this);
    view.setTitle(title);
  }

  // TODO maybe we should use some generic data object not a DTO
  public void openLocation(Promise<List<Location>> promise) {
    promise
        .then(
            new Operation<List<Location>>() {
              @Override
              public void apply(List<Location> arg) throws OperationException {
                showLocations(arg);
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError arg) throws OperationException {
                showError(arg);
              }
            });
  }

  public void showError(PromiseError arg) {
    notificationManager.notify(
        title,
        arg.getMessage(),
        StatusNotification.Status.FAIL,
        StatusNotification.DisplayMode.FLOAT_MODE);
  }

  private void showLocations(List<Location> arg) {
    view.setLocations(arg);
    openPart();
  }

  @Override
  public SVGResource getTitleImage() {
    return resources.fieldItem();
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public IsWidget getView() {
    return view;
  }

  @Override
  public String getTitleToolTip() {
    return title;
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }

  private void openPart() {
    workspaceAgent.openPart(this, PartStackType.INFORMATION);
    workspaceAgent.setActivePart(this);
  }

  @Override
  public void onLocationSelected(Location location) {
    helper.openLocation(location);
  }
}
