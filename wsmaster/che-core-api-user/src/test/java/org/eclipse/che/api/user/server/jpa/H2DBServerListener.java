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
package org.eclipse.che.api.user.server.jpa;

import org.eclipse.che.commons.test.tck.AbstractTestListener;
import org.eclipse.persistence.config.TargetServer;
import org.testng.ITestContext;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

import static javax.persistence.spi.PersistenceUnitTransactionType.RESOURCE_LOCAL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.EXCEPTION_HANDLER_CLASS;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.TARGET_SERVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.TRANSACTION_TYPE;

/**
 * @author Yevhenii Voevodin
 */
public class H2DBServerListener extends AbstractTestListener {

    public static final String ENTITY_MANAGER_FACTORY_ATTR_NAME = "entityManagerFactory";

    private EntityManagerFactory managerFactory;

    @Override
    public void onStart(ITestContext context) {
        final Map<String, String> properties = new HashMap<>();
        properties.put(TRANSACTION_TYPE, RESOURCE_LOCAL.name());
        properties.put(JDBC_DRIVER, "org.h2.Driver");
        properties.put(JDBC_URL, "jdbc:h2:mem:;DB_CLOSE_DELAY=0;MVCC=true;TRACE_LEVEL_FILE=4;TRACE_LEVEL_SYSTEM_OUT=4");
        properties.put(JDBC_USER, "");
        properties.put(JDBC_PASSWORD, "");
        properties.put(TARGET_SERVER, TargetServer.None);
        properties.put(EXCEPTION_HANDLER_CLASS, "org.eclipse.che.api.core.h2.jdbc.jpa.eclipselink.H2ExceptionHandler");

        managerFactory = Persistence.createEntityManagerFactory("main", properties);
        context.setAttribute(ENTITY_MANAGER_FACTORY_ATTR_NAME, managerFactory);
    }

    @Override
    public void onFinish(ITestContext context) {
        managerFactory.close();
    }
}
