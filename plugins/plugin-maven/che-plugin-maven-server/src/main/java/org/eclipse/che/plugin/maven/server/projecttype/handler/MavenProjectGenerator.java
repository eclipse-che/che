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
package org.eclipse.che.plugin.maven.server.projecttype.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author gazarenkov */
@Singleton
public class MavenProjectGenerator implements CreateProjectHandler {

  private static final Logger LOG = LoggerFactory.getLogger(MavenProjectGenerator.class);

  private final Map<String, GeneratorStrategy> strategies = new HashMap<>();

  @Inject
  public MavenProjectGenerator(Set<GeneratorStrategy> generatorStrategies) throws ServerException {
    for (GeneratorStrategy generatorStrategy : generatorStrategies) {
      strategies.put(generatorStrategy.getId(), generatorStrategy);
    }
  }

  @Override
  public String getProjectType() {
    return MavenAttributes.MAVEN_ID;
  }

  @Override
  public void onCreateProject(
      String projectWsPath, Map<String, AttributeValue> attributes, Map<String, String> options)
      throws ForbiddenException, ConflictException, ServerException, NotFoundException {
    if (options == null || options.isEmpty() || !options.containsKey("type")) {
      strategies
          .get(MavenAttributes.SIMPLE_GENERATION_STRATEGY)
          .generateProject(projectWsPath, attributes, options);
    } else {
      if (strategies.containsKey(options.get("type"))) {
        strategies.get(options.get("type")).generateProject(projectWsPath, attributes, options);
      } else {
        String errorMsg = String.format("Generation strategy %s not found", options.get("type"));
        LOG.warn("MavenProjectGenerator", errorMsg);
        throw new ServerException(errorMsg);
      }
    }
  }
}
