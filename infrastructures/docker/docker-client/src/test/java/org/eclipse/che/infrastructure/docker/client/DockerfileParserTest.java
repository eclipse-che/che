/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import org.eclipse.che.commons.lang.Pair;
import org.testng.annotations.Test;

/** @author andrew00x */
public class DockerfileParserTest {
  @Test
  public void testParse() throws Exception {
    String dockerfileContent =
        "FROM base_image\n"
            + "# Comment 1\n"
            + "MAINTAINER Codenvy Corp\n"
            + "# Comment 2\n"
            + "RUN echo 1 > /dev/null\n"
            + "RUN echo 2 > /dev/null\n"
            + "RUN echo 3 > /dev/null\n"
            + "ADD file1 /tmp/file1\n"
            + "ADD http://example.com/folder/some_file.txt /tmp/file.txt  \n"
            + "EXPOSE 6000 7000\n"
            + "EXPOSE 8000   9000\n"
            + "# Comment 3\n"
            + "ENV ENV_VAR1 hello world\n"
            + "ENV ENV_VAR2\t to be or not to be\n"
            + "VOLUME [\"/data1\", \t\"/data2\"]\n"
            + "USER andrew\n"
            + "WORKDIR /tmp\n"
            + "ENTRYPOINT echo hello > /dev/null\n"
            + "CMD echo hello > /tmp/test";
    File targetDir =
        new File(Thread.currentThread().getContextClassLoader().getResource(".").toURI())
            .getParentFile();
    File file = new File(targetDir, "testParse");
    FileWriter w = new FileWriter(file);
    w.write(dockerfileContent);
    w.flush();
    w.close();
    List<DockerImage> dockerImages = DockerfileParser.parse(file).getImages();
    assertEquals(dockerImages.size(), 1);
    DockerImage dockerImage = dockerImages.get(0);
    assertEquals(dockerImage.getFrom(), "base_image");
    assertEquals(dockerImage.getMaintainer(), Arrays.asList("Codenvy Corp"));
    assertEquals(
        dockerImage.getRun(),
        Arrays.asList("echo 1 > /dev/null", "echo 2 > /dev/null", "echo 3 > /dev/null"));
    assertEquals(dockerImage.getCmd(), "echo hello > /tmp/test");
    assertEquals(dockerImage.getExpose(), Arrays.asList("6000", "7000", "8000", "9000"));
    Map<String, String> env = new LinkedHashMap<>();
    env.put("ENV_VAR1", "hello world");
    env.put("ENV_VAR2", "to be or not to be");
    assertEquals(env, dockerImage.getEnv());
    assertEquals(
        dockerImage.getAdd(),
        Arrays.asList(
            Pair.of("file1", "/tmp/file1"),
            Pair.of("http://example.com/folder/some_file.txt", "/tmp/file.txt")));
    assertEquals(dockerImage.getEntrypoint(), "echo hello > /dev/null");
    assertEquals(dockerImage.getVolume(), Arrays.asList("/data1", "/data2"));
    assertEquals(dockerImage.getUser(), "andrew");
    assertEquals(dockerImage.getWorkdir(), "/tmp");
    assertEquals(dockerImage.getComments(), Arrays.asList("Comment 1", "Comment 2", "Comment 3"));
  }

