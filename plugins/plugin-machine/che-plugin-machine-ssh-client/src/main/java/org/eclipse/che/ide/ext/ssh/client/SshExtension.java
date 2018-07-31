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
package org.eclipse.che.ide.ext.ssh.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.extension.Extension;

/**
 * Extension add Ssh support to the IDE Application.
 *
 * @author Andrey Plotnikov
 */
@Singleton
@Extension(title = "SSH", version = "3.0.0")
public class SshExtension {
  @Inject
  public SshExtension() {}
}
