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
package org.eclipse.che.core.metrics;

import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.lang.reflect.Field;
import javax.servlet.ServletContext;
import org.apache.catalina.Manager;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.catalina.core.StandardContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsServletModule extends ServletModule {
  private static final Logger LOG = LoggerFactory.getLogger(TomcatMetricsProvider.class);

  @Override
  protected void configureServlets() {
    Multibinder<MeterBinder> meterMultibinder =
        Multibinder.newSetBinder(binder(), MeterBinder.class);
    meterMultibinder.addBinding().toProvider(TomcatMetricsProvider.class);

    bind(Manager.class).toInstance(getManager(getServletContext()));
  }

  private Manager getManager(ServletContext servletContext) {

    try {

      ApplicationContextFacade acf = (ApplicationContextFacade) servletContext;
      Field privateField = ApplicationContextFacade.class.getDeclaredField("context");
      privateField.setAccessible(true);
      ApplicationContext appContext = (ApplicationContext) privateField.get(acf);

      Field privateField2 = ApplicationContext.class.getDeclaredField("context");
      privateField2.setAccessible(true);
      StandardContext stdContext = (StandardContext) privateField2.get(appContext);
      return stdContext.getManager();

    } catch (Exception e) {
      // maybe not in Tomcat?
      LOG.error(e.getMessage(), e);
    }
    return null;
  }
}