  @Test
  public void testParseMultipleImages() throws Exception {
    String dockerfileContent =
        "FROM base_image_1\n"
            + "# Image 1\n"
            + "MAINTAINER Codenvy Corp\n"
            + "RUN echo 1 > /dev/null\n"
            + "ADD http://example.com/folder/some_file.txt /tmp/file.txt  \n"
            + "EXPOSE 6000 7000\n"
            + "ENV ENV_VAR\t to be or not to be\n"
            + "VOLUME [\"/data1\"]\n"
            + "USER andrew\n"
            + "WORKDIR /tmp\n"
            + "ENTRYPOINT echo hello > /dev/null\n"
            + "CMD echo hello > /tmp/test1"
            + "\n"
            + "\n"
            + "FROM base_image_2\n"
            + "# Image 2\n"
            + "MAINTAINER Codenvy Corp\n"
            + "RUN echo 2 > /dev/null\n"
            + "ADD file1 /tmp/file1\n"
            + "EXPOSE 8000 9000\n"
            + "ENV ENV_VAR\t to be or not to be\n"
            + "VOLUME [\"/data2\"]\n"
            + "USER andrew\n"
            + "WORKDIR /home/andrew\n"
            + "ENTRYPOINT echo test > /dev/null\n"
            + "CMD echo hello > /tmp/test2";
    File targetDir =
        new File(Thread.currentThread().getContextClassLoader().getResource(".").toURI())
            .getParentFile();
    File file = new File(targetDir, "testParse");
    FileWriter w = new FileWriter(file);
    w.write(dockerfileContent);
    w.flush();
    w.close();
    List<DockerImage> dockerImages = DockerfileParser.parse(file).getImages();
    assertEquals(2, dockerImages.size());
    DockerImage dockerImage1 = dockerImages.get(0);
    assertEquals(dockerImage1.getFrom(), "base_image_1");
    assertEquals(dockerImage1.getMaintainer(), Arrays.asList("Codenvy Corp"));
    assertEquals(dockerImage1.getRun(), Arrays.asList("echo 1 > /dev/null"));
    assertEquals(dockerImage1.getCmd(), "echo hello > /tmp/test1");
    assertEquals(dockerImage1.getExpose(), Arrays.asList("6000", "7000"));
    Map<String, String> env1 = new LinkedHashMap<>();
    env1.put("ENV_VAR", "to be or not to be");
    assertEquals(dockerImage1.getEnv(), env1);
    assertEquals(
        dockerImage1.getAdd(),
        Arrays.asList(Pair.of("http://example.com/folder/some_file.txt", "/tmp/file.txt")));
    assertEquals(dockerImage1.getEntrypoint(), "echo hello > /dev/null");
    assertEquals(dockerImage1.getVolume(), Arrays.asList("/data1"));
    assertEquals(dockerImage1.getUser(), "andrew");
    assertEquals(dockerImage1.getWorkdir(), "/tmp");
    assertEquals(dockerImage1.getComments(), Arrays.asList("Image 1"));

    DockerImage dockerImage2 = dockerImages.get(1);
    assertEquals(dockerImage2.getFrom(), "base_image_2");
    assertEquals(dockerImage2.getMaintainer(), Arrays.asList("Codenvy Corp"));
    assertEquals(dockerImage2.getRun(), Arrays.asList("echo 2 > /dev/null"));
    assertEquals(dockerImage2.getCmd(), "echo hello > /tmp/test2");
    assertEquals(dockerImage2.getExpose(), Arrays.asList("8000", "9000"));
    Map<String, String> env2 = new LinkedHashMap<>();
    env2.put("ENV_VAR", "to be or not to be");
    assertEquals(dockerImage2.getEnv(), env2);
    assertEquals(dockerImage2.getAdd(), Arrays.asList(Pair.of("file1", "/tmp/file1")));
    assertEquals(dockerImage2.getEntrypoint(), "echo test > /dev/null");
    assertEquals(dockerImage2.getVolume(), Arrays.asList("/data2"));
    assertEquals(dockerImage2.getUser(), "andrew");
    assertEquals(dockerImage2.getWorkdir(), "/home/andrew");
    assertEquals(dockerImage2.getComments(), Arrays.asList("Image 2"));
  }

  @Test
  public void testTemplate() throws Exception {
    String templateContent =
        "FROM $from$\n"
            + "MAINTAINER Codenvy Corp\n"
            + "ADD $app$ /tmp/$app$\n"
            + "CMD /bin/bash -cl \"java $jvm_args$ -classpath /tmp/$app$ $main_class$ $prg_args$\"\n";
    String expectedContent =
        "FROM base\n"
            + "MAINTAINER Codenvy Corp\n"
            + "ADD hello.jar /tmp/hello.jar\n"
            + "CMD /bin/bash -cl \"java -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n -classpath /tmp/hello.jar test.Main name=andrew\"\n";
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("from", "base");
    parameters.put("app", "hello.jar");
    parameters.put(
        "jvm_args", "-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n");
    parameters.put("main_class", "test.Main");
    parameters.put("prg_args", "name=andrew");
    writeDockerfileAndMatch(templateContent, parameters, expectedContent);
  }

