/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.project.server.type;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.InvalidValueException;
import org.eclipse.che.api.project.server.ProjectApiModule;
import org.eclipse.che.api.project.server.ProjectTypeService;
import org.eclipse.che.api.project.server.ValueProvider;
import org.eclipse.che.api.project.server.ValueProviderFactory;
import org.eclipse.che.api.project.server.ValueStorageException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author gazarenkov
 */
public class ProjectTypeTest {

    Injector injector;

    @Before
    public void setUp() throws Exception {
        // Bind components
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {

                install(new ProjectApiModule());

                Multibinder<ValueProviderFactory> valueProviderMultibinder = Multibinder.newSetBinder(binder(), ValueProviderFactory.class);
                valueProviderMultibinder.addBinding().to(MyVPFactory.class);

                Multibinder<ProjectTypeDef> projectTypesMultibinder = Multibinder.newSetBinder(binder(), ProjectTypeDef.class);
                projectTypesMultibinder.addBinding().to(MyProjectType.class);

                bind(ProjectTypeRegistry.class);
            }
        });
    }

    @Test
    public void testProjectTypeService() throws Exception {
        ProjectTypeRegistry registry = injector.getInstance(ProjectTypeRegistry.class);

        ProjectTypeService service = new ProjectTypeService(registry);

        assertEquals(2, service.getProjectTypes().size());
    }

    @Test
    public void testProjectTypeDefinition() throws Exception {
        ProjectTypeRegistry registry = injector.getInstance(ProjectTypeRegistry.class);

        ProjectTypeDef type = registry.getProjectType("my");

        assertNotNull(type);
        assertEquals(1, type.getParents().size());
        assertEquals(BaseProjectType.ID, type.getParents().get(0));
        assertNotNull(((Variable)type.getAttribute("var")).getValueProviderFactory());
        Assert.assertNull(type.getAttribute("var").getValue());
        assertEquals(3, type.getAttributes().size());
        assertNotNull(type.getAttribute("const"));
        assertEquals(new AttributeValue("const_value"), type.getAttribute("const").getValue());
        assertEquals(new AttributeValue("value"), type.getAttribute("var1").getValue());
        Assert.assertTrue(type.getAttribute("var1").isRequired());
        Assert.assertTrue(type.getAttribute("var1").isVariable());
        Assert.assertFalse(type.getAttribute("const").isVariable());
    }

    @Test
    public void testInvalidPTDefinition() throws Exception {
        ProjectTypeDef pt = new ProjectTypeDef("my", "second", true, false) {
        };

        Set<ProjectTypeDef> pts = new HashSet<>();
        pts.add(new MyProjectType(null));
        pts.add(pt);
        ProjectTypeRegistry reg = new ProjectTypeRegistry(pts);

        // BASE and MY (
        assertEquals(2, reg.getProjectTypes().size());

        // Invalid names
        pts.clear();
        pts.add(new ProjectTypeDef(null, "null id", true, false) {
        });
        pts.add(new ProjectTypeDef("", "empty id", true, false) {
        });
        pts.add(new ProjectTypeDef("invalid id", "invalid id", true, false) {
        });
        pts.add(new ProjectTypeDef("id1", null, true, false) {
        });
        pts.add(new ProjectTypeDef("id2", "", true, false) {
        });
        reg = new ProjectTypeRegistry(pts);
        // BASE only
        assertEquals(1, reg.getProjectTypes().size());

        // Invalid parent
        final ProjectTypeDef invalidParent = new ProjectTypeDef("i-parent", "parent", true, false) {
        };
        pts.add(new ProjectTypeDef("notRegParent", "not reg parent", true, false) {
            {
                addParent("i-parent");
            }
        });
        reg = new ProjectTypeRegistry(pts);
        // BASE only
        assertEquals(1, reg.getProjectTypes().size());
    }

    @Test
    public void testPTInheritance() throws Exception {
        Set<ProjectTypeDef> pts = new HashSet<>();
        final ProjectTypeDef parent = new ProjectTypeDef("parent", "parent", true, false) {
            {
                addConstantDefinition("parent_const", "Constant", "const_value");
            }

        };
        final ProjectTypeDef child = new ProjectTypeDef("child", "child", true, false) {
            {
                addParent("parent");
                addConstantDefinition("child_const", "Constant", "const_value");
            }
        };

        pts.add(child);
        pts.add(parent);

        ProjectTypeRegistry reg = new ProjectTypeRegistry(pts);
        assertEquals(3, reg.getProjectTypes().size());
        assertEquals(1, child.getParents().size());
        assertEquals(2, child.getAncestors().size());
        assertEquals(2, reg.getProjectType("child").getAttributes().size());
        assertEquals(1, reg.getProjectType("parent").getAttributes().size());
        Assert.assertTrue(reg.getProjectType("child").isTypeOf("parent"));
    }

    @Test
    public void testAttributeNameConflict() throws Exception {
        Set<ProjectTypeDef> pts = new HashSet<>();
        final ProjectTypeDef parent = new ProjectTypeDef("parent", "parent", true, false) {
            {
                addConstantDefinition("parent_const", "Constant", "const_value");
            }

        };
        final ProjectTypeDef child = new ProjectTypeDef("child", "child", true, false) {
            {
                addParent("parent");
                addConstantDefinition("parent_const", "Constant", "const_value");
            }
        };

        pts.add(child);
        pts.add(parent);

        ProjectTypeRegistry reg = new ProjectTypeRegistry(pts);

        assertNotNull(reg.getProjectType("parent"));
        //Assert.assertNull(reg.getProjectType("child"));

        try {
            ProjectTypeDef projectTypeDef = reg.getProjectType("child");
            assertThat(projectTypeDef, CoreMatchers.is(nullValue()));
        } catch (NotFoundException e) {
        }

        assertEquals(2, reg.getProjectTypes().size());
    }

    @Test
    public void testMultiInheritance() throws Exception {
        Set<ProjectTypeDef> pts = new HashSet<>();
        final ProjectTypeDef parent1 = new ProjectTypeDef("parent1", "parent", true, false) {
            {
                addConstantDefinition("parent1_const", "Constant", "const_value");
            }

        };
        final ProjectTypeDef parent2 = new ProjectTypeDef("parent2", "parent", true, false) {
            {
                addConstantDefinition("parent2_const", "Constant", "const_value");
            }

        };
        final ProjectTypeDef child = new ProjectTypeDef("child", "child", true, false) {
            {
                addParent("parent1");
                addParent("parent2");
                addConstantDefinition("child_const", "Constant", "const_value");
            }
        };

        pts.add(child);
        pts.add(parent1);
        pts.add(parent2);

        ProjectTypeRegistry reg = new ProjectTypeRegistry(pts);

        assertEquals(2, child.getParents().size());
        assertEquals(3, reg.getProjectType("child").getAttributes().size());
    }

    @Test
    public void testMultiInheritanceAttributeConflict() throws Exception {
        Set<ProjectTypeDef> pts = new HashSet<>();
        final ProjectTypeDef parent1 = new ProjectTypeDef("parent1", "parent", true, false) {
            {
                addConstantDefinition("parent_const", "Constant", "const_value");
            }

        };
        final ProjectTypeDef parent2 = new ProjectTypeDef("parent2", "parent", true, false) {
            {
                addConstantDefinition("parent_const", "Constant", "const_value");
            }

        };
        final ProjectTypeDef child = new ProjectTypeDef("child", "child", true, false) {
            {
                addParent("parent1");
                addParent("parent2");
                addConstantDefinition("child_const", "Constant", "const_value");
            }
        };

        pts.add(child);
        pts.add(parent1);
        pts.add(parent2);

        ProjectTypeRegistry reg = new ProjectTypeRegistry(pts);

        assertNotNull(reg.getProjectType("parent1"));
        assertNotNull(reg.getProjectType("parent2"));

        try {
            ProjectTypeDef projectTypeDef = reg.getProjectType("child");
            assertThat(projectTypeDef, CoreMatchers.is(nullValue()));
        } catch (NotFoundException e) {
        }
        //Assert.assertNull(reg.getProjectType("child"));
    }

    @Test
    public void testTypeOf() throws Exception {
        Set<ProjectTypeDef> pts = new HashSet<>();
        final ProjectTypeDef parent = new ProjectTypeDef("parent", "parent", true, false) {
        };

        final ProjectTypeDef parent1 = new ProjectTypeDef("parent1", "parent", true, false) {
        };

        final ProjectTypeDef parent2 = new ProjectTypeDef("parent2", "parent", true, false) {
        };

        final ProjectTypeDef child = new ProjectTypeDef("child", "child", true, false) {
            {
                addParent("parent");
                addParent("parent2");
            }
        };

        final ProjectTypeDef child2 = new ProjectTypeDef("child2", "child2", true, false) {
            {
                addParent("child");
            }
        };

        pts.add(child);
        pts.add(parent);
        pts.add(child2);
        pts.add(parent1);
        pts.add(parent2);

        ProjectTypeRegistry reg = new ProjectTypeRegistry(pts);

        ProjectTypeDef t1 = reg.getProjectType("child2");


        Assert.assertTrue(t1.isTypeOf("parent"));
        Assert.assertTrue(t1.isTypeOf("parent2"));
        Assert.assertTrue(t1.isTypeOf("blank"));
        Assert.assertFalse(t1.isTypeOf("parent1"));
    }

    @Test
    public void testSortPTs() throws Exception {
        Set<ProjectTypeDef> pts = new HashSet<>();
        final ProjectTypeDef parent = new ProjectTypeDef("parent", "parent", true, false) {
        };

        final ProjectTypeDef child = new ProjectTypeDef("child", "child", true, false) {
            {
                addParent("parent");
            }
        };

        final ProjectTypeDef child2 = new ProjectTypeDef("child2", "child2", true, false) {
            {
                addParent("child");
            }
        };

        pts.add(child);
        pts.add(parent);
        pts.add(child2);

        ProjectTypeRegistry reg = new ProjectTypeRegistry(pts);
        List<ProjectTypeDef> list = reg.getProjectTypes(new ProjectTypeRegistry.ChildToParentComparator());

        assertEquals(list.get(0).getId(), "child2");
        assertEquals(list.get(1).getId(), "child");
        assertEquals(list.get(2).getId(), "parent");
        assertEquals(list.get(3).getId(), "blank");
    }

    @Singleton
    public static class MyVPFactory implements ValueProviderFactory {

        @Override
        public ValueProvider newInstance(FolderEntry projectFolder) {
            return new MyValueProvider();
        }

        public static class MyValueProvider implements ValueProvider {

            @Override
            public List<String> getValues(String attributeName) throws ValueStorageException {
                return Arrays.asList("gena");
            }

            @Override
            public void setValues(String attributeName, List<String> value) throws ValueStorageException, InvalidValueException {
            }
        }
    }

    @Singleton
    public static class MyProjectType extends ProjectTypeDef {

        @Inject
        public MyProjectType(MyVPFactory myVPFactory) {
            super("my", "my type", true, false);

            addConstantDefinition("const", "Constant", "const_value");
            addVariableDefinition("var", "Variable", false, myVPFactory);
            addVariableDefinition("var1", "var", true, new AttributeValue("value"));
        }
    }
}
