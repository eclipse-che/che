package org.eclipse.che.plugin.activity;

import java.util.List;
import org.eclipse.che.api.core.ServerException;

/**
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public interface WorkspaceActivityDao {

  void setExpiration(WorkspaceExpiration expiration) throws ServerException;

  void removeExpiration(String workspaceId) throws ServerException;

  List<WorkspaceExpiration> findExpired(long timestamp) throws ServerException;
}
