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
package org.eclipse.che.ide.resources.impl;

import static java.util.Arrays.copyOf;
import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent.ResourceChangedHandler;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.api.resources.modification.ClipboardManager;
import org.eclipse.che.ide.api.resources.modification.CutResourceMarker;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Serves {@link ClipboardManager}, providing common mechanism for support cut/copy/paste operations
 * based on the input resources. Acts as intermediate level between resources and UI components.
 *
 * <p>Note, that this class is not intended to be used directly by third-party components.
 *
 * @author Vlad Zhukovskiy
 * @see ClipboardManager
 * @since 4.4.0
 */
@Beta
@Singleton
class CopyPasteManager implements ResourceChangedHandler {
  private final PromiseProvider promises;
  private final DialogFactory dialogFactory;
  private final NotificationManager notificationManager;
  private final EventBus eventBus;
  private Resource[] resources;
  private boolean move;

  @Inject
  public CopyPasteManager(
      PromiseProvider promises,
      DialogFactory dialogFactory,
      NotificationManager notificationManager,
      EventBus eventBus) {
    this.promises = promises;
    this.dialogFactory = dialogFactory;
    this.notificationManager = notificationManager;
    this.eventBus = eventBus;

    eventBus.addHandler(ResourceChangedEvent.getType(), this);
  }

  protected Resource[] getResources() {
    return resources;
  }

  protected void setResources(Resource[] resources, boolean move) {
    if (this.resources != null) {
      for (Resource resource : this.resources) {
        resource.deleteMarker(CutResourceMarker.ID);
      }
    }

    this.resources = resources;
    this.move = move;

    if (move) {
      for (Resource resource : resources) {
        resource.addMarker(new CutResourceMarker());
      }
    }
  }

  protected void paste(Path destination) {
    if (resources == null || resources.length == 0) {
      Log.debug(getClass(), "Resources to process was not found");
      return;
    }

    final Resource[] resourcesToProcess = copyOf(resources, resources.length);
    final Path lastCopiedResource =
        destination.append(resourcesToProcess[resourcesToProcess.length - 1].getName());

    pasteSuccessively(promises.resolve(null), resourcesToProcess, 0, destination)
        .then(
            ignored -> {
              eventBus.fireEvent(new RevealResourceEvent(lastCopiedResource));
            });
  }

  private Promise<Void> pasteSuccessively(
      Promise<Void> promise, Resource[] resources, int position, final Path destination) {
    if (position == resources.length) {
      return promise;
    }

    final Resource resource = resources[position];
    final Promise<Void> derivedPromise;

    if (move) {
      derivedPromise =
          promise.thenPromise(
              ignored -> moveResource(resource, destination.append(resource.getName())));
    } else {
      derivedPromise =
          promise.thenPromise(
              ignored -> copyResource(resource, destination.append(resource.getName())));
    }

    return pasteSuccessively(derivedPromise, resources, ++position, destination);
  }

  private Promise<Void> moveResource(final Resource resource, final Path destination) {
    // simple move without overwriting
    return resource
        .move(destination)
        .thenPromise((Function<Resource, Promise<Void>>) ignored -> promises.resolve(null))
        .catchErrorPromise(
            error -> {

              // resource may already exists
              if (error.getMessage().contains("exists")) {

                // create dialog with overwriting option
                return createFromAsyncRequest(
                    (RequestCall<Void>)
                        callback -> {

                          // handle overwrite operation
                          final ConfirmCallback overwrite =
                              () -> {

                                // copy with overwriting
                                resource
                                    .move(destination, true)
                                    .then(
                                        ignored -> {
                                          callback.onSuccess(null);
                                        })
                                    .catchError(
                                        error1 -> {
                                          callback.onFailure(error1.getCause());
                                        });
                              };

                          // skip this resource
                          final ConfirmCallback skip = () -> callback.onSuccess(null);

                          // change destination name
                          final ConfirmCallback rename =
                              () ->
                                  dialogFactory
                                      .createInputDialog(
                                          "Enter new name",
                                          "Enter new name",
                                          value -> {
                                            final Path newPath = destination.parent().append(value);

                                            moveResource(resource, newPath)
                                                .then(callback::onSuccess)
                                                .catchError(
                                                    error1 -> {
                                                      callback.onFailure(error1.getCause());
                                                    });
                                          },
                                          null)
                                      .show();

                          dialogFactory
                              .createChoiceDialog(
                                  "Error",
                                  error.getMessage(),
                                  "Overwrite",
                                  "Skip",
                                  "Change Name",
                                  overwrite,
                                  skip,
                                  rename)
                              .show();
                        });

              } else {
                // notify user about failed copying
                notificationManager.notify(
                    "Error moving resource", error.getMessage(), FAIL, FLOAT_MODE);

                return promises.resolve(null);
              }
            });
  }

  private Promise<Void> copyResource(final Resource resource, final Path destination) {
    // simple copy without overwriting
    return resource
        .copy(destination)
        .thenPromise((Function<Resource, Promise<Void>>) resource1 -> promises.resolve(null))
        .catchErrorPromise(
            error -> {

              // resource may already exists
              if (error.getMessage().contains("exists")) {

                // create dialog with overwriting option
                return createFromAsyncRequest(
                    (RequestCall<Void>)
                        callback -> {

                          // handle overwrite operation
                          final ConfirmCallback overwrite =
                              () -> {

                                // copy with overwriting
                                resource
                                    .copy(destination, true)
                                    .then(
                                        ignored -> {
                                          callback.onSuccess(null);
                                        })
                                    .catchError(
                                        error1 -> {
                                          callback.onFailure(error1.getCause());
                                        });
                              };

                          // skip this resource
                          final ConfirmCallback skip = () -> callback.onSuccess(null);

                          // change destination name
                          final ConfirmCallback rename =
                              () ->
                                  dialogFactory
                                      .createInputDialog(
                                          "Enter new name",
                                          "Enter new name",
                                          value -> {
                                            final Path newPath = destination.parent().append(value);

                                            copyResource(resource, newPath)
                                                .then(callback::onSuccess)
                                                .catchError(
                                                    error1 -> {
                                                      callback.onFailure(error1.getCause());
                                                    });
                                          },
                                          null)
                                      .show();

                          dialogFactory
                              .createChoiceDialog(
                                  "Error",
                                  error.getMessage(),
                                  "Overwrite",
                                  "Skip",
                                  "Change Name",
                                  overwrite,
                                  skip,
                                  rename)
                              .show();
                        });
              } else {
                // notify user about failed copying
                notificationManager.notify(
                    "Error copying resource", error.getMessage(), FAIL, FLOAT_MODE);

                return promises.resolve(null);
              }
            });
  }

  @Override
  public void onResourceChanged(ResourceChangedEvent event) {
    final ResourceDelta delta = event.getDelta();

    // delta should be removed and resources is not null
    if (delta.getKind() != REMOVED || resources == null) {
      return;
    }

    for (int i = 0; i < resources.length; i++) {
      final Resource resource = resources[i];

      if (delta.getResource().getLocation().isPrefixOf(resource.getLocation())) {
        int size = resources.length;
        int numMoved = resources.length - i - 1;
        if (numMoved > 0) {
          System.arraycopy(resources, i + 1, resources, i, numMoved);
        }
        resources = copyOf(resources, --size);
      }
    }
  }
}
