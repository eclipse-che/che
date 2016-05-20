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

package org.eclipse.che.plugin.java.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.Jar;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.ext.java.shared.JarEntry.JarEntryType;
import org.eclipse.che.ide.ext.java.shared.OpenDeclarationDescriptor;
import org.eclipse.che.ide.ext.java.shared.dto.ClassContent;
import org.eclipse.che.ide.ext.java.shared.dto.Region;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.Field;
import org.eclipse.che.ide.ext.java.shared.dto.model.Initializer;
import org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject;
import org.eclipse.che.ide.ext.java.shared.dto.model.Member;
import org.eclipse.che.ide.ext.java.shared.dto.model.Method;
import org.eclipse.che.ide.ext.java.shared.dto.model.PackageFragment;
import org.eclipse.che.ide.ext.java.shared.dto.model.PackageFragmentRoot;
import org.eclipse.che.ide.ext.java.shared.dto.model.Type;
import org.eclipse.che.jdt.javadoc.JavaElementLabels;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.JarEntryDirectory;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jdt.internal.core.JarEntryResource;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Manager for java navigation operations.
 * Contains methods that convert jdt Java models to DTO objects.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
@Singleton
public class JavaNavigation {
    private static final Logger               LOG           = LoggerFactory.getLogger(JavaNavigation.class);
    private static final ArrayList<JarEntry>  NO_ENTRIES    = new ArrayList<>(1);
    private static       Comparator<JarEntry> comparator    = new Comparator<JarEntry>() {
        @Override
        public int compare(JarEntry o1, JarEntry o2) {
            if (o1.getType() == JarEntryType.PACKAGE && o2.getType() != JarEntryType.PACKAGE) {
                return 1;
            }

            if (o2.getType() == JarEntryType.PACKAGE && o1.getType() != JarEntryType.PACKAGE) {
                return 1;
            }

            if (o1.getType() == JarEntryType.CLASS_FILE && o2.getType() != JarEntryType.CLASS_FILE) {
                return 1;
            }

            if (o1.getType() != JarEntryType.CLASS_FILE && o2.getType() == JarEntryType.CLASS_FILE) {
                return 1;
            }

            if (o1.getType() == JarEntryType.FOLDER && o2.getType() != JarEntryType.FOLDER) {
                return 1;
            }

            if (o1.getType() != JarEntryType.FOLDER && o2.getType() == JarEntryType.FOLDER) {
                return 1;
            }

            if (o1.getType() == JarEntryType.FILE && o2.getType() != JarEntryType.FILE) {
                return -1;
            }

            if (o1.getType() != JarEntryType.FILE && o2.getType() == JarEntryType.FILE) {
                return -1;
            }


            if (o1.getType() == o2.getType()) {
                return o1.getName().compareTo(o2.getName());
            }

            return 0;
        }
    };
    private              Gson                 gson          = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
    private              boolean              fFoldPackages = true;
    private SourcesFromBytecodeGenerator sourcesGenerator;

    @Inject
    public JavaNavigation(SourcesFromBytecodeGenerator sourcesGenerator) {
        this.sourcesGenerator = sourcesGenerator;
    }

    /**
     * Utility method to concatenate two arrays.
     *
     * @param a1
     *         the first array
     * @param a2
     *         the second array
     * @return the concatenated array
     */
    protected static Object[] concatenate(Object[] a1, Object[] a2) {
        int a1Len = a1.length;
        int a2Len = a2.length;
        if (a1Len == 0) return a2;
        if (a2Len == 0) return a1;
        Object[] res = new Object[a1Len + a2Len];
        System.arraycopy(a1, 0, res, 0, a1Len);
        System.arraycopy(a2, 0, res, a1Len, a2Len);
        return res;
    }

    private static IPackageFragment getFolded(IJavaElement[] children, IPackageFragment pack) throws JavaModelException {
        while (isEmpty(pack)) {
            IPackageFragment collapsed = findSinglePackageChild(pack, children);
            if (collapsed == null) {
                return pack;
            }
            pack = collapsed;
        }
        return pack;
    }

    private static boolean isEmpty(IPackageFragment fragment) throws JavaModelException {
        return !fragment.containsJavaResources() && fragment.getNonJavaResources().length == 0;
    }

