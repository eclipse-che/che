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
package org.eclipse.che.ide.factory.json;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;

import com.google.gwt.json.client.JSONException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.factory.model.FactoryImpl;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.factory.utils.FactoryProjectImporter;

/**
 * Imports project from factory.json file
 *
 * @author Sergii Leschenko
 */
public class ImportFromConfigPresenter implements ImportFromConfigView.ActionDelegate {
  private final CoreLocalizationConstant localizationConstant;
  private final ImportFromConfigView view;
  private final NotificationManager notificationManager;
  private final DtoFactory dtoFactory;
  private final FactoryProjectImporter projectImporter;
  private final AsyncCallback<Void> importerCallback;

  private StatusNotification notification;

  @Inject
  public ImportFromConfigPresenter(
      final CoreLocalizationConstant localizationConstant,
      FactoryProjectImporter projectImporter,
      ImportFromConfigView view,
      NotificationManager notificationManager,
      DtoFactory dtoFactory) {
    this.localizationConstant = localizationConstant;
    this.notificationManager = notificationManager;
    this.view = view;
    this.dtoFactory = dtoFactory;
    this.view.setDelegate(this);
    this.projectImporter = projectImporter;

    importerCallback =
        new AsyncCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            notification.setContent(localizationConstant.clonedSource(null));
            notification.setStatus(StatusNotification.Status.SUCCESS);
          }

          @Override
          public void onFailure(Throwable throwable) {
            notification.setContent(throwable.getMessage());
            notification.setStatus(StatusNotification.Status.FAIL);
          }
        };
  }

  /** Show dialog. */
  public void showDialog() {
    view.setEnabledImportButton(false);
    view.showDialog();
  }

  /** {@inheritDoc} */
  @Override
  public void onCancelClicked() {
    view.closeDialog();
  }

  /** {@inheritDoc} */
  @Override
  public void onImportClicked() {
    view.closeDialog();
    FactoryDto factoryDTO;
    try {
      factoryDTO = dtoFactory.createDtoFromJson(view.getFileContent(), FactoryDto.class);
    } catch (JSONException jsonException) {
      notification.setStatus(StatusNotification.Status.FAIL);
      notification.setContent("Error parsing factory object.");
      return;
    }

    notification =
        notificationManager.notify(
            localizationConstant.cloningSource(),
            null,
            StatusNotification.Status.PROGRESS,
            NOT_EMERGE_MODE);
    projectImporter.startImporting(new FactoryImpl(factoryDTO), importerCallback);
  }

  @Override
  public void onErrorReadingFile(String errorMessage) {
    view.setEnabledImportButton(false);
    notification.setStatus(StatusNotification.Status.FAIL);
    notification.setContent(errorMessage);
  }
}
