package org.eclipse.che.workspace.infrastructure.openshift.multiuser.oauth;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.UserDao;

public class OpenshiftUserDao implements UserDao {

  @Override
  public UserImpl getByAliasAndPassword(String emailOrName, String password)
      throws NotFoundException, ServerException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public void create(UserImpl user) throws ConflictException, ServerException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public void update(UserImpl user) throws NotFoundException, ServerException, ConflictException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public void remove(String id) throws ServerException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public UserImpl getByAlias(String alias) throws NotFoundException, ServerException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public UserImpl getById(String id) throws NotFoundException, ServerException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public UserImpl getByName(String name) throws NotFoundException, ServerException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public UserImpl getByEmail(String email) throws NotFoundException, ServerException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public Page<UserImpl> getAll(int maxItems, long skipCount) throws ServerException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public Page<UserImpl> getByNamePart(String namePart, int maxItems, long skipCount)
      throws ServerException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public Page<UserImpl> getByEmailPart(String emailPart, int maxItems, long skipCount)
      throws ServerException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public long getTotalCount() throws ServerException {
    throw new UnsupportedOperationException("not yet implemented");
  }
}
