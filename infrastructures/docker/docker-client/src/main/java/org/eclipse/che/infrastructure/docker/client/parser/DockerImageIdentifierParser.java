/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.che.infrastructure.docker.client.DockerFileException;

/**
 * Parse docker image reference.
 *
 * <p>For example reference used in FROM instruction of Dockerfile.<br>
 * This class doesn't validate all components as Docker do.<br>
 * It was designed to extract base docker image reference from dockerfile.
 *
 * @author Alexander Garagatyi
 */
public class DockerImageIdentifierParser {

  // Validation rules are taken from
  // https://github.com/docker/distribution/blob/master/reference/regexp.go
  private static final String HOSTNAME_COMPONENT =
      "(?:[a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9])";
  private static final String REGISTRY =
      HOSTNAME_COMPONENT + "(?:\\." + HOSTNAME_COMPONENT + ")*(?::[0-9]+)?";
  private static final String SEPARATOR = "(?:[._]|__|[-]*)";
  private static final String ALPHA_NUMERIC = "[a-z0-9]+";
  private static final String NAME_COMPONENT =
      ALPHA_NUMERIC + "(?:" + SEPARATOR + ALPHA_NUMERIC + ")*";
  private static final String REPOSITORY = NAME_COMPONENT + "(?:/" + NAME_COMPONENT + ")*";
  private static final String TAG = "[\\w][\\w.-]*";
  private static final String DIGEST = "[\\w+.:-]+";
  private static final String NAME = "(?:" + REGISTRY + "/)?" + REPOSITORY;
  private static final String REFERENCE = NAME + "(?::" + TAG + ")?" + "(?:@" + DIGEST + ")?";
  private static final Pattern IMAGE_PATTERN = Pattern.compile(REFERENCE);

  /**
   * Validates and parse docker image reference into object that holds reference components
   *
   * @param image image reference to parse
   * @throws DockerFileException if validation fails
   */
  public static DockerImageIdentifier parse(final String image) throws DockerFileException {
    if (image == null || image.isEmpty()) {
      throw new DockerFileException("Null and empty argument value is forbidden");
    }

    Matcher matcher = IMAGE_PATTERN.matcher(image);
    if (!matcher.matches()) {
      throw new DockerFileException("Provided image reference is invalid");
    }

    DockerImageIdentifier.DockerImageIdentifierBuilder identifierBuilder =
        DockerImageIdentifier.builder();
    String workingCopyOfImage = image;

    // extract digest
    int index = workingCopyOfImage.lastIndexOf('@');
    if (index != -1) {
      String digest = workingCopyOfImage.substring(index + 1);
      if (!digest.isEmpty()) {
        workingCopyOfImage = workingCopyOfImage.substring(0, index);
        identifierBuilder.setDigest(digest);
      }
    }

    // extract tag
    index = workingCopyOfImage.lastIndexOf(':');
    if (index != -1) {
      if (workingCopyOfImage.lastIndexOf('/') < index) {
        String tag = workingCopyOfImage.substring(index + 1);
        if (!tag.isEmpty()) {
          workingCopyOfImage = workingCopyOfImage.substring(0, index);
          identifierBuilder.setTag(tag);
        }
      }
    }

    // find first part of the name that can be registry or first repository part
    index = workingCopyOfImage.indexOf('/');
    String beforeSlash = index > -1 ? workingCopyOfImage.substring(0, index) : "";
    // consider first part of the name as registry if:
    // - there is dot symbol in it (consider it as dot in the hostname, e.g. eclipse.com)
    // - there is colon symbol in it (consider it as registry port mark)
    // - it is equal to 'localhost'
    if (!beforeSlash.isEmpty()
        && (beforeSlash.contains(".")
            || beforeSlash.contains(":")
            || "localhost".equals(beforeSlash))) {

      identifierBuilder
          .setRegistry(beforeSlash)
          .setRepository(workingCopyOfImage.substring(index + 1));
    } else {
      identifierBuilder.setRepository(workingCopyOfImage);
    }

    return identifierBuilder.build();
  }

  private DockerImageIdentifierParser() {}
}