  /**
   * Ensures that we're able to find and replace parameters with default values
   *
   * @throws Exception
   */
  @Test
  public void testDefaultValueInTemplate() throws Exception {
    final String TASKNAME_KEY = "taskName";
    final String TASKNAME_DEFAULT_VALUE = "server";
    final String TASKNAME_VALUE = "test";
    String templateContent =
        "FROM codenvy/mytemplatetest\n"
            + "grunt $"
            + TASKNAME_KEY
            + ":-"
            + TASKNAME_DEFAULT_VALUE
            + "$\n";
    // first case, parameter is not given so we are expecting the default value
    String expectedWithoutParameter = "FROM codenvy/mytemplatetest\n" + "grunt server\n";
    // second case, parameter is given so we are expecting the given value
    String expectedWithParameter =
        "FROM codenvy/mytemplatetest\n" + "grunt " + TASKNAME_VALUE + "\n";
    writeDockerfileAndMatch(templateContent, null, expectedWithoutParameter);
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("taskName", TASKNAME_VALUE);
    writeDockerfileAndMatch(templateContent, parameters, expectedWithParameter);
  }

  /**
   * Try with invalid syntax for default Here there is the missing - after : like
   * $myKey:wrongDefaultValue$
   */
  @Test
  public void testErrorInDefaultValueInTemplate() throws Exception {
    final String TASKNAME_KEY = "taskName";
    final String TASKNAME_DEFAULT_VALUE = "server";
    final String TASKNAME_VALUE = "test";
    String templateContent =
        "FROM codenvy/mytemplatetest\n"
            + "grunt $"
            + TASKNAME_KEY
            + ":"
            + TASKNAME_DEFAULT_VALUE
            + "$\n";
    // We shouldn't have exceptions
    // first case, parameter is not given so we are expecting the default value
    String expectedContent = "FROM codenvy/mytemplatetest\n" + "grunt $taskName:server$\n";
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("taskName", TASKNAME_VALUE);
    writeDockerfileAndMatch(templateContent, parameters, expectedContent);
  }

  @Test
  public void testConditionPatternBooleanCondition() {
    String conditionTemplate = "aaa?bbb:ccc";
    Matcher m = Dockerfile.TEMPLATE_CONDITIONAL_PATTERN.matcher(conditionTemplate);
    assertTrue(m.matches());
    assertEquals(m.groupCount(), 5);
    assertEquals(m.group(1), "aaa");
    assertNull(m.group(2));
    assertEquals(m.group(3), "");
    assertEquals(m.group(4), "bbb");
    assertEquals(m.group(5), "ccc");
  }

  @Test
  public void testConditionPatternEmptyCondition() {
    String conditionTemplate = "aaa=?bbb:ccc";
    Matcher m = Dockerfile.TEMPLATE_CONDITIONAL_PATTERN.matcher(conditionTemplate);
    assertTrue(m.matches());
    assertEquals(m.groupCount(), 5);
    assertEquals(m.group(1), "aaa");
    assertEquals(m.group(2), "=");
    assertEquals(m.group(3), "");
    assertEquals(m.group(4), "bbb");
    assertEquals(m.group(5), "ccc");
  }

  @Test
  public void testConditionPatternEmptyCondition2() {
    String conditionTemplate = "aaa>=?bbb:ccc";
    Matcher m = Dockerfile.TEMPLATE_CONDITIONAL_PATTERN.matcher(conditionTemplate);
    assertTrue(m.matches());
    assertEquals(m.groupCount(), 5);
    assertEquals(m.group(1), "aaa");
    assertEquals(m.group(2), ">=");
    assertEquals(m.group(3), "");
    assertEquals(m.group(4), "bbb");
    assertEquals(m.group(5), "ccc");
  }

  @Test
  public void testConditionPattern() {
    String conditionTemplate = "aaa>=xxx?bbb:ccc";
    Matcher m = Dockerfile.TEMPLATE_CONDITIONAL_PATTERN.matcher(conditionTemplate);
    assertTrue(m.matches());
    assertEquals(m.groupCount(), 5);
    assertEquals(m.group(1), "aaa");
    assertEquals(m.group(2), ">=");
    assertEquals(m.group(3), "xxx");
    assertEquals(m.group(4), "bbb");
    assertEquals(m.group(5), "ccc");
  }