    private static IPackageFragment findSinglePackageChild(IPackageFragment fragment, IJavaElement[] children) {
        String prefix = fragment.getElementName() + '.';
        int prefixLen = prefix.length();
        IPackageFragment found = null;
        for (int i = 0; i < children.length; i++) {
            IJavaElement element = children[i];
            String name = element.getElementName();
            if (name.startsWith(prefix) && name.length() > prefixLen && name.indexOf('.', prefixLen) == -1) {
                if (found == null) {
                    found = (IPackageFragment)element;
                } else {
                    return null;
                }
            }
        }
        return found;
    }

    public OpenDeclarationDescriptor findDeclaration(IJavaProject project, String fqn, int offset) throws JavaModelException {
        IJavaElement originalElement = null;
        IType type = project.findType(fqn);
        ICodeAssist codeAssist;
        if (type.isBinary()) {
            codeAssist = type.getClassFile();
        } else {
            codeAssist = type.getCompilationUnit();
        }

        IJavaElement[] elements = null;
        if (codeAssist != null) {
            elements = codeAssist.codeSelect(offset, 0);
        }

        if (elements != null && elements.length > 0) {
            originalElement = elements[0];
        }
        IJavaElement element = originalElement;
        while (element != null) {
            if (element instanceof ICompilationUnit) {
                ICompilationUnit unit = ((ICompilationUnit)element).getPrimary();
                return compilationUnitNavigation(unit, originalElement);
            }

            if (element instanceof IClassFile) {
                return classFileNavigation((IClassFile)element, originalElement);
            }
            element = element.getParent();
        }
        return null;
    }

    public List<Jar> getProjectDependecyJars(IJavaProject project) throws JavaModelException {
        List<Jar> jars = new ArrayList<>();
        for (IPackageFragmentRoot fragmentRoot : project.getAllPackageFragmentRoots()) {
            if (fragmentRoot instanceof JarPackageFragmentRoot) {
                Jar jar = DtoFactory.getInstance().createDto(Jar.class);
                jar.setId(fragmentRoot.hashCode());
                jar.setName(fragmentRoot.getElementName());
                jars.add(jar);
            }
        }

        return jars;
    }

    public List<JarEntry> getPackageFragmentRootContent(IJavaProject project, int hash) throws JavaModelException {
        IPackageFragmentRoot packageFragmentRoot = getPackageFragmentRoot(project, hash);

        if (packageFragmentRoot == null) {
            return NO_ENTRIES;
        }

        Object[] rootContent = getPackageFragmentRootContent(packageFragmentRoot);

        return convertToJarEntry(rootContent, packageFragmentRoot);
    }

    /**
     * Get the compilation unit representation of the java file.
     *
     * @param javaProject
     *         path to the project which is contained class file
     * @param fqn
     *         fully qualified name of the class file
     * @param isShowingInheritedMembers
     *         <code>true</code> iff inherited members are shown
     * @return instance of {@link CompilationUnit}
     * @throws JavaModelException
     *         when JavaModel has a failure
     */
    public CompilationUnit getCompilationUnitByPath(IJavaProject javaProject,
                                                    String fqn,
                                                    boolean isShowingInheritedMembers) throws JavaModelException {
        IType type = javaProject.findType(fqn);
        CompilationUnit compilationUnit = DtoFactory.newDto(CompilationUnit.class);
        ITypeRoot unit;
        if (type.isBinary()) {
            unit = type.getClassFile();
            compilationUnit.setPath(((IClassFile)unit).getType().getFullyQualifiedName());
        } else {
            unit = type.getCompilationUnit();
            compilationUnit.setProjectPath(unit.getJavaProject().getPath().toOSString());
            compilationUnit.setPath(unit.getResource().getFullPath().toOSString());
        }

        compilationUnit.setElementName(unit.getElementName());
        compilationUnit.setHandleIdentifier(unit.getHandleIdentifier());
        compilationUnit.setLabel(org.eclipse.jdt.ui.JavaElementLabels.getElementLabel(unit,
                                                                                      org.eclipse.jdt.ui.JavaElementLabels.ALL_DEFAULT));
        List<Type> types = new ArrayList<>(1);
        Type dtoType = convertToDTOType(type);
        dtoType.setPrimary(true);
        types.add(dtoType);
        compilationUnit.setTypes(types);

        if (isShowingInheritedMembers) {
            compilationUnit.setSuperTypes(calculateSuperTypes(type));
        }

        return compilationUnit;
    }

