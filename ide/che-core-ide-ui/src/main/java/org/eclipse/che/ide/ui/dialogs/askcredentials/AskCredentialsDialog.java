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
// TODO This is used in wizard/ProjectImporter, find a solution to move it to plugin-svn.
package org.eclipse.che.ide.ui.dialogs.askcredentials;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.auth.Credentials;

/**
 * Dialog for retrieving credentials for operations.
 *
 * @author Igor Vinokur
 */
public interface AskCredentialsDialog {

  /**
   * Returns credentials from dialog.
   *
   * @return {@link Credentials} that contains user name and password
   */
  Promise<Credentials> askCredentials();
}
