package org.eclipse.che.api.factory.server.scm;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.factory.server.scm.kubernetes.KubernetesGitCredentialManager;
import org.eclipse.che.api.factory.server.scm.kubernetes.KubernetesPersonalAccessTokenManager;

public class ScmModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(GitCredentialManager.class).to(KubernetesGitCredentialManager.class);
    bind(PersonalAccessTokenManager.class).to(KubernetesPersonalAccessTokenManager.class);
  }
}