/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.eclipse.che.api.core.model.workspace.devfile.Env;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;

/** Utility class for dealing with environment variables */
public class EnvVars {

  private static final Pattern REFERENCE_PATTERN = Pattern.compile("\\$\\(\\w+\\)");

  /**
   * Applies the specified env vars list to the specified pod data(containers and init containers).
   *
   * <p>If a container does not have the corresponding env - it will be provisioned, if it has - the
   * value will be overridden.
   *
   * @param podData pod to supply env vars
   * @param env env vars to apply
   */
  public void apply(PodData podData, List<? extends Env> env) {
    Stream.concat(
            podData.getSpec().getInitContainers().stream(),
            podData.getSpec().getContainers().stream())
        .forEach(c -> apply(c, env));
  }

  /**
   * Applies the specified env vars list to the specified containers.
   *
   * <p>If a container does not have the corresponding env - it will be provisioned, if it has - the
   * value will be overridden.
   *
   * @param container pod to supply env vars
   * @param toApply env vars to apply
   */
  public void apply(Container container, List<? extends Env> toApply) {
    List<EnvVar> targetEnv = container.getEnv();
    if (targetEnv == null) {
      targetEnv = new ArrayList<>();
      container.setEnv(targetEnv);
    }

    for (Env env : toApply) {
      apply(targetEnv, env);
    }
  }

  private void apply(List<EnvVar> targetEnv, Env env) {
    Optional<EnvVar> existingOpt =
        targetEnv.stream().filter(e -> e.getName().equals(env.getName())).findAny();
    if (existingOpt.isPresent()) {
      EnvVar envVar = existingOpt.get();
      envVar.setValue(env.getValue());
      envVar.setValueFrom(null);
    } else {
      targetEnv.add(new EnvVar(env.getName(), env.getValue(), null));
    }
  }

  /**
   * Looks at the value of the provided environment variable and returns a set of environment
   * variable references in the Kubernetes convention of {@literal $(VAR_NAME)}.
   *
   * <p>See <a
   * href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.13/#envvar-v1-core">API
   * docs</a> and/or <a
   * href="https://kubernetes.io/docs/tasks/inject-data-application/define-environment-variable-container/#using-environment-variables-inside-of-your-config">documentation</a>.
   *
   * @param var the environment variable to analyze
   * @return a set of variable references, never null
   */
  public static Set<String> extractReferencedVariables(EnvVar var) {
    String val = var.getValue();
    if (val == null) {
      return Collections.emptySet();
    }

    Matcher matcher = REFERENCE_PATTERN.matcher(val);

    // let's just keep the initial size small, because usually there are not that many references
    // present.
    Set<String> ret = new HashSet<>(2);

    while (matcher.find()) {
      int start = matcher.start();

      // the variable reference can be escaped using a double $, e.g. $$(VAR) is not a reference
      if (start > 0 && val.charAt(start - 1) == '$') {
        continue;
      }

      // extract the variable name out of the reference $(NAME) -> NAME
      String refName = matcher.group().substring(2, matcher.group().length() - 1);
      ret.add(refName);
    }

    return ret;
  }
}
