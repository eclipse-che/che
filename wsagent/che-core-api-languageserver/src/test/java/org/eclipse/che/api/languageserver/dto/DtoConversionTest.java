/*******************************************************************************
 * Copyright (c) 2017 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.che.api.languageserver.dto;

import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.ExtendedCompletionItemDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.ExtendedCompletionListDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.HoverDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.ParameterInformationDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.WorkspaceEditDto;
import org.eclipse.che.api.languageserver.shared.model.ExtendedCompletionItem;
import org.eclipse.che.api.languageserver.shared.model.ExtendedCompletionList;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DtoConversionTest {

    @Test
    public void testListConversion() {
        ExtendedCompletionList ecl = new ExtendedCompletionList();
        ecl.setInComplete(true);
        List<ExtendedCompletionItem> items = new ArrayList<>();
        ExtendedCompletionItem item = new ExtendedCompletionItemDto();
        item.setItem(new CompletionItem());
        item.getItem().setTextEdit(new TextEdit(new Range(new Position(1, 2), new Position(3, 4)), "changed text"));

        // cannot unmarshal object type stuff from json. So need to set json
        // element for equality test to work.
        item.getItem().setData(new ParameterInformationDto(new ParameterInformation("the label", "the doc")).toJsonElement());
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
        Map<String, List<TextEdit>> changes = Collections.singletonMap("anURL",
                                                                       Arrays.asList(new TextEdit(
                                                                               new Range(new Position(0, 1), new Position(3, 4)),
                                                                               "blabla")));
        WorkspaceEdit edit = new WorkspaceEdit();
        edit.setChanges(changes);

        WorkspaceEditDto originalDto = new WorkspaceEditDto(edit);
        Assert.assertTrue(reflectionEquals(originalDto, edit));
        String jsonString = originalDto.toJson();
        WorkspaceEditDto convertedDto = WorkspaceEditDto.fromJson(jsonString);
        Assert.assertTrue(reflectionEquals(originalDto, convertedDto));
    }

    @Test
    public void testEitherConversion() {
        List<String> entries = Arrays.asList("foobar", "bar");
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

    private boolean reflectionEquals(Object left, Object right, Class<?> leftClass, Class<?> rightClass) {
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
                        if (!reflectionEquals(field.get(left), field.get(right), field.getType(), field.getType())) {
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
            if (!reflectionEquals(Array.get(left, i), Array.get(right, i), left.getClass().getComponentType(),
                                  right.getClass().getComponentType())) {
                return false;
            }
        }
        return true;
    }
}
