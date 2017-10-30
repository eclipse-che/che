package org.eclipse.che.api.languageserver.launcher;

import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.service.LanguageServiceUtils;

public class PerWorkspaceLaunchingStrategy implements LaunchingStrategy {
  public static final PerWorkspaceLaunchingStrategy INSTANCE = new PerWorkspaceLaunchingStrategy();

  private PerWorkspaceLaunchingStrategy() {}

  @Override
  public String getLaunchKey(String fileUri) {
    return "";
  }

  @Override
  public boolean isApplicable(String launchKey, String fileUri) {
    return true;
  }

  @Override
  public String getRootUri(String fileUri) throws LanguageServerException {
    return LanguageServiceUtils.prefixURI("/");
  }
}