    private List<Type> calculateSuperTypes(IType type) throws JavaModelException {
        List<Type> superTypes = new ArrayList<>();
        ITypeHierarchy superTypeHierarchy = SuperTypeHierarchyCache.getTypeHierarchy(type);
        if (superTypeHierarchy != null) {
            IType[] superITypes = superTypeHierarchy.getAllSupertypes(type);
            for (IType iType : superITypes) {
                superTypes.add(convertToDTOType(iType));
            }
        }
        return superTypes;
    }

    private Type convertToDTOType(IType iType) throws JavaModelException {
        List<Type> types = new ArrayList<>();
        List<Method> methods = new ArrayList<>();
        List<Field> fields = new ArrayList<>();
        List<Initializer> initializers = new ArrayList<>();

        Type type = DtoFactory.newDto(Type.class);

        setRootPath(iType, type);

        type.setElementName(iType.getElementName());
        type.setLabel(
                org.eclipse.jdt.ui.JavaElementLabels.getElementLabel(iType, org.eclipse.jdt.ui.JavaElementLabels.ALL_DEFAULT));
        type.setHandleIdentifier(iType.getHandleIdentifier());
        type.setFlags(iType.getFlags());
        type.setFileRegion(convertToDTORegion(iType.getNameRange()));

        if (!iType.hasChildren()) {
            type.setTypes(types);
            return type;
        }

        IJavaElement[] children = iType.getChildren();
        for (IJavaElement child : children) {
            switch (child.getElementType()) {
                case 7: //type
                    types.add(convertToDTOType((IType)child));
                    break;
                case 8: //field
                    fields.add(convertToDTOField((IField)child));
                    break;
                case 9: //method
                    methods.add(convertToDTOMethod((IMethod)child));
                    break;
                case 10: //initializer
                    initializers.add(convertToDTOInitializer((IInitializer)child));
                    break;
                default:
                    break;
            }
        }

        type.setFields(fields);
        type.setMethods(methods);
        type.setInitializers(initializers);
        type.setTypes(types);

        return type;
    }

    private Field convertToDTOField(IField iField) throws JavaModelException {
        Field field = DtoFactory.newDto(Field.class);

        setRootPath(iField, field);

        field.setFileRegion(convertToDTORegion(iField.getNameRange()));
        field.setElementName(iField.getElementName());
        field.setHandleIdentifier(iField.getHandleIdentifier());
        field.setFlags(iField.getFlags());
        field.setLabel(org.eclipse.jdt.ui.JavaElementLabels.getElementLabel(iField, org.eclipse.jdt.ui.JavaElementLabels.ALL_DEFAULT));

        return field;
    }

    private Method convertToDTOMethod(IMethod iMethod) throws JavaModelException {
        Method method = DtoFactory.newDto(Method.class);

        setRootPath(iMethod, method);

        method.setFileRegion(convertToDTORegion(iMethod.getNameRange()));
        method.setElementName(iMethod.getElementName());
        method.setReturnType(Signature.toString(iMethod.getReturnType()));
        method.setHandleIdentifier(iMethod.getHandleIdentifier());
        method.setFlags(iMethod.getFlags());
        method.setLabel(org.eclipse.jdt.ui.JavaElementLabels.getElementLabel(iMethod, org.eclipse.jdt.ui.JavaElementLabels.ALL_DEFAULT));

        return method;
    }

    private void setRootPath(IMember iMember, Member member) {
        if (iMember.isBinary()) {
            member.setBinary(true);
            member.setRootPath(iMember.getClassFile().getType().getFullyQualifiedName());
            member.setLibId(iMember.getClassFile().getAncestor(IPackageFragmentRoot.PACKAGE_FRAGMENT_ROOT).hashCode());
        } else {
            member.setBinary(false);
            member.setRootPath(iMember.getCompilationUnit().getPath().toOSString());
        }
    }

    private Initializer convertToDTOInitializer(IInitializer iInitializer) throws JavaModelException {
        Initializer initializer = DtoFactory.newDto(Initializer.class);

        initializer.setFileRegion(convertToDTORegion(iInitializer.getSourceRange()));
        initializer.setElementName(iInitializer.getElementName());
        initializer.setHandleIdentifier(iInitializer.getHandleIdentifier());
        initializer.setFlags(iInitializer.getFlags());
        initializer.setLabel(org.eclipse.jdt.ui.JavaElementLabels.getElementLabel(iInitializer,
                                                                                  org.eclipse.jdt.ui.JavaElementLabels.ALL_DEFAULT));

        return initializer;
    }

