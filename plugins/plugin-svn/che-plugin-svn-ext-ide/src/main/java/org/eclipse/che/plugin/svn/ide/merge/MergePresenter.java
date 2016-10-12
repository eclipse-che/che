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
package org.eclipse.che.plugin.svn.ide.merge;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.data.HasDataObject;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.subversion.Credentials;
import org.eclipse.che.ide.api.subversion.SubversionCredentialsDialog;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;
import org.eclipse.che.plugin.svn.shared.InfoResponse;
import org.eclipse.che.plugin.svn.shared.SubversionItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Manages merging the branches and folders.
 */
@Singleton
public class MergePresenter extends SubversionActionPresenter implements MergeView.ActionDelegate {

    private final MergeView                                view;
    private final SubversionClientService                  service;
    private final NotificationManager                      notificationManager;
    private final SubversionExtensionLocalizationConstants constants;

    /** Subversion target to merge. */
    private SubversionItem mergeTarget;

    /** Source URL to merge with. */
    private String sourceURL;

    /**
     * Creates an instance of this presenter.
     */
    @Inject
    public MergePresenter(MergeView view,
                          SubversionClientService service,
                          AppContext appContext,
                          SubversionOutputConsoleFactory consoleFactory,
                          SubversionCredentialsDialog subversionCredentialsDialog,
                          ProcessesPanelPresenter processesPanelPresenter,
                          NotificationManager notificationManager,
                          SubversionExtensionLocalizationConstants constants,
                          StatusColors statusColors) {
        super(appContext, consoleFactory, processesPanelPresenter, statusColors, constants, notificationManager, subversionCredentialsDialog);

        this.view = view;
        this.service = service;
        this.notificationManager = notificationManager;
        this.constants = constants;

        view.setDelegate(this);
    }

    /**
     * Prepares to merging and opens Merge dialog.
     */
    public void merge() {
        view.enableMergeButton(false);
        view.sourceCheckBox().setValue(false);

        final Project project = appContext.getRootProject();

        checkState(project != null);

        final Resource[] resources = appContext.getResources();

        checkState(resources != null && resources.length == 1);

        performOperationWithCredentialsRequestIfNeeded(new RemoteSubversionOperation<InfoResponse>() {
            @Override
            public Promise<InfoResponse> perform(Credentials credentials) {
                return service.info(project.getLocation(), toRelative(project, resources[0]).toString(), "HEAD", false, credentials);
            }
        }, null).then(new Operation<InfoResponse>() {
            @Override
            public void apply(InfoResponse response) throws OperationException {
                if (response.getErrorOutput() != null && !response.getErrorOutput().isEmpty()) {
                    printErrors(response.getErrorOutput(), constants.commandInfo());
                    notificationManager.notify("Unable to execute subversion command", FAIL, FLOAT_MODE);
                    return;
                }

                mergeTarget = response.getItems().get(0);
                view.targetTextBox().setValue(mergeTarget.getRelativeURL());

                String repositoryRoot = mergeTarget.getRepositoryRoot();

                service.info(project.getLocation(), repositoryRoot, "HEAD", true)
                       .then(new Operation<InfoResponse>() {
                           @Override
                           public void apply(InfoResponse response) throws OperationException {
                               if (!response.getErrorOutput().isEmpty()) {
                                   printErrors(response.getErrorOutput(), constants.commandInfo());
                                   notificationManager.notify("Unable to execute subversion command", FAIL, FLOAT_MODE);
                                   return;
                               }

                               sourceURL = response.getItems().get(0).getURL();
                               SubversionItemNode subversionTreeNode = new SubversionItemNode(response.getItems().get(0));

                               List<Node> children = new ArrayList<>();
                               if (response.getItems().size() > 1) {
                                   for (int i = 1; i < response.getItems().size(); i++) {
                                       SubversionItem item = response.getItems().get(i);
                                       if (!"file".equals(item.getNodeKind())) {
                                           children.add(new SubversionItemNode(item));
                                       }
                                   }
                               }

                               subversionTreeNode.setChildren(children);
                               view.setRootNode(subversionTreeNode);
                               view.show();
                               validateSourceURL();
                           }
                       })
                       .catchError(new Operation<PromiseError>() {
                           @Override
                           public void apply(PromiseError error) throws OperationException {
                               notificationManager.notify(error.getMessage(), FAIL, FLOAT_MODE);
                           }
                       });
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                notificationManager.notify(error.getMessage(), FAIL, FLOAT_MODE);
            }
        });
    }

