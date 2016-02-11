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
package org.eclipse.che.plugin.docker.machine;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.machine.server.impl.InstanceKeyImpl;
import org.eclipse.che.api.machine.server.spi.InstanceKey;

/**
 * Set of helper methods that identifies docker image properties
 *
 * @author Sergii Kabashnyuk
 */
public class DockerInstanceKey extends InstanceKeyImpl {
    public static final String REPOSITORY = "repository";
    public static final String TAG        = "tag";
    public static final String ID         = "id";
    public static final String REGISTRY   = "registry";

    public DockerInstanceKey(InstanceKey key) {
        super(key);
    }

    public DockerInstanceKey(String repository, String tag, String id, String registry) {
        super(ImmutableMap.of(REPOSITORY, repository, TAG, tag, ID, id, REGISTRY, registry));
    }

    public DockerInstanceKey(String repository, String id, String registry) {
        super(ImmutableMap.of(REPOSITORY, repository, ID, id, REGISTRY, registry));
    }

    public DockerInstanceKey(String repository,  String registry) {
        super(ImmutableMap.of(REPOSITORY, repository, REGISTRY, registry));
    }

    public String getRepository() {
        return getFields().get(REPOSITORY);
    }

    public String getTag() {
        return getFields().get(TAG);
    }

    public String getImageId() {
        return getFields().get(ID);
    }

    public String getRegistry() {
        return getFields().get(REGISTRY);
    }

    /**
     * Returns full name of docker image.
     *
     * It consists of registry, userspace, repository name, tag.
     * E.g. docker-registry.company.com:5000/userspace1/my-repository:some-tag
     */
    public String getFullName() {
        final StringBuilder fullRepoId = new StringBuilder();
        if (getRegistry() != null) {
            fullRepoId.append(getRegistry()).append('/');
        }
        fullRepoId.append(getRepository());
        if (getTag() != null) {
            fullRepoId.append(':').append(getTag());
        }
        return fullRepoId.toString();
    }

}
