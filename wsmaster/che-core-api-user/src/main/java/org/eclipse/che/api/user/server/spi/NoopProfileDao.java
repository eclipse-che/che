package org.eclipse.che.api.user.server.spi;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;

public class NoopProfileDao implements ProfileDao {

  @Override
  public void create(ProfileImpl profile) throws ServerException, ConflictException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public void update(ProfileImpl profile) throws NotFoundException, ServerException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public void remove(String id) throws ServerException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public ProfileImpl getById(String id) throws NotFoundException, ServerException {
    throw new UnsupportedOperationException("not yet implemented");
  }
}
