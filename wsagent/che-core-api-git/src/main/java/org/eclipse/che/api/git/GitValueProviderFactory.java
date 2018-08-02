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
package org.eclipse.che.api.git;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.git.GitProjectType.GIT_CURRENT_HEAD_NAME;
import static org.eclipse.che.api.git.GitProjectType.GIT_REPOSITORY_REMOTES;
import static org.eclipse.che.api.project.shared.Constants.VCS_PROVIDER_NAME;

import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.project.server.type.ReadonlyValueProvider;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.api.project.server.type.ValueStorageException;

/** @author Roman Nikitenko */
@Singleton
public class GitValueProviderFactory implements ValueProviderFactory {

  @Inject private PathTransformer pathTransformer;

  @Inject private GitConnectionFactory gitConnectionFactory;

  @Override
  public ValueProvider newInstance(String wsPath) {
    return new ReadonlyValueProvider() {
      @Override
      public List<String> getValues(String attributeName) throws ValueStorageException {
        if (isNullOrEmpty(wsPath)) {
          return emptyList();
        }

        String fsPath = pathTransformer.transform(wsPath).toString();

        try (GitConnection gitConnection = gitConnectionFactory.getConnection(fsPath)) {
          // check whether the folder belongs to git repository
          if (!gitConnection.isInsideWorkTree()) {
            return emptyList();
          }

          switch (attributeName) {
            case VCS_PROVIDER_NAME:
              return singletonList("git");
            case GIT_CURRENT_HEAD_NAME:
              return singletonList(gitConnection.getCurrentReference().getName());
            case GIT_REPOSITORY_REMOTES:
              return gitConnection
                  .remoteList(null, false)
                  .stream()
                  .map(Remote::getUrl)
                  .collect(Collectors.toList());
            default:
              return emptyList();
          }
        } catch (ApiException e) {
          throw new ValueStorageException(e.getMessage());
        }
      }
    };
  }
}