  @Test
  public void testConditionPatternNoExpression1() {
    String conditionTemplate = "aaa>=xxx?:ccc";
    Matcher m = Dockerfile.TEMPLATE_CONDITIONAL_PATTERN.matcher(conditionTemplate);
    assertTrue(m.matches());
    assertEquals(m.groupCount(), 5);
    assertEquals(m.group(1), "aaa");
    assertEquals(m.group(2), ">=");
    assertEquals(m.group(3), "xxx");
    assertEquals(m.group(4), "");
    assertEquals(m.group(5), "ccc");
  }

  @Test
  public void testConditionPatternNoExpression2() {
    String conditionTemplate = "aaa>=xxx?bbb:";
    Matcher m = Dockerfile.TEMPLATE_CONDITIONAL_PATTERN.matcher(conditionTemplate);
    assertTrue(m.matches());
    assertEquals(m.groupCount(), 5);
    assertEquals(m.group(1), "aaa");
    assertEquals(m.group(2), ">=");
    assertEquals(m.group(3), "xxx");
    assertEquals(m.group(4), "bbb");
    assertEquals(m.group(5), "");
  }

  @Test
  public void testConditionPatternEmptyConditionAndNoExpression1() {
    String conditionTemplate = "aaa>=?:ccc";
    Matcher m = Dockerfile.TEMPLATE_CONDITIONAL_PATTERN.matcher(conditionTemplate);
    assertTrue(m.matches());
    assertEquals(m.groupCount(), 5);
    assertEquals(m.group(1), "aaa");
    assertEquals(m.group(2), ">=");
    assertEquals(m.group(4), "");
    assertEquals(m.group(5), "ccc");
  }

  @Test
  public void testConditionPatternEmptyConditionAndNoExpression2() {
    String conditionTemplate = "aaa=?bbb:";
    Matcher m = Dockerfile.TEMPLATE_CONDITIONAL_PATTERN.matcher(conditionTemplate);
    assertTrue(m.matches());
    assertEquals(m.groupCount(), 5);
    assertEquals(m.group(1), "aaa");
    assertEquals(m.group(2), "=");
    assertEquals(m.group(3), "");
    assertEquals(m.group(4), "bbb");
    assertEquals(m.group(5), "");
  }

  @Test
  public void testConditionPatternBooleanConditionAndNoExpression1() {
    String conditionTemplate = "aaa?:ccc";
    Matcher m = Dockerfile.TEMPLATE_CONDITIONAL_PATTERN.matcher(conditionTemplate);
    assertTrue(m.matches());
    assertEquals(m.groupCount(), 5);
    assertEquals(m.group(1), "aaa");
    assertNull(m.group(2));
    assertEquals(m.group(4), "");
    assertEquals(m.group(5), "ccc");
  }

  @Test
  public void testConditionPatternBooleanConditionAndNoExpression2() {
    String conditionTemplate = "aaa?bbb:";
    Matcher m = Dockerfile.TEMPLATE_CONDITIONAL_PATTERN.matcher(conditionTemplate);
    assertTrue(m.matches());
    assertEquals(m.groupCount(), 5);
    assertEquals(m.group(1), "aaa");
    assertEquals(m.group(2), null);
    assertEquals(m.group(3), "");
    assertEquals(m.group(4), "bbb");
    assertEquals(m.group(5), "");
  }

  @Test
  public void testBooleanConditionTemplateMatched() throws Exception {
    String templateContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java $debug?-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n:$ test.Main\"\n";
    String expectedContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n test.Main\"\n";
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("debug", true);
    writeDockerfileAndMatch(templateContent, parameters, expectedContent);
  }

  @Test
  public void testBooleanConditionTemplateNotMatched() throws Exception {
    String templateContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java $debug?-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n:$ test.Main\"\n";
    String expectedContent = "FROM base\n" + "CMD /bin/bash -cl \"java  test.Main\"\n";
    writeDockerfileAndMatch(templateContent, null, expectedContent);
  }

  @Test
  public void testBooleanConditionTemplateEmptyOutput() throws Exception {
    String templateContent =
        "$debug?EXPOSE 8000:$\n"
            + "$debug?ENV CODENVY_APP_PORT_8000_DEBUG 8000:$\n"
            + "$debug?CMD ./catalina.sh jpda run 2>&1:$\n";
    String expectedContent = "";
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("debug", false);
    writeDockerfileAndMatch(templateContent, parameters, expectedContent);
  }

