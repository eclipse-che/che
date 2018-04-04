/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.pullrequest.client.vcs.hosting;

/**
 * This exception should be thrown when separate branches have no commits in common.
 *
 * @author Anton Korneta
 */
public class NoHistoryInCommonException extends Exception {

  public NoHistoryInCommonException(String msg) {
    super(msg);
  }
}
