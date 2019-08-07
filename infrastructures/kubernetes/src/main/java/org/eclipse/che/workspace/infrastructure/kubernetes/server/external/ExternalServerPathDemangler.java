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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import io.fabric8.kubernetes.api.model.HasMetadata;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.commons.annotation.Nullable;

public class ExternalServerPathDemangler {
  private final Pattern pathTransformInverse;

  @Inject
  public ExternalServerPathDemangler(
      @Nullable @Named("che.infra.kubernetes.ingress.path_transform") String pathTransformFmt) {
    this.pathTransformInverse = extractPathFromFmt(pathTransformFmt);
  }

  private static Pattern extractPathFromFmt(String pathTransformFmt) {
    int refIdx = pathTransformFmt.indexOf("%s");
    String matchPath = "(.*)";

    String transformed;
    if (refIdx < 0) {
      transformed = Pattern.quote(pathTransformFmt);
    } else {
      if (refIdx == 0) {
        if (pathTransformFmt.length() > 2) {
          transformed = matchPath + Pattern.quote(pathTransformFmt.substring(2));
        } else {
          transformed = matchPath;
        }
      } else {
        String prefix = Pattern.quote(pathTransformFmt.substring(0, refIdx));
        String suffix =
            refIdx < pathTransformFmt.length() - 2
                ? Pattern.quote(pathTransformFmt.substring(refIdx + 2))
                : "";

        transformed = prefix + matchPath + suffix;
      }
    }

    return Pattern.compile("^" + transformed + "$");
  }

  /**
   * Sometimes, the exposer needs to modify the path contained in the object exposing the server
   * (ingress or route). Namely, this is needed to make the URL rewriting work for single-host
   * strategy where the path needs to contain a regular expression match group to retain some of the
   * path.
   *
   * <p>This method reverts such mangling and returns to the user a path that can be used by the
   * HTTP clients.
   *
   * @param exposingObject a Kubernetes object in charge of actual exposure of the server (i.e.
   *     ingress or route)
   * @param path the path contained within the configuration of the object that needs to be
   *     demangled
   * @return the path demangled such that it can be used in an externally reachable URL
   */
  public String demanglePath(HasMetadata exposingObject, String path) {
    Matcher matcher = pathTransformInverse.matcher(path);
    if (matcher.matches()) {
      return matcher.group(1);
    } else {
      return path;
    }
  }
}
