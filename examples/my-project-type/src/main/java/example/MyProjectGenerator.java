/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package example;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;

import javax.inject.Singleton;
import java.util.Map;

import static example.MyAttributes.My_PROJECT_TYPE_ID;

@Singleton
public class MyProjectGenerator implements CreateProjectHandler {

    private static final String POM_XML_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project>\n" +
            "    <modelVersion>4.0.0</modelVersion>\n" +
            "    <artifactId>console-java</artifactId>\n" +
            "    <groupId>example</groupId>\n" +
            "    <packaging>jar</packaging>\n" +
            "    <version>1.0-SNAPSHOT</version>\n" +
            "    <name>hello-app</name>\n" +
            "    <url>http://maven.apache.org</url>\n" +
            "    <dependencies>\n" +
            "        <dependency>\n" +
            "            <groupId>junit</groupId>\n" +
            "            <artifactId>junit</artifactId>\n" +
            "            <version>3.8.1</version>\n" +
            "            <scope>test</scope>\n" +
            "        </dependency>\n" +
            "    </dependencies>\n" +
            "    <build>\n" +
            "        <plugins>\n" +
            "            <plugin>\n" +
            "                <groupId>org.apache.maven.plugins</groupId>\n" +
            "                <artifactId>maven-jar-plugin</artifactId>\n" +
            "                <version>2.4</version>\n" +
            "                <configuration>\n" +
            "                    <archive>\n" +
            "                        <manifest>\n" +
            "                            <addClasspath>true</addClasspath>\n" +
            "                            <classpathPrefix>lib/</classpathPrefix>\n" +
            "                            <mainClass>example.App</mainClass>\n" +
            "                        </manifest>\n" +
            "                    </archive>\n" +
            "                </configuration>\n" +
            "            </plugin>\n" +
            "        </plugins>\n" +
            "    </build>\n" +
            "</project>";

    private static final String GITIGNORE_TEMPLATE = "# Idea #\n" +
            "##################\n" +
            "*.iml\n" +
            "*.ipr\n" +
            "*.iws\n" +
            ".idea/\n" +
            "# Compiled source #\n" +
            "###################\n" +
            "*.com\n" +
            "*.class\n" +
            "*.dll\n" +
            "*.exe\n" +
            "*.o\n" +
            "*.so\n" +
            "*.sh\n" +
            "# Packages #\n" +
            "############\n" +
            "# it's better to unpack these files and commit the raw source\n" +
            "# git has its own built in compression methods\n" +
            "*.7z\n" +
            "*.dmg\n" +
            "*.gz\n" +
            "*.iso\n" +
            "*.jar\n" +
            "*.rar\n" +
            "*.tar\n" +
            "*.zip\n" +
            "*.war\n" +
            "*.ear\n" +
            "# Logs and databases #\n" +
            "######################\n" +
            "*.log\n" +
            "*.sql\n" +
            "*.sqlite\n" +
            "# OS generated files #\n" +
            "######################\n" +
            ".DS_Store\n" +
            "ehthumbs.db\n" +
            "Icon?\n" +
            "Thumbs.db\n" +
            "*/overlays\n" +
            "*~\n" +
            "target/*";

    private static final String APP_JAVA_TEMPLATE = "package example;\n" +
            "\n" +
            "/**\n" +
            " * Hello world!\n" +
            " */\n" +
            "public class App {\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.println(\"Hello World!\");\n" +
            "    }\n" +
            "}";

    private static final String APPTEST_JAVA_TEMPLATE = "package example;\n" +
            "\n" +
            "import junit.framework.Test;\n" +
            "import junit.framework.TestCase;\n" +
            "import junit.framework.TestSuite;\n" +
            "\n" +
            "/**\n" +
            " * Unit test for simple App.\n" +
            " */\n" +
            "public class AppTest\n" +
            "        extends TestCase {\n" +
            "    /**\n" +
            "     * Create the test case\n" +
            "     *\n" +
            "     * @param testName\n" +
            "     *         name of the test case\n" +
            "     */\n" +
            "    public AppTest(String testName) {\n" +
            "        super(testName);\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * @return the suite of tests being tested\n" +
            "     */\n" +
            "    public static Test suite() {\n" +
            "        return new TestSuite(AppTest.class);\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * Rigourous Test :-)\n" +
            "     */\n" +
            "    public void testApp() {\n" +
            "        assertTrue(true);\n" +
            "    }\n" +
            "}";

    private static final String PROJECT_JSON_TEMPLATE = "{\n" +
            "  \"type\":\"" + My_PROJECT_TYPE_ID + "\",\n" +
            "  \"properties\":[\n" +
            "    {\n" +
            "      \"name\":\"builder.name\",\n" +
            "      \"value\":[\n" +
            "        \"maven\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\":\"runner.name\",\n" +
            "      \"value\":[\n" +
            "        \"java-standalone-default\"\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    @Override
    public void onCreateProject(FolderEntry baseFolder, Map<String, AttributeValue> attributes, Map<String, String> options) throws ForbiddenException, ConflictException, ServerException {
        baseFolder.createFile(".gitignore", String.format(GITIGNORE_TEMPLATE).getBytes(), "text/plain");
        baseFolder.createFile("pom.xml", String.format(POM_XML_TEMPLATE, "pom").getBytes(), "text/xml");

        baseFolder.createFolder("src/main/java/example");
        baseFolder.createFile("src/main/java/example/App.java", String.format(APP_JAVA_TEMPLATE).getBytes(), "text/java");

        baseFolder.createFolder("src/test/java/example");
        baseFolder.createFile("src/test/java/example/AppTest.java", String.format(APPTEST_JAVA_TEMPLATE).getBytes(), "text/java");

        baseFolder.createFolder(".codenvy");
        baseFolder.createFile(".codenvy/project.json", String.format(PROJECT_JSON_TEMPLATE).getBytes(), "application/json");
    }

    @Override
    public String getProjectType() {
        return My_PROJECT_TYPE_ID;
    }
}
