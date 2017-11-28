import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.eclipse.che.commons.lang.NameGenerator;
import org.kohsuke.github.GitHub;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ExperementalGithub {

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  private GitHub gitHub;

  @BeforeClass
  public void setUp() throws IOException {
    this.gitHub = GitHub.connectUsingPassword(gitHubUsername, gitHubPassword).getRepository("").fork();
  }

  @Test
  public void checkConnection() throws IOException {
    byte[] array = Files.readAllBytes(Paths.get(getClass().getResource("/projects/lib/README").getPath()));
    String currentNameRepo = NameGenerator.generate(ExperementalGithub.class.getSimpleName(), 3);
    gitHub.get
    gitHub.getRepository(gitHubUsername + "/" + currentNameRepo).createContent(array, "add new content", "add.readme");
    gitHub.getRepository(gitHubUsername + "/" + currentNameRepo).delete();
  }
}