    private Region convertToDTORegion(ISourceRange iSourceRange) {
        Region region = DtoFactory.newDto(Region.class);
        return iSourceRange == null ? region : region.withLength(iSourceRange.getLength()).withOffset(iSourceRange.getOffset());
    }

    private IPackageFragmentRoot getPackageFragmentRoot(IJavaProject project, int hash) throws JavaModelException {
        IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
        IPackageFragmentRoot packageFragmentRoot = null;
        for (IPackageFragmentRoot root : roots) {
            if (root.hashCode() == hash) {
                packageFragmentRoot = root;
                break;
            }
        }
        return packageFragmentRoot;
    }

    private List<JarEntry> convertToJarEntry(Object[] rootContent, IPackageFragmentRoot root) throws JavaModelException {
        List<JarEntry> result = new ArrayList<>();
        for (Object o : rootContent) {
            if (o instanceof IPackageFragment) {
                JarEntry entry = DtoFactory.getInstance().createDto(JarEntry.class);
                IPackageFragment packageFragment = (IPackageFragment)o;
                entry.setName(getSpecificText((IJavaElement)o));
                entry.setPath(packageFragment.getElementName());
                entry.setType(JarEntryType.PACKAGE);
                result.add(entry);
            }

            if (o instanceof IClassFile) {
                JarEntry entry = getJarClass((IClassFile)o);
                result.add(entry);
            }

            if (o instanceof JarEntryResource) {
                result.add(getJarEntryResource((JarEntryResource)o));
            }
        }
        Collections.sort(result, comparator);
        return result;
    }

    private JarEntry getJarClass(IClassFile classFile) {
        JarEntry entry = DtoFactory.getInstance().createDto(JarEntry.class);
        entry.setType(JarEntryType.CLASS_FILE);
        entry.setName(classFile.getElementName());
        entry.setPath(classFile.getType().getFullyQualifiedName());
        return entry;
    }

    private String getSpecificText(IJavaElement element) {
        if (element instanceof IPackageFragment) {
            IPackageFragment fragment = (IPackageFragment)element;
            Object parent = getHierarchicalPackageParent(fragment);
            if (parent instanceof IPackageFragment) {
                return getNameDelta((IPackageFragment)parent, fragment);
            }
        }

        return JavaElementLabels.getElementLabel(element, 0);
    }

    private String getNameDelta(IPackageFragment parent, IPackageFragment fragment) {
        String prefix = parent.getElementName() + '.';
        String fullName = fragment.getElementName();
        if (fullName.startsWith(prefix)) {
            return fullName.substring(prefix.length());
        }
        return fullName;
    }

    public Object getHierarchicalPackageParent(IPackageFragment child) {
        String name = child.getElementName();
        IPackageFragmentRoot parent = (IPackageFragmentRoot)child.getParent();
        int index = name.lastIndexOf('.');
        if (index != -1) {
            String realParentName = name.substring(0, index);
            IPackageFragment element = parent.getPackageFragment(realParentName);
            if (element.exists()) {
                try {
                    if (fFoldPackages && isEmpty(element) && findSinglePackageChild(element, parent.getChildren()) != null) {
                        return getHierarchicalPackageParent(element);
                    }
                } catch (JavaModelException e) {
                    // ignore
                }
                return element;
            } /*else { // bug 65240
                IResource resource= element.getResource();
                if (resource != null) {
                    return resource;
                }
            }*/
        }
//        if (parent.getResource() instanceof IProject) {
//            return parent.getJavaProject();
//        }
        return parent;
    }

    private JarEntry getJarEntryResource(JarEntryResource resource) {
        JarEntry entry = DtoFactory.getInstance().createDto(JarEntry.class);
        if (resource instanceof JarEntryDirectory) {
            entry.setType(JarEntryType.FOLDER);
        }
        if (resource instanceof JarEntryFile) {
            entry.setType(JarEntryType.FILE);
        }
        entry.setName(resource.getName());
        entry.setPath(resource.getFullPath().toOSString());
        return entry;
    }

