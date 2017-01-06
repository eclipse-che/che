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
package org.eclipse.che.api.git;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.git.shared.GitUser;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Resolves git user from environment preferences.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class LocalGitUserResolver implements GitUserResolver {

    private static final Logger LOG = LoggerFactory.getLogger(LocalGitUserResolver.class);

    private final PreferenceDao preferenceDao;

    @Inject
    public LocalGitUserResolver(PreferenceDao preferenceDao) {
        this.preferenceDao = preferenceDao;
    }

    @Override
    public GitUser getUser() {
        String name = null;
        String email = null;
        try {
            Map<String, String> preferences = preferenceDao.getPreferences(EnvironmentContext.getCurrent().getSubject().getUserId(),
                                                                           "git.committer.\\w+");
            name = preferences.get("git.committer.name");
            email = preferences.get("git.committer.email");
        } catch (ServerException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        GitUser gitUser = newDto(GitUser.class);
        if (!isNullOrEmpty(name)) {
            gitUser.setName(name);
        }
        if (!isNullOrEmpty(email)) {
            gitUser.setEmail(email);
        }
        return gitUser;
    }
}
