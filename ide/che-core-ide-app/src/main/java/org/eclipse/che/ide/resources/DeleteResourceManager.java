/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.resources;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.api.resources.Resource.FOLDER;
import static org.eclipse.che.ide.api.resources.Resource.PROJECT;

import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Folder;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;

/**
 * Manager that performs removing resources. Support confirmation for removing.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class DeleteResourceManager {

  private final CoreLocalizationConstant localization;
  private final DialogFactory dialogFactory;
  private final PromiseProvider promiseProvider;
  private final NotificationManager notificationManager;

  @Inject
  public DeleteResourceManager(
      CoreLocalizationConstant localization,
      DialogFactory dialogFactory,
      PromiseProvider promiseProvider,
      NotificationManager notificationManager) {
    this.localization = localization;
    this.dialogFactory = dialogFactory;
    this.promiseProvider = promiseProvider;
    this.notificationManager = notificationManager;
  }

  /**
   * Deletes the given resources and its descendants in the standard manner from file system. Method
   * doesn't require a confirmation from the user and removes resources silently.
   *
   * @param resources the resources to delete
   * @return the {@link Promise} with void if removal has successfully completed
   * @see #delete(boolean, Resource...)
   */
  public Promise<Void> delete(Resource... resources) {
    return delete(false, resources);
  }

  /**
   * Deletes the given resources and its descendants in the standard manner from file system. Method
   * requires a confirmation from the user before resource will be removed.
   *
   * @param needConfirmation true if confirmation is need
   * @param resources the resources to delete
   * @return the {@link Promise} with void if removal has successfully completed
   * @see #delete(Resource...)
   */
  public Promise<Void> delete(boolean needConfirmation, Resource... resources) {
    checkArgument(resources != null, "Null resource occurred");
    checkArgument(resources.length > 0, "No resources were provided to remove");

    final Resource[] filtered = filterDescendants(resources);

    if (!needConfirmation) {
      Promise<?>[] deleteAll = new Promise<?>[resources.length];
      for (int i = 0; i < resources.length; i++) {
        deleteAll[i] = resources[i].delete();
      }

      return promiseProvider
          .all(deleteAll)
          .then(
              new Function<JsArrayMixed, Void>() {
                @Override
                public Void apply(JsArrayMixed arg) throws FunctionException {
                  return null;
                }
              });
    }

    List<Resource> projectsList = newArrayList();

    for (Resource resource : filtered) {
      if (resource.getResourceType() == PROJECT) {
        projectsList.add(resource);
      }
    }

    Resource[] projects = projectsList.toArray(new Resource[projectsList.size()]);

    if (projectsList.isEmpty()) {
      // if no project were found in nodes list
      return promptUserToDelete(filtered);
    } else if (projects.length < filtered.length) {
      // inform user that we can't delete mixed list of the nodes
      return promiseProvider.reject(
          JsPromiseError.create(localization.mixedProjectDeleteMessage()));
    } else {
      // delete only project nodes
      return promptUserToDelete(projects);
    }
  }

  private Promise<Void> promptUserToDelete(final Resource[] resources) {
    return createFromAsyncRequest(
        new RequestCall<Void>() {
          @Override
          public void makeCall(AsyncCallback<Void> callback) {
            String warningMessage = generateWarningMessage(resources);

            boolean anyDirectories = false;

            String directoryName = null;
            for (Resource resource : resources) {
              if (resource instanceof Folder) {
                anyDirectories = true;
                directoryName = resource.getName();
                break;
              }
            }

            if (anyDirectories) {
              warningMessage +=
                  resources.length == 1
                      ? localization.deleteAllFilesAndSubdirectories(directoryName)
                      : localization.deleteFilesAndSubdirectoriesInTheSelectedDirectory();
            }

            dialogFactory
                .createConfirmDialog(
                    localization.deleteDialogTitle(),
                    warningMessage,
                    onConfirm(resources, callback),
                    onCancel(callback))
                .show();
          }
        });
  }

  private String generateWarningMessage(Resource[] resources) {
    if (resources.length == 1) {
      String name = resources[0].getName();
      String type = getDisplayType(resources[0]);

      return "Delete " + type + " \"" + name + "\"?";
    }

    Map<String, Integer> pluralToSingular = new HashMap<>();
    for (Resource resource : resources) {
      final String type = getDisplayType(resource);

      if (!pluralToSingular.containsKey(type)) {
        pluralToSingular.put(type, 1);
      } else {
        Integer count = pluralToSingular.get(type);
        count++;
        pluralToSingular.put(type, count);
      }
    }

    StringBuilder buffer = new StringBuilder("Delete ");

    Iterator<Map.Entry<String, Integer>> iterator = pluralToSingular.entrySet().iterator();
    if (iterator.hasNext()) {
      Map.Entry<String, Integer> entry = iterator.next();
      buffer.append(entry.getValue()).append(" ").append(entry.getKey());

      if (entry.getValue() > 1) {
        buffer.append("s");
      }

      while (iterator.hasNext()) {
        Map.Entry<String, Integer> e = iterator.next();

        buffer.append(" and ").append(e.getValue()).append(" ").append(e.getKey());

        if (e.getValue() > 1) {
          buffer.append("s");
        }
      }
    }

    buffer.append("?");

    return buffer.toString();
  }

  private String getDisplayType(Resource resource) {
    if (resource.getResourceType() == PROJECT) {
      return "project";
    } else if (resource.getResourceType() == FOLDER) {
      return "folder";
    } else if (resource.getResourceType() == FILE) {
      return "file";
    } else {
      return "resource";
    }
  }

  private Resource[] filterDescendants(Resource[] resources) {
    List<Resource> filteredElements = newArrayList(resources);

    int previousSize;

    do {
      previousSize = filteredElements.size();
      outer:
      for (Resource element : filteredElements) {
        for (Resource element2 : filteredElements) {
          if (element == element2) {
            continue;
          }
          // compare only paths to increase performance, don't operation in this case with parents
          if (element.getLocation().isPrefixOf(element2.getLocation())) {
            filteredElements.remove(element2);
            break outer;
          }
        }
      }
    } while (filteredElements.size() != previousSize);

    return filteredElements.toArray(new Resource[filteredElements.size()]);
  }

  private ConfirmCallback onConfirm(
      final Resource[] resources, final AsyncCallback<Void> callback) {
    return new ConfirmCallback() {
      @Override
      public void accepted() {
        if (resources == null) { // sometimes we may occur NPE here while reading length
          callback.onFailure(new IllegalStateException());
          return;
        }

        Promise<?>[] deleteAll = new Promise<?>[resources.length];
        for (int i = 0; i < resources.length; i++) {
          final Resource resource = resources[i];
          deleteAll[i] =
              resource
                  .delete()
                  .catchError(
                      new Operation<PromiseError>() {
                        @Override
                        public void apply(PromiseError error) throws OperationException {
                          notificationManager.notify(
                              "Failed to delete '" + resource.getName() + "'",
                              error.getMessage(),
                              FAIL,
                              StatusNotification.DisplayMode.FLOAT_MODE);
                        }
                      });
        }

        promiseProvider
            .all(deleteAll)
            .then(
                new Operation<JsArrayMixed>() {
                  @Override
                  public void apply(JsArrayMixed arg) throws OperationException {
                    callback.onSuccess(null);
                  }
                });
      }
    };
  }

  private CancelCallback onCancel(final AsyncCallback<Void> callback) {
    return new CancelCallback() {
      @Override
      public void cancelled() {
        callback.onFailure(new Exception("Cancelled"));
      }
    };
  }
}
