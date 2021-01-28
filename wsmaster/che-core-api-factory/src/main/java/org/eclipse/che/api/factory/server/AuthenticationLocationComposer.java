package org.eclipse.che.api.factory.server;

public interface AuthenticationLocationComposer {

  String composeLocation(String redirectAfterLogin);

}
