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
package org.eclipse.che.plugin.maven.server.core;

/**
 * Notification interface, mostly used for notification of maven artifact downloading process.
 *
 * <p>WARNING: All implementation of this interface MUST be thread safe.
 *
 * @author Evgen Vidolob
 */
public interface MavenProgressNotifier {

  void setText(String text);

  void setPercent(double percent);

  void setPercentUndefined(boolean undefined);

  boolean isCanceled();

  void start();

  void stop();
}
