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
package org.eclipse.che.core.db;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import org.eclipse.che.core.db.jpa.JpaInitializer;
import org.eclipse.che.core.db.jpa.eclipselink.GuiceEntityListenerInjectionManager;
import org.eclipse.che.core.db.schema.SchemaInitializationException;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.persistence.internal.sessions.AbstractSession;

/**
 * Initializes database components.
 *
 * <p>Those components which require any persistence operations on their bootstrap have to depend on
 * this component. For example:
 *
 * <pre>
 * class StackExistsChecker {
 *
 *     &#064;@Inject
 *     &#064;SuppressWarnings("unused")
 *     private DBInitializer dbInitializer;
 *
 *     &#064;PostConstruct
 *     public void check() {
 *         ....
 *     }
 * }
 * </pre>
 *
 * In this way it is guaranteed that all database related components will be appropriately
 * initialized before {@code check} method is executed.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class DBInitializer {

  /**
   * when value for this key true, then its mean that db is initialized at first time, otherwise db
   * was previously initialized
   */
  public static final String BARE_DB_INIT_PROPERTY_NAME = "bare_database_init";

  private final Map<String, String> initProperties;

  @Inject
  public DBInitializer(SchemaInitializer schemaInitializer, JpaInitializer jpaInitializer)
      throws SchemaInitializationException {
    // schema must be initialized before any other component that may interact with database
    initProperties = ImmutableMap.copyOf(schemaInitializer.init());

    // jpa initialization goes next
    jpaInitializer.init();
  }

  @Inject
  public void setUpInjectionManager(
      GuiceEntityListenerInjectionManager injManager, EntityManagerFactory emFactory) {
    final AbstractSession session = emFactory.unwrap(AbstractSession.class);
    session.setInjectionManager(injManager);
  }

  /** Returns map of properties which represents state of database while initialization process */
  public Map<String, String> getInitProperties() {
    return initProperties;
  }

  /**
   * Returns true only if database was initialized at first time otherwise false would be returned
   */
  public boolean isBareInit() {
    return Boolean.parseBoolean(initProperties.get(BARE_DB_INIT_PROPERTY_NAME));
  }
}
