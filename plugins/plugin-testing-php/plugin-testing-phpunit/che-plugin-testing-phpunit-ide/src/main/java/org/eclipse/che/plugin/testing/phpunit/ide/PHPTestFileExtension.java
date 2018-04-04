package org.eclipse.che.plugin.testing.phpunit.ide;

import com.google.common.collect.Sets;
import java.util.Set;
import org.eclipse.che.plugin.testing.ide.detector.TestFileExtension;

/** Describes file extensions for PHPUnit test framework. */
public class PHPTestFileExtension implements TestFileExtension {
  private Set<String> phpExtensions;

  public PHPTestFileExtension() {
    phpExtensions = Sets.newHashSet(".php", ".phtml");
  }

  @Override
  public Set<String> getExtensions() {
    return phpExtensions;
  }
}
