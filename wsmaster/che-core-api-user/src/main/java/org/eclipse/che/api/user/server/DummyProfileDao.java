package org.eclipse.che.api.user.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;

public class DummyProfileDao implements ProfileDao {

  @Override
  public void create(ProfileImpl profile) throws ServerException, ConflictException {

  }

  @Override
  public void update(ProfileImpl profile) throws NotFoundException, ServerException {

  }

  @Override
  public void remove(String id) throws ServerException {

  }

  @Override
  public ProfileImpl getById(String id) throws NotFoundException, ServerException {
    return null;
  }
}
