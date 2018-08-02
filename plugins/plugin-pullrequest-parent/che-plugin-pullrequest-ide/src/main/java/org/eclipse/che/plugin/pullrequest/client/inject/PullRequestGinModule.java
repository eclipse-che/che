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
package org.eclipse.che.plugin.pullrequest.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import javax.inject.Singleton;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.plugin.pullrequest.client.dialogs.commit.CommitView;
import org.eclipse.che.plugin.pullrequest.client.dialogs.commit.CommitViewImpl;
import org.eclipse.che.plugin.pullrequest.client.parts.contribute.ContributePartView;
import org.eclipse.che.plugin.pullrequest.client.parts.contribute.ContributePartViewImpl;
import org.eclipse.che.plugin.pullrequest.client.preference.ContributePreferencePresenter;
import org.eclipse.che.plugin.pullrequest.client.steps.AddForkRemoteStepFactory;
import org.eclipse.che.plugin.pullrequest.client.steps.PushBranchOnForkStep;
import org.eclipse.che.plugin.pullrequest.client.steps.PushBranchStepFactory;
import org.eclipse.che.plugin.pullrequest.client.steps.WaitForkOnRemoteStepFactory;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowExecutor;

/** Gin module definition for the contributor extension. */
@ExtensionGinModule
public class PullRequestGinModule extends AbstractGinModule {

  @Override
  protected void configure() {

    // bind the commit dialog view
    bind(CommitView.class).to(CommitViewImpl.class);

    // bind the part view
    bind(ContributePartView.class).to(ContributePartViewImpl.class);

    // the steps
    bind(WorkflowExecutor.class).in(Singleton.class);
    bind(PushBranchOnForkStep.class);
    install(new GinFactoryModuleBuilder().build(WaitForkOnRemoteStepFactory.class));
    install(new GinFactoryModuleBuilder().build(PushBranchStepFactory.class));
    install(new GinFactoryModuleBuilder().build(AddForkRemoteStepFactory.class));
    GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class)
        .addBinding()
        .to(ContributePreferencePresenter.class);
  }
}
