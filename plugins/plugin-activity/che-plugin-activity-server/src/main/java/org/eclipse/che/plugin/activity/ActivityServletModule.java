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
package org.eclipse.che.plugin.activity;

import com.google.inject.servlet.ServletModule;
import org.eclipse.che.inject.DynaModule;

/** @author Mihail Kuznyetsov */
@DynaModule
public class ActivityServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    filter("/*").through(org.eclipse.che.plugin.activity.LastAccessTimeFilter.class);
  }
}
