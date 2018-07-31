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
package org.eclipse.che.api.git;

import java.util.regex.Pattern;

/**
 * Utility class for working with Git urls.
 *
 * @author Vladyslav Zhukovskii
 * @author Kevin Pollet
 */
public class GitUrlUtils {
  public static final Pattern GIT_SSH_URL_PATTERN =
      Pattern.compile(
          "((((git|ssh)://)(([^\\\\/@:]+@)??)[^\\\\/@:]+)|([^\\\\/@:]+@[^\\\\/@:]+))(:|/)[^\\\\@:]+");

  private GitUrlUtils() {}

  public static boolean isSSH(String url) {
    return url != null && GIT_SSH_URL_PATTERN.matcher(url).matches();
  }
}
