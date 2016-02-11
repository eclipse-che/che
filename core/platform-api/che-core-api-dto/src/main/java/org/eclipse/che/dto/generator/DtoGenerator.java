// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.eclipse.che.dto.generator;

import org.eclipse.che.dto.server.DtoFactoryVisitor;
import org.eclipse.che.dto.shared.DTO;

import org.eclipse.che.dto.shared.DTOImpl;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Simple source generator that takes in the packages list with interface definitions and generates client and server DTO impls. */
public class DtoGenerator {
    /** Flag: location of the packages that contains dto interfaces. */
    private String[] dtoPackages = null;

    /** Flag: Name of the generated java class file that contains the DTOs. */
    private String genFileName = "DataObjects.java";

    /** Flag: The type of impls to be generated, either client or server. */
    private String impl = "client";

    /** Flag: A pattern we can use to search an absolute path and find the start of the package definition.") */
    private String packageBase = "java.";

    public String[] getDtoPackages() {
        return dtoPackages;
    }

    public void setDtoPackages(String[] dtoPackages) {
        this.dtoPackages = new String[dtoPackages.length];
        System.arraycopy(dtoPackages, 0, this.dtoPackages, 0, this.dtoPackages.length);
    }

    public String getGenFileName() {
        return genFileName;
    }

    public void setGenFileName(String genFileName) {
        this.genFileName = genFileName;
    }

    public String getImpl() {
        return impl;
    }

    public void setImpl(String impl) {
        this.impl = impl;
    }

    public String getPackageBase() {
        return packageBase;
    }

    public void setPackageBase(String packageBase) {
        this.packageBase = packageBase;
    }

    public static void main(String[] args) {
        DtoGenerator generator = new DtoGenerator();
        for (String arg : args) {
            if (arg.startsWith("--dto_packages=")) {
                generator.setDtoPackages(arg.substring("--dto_packages=".length()));
            } else if (arg.startsWith("--gen_file_name=")) {
                generator.setGenFileName(arg.substring("--gen_file_name=".length()));
            } else if (arg.startsWith("--impl=")) {
                generator.setImpl(arg.substring("--impl=".length()));
            } else if (arg.startsWith("--package_base=")) {
                generator.setPackageBase(arg.substring("--package_base=".length()));
            } else {
                System.err.println("Unknown flag: " + arg);
                System.exit(1);
            }
        }
        generator.generate();
    }

    private void setDtoPackages(String packagesParam) {
        setDtoPackages(packagesParam.split(","));
    }

    public void generate() {

        Set<URL> urls = getClasspathForPackages(dtoPackages);
        genFileName = genFileName.replace('/', File.separatorChar);
        String outputFilePath = genFileName;

        // Extract the name of the output file that will contain all the DTOs and its package.
        String myPackageBase = packageBase;
        if (!myPackageBase.endsWith("/")) {
            myPackageBase += "/";
        }
        myPackageBase = myPackageBase.replace('/', File.separatorChar);

        int packageStart = outputFilePath.lastIndexOf(myPackageBase) + myPackageBase.length();
        int packageEnd = outputFilePath.lastIndexOf(File.separatorChar);
        String fileName = outputFilePath.substring(packageEnd + 1);
        String className = fileName.substring(0, fileName.indexOf(".java"));
        String packageName = outputFilePath.substring(packageStart, packageEnd).replace(File.separatorChar, '.');

        File outFile = new File(outputFilePath);

        try {
            StringBuilder sb = new StringBuilder();
            for (String dtoPackage : dtoPackages) {
                if (sb.length() > 0) {
                    sb.append(dtoPackage);
                }
                sb.append(dtoPackage);
            }

            DtoTemplate dtoTemplate = new DtoTemplate(packageName, className, impl);
            Reflections reflection = new Reflections(new ConfigurationBuilder().setUrls(urls).setScanners(new SubTypesScanner(), new TypeAnnotationsScanner()));
            
            List<Class<?>> dtos = new ArrayList<>(reflection.getTypesAnnotatedWith(DTO.class));

            // We sort alphabetically to ensure deterministic order of routing types.
            Collections.sort(dtos, new ClassesComparator());

            for (Class<?> clazz : dtos) {

                // DTO are interface
                if (clazz.isInterface()) {
                    dtoTemplate.addInterface(clazz);
                }
            }

            reflection = new Reflections(
                    new ConfigurationBuilder().setUrls(ClasspathHelper.forClassLoader()).setScanners(new SubTypesScanner(), new TypeAnnotationsScanner()));
            List<Class<?>> dtosDependencies = new ArrayList<>(reflection.getTypesAnnotatedWith(DTO.class));
            dtosDependencies.removeAll(dtos);

            reflection = new Reflections(
                    new ConfigurationBuilder().setUrls(ClasspathHelper.forClassLoader()).setScanners(new SubTypesScanner()));

            for (Class<?> clazz : dtosDependencies) {
                for (Class impl : reflection.getSubTypesOf(clazz)) {
                    if (!(impl.isInterface() || urls.contains(impl.getProtectionDomain().getCodeSource().getLocation()))) {
                        if ("client".equals(dtoTemplate.getImplType())) {
                           if (isClientImpl(impl))  {
                               dtoTemplate.addImplementation(clazz, impl);
                           }
                        } else if ("server".equals(dtoTemplate.getImplType())) {
                            if (isServerImpl(impl))  {
                                dtoTemplate.addImplementation(clazz, impl);
                            }
                        }
                    }
                }
            }

            // Emit the generated file.
            Files.createDirectories(outFile.toPath().getParent());
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
                writer.write(dtoTemplate.toString());
            }

            if ("server".equals(impl)) {
                // Create file in META-INF/services/
                File outServiceFile = new File(myPackageBase + "META-INF/services/" + DtoFactoryVisitor.class.getCanonicalName());
                Files.createDirectories(outServiceFile.toPath().getParent());
                try (BufferedWriter serviceFileWriter = new BufferedWriter(new FileWriter(outServiceFile))) {
                    serviceFileWriter.write(packageName + "." + className);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isClientImpl(Class<?> impl) {
        DTOImpl a = impl.getAnnotation(DTOImpl.class);
        return a != null && "client".equals(a.value());
    }

    private boolean isServerImpl(Class<?> impl) {
        DTOImpl a = impl.getAnnotation(DTOImpl.class);
        return a != null && "server".equals(a.value());
    }

    private static Set<URL> getClasspathForPackages(String[] packages) {
        Set<URL> urls = new HashSet<>();
        for (String pack : packages) {
            urls.addAll(ClasspathHelper.forPackage(pack));
        }
        return urls;
    }

    private static class ClassesComparator implements Comparator<Class> {
        @Override
        public int compare(Class o1, Class o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}