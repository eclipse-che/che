/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.test.db;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.persist.jpa.JpaPersistModule;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.exceptions.ExceptionHandler;
import org.stringtemplate.v4.ST;

/**
 * Helps to build persistence.xml for test purposes. If bound creates META-INF/persistence.xml with
 * generated content.
 *
 * <p>The example of generated content is:
 *
 * <pre>{@code
 *  // for binding
 *  new PersistTestModuleBuilder().setDriver("org.h2.Driver")
 *                                .addEntityClass(MyEntity.class)
 *                                .addEntityClass(MyEntity2.class)
 *                                .setUrl("jdbc:h2:mem:test")
 *                                .setUser("username")
 *                                .setPassword("secret")
 *                                .build();
 *
 *  // generated persistence.xml
 *  <persistence xmlns="http://java.sun.com/xml/ns/persistence"
 *               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *               xsi:schemaLocation="http://java.sun.com/xml/ns/persistence persistence_1_0.xsd" version="1.0">
 *      <persistence-unit name="test" transaction-type="RESOURCE_LOCAL">
 *          <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>
 *          <class>org.eclipse.che.module.MyEntity</class>
 *          <class>org.eclipse.che.module.MyEntity2</class>
 *          <properties>
 *              <property name="javax.persistence.jdbc.driver" value="org.h2.Driver"/>
 *              <property name="javax.persistence.jdbc.url" value="jdbc:h2:mem:test"/>
 *              <property name="javax.persistence.jdbc.user" value="username"/>
 *              <property name="javax.persistence.jdbc.password" value="secret"/>
 *          </properties>
 *      </persistence-unit>
 * </persistence>
 * }</pre>
 *
 * @author Yevhenii Voevodin
 */
public class PersistTestModuleBuilder {

  private static final String PERSISTENCE_XML_TEMPLATE_RESOURCE_PATH =
      "org/eclipse/che/commons/test/db/persistence.xml.template";

  private final Map<String, String> properties = new LinkedHashMap<>();
  private final Set<String> entityFqnSet = new LinkedHashSet<>();

  private String persistenceUnit = "test";

  /** Sets url, user and password equal to the values provided by server. */
  public PersistTestModuleBuilder runningOn(DBTestServer server) {
    setUrl(server.getUrl());
    setUser(server.getUser());
    setPassword(server.getPassword());
    return this;
  }

  /** Sets the value of {@value PersistenceUnitProperties#JDBC_DRIVER} property. */
  public PersistTestModuleBuilder setDriver(String driver) {
    return setProperty(PersistenceUnitProperties.JDBC_DRIVER, driver);
  }

  /**
   * Sets the value of {@value PersistenceUnitProperties#JDBC_DRIVER} property, the value would be
   * equal to class fqn.
   */
  public PersistTestModuleBuilder setDriver(Class<? extends java.sql.Driver> driverClass) {
    return setDriver(driverClass.getName());
  }

  /** Sets the value of {@value PersistenceUnitProperties#JDBC_URL} property. */
  public PersistTestModuleBuilder setUrl(String url) {
    return setProperty(PersistenceUnitProperties.JDBC_URL, url);
  }

  /** Sets the value of {@value PersistenceUnitProperties#JDBC_USER} property. */
  public PersistTestModuleBuilder setUser(String user) {
    return setProperty(PersistenceUnitProperties.JDBC_USER, user);
  }

  /** Sets the value of {@value PersistenceUnitProperties#JDBC_PASSWORD} property. */
  public PersistTestModuleBuilder setPassword(String password) {
    return setProperty(PersistenceUnitProperties.JDBC_PASSWORD, password);
  }

  /** Sets the value of {@value PersistenceUnitProperties#EXCEPTION_HANDLER_CLASS} property. */
  public PersistTestModuleBuilder setExceptionHandler(Class<? extends ExceptionHandler> exHandler) {
    return setProperty(PersistenceUnitProperties.EXCEPTION_HANDLER_CLASS, exHandler.getName());
  }

  /** Adds class to the listing of the entities defined by persistence unit. */
  public PersistTestModuleBuilder addEntityClass(Class<?> entityClass) {
    entityFqnSet.add(entityClass.getName());
    return this;
  }

  /** Adds class to the listing of the entities defined by persistence unit. */
  public PersistTestModuleBuilder addEntityClass(String fqn) {
    entityFqnSet.add(fqn);
    return this;
  }

  /** Batch add of entity classes to the persistence unit listing. */
  public PersistTestModuleBuilder addEntityClasses(Class<?>... entityClasses) {
    for (Class<?> entityClass : entityClasses) {
      addEntityClass(entityClass);
    }
    return this;
  }

  /** Adds another non-entity class (like attribute converters etc) to class list. */
  public PersistTestModuleBuilder addClass(Class<?> entityClass) {
    entityFqnSet.add(entityClass.getName());
    return this;
  }

  /** Sets persistence unit custom property. */
  public PersistTestModuleBuilder setProperty(String name, String value) {
    if (name != null && value != null) {
      properties.put(name, value);
    }
    return this;
  }

  /** Sets the the value of {@link PersistenceUnitProperties#LOGGING_LEVEL} property. */
  public PersistTestModuleBuilder setLogLevel(String logLevel) {
    return setProperty(PersistenceUnitProperties.LOGGING_LEVEL, logLevel);
  }

  /** Sets the name of persistence unit. */
  public PersistTestModuleBuilder setPersistenceUnit(String persistenceUnit) {
    this.persistenceUnit = persistenceUnit;
    return this;
  }

  /** Creates or overrides META-INF/persistence.xml. */
  public Path savePersistenceXml() throws IOException, URISyntaxException {
    Path persistenceXmlPath = getOrCreateMetaInf().resolve("persistence.xml");
    URL url =
        Thread.currentThread()
            .getContextClassLoader()
            .getResource(PERSISTENCE_XML_TEMPLATE_RESOURCE_PATH);
    if (url == null) {
      throw new IOException(
          "Resource '" + PERSISTENCE_XML_TEMPLATE_RESOURCE_PATH + "' doesn't exist");
    }
    ST st = new ST(Resources.toString(url, UTF_8), '$', '$');
    if (persistenceUnit != null) {
      st.add("unit", persistenceUnit);
    }
    if (!entityFqnSet.isEmpty()) {
      st.add("entity_classes", entityFqnSet);
    }
    if (!properties.isEmpty()) {
      st.add("properties", properties);
    }
    Files.write(persistenceXmlPath, st.render().getBytes(UTF_8));
    return persistenceXmlPath;
  }

  /** Creates persistence.xml and builds module for testing. */
  public Module build() {
    return new PersistTestModule();
  }

  private class PersistTestModule extends AbstractModule {
    @Override
    protected void configure() {
      try {
        savePersistenceXml();
      } catch (Exception x) {
        throw new RuntimeException(x.getMessage());
      }
      install(new JpaPersistModule(persistenceUnit));
    }
  }

  private Path getOrCreateMetaInf() throws URISyntaxException, IOException {
    Path root = Paths.get(Thread.currentThread().getContextClassLoader().getResource(".").toURI());
    Path metaInf = root.resolve("META-INF");
    if (!Files.exists(metaInf)) {
      Files.createDirectory(metaInf);
    }
    return metaInf;
  }
}
