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
package org.eclipse.che.ide.resources.impl;

import com.google.common.annotations.Beta;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.dialogs.InputCallback;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent.ResourceChangedHandler;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.api.resources.modification.ClipboardManager;
import org.eclipse.che.ide.api.resources.modification.CutResourceMarker;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;

import static java.util.Arrays.copyOf;
import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;

/**
 * Serves {@link ClipboardManager}, providing common mechanism for support cut/copy/paste operations based on the
 * input resources. Acts as intermediate level between resources and UI components.
 * <p/>
 * Note, that this class is not intended to be used directly by third-party components.
 *
 * @author Vlad Zhukovskiy
 * @see ClipboardManager
 * @since 4.4.0
 */
@Beta
@Singleton
class CopyPasteManager implements ResourceChangedHandler {
    private final PromiseProvider     promises;
    private final DialogFactory       dialogFactory;
    private final NotificationManager notificationManager;
    private final EventBus            eventBus;
    private       Resource[]          resources;
    private       boolean             move;

    @Inject
    public CopyPasteManager(PromiseProvider promises,
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
        final Promise<Void> promise = promises.resolve(null);

        pasteSuccessively(promise, resources, 0, destination).then(new Operation<Void>() {
            @Override
            public void apply(Void ignored) throws OperationException {
                resources = new Resource[0];
            }
        });
    }

    private Promise<Void> pasteSuccessively(Promise<Void> promise, Resource[] resources, int position, final Path destination) {
        if (position == resources.length) {
            return promise;
        }

        final Resource resource = resources[position];
        final Promise<Void> derivedPromise;

        if (move) {
            derivedPromise = promise.thenPromise(new Function<Void, Promise<Void>>() {
                @Override
                public Promise<Void> apply(Void ignored) throws FunctionException {
                    return moveResource(resource, destination.append(resource.getName()));
                }
            });
        } else {
            derivedPromise = promise.thenPromise(new Function<Void, Promise<Void>>() {
                @Override
                public Promise<Void> apply(Void ignored) throws FunctionException {
                    return copyResource(resource, destination.append(resource.getName()));
                }
            });
        }

        return pasteSuccessively(derivedPromise, resources, ++position, destination);
    }

    private Promise<Void> moveResource(final Resource resource, final Path destination) {
        //simple move without overwriting
        return resource.move(destination).thenPromise(new Function<Resource, Promise<Void>>() {
            @Override
            public Promise<Void> apply(Resource resource) throws FunctionException {
                eventBus.fireEvent(new RevealResourceEvent(resource));
                return promises.resolve(null);
            }
        }).catchErrorPromise(new Function<PromiseError, Promise<Void>>() {
            @Override
            public Promise<Void> apply(final PromiseError error) throws FunctionException {

                //resource may already exists
                if (error.getMessage().contains("exists")) {

                    //create dialog with overwriting option
                    return createFromAsyncRequest(new RequestCall<Void>() {
                        @Override
                        public void makeCall(final AsyncCallback<Void> callback) {

                            //handle overwrite operation
                            final ConfirmCallback overwrite = new ConfirmCallback() {
                                @Override
                                public void accepted() {

                                    //copy with overwriting
                                    resource.move(destination, true).then(new Operation<Resource>() {
                                        @Override
                                        public void apply(Resource ignored) throws OperationException {
                                            callback.onSuccess(null);
                                        }
                                    }).catchError(new Operation<PromiseError>() {
                                        @Override
                                        public void apply(PromiseError error) throws OperationException {
                                            callback.onFailure(error.getCause());
                                        }
                                    });
                                }
                            };

                            //skip this resource
                            final ConfirmCallback skip = new ConfirmCallback() {
                                @Override
                                public void accepted() {
                                    callback.onSuccess(null);
                                }
                            };

                            //change destination name
                            final ConfirmCallback rename = new ConfirmCallback() {
                                @Override
                                public void accepted() {
                                    dialogFactory.createInputDialog("Enter new name", "Enter new name",
                                                                    new InputCallback() {
                                                                        @Override
                                                                        public void accepted(String value) {
                                                                            final Path newPath =
                                                                                    destination.parent().append(value);

                                                                            moveResource(resource, newPath).then(new Operation<Void>() {
                                                                                @Override
                                                                                public void apply(Void result) throws OperationException {
                                                                                    callback.onSuccess(result);
                                                                                }
                                                                            }).catchError(new Operation<PromiseError>() {
                                                                                @Override
                                                                                public void apply(PromiseError error)
                                                                                        throws OperationException {
                                                                                    callback.onFailure(error.getCause());
                                                                                }
                                                                            });
                                                                        }
                                                                    },
                                                                    new CancelCallback() {
                                                                        @Override
                                                                        public void cancelled() {

                                                                        }
                                                                    }).show();
                                }
                            };

                            dialogFactory.createChoiceDialog("Error",
                                                             error.getMessage(),
                                                             "Overwrite",
                                                             "Skip",
                                                             "Change Name",
                                                             overwrite,
                                                             skip,
                                                             rename).show();
                        }
                    });

                } else {
                    //notify user about failed copying
                    notificationManager.notify("Error moving resource", error.getMessage(), FAIL, FLOAT_MODE);

                    return promises.resolve(null);
                }

            }
        });
    }

