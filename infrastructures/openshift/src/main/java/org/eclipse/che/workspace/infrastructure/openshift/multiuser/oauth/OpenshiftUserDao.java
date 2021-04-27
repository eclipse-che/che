package org.eclipse.che.workspace.infrastructure.openshift.multiuser.oauth;

import io.fabric8.openshift.api.model.User;
import io.fabric8.openshift.api.model.UserList;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OpenshiftUserDao implements UserDao {

  private static final Logger LOG = LoggerFactory.getLogger(OpenshiftUserDao.class);
  private final OpenShiftClientFactory clientFactory;

  @Inject
  public OpenshiftUserDao(
      OpenShiftClientFactory clientFactory) {
    this.clientFactory = clientFactory;
  }


  @Override
  public UserImpl getByAliasAndPassword(String emailOrName, String password)
      throws NotFoundException, ServerException {
    LOG.info("OpenshiftUserDao#getByAliasAndPassword");
    return null;
  }

  @Override
  public void create(UserImpl user) throws ConflictException, ServerException {
    LOG.info("OpenshiftUserDao#create");

  }

  @Override
  public void update(UserImpl user) throws NotFoundException, ServerException, ConflictException {
    LOG.info("OpenshiftUserDao#update");

  }

  @Override
  public void remove(String id) throws ServerException {
    LOG.info("OpenshiftUserDao#remove");

  }

  @Override
  public UserImpl getByAlias(String alias) throws NotFoundException, ServerException {
    LOG.info("OpenshiftUserDao#getByAlias");
    return null;
  }

  @Override
  public UserImpl getById(String id) throws NotFoundException, ServerException {
    LOG.info("OpenshiftUserDao#getById({})", id);
    try {
      User u = clientFactory.createOC().currentUser();
      if (u.getMetadata().getUid().equals(id)) {
        return createFromOpenshiftUser(u);
      } else {
        throw new NotFoundException("nonono");
      }
    } catch (InfrastructureException e) {
      throw new ServerException(e);
    }
  }

  @Override
  public UserImpl getByName(String name) throws NotFoundException, ServerException {
    LOG.info("OpenshiftUserDao#getByName({})", name);
    try {
      User u = clientFactory.createOC().users().withName(name).get();
      return createFromOpenshiftUser(u);
    } catch (InfrastructureException e) {
      throw new ServerException(e);
    }
  }

  @Override
  public UserImpl getByEmail(String email) throws NotFoundException, ServerException {
    LOG.info("OpenshiftUserDao#getByEmail");
    return null;
  }

  @Override
  public Page<UserImpl> getAll(int maxItems, long skipCount) throws ServerException {
    LOG.info("OpenshiftUserDao#getAll");
    return null;
  }

  @Override
  public Page<UserImpl> getByNamePart(String namePart, int maxItems, long skipCount)
      throws ServerException {
    LOG.info("OpenshiftUserDao#getByNamePart");
    return null;
  }

  @Override
  public Page<UserImpl> getByEmailPart(String emailPart, int maxItems, long skipCount)
      throws ServerException {
    LOG.info("OpenshiftUserDao#getByEmailPart");
    return null;
  }

  @Override
  public long getTotalCount() throws ServerException {
    LOG.info("OpenshiftUserDao#getTotalCount");
    return 0;
  }

  private UserImpl createFromOpenshiftUser(User u) {
    UserImpl user = new UserImpl();
    user.setId(u.getMetadata().getUid());
    user.setName(u.getMetadata().getName());
    user.setEmail(u.getMetadata().getName() + "@che");
    return user;
  }
}
