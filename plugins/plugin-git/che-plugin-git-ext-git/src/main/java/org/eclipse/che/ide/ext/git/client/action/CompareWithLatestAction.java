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
package org.eclipse.che.ide.ext.git.client.action;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;
import org.eclipse.che.ide.ext.git.client.compare.changedList.ChangedListPresenter;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.git.shared.DiffType.NAME_STATUS;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.git.client.compare.FileStatus.defineStatus;

/**
 * Action for comparing with latest repository version
 *
 * @author Igor Vinokur
 * @author Vlad Zhukovskyi
 */
@Singleton
public class CompareWithLatestAction extends GitAction {
    private final ComparePresenter        comparePresenter;
    private final ChangedListPresenter    changedListPresenter;
    private final DialogFactory           dialogFactory;
    private final NotificationManager     notificationManager;
    private final GitServiceClient        service;
    private final GitLocalizationConstant locale;

    private final static String REVISION = "HEAD";

    @Inject
    public CompareWithLatestAction(ComparePresenter presenter,
                                   ChangedListPresenter changedListPresenter,
                                   AppContext appContext,
                                   DialogFactory dialogFactory,
                                   NotificationManager notificationManager,
                                   GitServiceClient service,
                                   GitLocalizationConstant constant) {
        super(constant.compareWithLatestTitle(), constant.compareWithLatestTitle(), null, appContext);
        this.comparePresenter = presenter;
        this.changedListPresenter = changedListPresenter;
        this.dialogFactory = dialogFactory;
        this.notificationManager = notificationManager;
        this.service = service;
        this.locale = constant;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {

        final Project project = appContext.getRootProject();
        final Resource resource = appContext.getResource();

        checkState(project != null, "Null project occurred");
        checkState(project.getLocation().isPrefixOf(resource.getLocation()), "Given selected item is not descendant of given project");

        final String selectedItemPath = resource.getLocation()
                                                .removeFirstSegments(project.getLocation().segmentCount())
                                                .removeTrailingSeparator()
                                                .toString();

        service.diff(appContext.getDevMachine(),
                     project.getLocation(),
                     selectedItemPath.isEmpty() ? null : singletonList(selectedItemPath),
                     NAME_STATUS, false, 0, REVISION, false)
               .then(new Operation<String>() {
                   @Override
                   public void apply(String diff) throws OperationException {
                       if (diff.isEmpty()) {
                           dialogFactory.createMessageDialog(locale.compareMessageIdenticalContentTitle(),
                                                             locale.compareMessageIdenticalContentText(), null).show();
                       } else {
                           final String[] changedFiles = diff.split("\n");
                           if (changedFiles.length == 1) {
                               project.getFile(changedFiles[0].substring(2)).then(new Operation<Optional<File>>() {
                                   @Override
                                   public void apply(Optional<File> file) throws OperationException {
                                       if (file.isPresent()) {
                                           comparePresenter.show(file.get(), defineStatus(changedFiles[0].substring(0, 1)), REVISION);
                                       }
                                   }
                               });
                           } else {
                               Map<String, Status> items = new HashMap<>();
                               for (String item : changedFiles) {
                                   items.put(item.substring(2, item.length()), defineStatus(item.substring(0, 1)));
                               }
                               changedListPresenter.show(items, REVISION, project);
                           }
                       }
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError arg) throws OperationException {
                       notificationManager.notify(locale.diffFailed(), FAIL, NOT_EMERGE_MODE);
                   }
               });
    }
}
