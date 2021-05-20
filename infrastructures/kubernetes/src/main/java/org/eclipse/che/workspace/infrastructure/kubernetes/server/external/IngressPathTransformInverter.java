/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.IngressServerExposer.PATH_TRANSFORM_PATH_CATCH;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to undo the effect of the "che.infra.kubernetes.ingress.path_transform"
 * configuration on the ingress paths. I.e. use this to get the path-part of the URL exposed by an
 * ingress given an ingress path.
 *
 * <p>This is usually a noop, apart from the single-host mode.
 */
public class IngressPathTransformInverter {
  private static final Logger LOGGER = LoggerFactory.getLogger(IngressPathTransformInverter.class);
  private static final Pattern PATH_FORMAT_DECONSTRUCTION_REGEX =
      Pattern.compile("(.*)" + PATH_TRANSFORM_PATH_CATCH + "(.*)");

  private final Pattern pathTransformInverse;

  @Inject
  public IngressPathTransformInverter(
      @Nullable @Named("che.infra.kubernetes.ingress.path_transform") String pathTransformFmt) {
    this.pathTransformInverse = extractPathFromFmt(pathTransformFmt);
  }

  /**
   * Given the ingress path transformation from the configuration, this method constructs a regex
   * that is able to extract the original path from a value transformed by the path transformation.
   * E.g. when applied to the transformed path, the regex will extract the substring corresponding
   * to the original "%s" in the path transformation.
   *
   * @param pathTransformFmt the path transformation format
   * @return the regex that essentially reverts the effect of the path transformation
   */
  private static Pattern extractPathFromFmt(String pathTransformFmt) {
    if (pathTransformFmt == null) {
      return Pattern.compile("^(.*)$");
    }
    Matcher m = PATH_FORMAT_DECONSTRUCTION_REGEX.matcher(pathTransformFmt);
    if (m.matches() && m.groupCount() == 2) {
      String prefix = Pattern.quote(m.group(1));
      String suffix = Pattern.quote(m.group(2));
      return Pattern.compile("^" + prefix + "(.*)" + suffix + "$");
    } else {
      LOGGER.warn(
          format(
              "Invalid path transformation format '%s' could not be successfully matched by the"
                  + " deconstruction regex '%s'. Using the path transformation as is which can result in malfunctioning"
                  + " ingresses.",
              pathTransformFmt, PATH_FORMAT_DECONSTRUCTION_REGEX));
      return Pattern.compile(Pattern.quote(pathTransformFmt));
    }
  }

  /**
   * Sometimes, the exposer needs to modify the path contained in the object exposing the server
   * (i.e. ingress in this case). Namely, this is needed to make the URL rewriting work for
   * single-host strategy where the path needs to contain a regular expression match group to retain
   * some of the path (at least in the case of the nginx ingress controller).
   *
   * <p>This method reverts such mangling and returns to the user a path that can be used by the
   * HTTP clients.
   *
   * @param path the path contained within the configuration of the object that needs to be
   *     demangled
   * @return the path demangled such that it can be used in an externally reachable URL or the
   *     untouched path if the path doesn't match the configured path format.
   */
  public String undoPathTransformation(String path) {
    Matcher matcher = pathTransformInverse.matcher(path);
    if (matcher.matches()) {
      return matcher.group(1);
    } else {
      return path;
    }
  }
}
