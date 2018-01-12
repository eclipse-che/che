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
package org.eclipse.che.ide.ext.git.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistrar;
import org.eclipse.che.ide.api.vcs.VcsChangeMarkerRenderFactory;
import org.eclipse.che.ide.ext.git.client.GitChangeMarkerRendererFactory;
import org.eclipse.che.ide.ext.git.client.GitEventSubscribable;
import org.eclipse.che.ide.ext.git.client.GitEventsHandler;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.ext.git.client.GitServiceClientImpl;
import org.eclipse.che.ide.ext.git.client.add.AddToIndexView;
import org.eclipse.che.ide.ext.git.client.add.AddToIndexViewImpl;
import org.eclipse.che.ide.ext.git.client.branch.BranchView;
import org.eclipse.che.ide.ext.git.client.branch.BranchViewImpl;
import org.eclipse.che.ide.ext.git.client.commit.CommitView;
import org.eclipse.che.ide.ext.git.client.commit.CommitViewImpl;
import org.eclipse.che.ide.ext.git.client.compare.branchlist.BranchListView;
import org.eclipse.che.ide.ext.git.client.compare.branchlist.BranchListViewImpl;
import org.eclipse.che.ide.ext.git.client.compare.changeslist.ChangesListView;
import org.eclipse.che.ide.ext.git.client.compare.changeslist.ChangesListViewImpl;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelView;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelViewImpl;
import org.eclipse.che.ide.ext.git.client.compare.revisionslist.RevisionListView;
import org.eclipse.che.ide.ext.git.client.compare.revisionslist.RevisionListViewImpl;
import org.eclipse.che.ide.ext.git.client.compare.selectablechangespanel.SelectableChangesPanelView;
import org.eclipse.che.ide.ext.git.client.compare.selectablechangespanel.SelectableChangesPanelViewImpl;
import org.eclipse.che.ide.ext.git.client.fetch.FetchView;
import org.eclipse.che.ide.ext.git.client.fetch.FetchViewImpl;
import org.eclipse.che.ide.ext.git.client.history.HistoryView;
import org.eclipse.che.ide.ext.git.client.history.HistoryViewImpl;
import org.eclipse.che.ide.ext.git.client.importer.GitImportWizardRegistrar;
import org.eclipse.che.ide.ext.git.client.merge.MergeView;
import org.eclipse.che.ide.ext.git.client.merge.MergeViewImpl;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsolePresenter;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputPartView;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputPartViewImpl;
import org.eclipse.che.ide.ext.git.client.panel.GitPanelView;
import org.eclipse.che.ide.ext.git.client.panel.GitPanelViewImpl;
import org.eclipse.che.ide.ext.git.client.panel.RepositoryNodeFactory;
import org.eclipse.che.ide.ext.git.client.plugins.EditorTabsColorizer;
import org.eclipse.che.ide.ext.git.client.plugins.GitChangeMarkerManager;
import org.eclipse.che.ide.ext.git.client.plugins.GitCheckoutNotifier;
import org.eclipse.che.ide.ext.git.client.plugins.ProjectExplorerTreeColorizer;
import org.eclipse.che.ide.ext.git.client.preference.CommitterPreferencePresenter;
import org.eclipse.che.ide.ext.git.client.pull.PullView;
import org.eclipse.che.ide.ext.git.client.pull.PullViewImpl;
import org.eclipse.che.ide.ext.git.client.push.PushToRemoteView;
import org.eclipse.che.ide.ext.git.client.push.PushToRemoteViewImpl;
import org.eclipse.che.ide.ext.git.client.remote.RemoteView;
import org.eclipse.che.ide.ext.git.client.remote.RemoteViewImpl;
import org.eclipse.che.ide.ext.git.client.remote.add.AddRemoteRepositoryView;
import org.eclipse.che.ide.ext.git.client.remote.add.AddRemoteRepositoryViewImpl;
import org.eclipse.che.ide.ext.git.client.remove.RemoveFromIndexView;
import org.eclipse.che.ide.ext.git.client.remove.RemoveFromIndexViewImpl;
import org.eclipse.che.ide.ext.git.client.reset.commit.ResetToCommitView;
import org.eclipse.che.ide.ext.git.client.reset.commit.ResetToCommitViewImpl;
import org.eclipse.che.ide.ext.git.client.reset.files.ResetFilesView;
import org.eclipse.che.ide.ext.git.client.reset.files.ResetFilesViewImpl;
import org.eclipse.che.ide.ext.git.client.revert.RevertCommitView;
import org.eclipse.che.ide.ext.git.client.revert.RevertCommitViewImpl;

/** @author Andrey Plotnikov */
@ExtensionGinModule
public class GitGinModule extends AbstractGinModule {

  /** {@inheritDoc} */
  @Override
  protected void configure() {
    bind(GitServiceClient.class).to(GitServiceClientImpl.class).in(Singleton.class);

    GinMultibinder.newSetBinder(binder(), ImportWizardRegistrar.class)
        .addBinding()
        .to(GitImportWizardRegistrar.class);
    GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class)
        .addBinding()
        .to(CommitterPreferencePresenter.class);

    GinMapBinder<String, VcsChangeMarkerRenderFactory> vcsChangeMarkerRenderFactoryMapBinder =
        GinMapBinder.newMapBinder(binder(), String.class, VcsChangeMarkerRenderFactory.class);
    vcsChangeMarkerRenderFactoryMapBinder
        .addBinding("git")
        .to(GitChangeMarkerRendererFactory.class);

    bind(AddToIndexView.class).to(AddToIndexViewImpl.class).in(Singleton.class);
    bind(ResetToCommitView.class).to(ResetToCommitViewImpl.class).in(Singleton.class);
    bind(RevertCommitView.class).to(RevertCommitViewImpl.class).in(Singleton.class);
    bind(RemoveFromIndexView.class).to(RemoveFromIndexViewImpl.class).in(Singleton.class);
    bind(RevisionListView.class).to(RevisionListViewImpl.class).in(Singleton.class);
    bind(CommitView.class).to(CommitViewImpl.class).in(Singleton.class);
    bind(ChangesListView.class).to(ChangesListViewImpl.class).in(Singleton.class);
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
    bind(GitPanelView.class).to(GitPanelViewImpl.class).in(Singleton.class);
    bind(GitEventSubscribable.class).to(GitEventsHandler.class).in(Singleton.class);
    bind(GitOutputPartView.class).to(GitOutputPartViewImpl.class);
    bind(ChangesPanelView.class).to(ChangesPanelViewImpl.class);
    bind(SelectableChangesPanelView.class).to(SelectableChangesPanelViewImpl.class);
    install(
        new GinFactoryModuleBuilder()
            .implement(GitOutputConsole.class, GitOutputConsolePresenter.class)
            .build(GitOutputConsoleFactory.class));
    install(new GinFactoryModuleBuilder().build(RepositoryNodeFactory.class));

    bind(GitEventsHandler.class).asEagerSingleton();

    bind(ProjectExplorerTreeColorizer.class).asEagerSingleton();
    bind(EditorTabsColorizer.class).asEagerSingleton();
    bind(GitChangeMarkerManager.class).asEagerSingleton();
    bind(GitCheckoutNotifier.class).asEagerSingleton();
  }
}
