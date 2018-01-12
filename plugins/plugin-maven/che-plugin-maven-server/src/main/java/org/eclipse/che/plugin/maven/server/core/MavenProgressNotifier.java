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
