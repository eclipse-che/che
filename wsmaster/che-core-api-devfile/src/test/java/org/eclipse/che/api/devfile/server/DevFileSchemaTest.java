package org.eclipse.che.api.devfile.server;

import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

public class DevFileSchemaTest {

  private JsonValidator validator;
  private ObjectMapper yamlReader;

  @BeforeClass
  public void setUp() {
    final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
    validator = factory.getValidator();
    yamlReader = new ObjectMapper(new YAMLFactory());
  }

  @Test
  public void shouldValidateSchema() throws Exception {

    String devFileYamlContent = Files
        .readFile(getClass().getClassLoader().getResourceAsStream("devfile.yaml"));
    String devFileSchemaContent = Files
        .readFile(getClass().getClassLoader().getResourceAsStream("devfile_schema.json"));

    final JsonNode data = yamlReader.readTree(devFileYamlContent);
    final JsonNode schema = JsonLoader.fromString(devFileSchemaContent);
    // when
    ProcessingReport report = validator.validate(schema, data);
    // then
    assertTrue(report.isSuccess());
  }
}
