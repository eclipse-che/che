/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver.dto;

import com.google.gson.JsonElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.ExtendedCompletionItemDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.ExtendedCompletionListDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.HoverDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.ParameterInformationDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.WorkspaceEditDto;
import org.eclipse.che.api.languageserver.shared.model.ExtendedCompletionItem;
import org.eclipse.che.api.languageserver.shared.model.ExtendedCompletionList;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DtoConversionTest {

  @Test
  public void testListConversion() {
    ExtendedCompletionList ecl = new ExtendedCompletionList();
    ecl.setInComplete(true);
    List<ExtendedCompletionItem> items = new ArrayList<>();
    ExtendedCompletionItem item = new ExtendedCompletionItemDto();
    item.setItem(new CompletionItem());
    item.getItem()
        .setTextEdit(
            new TextEdit(new Range(new Position(1, 2), new Position(3, 4)), "changed text"));

    // cannot unmarshal object type stuff from json. So need to set json
    // element for equality test to work.
    item.getItem()
        .setData(
            new ParameterInformationDto(new ParameterInformation("the label", "the doc"))
                .toJsonElement());
    items.add(item);
    ecl.setItems(items);

    ExtendedCompletionListDto originalDto = new ExtendedCompletionListDto(ecl);
    Assert.assertTrue(reflectionEquals(originalDto, ecl));
    String jsonString = originalDto.toJson();
    ExtendedCompletionListDto convertedDto = ExtendedCompletionListDto.fromJson(jsonString);
    Assert.assertTrue(reflectionEquals(originalDto, convertedDto));
  }

  @Test
  public void testMapConversion() {
    Map<String, List<TextEdit>> changes =
        Collections.singletonMap(
            "anURL",
            Arrays.asList(
                new TextEdit(new Range(new Position(0, 1), new Position(3, 4)), "blabla")));
    WorkspaceEdit edit = new WorkspaceEdit();
    edit.setChanges(changes);

    WorkspaceEditDto originalDto = new WorkspaceEditDto(edit);
    Assert.assertTrue(reflectionEquals(originalDto, edit));
    String jsonString = originalDto.toJson();
    WorkspaceEditDto convertedDto = WorkspaceEditDto.fromJson(jsonString);
    Assert.assertTrue(reflectionEquals(originalDto, convertedDto));
  }

  @Test
  public void testDocumentFormattingParamsDeserializerWithGson() throws Exception {
    FormattingOptions formattingOptions = new FormattingOptions(4, true);
    TextDocumentIdentifier textDocument =
        DtoFactory.getInstance().createDto(TextDocumentIdentifier.class);
    textDocument.setUri("/console-java-simple/src/main/java/org/eclipse/che/examples/A.java");
    DocumentFormattingParams documentFormattingParams =
        DtoFactory.getInstance().createDto(DocumentFormattingParams.class);
    documentFormattingParams.setOptions(formattingOptions);
    documentFormattingParams.setTextDocument(textDocument);
    JsonElement json = DtoFactory.getInstance().toJsonElement(documentFormattingParams);

    DocumentFormattingParams params =
        DtoFactory.getInstance().createDtoFromJson(json.toString(), DocumentFormattingParams.class);

    Assert.assertTrue(params.getOptions().isInsertSpaces());
  }

  @Test
  public void testDocumentFormattingParamsDeserializerWithDto() throws Exception {
    FormattingOptions formattingOptions = new FormattingOptions(4, true);
    TextDocumentIdentifier textDocument =
        DtoFactory.getInstance().createDto(TextDocumentIdentifier.class);
    textDocument.setUri("/console-java-simple/src/main/java/org/eclipse/che/examples/A.java");
    DocumentFormattingParams documentFormattingParams =
        DtoFactory.getInstance().createDto(DocumentFormattingParams.class);
    documentFormattingParams.setOptions(formattingOptions);
    documentFormattingParams.setTextDocument(textDocument);
    JsonElement json = DtoFactory.getInstance().toJsonElement(documentFormattingParams);

    DocumentFormattingParams params =
        DtoFactory.getInstance().createDtoFromJson(json, DocumentFormattingParams.class);

    Assert.assertTrue(params.getOptions().isInsertSpaces());
  }

  /** Test the Either conversion by using a Hover object. */
  @Test
  public void testEitherDeserializerWithDto() throws Exception {
    Hover hover = DtoFactory.getInstance().createDto(Hover.class);
    MarkedString markedString = DtoFactory.getInstance().createDto(MarkedString.class);
    markedString.setLanguage("Language");
    markedString.setValue("Value");
    List<Either<String, MarkedString>> list = new ArrayList<>();
    list.add(Either.forRight(markedString));
    list.add(Either.forLeft("normal String"));
    hover.setContents(list);

    JsonElement json = DtoFactory.getInstance().toJsonElement(hover);

    Hover params = DtoFactory.getInstance().createDtoFromJson(json, Hover.class);

    Assert.assertTrue(params.getContents().get(0).isRight());
    Assert.assertEquals("Value", params.getContents().get(0).getRight().getValue());
    Assert.assertEquals("Language", params.getContents().get(0).getRight().getLanguage());
    Assert.assertTrue(params.getContents().get(1).isLeft());
    Assert.assertEquals("normal String", params.getContents().get(1).getLeft());
  }

  /** Test the Either conversion by using a Hover object. */
  @Test
  public void testEitherDeserializerWithGson() throws Exception {
    Hover hover = DtoFactory.getInstance().createDto(Hover.class);
    MarkedString markedString = DtoFactory.getInstance().createDto(MarkedString.class);
    markedString.setLanguage("Language");
    markedString.setValue("Value");
    List<Either<String, MarkedString>> list = new ArrayList<>();
    list.add(Either.forRight(markedString));
    list.add(Either.forLeft("normal String"));
    hover.setContents(list);

    JsonElement json = DtoFactory.getInstance().toJsonElement(hover);

    Hover params = DtoFactory.getInstance().createDtoFromJson(json.toString(), Hover.class);

    Assert.assertTrue(params.getContents().get(0).isRight());
    Assert.assertEquals("Value", params.getContents().get(0).getRight().getValue());
    Assert.assertEquals("Language", params.getContents().get(0).getRight().getLanguage());
    Assert.assertTrue(params.getContents().get(1).isLeft());
    Assert.assertEquals("normal String", params.getContents().get(1).getLeft());
  }

  @Test
  public void testEitherConversion() {
    Either<String, MarkedString> either1 = Either.forLeft("foobar");
    Either<String, MarkedString> either2 = Either.forLeft("bar");
    List<Either<String, MarkedString>> entries = Arrays.asList(either1, either2);
    Hover hover = new Hover(entries, new Range(new Position(0, 1), new Position(2, 3)));

    HoverDto originalDto = new HoverDto(hover);
    Assert.assertTrue(reflectionEquals(originalDto, hover));
    String jsonString = originalDto.toJson();
    HoverDto convertedDto = HoverDto.fromJson(jsonString);
    Assert.assertTrue(reflectionEquals(originalDto, convertedDto));
  }

  /*
   * This method compares objects reflectively. This probably won't work in general, because of ordering problems in collections
   */
  private boolean reflectionEquals(Object left, Object right) {
    Class<?> leftClass = left.getClass();
    Class<?> rightClass = right.getClass();
    return reflectionEquals(left, right, leftClass, rightClass);
  }

  private boolean reflectionEquals(
      Object left, Object right, Class<?> leftClass, Class<?> rightClass) {
    if (left == right) {
      return true;
    }
    if (left == null || right == null) {
      return false;
    }

    if (leftClass.isPrimitive() || rightClass.isPrimitive()) {
      return left.equals(right);
    }
    if (leftClass.isArray()) {
      if (!rightClass.isArray()) {
        return false;
      }
      return compareArrays(left, right);
    }
    while (leftClass != null) {
      Field[] fields = leftClass.getDeclaredFields();
      Field.setAccessible(fields, true);
      for (Field field : fields) {
        try {
          if (!field.isSynthetic() && !Modifier.isStatic(field.getModifiers())) {
            if (!reflectionEquals(
                field.get(left), field.get(right), field.getType(), field.getType())) {
              return false;
            }
          }
        } catch (IllegalArgumentException e) {
          return false;
        } catch (IllegalAccessException e) {
          throw new RuntimeException("should not happen", e);
        }
      }
      leftClass = leftClass.getSuperclass();
    }
    return true;
  }

  private boolean compareArrays(Object left, Object right) {
    int size = Array.getLength(left);
    if (size != Array.getLength(right)) {
      return false;
    }
    for (int i = 0; i < size; i++) {
      if (!reflectionEquals(
          Array.get(left, i),
          Array.get(right, i),
          left.getClass().getComponentType(),
          right.getClass().getComponentType())) {
        return false;
      }
    }
    return true;
  }
}
