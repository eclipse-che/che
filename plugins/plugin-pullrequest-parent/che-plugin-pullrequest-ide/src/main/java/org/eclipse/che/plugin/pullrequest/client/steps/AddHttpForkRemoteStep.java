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
package org.eclipse.che.plugin.pullrequest.client.steps;

import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowExecutor;
import com.google.inject.Singleton;

import javax.inject.Inject;

/**
 * Add HTTP fork remote URL to repository.
 *
 * @author Mihail Kuznyetsov
 */
@Singleton
public class AddHttpForkRemoteStep implements Step {
    private final AddForkRemoteStepFactory addForkRemoteStepFactory;

    @Inject
    public AddHttpForkRemoteStep(AddForkRemoteStepFactory addForkRemoteStepFactory) {
        this.addForkRemoteStepFactory = addForkRemoteStepFactory;
    }

    @Override
    public void execute(final WorkflowExecutor executor, final Context context) {
        String remoteUrl = context.getVcsHostingService().makeHttpRemoteUrl(context.getHostUserLogin(), context.getForkedRepositoryName());
        addForkRemoteStepFactory.create(this, remoteUrl)
                                .execute(executor, context);
    }
}
