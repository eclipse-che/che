package org.eclipse.che.api.factory.server.bitbucket;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.factory.server.scm.PersonalAccessTokenFetcher;

public class BitbucketModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder<PersonalAccessTokenFetcher> tokenFetcherMultibinder =
        Multibinder.newSetBinder(binder(), PersonalAccessTokenFetcher.class);
    tokenFetcherMultibinder.addBinding().to(BitbucketServerPersonalAccessTokenFetcher.class);

  }
}