    protected Object[] getPackageFragmentRootContent(IPackageFragmentRoot root) throws JavaModelException {

        // hierarchical package mode
        ArrayList<Object> result = new ArrayList<>();
        getHierarchicalPackageChildren(root, null, result);
        Object[] nonJavaResources = root.getNonJavaResources();
        for (int i = 0; i < nonJavaResources.length; i++) {
            result.add(nonJavaResources[i]);
        }
        return result.toArray();
    }

    /* (non-Javadoc)
 * @see org.eclipse.jdt.ui.StandardJavaElementContentProvider#getPackageContent(org.eclipse.jdt.core.IPackageFragment)
 */
    protected Object[] getPackageContent(IPackageFragment fragment) throws JavaModelException {

        // hierarchical package mode
        ArrayList<Object> result = new ArrayList<Object>();

        getHierarchicalPackageChildren((IPackageFragmentRoot)fragment.getParent(), fragment, result);
        IClassFile[] classFiles = fragment.getClassFiles();
        List<IClassFile> filtered = new ArrayList<>();
        //filter inner classes
        for (IClassFile classFile : classFiles) {
            if (!classFile.getElementName().contains("$")) {
                filtered.add(classFile);
            }
        }
        Object[] nonPackages = concatenate(filtered.toArray(), fragment.getNonJavaResources());
        if (result.isEmpty())
            return nonPackages;
        Collections.addAll(result, nonPackages);
        return result.toArray();
    }

    /**
     * Returns the hierarchical packages inside a given fragment or root.
     *
     * @param parent
     *         the parent package fragment root
     * @param fragment
     *         the package to get the children for or 'null' to get the children of the root
     * @param result
     *         Collection where the resulting elements are added
     * @throws JavaModelException
     *         if fetching the children fails
     */
    private void getHierarchicalPackageChildren(IPackageFragmentRoot parent, IPackageFragment fragment, Collection<Object> result)
            throws JavaModelException {
        IJavaElement[] children = parent.getChildren();
        String prefix = fragment != null ? fragment.getElementName() + '.' : ""; //$NON-NLS-1$
        int prefixLen = prefix.length();
        for (int i = 0; i < children.length; i++) {
            IPackageFragment curr = (IPackageFragment)children[i];
            String name = curr.getElementName();
            if (name.startsWith(prefix) && name.length() > prefixLen && name.indexOf('.', prefixLen) == -1) {
                if (fFoldPackages) {
                    curr = getFolded(children, curr);
                }
                result.add(curr);
            } else if (fragment == null && curr.isDefaultPackage()) {
                IJavaElement[] currChildren = curr.getChildren();
                if (currChildren != null && currChildren.length >= 1) {
                    result.add(curr);
                }
            }
        }
    }

    private OpenDeclarationDescriptor classFileNavigation(IClassFile classFile, IJavaElement element) throws JavaModelException {
        OpenDeclarationDescriptor dto = DtoFactory.getInstance().createDto(OpenDeclarationDescriptor.class);
        dto.setPath(classFile.getType().getFullyQualifiedName());
        dto.setLibId(classFile.getAncestor(IPackageFragmentRoot.PACKAGE_FRAGMENT_ROOT).hashCode());
        dto.setBinary(true);
        if (classFile.getSourceRange() != null) {
            if (element instanceof ISourceReference) {
                ISourceRange nameRange = ((ISourceReference)element).getNameRange();
                dto.setOffset(nameRange.getOffset());
                dto.setLength(nameRange.getLength());
            }
        }
        return dto;
    }

    private OpenDeclarationDescriptor compilationUnitNavigation(ICompilationUnit unit, IJavaElement element)
            throws JavaModelException {
        OpenDeclarationDescriptor dto = DtoFactory.getInstance().createDto(OpenDeclarationDescriptor.class);
        String absolutePath = unit.getPath().toOSString();
        dto.setPath(absolutePath);
        dto.setBinary(false);
        if (element instanceof ISourceReference) {
            ISourceRange nameRange = ((ISourceReference)element).getNameRange();
            dto.setOffset(nameRange.getOffset());
            dto.setLength(nameRange.getLength());
        }

        return dto;
    }