    /**
     * Performs actions when clicking Merge button.
     */
    @Override
    public void mergeClicked() {
        view.hide();

        final Project project = appContext.getRootProject();

        checkState(project != null);

        final Resource[] resources = appContext.getResources();

        checkState(resources != null && resources.length == 1);

        service.merge(project.getLocation(), resources[0].getLocation(), Path.valueOf(sourceURL))
               .then(new Operation<CLIOutputResponse>() {
                   @Override
                   public void apply(CLIOutputResponse response) throws OperationException {
                       printResponse(response.getCommand(), response.getOutput(), null, constants.commandMerge());
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError error) throws OperationException {
                       notificationManager.notify(error.getMessage(), FAIL, FLOAT_MODE);
                   }
               });
    }

    /**
     * Closes the dialog when clicking Cancel button.
     */
    @Override
    public void cancelClicked() {
        view.hide();
    }

    /** {@inheritDoc} */
    @Override
    public void onSourceCheckBoxClicked() {
        if (view.sourceCheckBox().getValue()) {
            view.sourceURLTextBox().setValue(sourceURL);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onSourceURLChanged(String sourceURL) {
        this.sourceURL = sourceURL;
        validateSourceURL();
    }

    /** {@inheritDoc} */
    @Override
    public void onNodeSelected(Node node) {
        if (!(node instanceof SubversionItemNode)) {
            return;
        }

        SubversionItemNode treeNode = (SubversionItemNode)node;
        sourceURL = treeNode.getData().getURL();
        validateSourceURL();
    }

    /** Validates source URL and disables Merge button if it's unable to merge. */
    private void validateSourceURL() {
        if (sourceURL.startsWith(mergeTarget.getURL())) {
            view.setError("Cannot merge directory itself");
            view.enableMergeButton(false);
            return;
        }

        if (mergeTarget.getURL().startsWith(sourceURL)) {
            view.setError("Cannot merge with parent directory");
            view.enableMergeButton(false);
            return;
        }

        view.setError(null);
        view.enableMergeButton(true);
    }

    class SubversionItemNode extends AbstractTreeNode implements HasDataObject<SubversionItem> {

        private SubversionItem dataItem;

        public SubversionItemNode(SubversionItem dataItem) {
            this.dataItem = dataItem;
        }

        @Override
        protected Promise<List<Node>> getChildrenImpl() {
            final Project project = appContext.getRootProject();
            checkState(project != null);

            return service.info(project.getLocation(), getData().getURL(), "HEAD", true)
                          .then(new Function<InfoResponse, List<Node>>() {
                              @Override
                              public List<Node> apply(InfoResponse response) throws FunctionException {
                                  if (response.getErrorOutput() != null && !response.getErrorOutput().isEmpty()) {
                                      printErrors(response.getErrorOutput(), constants.commandInfo());
                                      notificationManager.notify("Unable to execute subversion command", FAIL, FLOAT_MODE);
                                      return Collections.emptyList();
                                  }

                                  List<Node> children = new ArrayList<>();
                                  if (response.getItems().size() > 1) {
                                      for (int i = 1; i < response.getItems().size(); i++) {
                                          SubversionItem item = response.getItems().get(i);
                                          if (!"file".equals(item.getNodeKind())) {
                                              children.add(new SubversionItemNode(item));
                                          }
                                      }
                                  }

                                  return children;
                              }
                          }).catchError(new Operation<PromiseError>() {
                        @Override
                        public void apply(PromiseError error) throws OperationException {
                            notificationManager.notify(error.getMessage(), FAIL, FLOAT_MODE);
                        }
                    });
        }

        @Override
        public SubversionItem getData() {
            return dataItem;
        }

        @Override
        public void setData(SubversionItem data) {
            dataItem = data;
        }

        @Override
        public String getName() {
            if (getData().getRepositoryRoot().equals(getData().getURL())) {
                return getData().getRepositoryRoot();
            }

            return getData().getPath();
        }

        @Override
        public boolean isLeaf() {
            return "file".equals(getData().getNodeKind());
        }
    }

}
