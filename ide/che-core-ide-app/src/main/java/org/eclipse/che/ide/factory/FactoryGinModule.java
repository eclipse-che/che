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
package org.eclipse.che.ide.factory;

import com.google.gwt.inject.client.AbstractGinModule;
import javax.inject.Singleton;
import org.eclipse.che.ide.api.factory.FactoryServiceClient;
import org.eclipse.che.ide.factory.welcome.GreetingPartView;
import org.eclipse.che.ide.factory.welcome.GreetingPartViewImpl;

/** @author Vladyslav Zhukovskii */
public class FactoryGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(JsIntervalSetter.class).asEagerSingleton();
    bind(GreetingPartView.class).to(GreetingPartViewImpl.class).in(Singleton.class);
    bind(FactoryServiceClient.class).to(FactoryServiceClientImpl.class).in(Singleton.class);
  }
}
