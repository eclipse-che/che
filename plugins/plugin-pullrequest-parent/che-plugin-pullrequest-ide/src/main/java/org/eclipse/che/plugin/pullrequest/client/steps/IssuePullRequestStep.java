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


import org.eclipse.che.plugin.pullrequest.client.ContributeMessages;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.NoCommitsInPullRequestException;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.NoHistoryInCommonException;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.PullRequestAlreadyExistsException;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowExecutor;
import org.eclipse.che.plugin.pullrequest.shared.dto.Configuration;
import org.eclipse.che.plugin.pullrequest.shared.dto.PullRequest;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;

import javax.inject.Inject;

/**
 * Create the pull request on the remote VCS repository.
 *
 * @author Kevin Pollet
 */
@Singleton
public class IssuePullRequestStep implements Step {
    private final ContributeMessages messages;

    @Inject
    public IssuePullRequestStep(final ContributeMessages messages) {
        this.messages = messages;
    }

    @Override
    public void execute(final WorkflowExecutor executor, final Context context) {
        final Configuration configuration = context.getConfiguration();
        context.getVcsHostingService()
               .createPullRequest(context.getUpstreamRepositoryOwner(),
                                  context.getUpstreamRepositoryName(),
                                  context.getHostUserLogin(),
                                  context.getWorkBranchName(),
                                  context.getContributeToBranchName(),
                                  configuration.getContributionTitle(),
                                  configuration.getContributionComment())
               .then(new Operation<PullRequest>() {
                   @Override
                   public void apply(PullRequest pullRequest) throws OperationException {
                       context.setPullRequestIssueNumber(pullRequest.getNumber());
                       context.setPullRequest(pullRequest);
                       executor.done(IssuePullRequestStep.this, context);
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(final PromiseError exception) throws OperationException {
                       try {
                           throw exception.getCause();
                       } catch (PullRequestAlreadyExistsException | NoHistoryInCommonException ex) {
                           executor.fail(IssuePullRequestStep.this,
                                         context,
                                         ex.getLocalizedMessage());
                       } catch (NoCommitsInPullRequestException noCommitsEx) {
                           executor.fail(IssuePullRequestStep.this,
                                         context,
                                         messages.stepIssuePullRequestErrorCreatePullRequestWithoutCommits());
                       } catch (Throwable thr) {
                           executor.fail(IssuePullRequestStep.this,
                                         context,
                                         messages.stepIssuePullRequestErrorCreatePullRequest());
                       }
                   }
               });
    }
}