  @Test
  public void testEqualsConditionTemplateMatched() throws Exception {
    String templateContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java $debug=y?-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n:$ test.Main\"\n";
    String expectedContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n test.Main\"\n";
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("debug", "y");
    writeDockerfileAndMatch(templateContent, parameters, expectedContent);
  }

  @Test
  public void testEqualsConditionTemplateNotMatched() throws Exception {
    String templateContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java $debug=y?-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n:$ test.Main\"\n";
    String expectedContent = "FROM base\n" + "CMD /bin/bash -cl \"java  test.Main\"\n";
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("debug", "n");
    writeDockerfileAndMatch(templateContent, parameters, expectedContent);
  }

  @Test
  public void testNotEqualsConditionTemplateMatched() throws Exception {
    String templateContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java $debug!=n?-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n:$ test.Main\"\n";
    String expectedContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n test.Main\"\n";
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("debug", "yeeeeesssss");
    writeDockerfileAndMatch(templateContent, parameters, expectedContent);
  }

  @Test
  public void testNotEqualsConditionTemplateNotMatched() throws Exception {
    String templateContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java $debug!=n?-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n:$ test.Main\"\n";
    String expectedContent = "FROM base\n" + "CMD /bin/bash -cl \"java  test.Main\"\n";
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("debug", "n");
    writeDockerfileAndMatch(templateContent, parameters, expectedContent);
  }

  @Test
  public void testNumberMoreConditionTemplateMatched() throws Exception {
    String templateContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java $debug>123?-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n:$ test.Main\"\n";
    String expectedContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n test.Main\"\n";
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("debug", 127);
    writeDockerfileAndMatch(templateContent, parameters, expectedContent);
  }

  @Test
  public void testNumberMoreConditionTemplateNotMatched() throws Exception {
    String templateContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java $debug>132?-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n:$ test.Main\"\n";
    String expectedContent = "FROM base\n" + "CMD /bin/bash -cl \"java  test.Main\"\n";
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("debug", 127);
    writeDockerfileAndMatch(templateContent, parameters, expectedContent);
  }

  @Test
  public void testInvalidNumberMoreConditionTemplateNotMatched() throws Exception {
    String templateContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java $debug>test?-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n:$ test.Main\"\n";
    String expectedContent = "FROM base\n" + "CMD /bin/bash -cl \"java  test.Main\"\n";
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("debug", 127);
    writeDockerfileAndMatch(templateContent, parameters, expectedContent);
  }

  @Test
  public void testNumberMoreEqualsConditionTemplateMatched() throws Exception {
    String templateContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java $debug>=123?-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n:$ test.Main\"\n";
    String expectedContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n test.Main\"\n";
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("debug", 123);
    writeDockerfileAndMatch(templateContent, parameters, expectedContent);
  }

  @Test
  public void testNumberMoreEqualsConditionTemplateNotMatched() throws Exception {
    String templateContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java $debug>=123?-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n:$ test.Main\"\n";
    String expectedContent = "FROM base\n" + "CMD /bin/bash -cl \"java  test.Main\"\n";
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("debug", 111);
    writeDockerfileAndMatch(templateContent, parameters, expectedContent);
  }

  @Test
  public void testNumberLessConditionTemplateMatched() throws Exception {
    String templateContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java $debug<123?-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n:$ test.Main\"\n";
    String expectedContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n test.Main\"\n";
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("debug", 121);
    writeDockerfileAndMatch(templateContent, parameters, expectedContent);
  }

  @Test
  public void testNumberLessConditionTemplateNotMatched() throws Exception {
    String templateContent =
        "FROM base\n"
            + "CMD /bin/bash -cl \"java $debug<123?-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n:$ test.Main\"\n";
    String expectedContent = "FROM base\n" + "CMD /bin/bash -cl \"java  test.Main\"\n";
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("debug", 123);
    writeDockerfileAndMatch(templateContent, parameters, expectedContent);
  }

  private void writeDockerfileAndMatch(
      String templateContent, Map<String, Object> parameters, String expectedContent)
      throws Exception {
    Dockerfile template = DockerfileParser.parse(templateContent);
    StringBuilder buf = new StringBuilder();
    if (parameters != null) {
      template.getParameters().putAll(parameters);
    }
    template.writeDockerfile(buf);
    assertEquals(buf.toString(), expectedContent);
  }
}
