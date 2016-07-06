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
package org.eclipse.che.plugin.docker.machine.local.interceptor;

import com.google.common.base.MoreObjects;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerFileException;
import org.eclipse.che.plugin.docker.client.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.plugin.docker.client.Dockerfile;
import org.eclipse.che.plugin.docker.client.ProgressLineFormatterImpl;
import org.eclipse.che.plugin.docker.client.params.PullParams;
import org.eclipse.che.plugin.docker.client.parser.DockerImageIdentifier;
import org.eclipse.che.plugin.docker.client.parser.DockerImageIdentifierParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Allow to build docker machine if base image of machine recipe cached but network is gone.
 *
 * @author Alexander Garagatyi
 *
 * @see org.eclipse.che.plugin.docker.machine.DockerInstanceProvider#buildImage(Dockerfile, LineConsumer, String, boolean, long, long)
 */
public class EnableOfflineDockerMachineBuildInterceptor implements MethodInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(EnableOfflineDockerMachineBuildInterceptor.class);

    @Inject
    DockerConnector                               dockerConnector;
    @Inject
    UserSpecificDockerRegistryCredentialsProvider dockerCredentials;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        // If force pull of base image is not disabled ensure that image build won't fail if needed layers are cached
        // but update of them fails due to network outage.
        // To do that do pull manually if needed and do not force docker to do pull itself.
        final boolean isForcePullEnabled = (boolean)methodInvocation.getArguments()[3];
        if (isForcePullEnabled) {
            final Dockerfile dockerfile = (Dockerfile)methodInvocation.getArguments()[0];
            final LineConsumer creationLogsOutput = (LineConsumer)methodInvocation.getArguments()[1];

            try {
                pullImage(dockerfile.getImages().get(0).getFrom(), creationLogsOutput);
            } catch (IOException | DockerFileException | InterruptedException ignored) {
            }
        }

        methodInvocation.getArguments()[3] = Boolean.FALSE;
        return methodInvocation.proceed();
    }

    private void pullImage(String image, final LineConsumer creationLogsOutput)
            throws DockerFileException, IOException, InterruptedException {

        DockerImageIdentifier imageIdentifier = DockerImageIdentifierParser.parse(image);
        final ProgressLineFormatterImpl progressLineFormatter = new ProgressLineFormatterImpl();

        dockerConnector.pull(PullParams.create(imageIdentifier.getRepository())
                                       .withTag(MoreObjects.firstNonNull(imageIdentifier.getTag(), "latest"))
                                       .withRegistry(imageIdentifier.getRegistry())
                                       .withAuthConfigs(dockerCredentials.getCredentials()),
                             currentProgressStatus -> {
                                 try {
                                     creationLogsOutput.writeLine(progressLineFormatter.format(currentProgressStatus));
                                 } catch (IOException e) {
                                     LOG.error(e.getLocalizedMessage(), e);
                                 }
                             });
    }
}
