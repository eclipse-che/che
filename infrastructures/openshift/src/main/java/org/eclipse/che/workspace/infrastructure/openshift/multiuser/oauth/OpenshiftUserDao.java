package org.eclipse.che.workspace.infrastructure.openshift.multiuser.oauth;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.openshift.api.model.User;
import io.fabric8.openshift.client.OpenShiftClient;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;

@Singleton
public class OpenshiftUserDao implements UserDao {

  private final OpenShiftClientFactory clientFactory;

  @Inject
  public OpenshiftUserDao(OpenShiftClientFactory clientFactory) {
    this.clientFactory = clientFactory;
  }

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
    try {
      OpenShiftClient client =
          clientFactory.createAuthenticatedOC(
              EnvironmentContext.getCurrent().getSubject().getToken());
      User openshiftUser = client.currentUser();
      if (openshiftUser == null
          || openshiftUser.getMetadata() == null
          || !id.equals(openshiftUser.getMetadata().getUid())) {
        throw new NotFoundException(String.format("User with ID '%s' not found.", id));
      }
      ObjectMeta userMeta = openshiftUser.getMetadata();
      return new UserImpl(userMeta.getUid(), userMeta.getName() + "@che", userMeta.getName());
    } catch (InfrastructureException e) {
      throw new ServerException(e);
    }
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
