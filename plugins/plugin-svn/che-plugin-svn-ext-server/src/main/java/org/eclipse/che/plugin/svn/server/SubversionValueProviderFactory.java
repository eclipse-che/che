/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.svn.server;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.FsPathResolver;
import org.eclipse.che.api.project.server.type.ReadonlyValueProvider;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.api.project.server.type.ValueStorageException;
import org.eclipse.che.plugin.svn.shared.SubversionTypeConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ValueProviderFactory} for indicating the project is a Subversion working
 * copy.
 */
public class SubversionValueProviderFactory implements ValueProviderFactory {

  private static final Logger LOG = LoggerFactory.getLogger(SubversionValueProviderFactory.class);

  private final SubversionApi subversionApi;
  private final FsPathResolver fsPathResolver;
  private final FsManager fsManager;

  @Inject
  public SubversionValueProviderFactory(
      SubversionApi subversionApi, FsPathResolver fsPathResolver, FsManager fsManager) {
    this.subversionApi = subversionApi;
    this.fsPathResolver = fsPathResolver;
    this.fsManager = fsManager;
  }

  @Override
  public ValueProvider newInstance(ProjectConfig projectConfig) {
    return new ReadonlyValueProvider() {
      @Override
      public List<String> getValues(final String attributeName) throws ValueStorageException {
        if (isNullOrEmpty(projectConfig.getPath())) {
          return Collections.emptyList();
        }
        LOG.debug("Asked value for attribute {}.", attributeName);
        if (attributeName == null) {
          throw new ValueStorageException("Invalid attribute name: null");
        }
        switch (attributeName) {
          case SubversionTypeConstant.SUBVERSION_ATTRIBUTE_REPOSITORY_URL:
            final List<String> result = getRepositoryUrl(projectConfig.getPath());
            LOG.debug(
                "Attribute {}, returning value {}",
                attributeName,
                Arrays.toString(result.toArray(new String[result.size()])));
            return result;
          default:
            throw new ValueStorageException("Unsupported attribute: " + attributeName);
        }
      }
    };
  }

  private List<String> getRepositoryUrl(String projectWsPath) throws ValueStorageException {
    try {
      if (isSvn(projectWsPath)) {
        if (isNullOrEmpty(projectWsPath)) {
          final String response = subversionApi.getRepositoryUrl(projectWsPath);
          return Collections.singletonList(response);
        } else {
          LOG.debug("invalid project path");
          throw new ValueStorageException("invalid project path");
        }
      } else {
        return Collections.emptyList();
      }
    } catch (ForbiddenException | ServerException e) {
      LOG.debug("svn info error", e);
      throw new ValueStorageException(e.getMessage());
    }
  }

  private boolean isSvn(String projectWsPath) throws ForbiddenException, ServerException {
    LOG.debug("Searching for '.svn' in {}.", projectWsPath);
    String svnDirectoryWsPath = fsPathResolver.resolve(projectWsPath, ".svn");
    if (fsManager.existsAsDirectory(svnDirectoryWsPath)) {
      LOG.debug("Found it.");
      return true;
    } else {
      LOG.debug("Didn't find it.");
      return false;
    }
  }
}