    private Object[] findJarDirectoryChildren(JarEntryDirectory directory, String path) {
        String directoryPath = directory.getFullPath().toOSString();
        if (directoryPath.equals(path)) {
            return directory.getChildren();
        }
        if (path.startsWith(directoryPath)) {
            for (IJarEntryResource resource : directory.getChildren()) {
                String childrenPath = resource.getFullPath().toOSString();
                if (childrenPath.equals(path)) {
                    return resource.getChildren();
                }
                if (path.startsWith(childrenPath) && resource instanceof JarEntryDirectory) {
                    findJarDirectoryChildren((JarEntryDirectory)resource, path);
                }
            }
        }
        return null;
    }

    public List<JarEntry> getChildren(IJavaProject project, int rootId, String path) throws JavaModelException {
        IPackageFragmentRoot root = getPackageFragmentRoot(project, rootId);
        if (root == null) {
            return NO_ENTRIES;
        }

        if (path.startsWith("/")) {
            // jar file and folders
            Object[] resources = root.getNonJavaResources();
            for (Object resource : resources) {
                if (resource instanceof JarEntryDirectory) {
                    JarEntryDirectory directory = (JarEntryDirectory)resource;
                    Object[] children = findJarDirectoryChildren(directory, path);
                    if (children != null) {
                        return convertToJarEntry(children, root);
                    }
                }
            }

        } else {
            // packages and class files
            IPackageFragment fragment = root.getPackageFragment(path);
            if (fragment == null) {
                return NO_ENTRIES;
            }
            return convertToJarEntry(getPackageContent(fragment), root);
        }
        return NO_ENTRIES;
    }

    public ClassContent getContent(IJavaProject project, int rootId, String path) throws CoreException {
        IPackageFragmentRoot root = getPackageFragmentRoot(project, rootId);
        if (root == null) {
            return null;
        }

        if (path.startsWith("/")) {
            //non java file
            if (root instanceof JarPackageFragmentRoot) {
                JarPackageFragmentRoot jarPackageFragmentRoot = (JarPackageFragmentRoot)root;
                ZipFile jar = null;
                try {
                    jar = jarPackageFragmentRoot.getJar();
                    ZipEntry entry = jar.getEntry(path.substring(1));
                    if (entry != null) {
                        try (InputStream stream = jar.getInputStream(entry)) {
                            return createContent(IoUtil.readStream(stream), false);
                        } catch (IOException e) {
                            LOG.error("Can't read file content: " + entry.getName(), e);
                        }
                    }
                } finally {
                    if (jar != null) {
                        JavaModelManager.getJavaModelManager().closeZipFile(jar);
                    }
                }
            }
            Object[] resources = root.getNonJavaResources();

            for (Object resource : resources) {
                if (resource instanceof JarEntryFile) {
                    JarEntryFile file = (JarEntryFile)resource;
                    if (file.getFullPath().toOSString().equals(path)) {
                        return readFileContent(file);
                    }
                }
                if (resource instanceof JarEntryDirectory) {
                    JarEntryDirectory directory = (JarEntryDirectory)resource;
                    JarEntryFile file = findJarFile(directory, path);
                    if (file != null) {
                        return readFileContent(file);
                    }
                }
            }
        } else {
            return getContent(project, path);
        }
        return null;
    }

    public ClassContent getContent(IJavaProject project, String path) throws JavaModelException {
        //java class or file
        IType type = project.findType(path);
        if (type != null) {
            if (type.isBinary()) {
                IClassFile classFile = type.getClassFile();
                if (classFile.getSourceRange() != null) {
                    return createContent(classFile.getSource(), false);
                } else {

                    return createContent(sourcesGenerator.generateSource(classFile.getType()), true);
                }
            } else {
                return createContent(type.getCompilationUnit().getSource(), false);
            }
        }
        throw new JavaModelException(new JavaModelStatus(0, "Can't find type: " + path));
    }

    private ClassContent readFileContent(JarEntryFile file) {
        try (InputStream stream = (file.getContents())) {
            return createContent(IoUtil.readStream(stream), false);
        } catch (IOException | CoreException e) {
            LOG.error("Can't read file content: " + file.getFullPath(), e);
        }
        return null;
    }

    private ClassContent createContent(String content, boolean generated) {
        ClassContent classContent = DtoFactory.newDto(ClassContent.class);
        classContent.setContent(content);
        classContent.setGenerated(generated);
        return classContent;
    }

