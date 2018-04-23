package org.eclipse.che.core.db.jpa;

import com.google.inject.Inject;
import java.util.Collections;
import java.util.Set;
import javax.persistence.EntityManagerFactory;
import org.eclipse.che.api.system.server.ServiceTermination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Anton Korneta */
public class DBTerminator implements ServiceTermination {

  private static final Logger LOG = LoggerFactory.getLogger(DBTerminator.class);

  private final EntityManagerFactory emFactory;

  @Inject
  public DBTerminator(EntityManagerFactory emFactory) {
    this.emFactory = emFactory;
  }

  @Override
  public void terminate() throws InterruptedException {
    suspend();
  }

  @Override
  public void suspend() throws InterruptedException, UnsupportedOperationException {
    try {
      LOG.info("Close entity manager factory..");
      emFactory.close();
    } catch (Exception ex) {
      LOG.error(ex.getMessage());
    }
  }

  @Override
  public String getServiceName() {
    return "dbterminator";
  }

  @Override
  public Set<String> getDependencies() {
    return Collections.singleton("workspace");
  }
}
