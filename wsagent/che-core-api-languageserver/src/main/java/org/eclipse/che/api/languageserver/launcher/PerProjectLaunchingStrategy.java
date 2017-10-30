package org.eclipse.che.api.languageserver.launcher;

import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.service.LanguageServiceUtils;
import org.eclipse.che.api.vfs.Path;

public class PerProjectLaunchingStrategy implements LaunchingStrategy {
  public static final PerProjectLaunchingStrategy INSTANCE = new PerProjectLaunchingStrategy();

  private PerProjectLaunchingStrategy() {}

  @Override
  public String getLaunchKey(String fileUri) {
    String path = LanguageServiceUtils.removePrefixUri(fileUri);
    return Path.of(path).element(0);
  }

  @Override
  public boolean isApplicable(String launchKey, String fileUri) {
    String path = LanguageServiceUtils.removePrefixUri(fileUri);
    String project = Path.of(path).element(0);
    return project.equals(launchKey);
  }

  @Override
  public String getRootUri(String fileUri) throws LanguageServerException {
    return LanguageServiceUtils.extractProjectPath(fileUri);
  }
}