    private Promise<Void> copyResource(final Resource resource, final Path destination) {
        //simple copy without overwriting
        return resource.copy(destination).thenPromise(new Function<Resource, Promise<Void>>() {
            @Override
            public Promise<Void> apply(Resource resource) throws FunctionException {
                eventBus.fireEvent(new RevealResourceEvent(resource));
                return promises.resolve(null);
            }
        }).catchErrorPromise(new Function<PromiseError, Promise<Void>>() {
            @Override
            public Promise<Void> apply(final PromiseError error) throws FunctionException {

                //resource may already exists
                if (error.getMessage().contains("exists")) {

                    //create dialog with overwriting option
                    return createFromAsyncRequest(new RequestCall<Void>() {
                        @Override
                        public void makeCall(final AsyncCallback<Void> callback) {

                            //handle overwrite operation
                            final ConfirmCallback overwrite = new ConfirmCallback() {
                                @Override
                                public void accepted() {

                                    //copy with overwriting
                                    resource.copy(destination, true).then(new Operation<Resource>() {
                                        @Override
                                        public void apply(Resource ignored) throws OperationException {
                                            callback.onSuccess(null);
                                        }
                                    }).catchError(new Operation<PromiseError>() {
                                        @Override
                                        public void apply(PromiseError error) throws OperationException {
                                            callback.onFailure(error.getCause());
                                        }
                                    });
                                }
                            };

                            //skip this resource
                            final ConfirmCallback skip = new ConfirmCallback() {
                                @Override
                                public void accepted() {
                                    callback.onSuccess(null);
                                }
                            };

                            //change destination name
                            final ConfirmCallback rename = new ConfirmCallback() {
                                @Override
                                public void accepted() {
                                    dialogFactory.createInputDialog("Enter new name", "Enter new name",
                                                                    new InputCallback() {
                                                                        @Override
                                                                        public void accepted(String value) {
                                                                            final Path newPath = destination.parent().append(value);

                                                                            copyResource(resource, newPath).then(new Operation<Void>() {
                                                                                @Override
                                                                                public void apply(Void result) throws OperationException {
                                                                                    callback.onSuccess(result);
                                                                                }
                                                                            }).catchError(new Operation<PromiseError>() {
                                                                                @Override
                                                                                public void apply(PromiseError error)
                                                                                        throws OperationException {
                                                                                    callback.onFailure(error.getCause());
                                                                                }
                                                                            });
                                                                        }
                                                                    },
                                                                    new CancelCallback() {
                                                                        @Override
                                                                        public void cancelled() {

                                                                        }
                                                                    }).show();
                                }
                            };

                            dialogFactory.createChoiceDialog("Error",
                                                             error.getMessage(),
                                                             "Overwrite",
                                                             "Skip",
                                                             "Change Name",
                                                             overwrite,
                                                             skip,
                                                             rename).show();
                        }
                    });
                } else {
                    //notify user about failed copying
                    notificationManager.notify("Error copying resource", error.getMessage(), FAIL, FLOAT_MODE);

                    return promises.resolve(null);
                }
            }
        });
    }

    @Override
    public void onResourceChanged(ResourceChangedEvent event) {
        final ResourceDelta delta = event.getDelta();

        //delta should be removed and resources is not null
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
