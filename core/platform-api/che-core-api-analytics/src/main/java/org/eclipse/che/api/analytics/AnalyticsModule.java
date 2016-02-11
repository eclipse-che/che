/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.analytics;

import org.eclipse.che.api.analytics.impl.DummyMetricHandler;
import com.google.inject.AbstractModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/** @author Anatoliy Bazko */
public class AnalyticsModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsModule.class);

    private static final String CHE_LOCAL_CONF_DIR      = "che.local.conf.dir";
    private static final String ANALYTICS_CONF_FILENAME = "analytics.properties";
    
    private static final String METRIC_HANDLER_CLASS_NAME = "analytics.api.metric_handler";

    @Override
    protected void configure() {
        MetricHandler metricHandler;
        try {
            metricHandler = instantiateMetricHandler();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                InstantiationException e) {
            metricHandler = new DummyMetricHandler();
            LOG.error(e.getMessage(), e);
        }

        LOG.info(metricHandler.getClass().getName() + " is used");

        bind(MetricHandler.class).toInstance(metricHandler);
        bind(AnalyticsService.class);
    }

    private MetricHandler instantiateMetricHandler() throws NoSuchMethodException,
                                                            IllegalAccessException,
                                                            InvocationTargetException,
                                                            InstantiationException {
        Properties properties;
        try {
            properties = readAnalyticsProperties();
        } catch (IOException e) {
            LOG.warn("Error reading " + ANALYTICS_CONF_FILENAME + " " + e.getMessage());
            return new DummyMetricHandler();
        }

        String clazzName = (String)properties.get(METRIC_HANDLER_CLASS_NAME);
        if (clazzName == null) {
            return new DummyMetricHandler();

        } else {
            try {
                Class<?> clazz = Class.forName(clazzName);

                try {
                    return (MetricHandler)clazz.getConstructor(Properties.class).newInstance(properties);
                } catch (NoSuchMethodException e) {
                    return (MetricHandler)clazz.getConstructor().newInstance();
                }
            } catch (ClassNotFoundException e) {
                return new DummyMetricHandler();
            }
        }
    }

    private Properties readAnalyticsProperties() throws IOException {
        String fileName = System.getProperty(CHE_LOCAL_CONF_DIR) + File.separator + ANALYTICS_CONF_FILENAME;

        try (InputStream in = new FileInputStream(new File(fileName))) {
            Properties properties = new Properties();
            properties.load(in);

            return properties;
        }
    }
}