    private JarEntryFile findJarFile(JarEntryDirectory directory, String path) {
        for (IJarEntryResource children : directory.getChildren()) {
            if (children.isFile() && children.getFullPath().toOSString().equals(path)) {
                return (JarEntryFile)children;
            }
            if (!children.isFile()) {
                JarEntryFile file = findJarFile((JarEntryDirectory)children, path);
                if (file != null) {
                    return file;
                }
            }
        }
        return null;
    }

    public JarEntry getEntry(IJavaProject project, int rootId, String path) throws CoreException {
        IPackageFragmentRoot root = getPackageFragmentRoot(project, rootId);
        if (root == null) {
            return null;
        }
        if (path.startsWith("/")) {

            JarPackageFragmentRoot jarPackageFragmentRoot = (JarPackageFragmentRoot)root;
            ZipFile jar = null;
            try {
                jar = jarPackageFragmentRoot.getJar();
                ZipEntry entry = jar.getEntry(path.substring(1));
                if (entry != null) {
                    JarEntry result = DtoFactory.getInstance().createDto(JarEntry.class);
                    result.setType(JarEntryType.FILE);
                    result.setPath(path);
                    result.setName(entry.getName().substring(entry.getName().lastIndexOf("/") + 1));
                    return result;
                }
            } finally {
                if (jar != null) {
                    JavaModelManager.getJavaModelManager().closeZipFile(jar);
                }
            }

            Object[] resources = root.getNonJavaResources();

            for (Object resource : resources) {
                if (resource instanceof JarEntryFile) {
                    JarEntryFile file = (JarEntryFile)resource;
                    if (file.getFullPath().toOSString().equals(path)) {
                        return getJarEntryResource(file);
                    }
                }
                if (resource instanceof JarEntryDirectory) {
                    JarEntryDirectory directory = (JarEntryDirectory)resource;
                    JarEntryFile file = findJarFile(directory, path);
                    if (file != null) {
                        return getJarEntryResource(file);
                    }
                }
            }

        } else {
            //java class or file
            IType type = project.findType(path);
            if (type != null && type.isBinary()) {
                IClassFile classFile = type.getClassFile();
                return getJarClass(classFile);
            }
        }

        return null;
    }

    public List<JavaProject> getAllProjectsAndPackages(boolean includePackages) throws JavaModelException {
        JavaModel javaModel = JavaModelManager.getJavaModelManager().getJavaModel();
        IJavaProject[] javaProjects = javaModel.getJavaProjects();
        List<JavaProject> result = new ArrayList<>();
        for (IJavaProject javaProject : javaProjects) {
            if (javaProject.exists()) {
                JavaProject project = DtoFactory.newDto(JavaProject.class);
                project.setName(javaProject.getElementName());
                project.setPath(javaProject.getPath().toOSString());
                project.setPackageFragmentRoots(toPackageRoots(javaProject, includePackages));
                result.add(project);
            }
        }
        return result;
    }

    private List<PackageFragmentRoot> toPackageRoots(IJavaProject javaProject, boolean includePackages) throws JavaModelException {
        IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
        List<PackageFragmentRoot> result = new ArrayList<>();
        for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
            if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
                PackageFragmentRoot root = DtoFactory.newDto(PackageFragmentRoot.class);
                root.setPath(packageFragmentRoot.getPath().toOSString());
                root.setProjectPath(packageFragmentRoot.getJavaProject().getPath().toOSString());
                if (includePackages) {
                    root.setPackageFragments(toPackageFragments(packageFragmentRoot));
                }
                result.add(root);
            }
        }
        return result;
    }

    private List<PackageFragment> toPackageFragments(IPackageFragmentRoot packageFragmentRoot) throws JavaModelException {
        IJavaElement[] children = packageFragmentRoot.getChildren();
        if (children == null) {
            return null;
        }
        List<PackageFragment> result = new ArrayList<>();
        for (IJavaElement child : children) {
            if (child instanceof IPackageFragment) {
                IPackageFragment packageFragment = (IPackageFragment)child;
                PackageFragment fragment = DtoFactory.newDto(PackageFragment.class);
                fragment.setElementName(packageFragment.getElementName());
                fragment.setPath(packageFragment.getPath().toOSString());
                fragment.setProjectPath(packageFragment.getJavaProject().getPath().toOSString());
                result.add(fragment);
            }
        }
        return result;
    }
}
