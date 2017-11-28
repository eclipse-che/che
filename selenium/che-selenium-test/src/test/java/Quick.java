
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Quick {
    Stream<Path> entries;
    Quick() throws IOException {
        this.entries= Files.walk(Paths.get(getClass().getResource("/suites/").getPath()));
    }
    public Stream<Path> getEntries(){
        return entries;
    }
  public static void main(String[] args) throws IOException {
    new Quick().getEntries().filter(Files::isRegularFile).forEach(System.out::println);
  }
}
