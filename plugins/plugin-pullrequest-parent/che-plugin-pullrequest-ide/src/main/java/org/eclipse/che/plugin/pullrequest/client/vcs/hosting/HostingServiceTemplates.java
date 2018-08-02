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
package org.eclipse.che.plugin.pullrequest.client.vcs.hosting;

import com.google.gwt.i18n.client.Messages;

/**
 * Hosting service templates.
 *
 * @author Kevin Pollet
 */
public interface HostingServiceTemplates extends Messages {
  /**
   * The SSH URL to a repository.
   *
   * @param username the user name.
   * @param repository the repository name.
   * @return the URL
   */
  String sshUrlTemplate(String username, String repository);

  /**
   * The HTTP URL to a repository.
   *
   * @param username the user name.
   * @param repository the repository name.
   * @return the URL
   */
  String httpUrlTemplate(String username, String repository);

  /**
   * The URL to a pull request.
   *
   * @param username the user name.
   * @param repository the repository name.
   * @param pullRequestNumber the pull request number.
   * @return the URL
   */
  String pullRequestUrlTemplate(String username, String repository, String pullRequestNumber);

  /**
   * The formatted version of the review factory url using the Hosting service markup language.
   *
   * @param protocol the protocol used http or https
   * @param host the host.
   * @param reviewFactoryUrl the review factory url.
   * @return the formatted version of the review factory url
   */
  String formattedReviewFactoryUrlTemplate(String protocol, String host, String reviewFactoryUrl);
}
