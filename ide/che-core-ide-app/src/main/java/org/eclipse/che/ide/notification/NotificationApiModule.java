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
package org.eclipse.che.ide.notification;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.notification.NotificationManager;

/**
 * GIN module for configuring Notification API components.
 *
 * @author Artem Zatsarynnyi
 */
public class NotificationApiModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(NotificationManager.class).to(NotificationManagerImpl.class).in(Singleton.class);
    bind(NotificationManagerView.class).to(NotificationManagerViewImpl.class).in(Singleton.class);
  }
}
