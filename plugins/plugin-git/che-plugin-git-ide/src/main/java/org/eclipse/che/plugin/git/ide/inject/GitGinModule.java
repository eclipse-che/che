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
package org.eclipse.che.plugin.git.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistrar;
import org.eclipse.che.plugin.git.ide.GitCheckoutStatusNotificationOperation;
import org.eclipse.che.plugin.git.ide.add.AddToIndexView;
import org.eclipse.che.plugin.git.ide.add.AddToIndexViewImpl;
import org.eclipse.che.plugin.git.ide.branch.BranchView;
import org.eclipse.che.plugin.git.ide.branch.BranchViewImpl;
import org.eclipse.che.plugin.git.ide.commit.CommitView;
import org.eclipse.che.plugin.git.ide.commit.CommitViewImpl;
import org.eclipse.che.plugin.git.ide.compare.branchList.BranchListView;
import org.eclipse.che.plugin.git.ide.compare.branchList.BranchListViewImpl;
import org.eclipse.che.plugin.git.ide.compare.changedList.ChangedListView;
import org.eclipse.che.plugin.git.ide.compare.changedList.ChangedListViewImpl;
import org.eclipse.che.plugin.git.ide.compare.revisionsList.RevisionListView;
import org.eclipse.che.plugin.git.ide.compare.revisionsList.RevisionListViewImpl;
import org.eclipse.che.plugin.git.ide.fetch.FetchView;
import org.eclipse.che.plugin.git.ide.fetch.FetchViewImpl;
import org.eclipse.che.plugin.git.ide.history.HistoryView;
import org.eclipse.che.plugin.git.ide.history.HistoryViewImpl;
import org.eclipse.che.plugin.git.ide.importer.GitImportWizardRegistrar;
import org.eclipse.che.plugin.git.ide.merge.MergeView;
import org.eclipse.che.plugin.git.ide.merge.MergeViewImpl;
import org.eclipse.che.plugin.git.ide.outputconsole.GitOutputConsole;
import org.eclipse.che.plugin.git.ide.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.plugin.git.ide.outputconsole.GitOutputConsolePresenter;
import org.eclipse.che.plugin.git.ide.outputconsole.GitOutputPartView;
import org.eclipse.che.plugin.git.ide.outputconsole.GitOutputPartViewImpl;
import org.eclipse.che.plugin.git.ide.preference.CommitterPreferencePresenter;
import org.eclipse.che.plugin.git.ide.pull.PullView;
import org.eclipse.che.plugin.git.ide.pull.PullViewImpl;
import org.eclipse.che.plugin.git.ide.push.PushToRemoteView;
import org.eclipse.che.plugin.git.ide.push.PushToRemoteViewImpl;
import org.eclipse.che.plugin.git.ide.remote.RemoteView;
import org.eclipse.che.plugin.git.ide.remote.RemoteViewImpl;
import org.eclipse.che.plugin.git.ide.remote.add.AddRemoteRepositoryView;
import org.eclipse.che.plugin.git.ide.remote.add.AddRemoteRepositoryViewImpl;
import org.eclipse.che.plugin.git.ide.remove.RemoveFromIndexView;
import org.eclipse.che.plugin.git.ide.remove.RemoveFromIndexViewImpl;
import org.eclipse.che.plugin.git.ide.reset.commit.ResetToCommitView;
import org.eclipse.che.plugin.git.ide.reset.commit.ResetToCommitViewImpl;
import org.eclipse.che.plugin.git.ide.reset.files.ResetFilesView;
import org.eclipse.che.plugin.git.ide.reset.files.ResetFilesViewImpl;

/** @author Andrey Plotnikov */
@ExtensionGinModule
public class GitGinModule extends AbstractGinModule {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        GinMultibinder.newSetBinder(binder(), ImportWizardRegistrar.class).addBinding().to(GitImportWizardRegistrar.class);
        GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class).addBinding().to(CommitterPreferencePresenter.class);

        bind(AddToIndexView.class).to(AddToIndexViewImpl.class).in(Singleton.class);
        bind(ResetToCommitView.class).to(ResetToCommitViewImpl.class).in(Singleton.class);
        bind(RemoveFromIndexView.class).to(RemoveFromIndexViewImpl.class).in(Singleton.class);
        bind(RevisionListView.class).to(RevisionListViewImpl.class).in(Singleton.class);
        bind(CommitView.class).to(CommitViewImpl.class).in(Singleton.class);
        bind(ChangedListView.class).to(ChangedListViewImpl.class).in(Singleton.class);
        bind(BranchView.class).to(BranchViewImpl.class).in(Singleton.class);
        bind(BranchListView.class).to(BranchListViewImpl.class).in(Singleton.class);
        bind(MergeView.class).to(MergeViewImpl.class).in(Singleton.class);
        bind(ResetFilesView.class).to(ResetFilesViewImpl.class).in(Singleton.class);
        bind(RemoteView.class).to(RemoteViewImpl.class).in(Singleton.class);
        bind(AddRemoteRepositoryView.class).to(AddRemoteRepositoryViewImpl.class).in(Singleton.class);
        bind(PushToRemoteView.class).to(PushToRemoteViewImpl.class).in(Singleton.class);
        bind(FetchView.class).to(FetchViewImpl.class).in(Singleton.class);
        bind(PullView.class).to(PullViewImpl.class).in(Singleton.class);
        bind(HistoryView.class).to(HistoryViewImpl.class).in(Singleton.class);
        bind(GitOutputPartView.class).to(GitOutputPartViewImpl.class);

        install(new GinFactoryModuleBuilder().implement(GitOutputConsole.class, GitOutputConsolePresenter.class)
                                             .build(GitOutputConsoleFactory.class));

        bind(GitCheckoutStatusNotificationOperation.class).asEagerSingleton();
    }
}
