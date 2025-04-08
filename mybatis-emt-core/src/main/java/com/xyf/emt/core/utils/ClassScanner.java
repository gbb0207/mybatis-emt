package com.xyf.emt.core.utils;

import com.xyf.emt.core.EmtAnnotationFinder;
import com.xyf.emt.core.EmtGlobalConfig;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 基于注解扫描java类
 */
public class ClassScanner {

    public static Set<Class<?>> scan(String[] basePackages, Set<Class<? extends Annotation>> includeAnnotations, Set<Class<? extends Annotation>> excludeAnnotations) {

        if (basePackages == null || includeAnnotations == null) {
            return Collections.emptySet();
        }

        EmtAnnotationFinder emtAnnotationFinder = EmtGlobalConfig.getEmtAnnotationFinder();

        return Arrays.stream(basePackages)
                .map(basePackage -> {
                    try {
                        return getClasses(basePackage,
                                clazz -> includeAnnotations.stream().anyMatch(anno -> emtAnnotationFinder.exist(clazz, anno)) &&
                                        excludeAnnotations.stream().noneMatch(anno -> emtAnnotationFinder.exist(clazz, anno))
                        );
                    } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException(String.format("扫描包%s下实体出错", basePackage), e);
                    }
                }).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public static Set<Class<?>> getClasses(String packageName, Function<Class<?>, Boolean> checker) throws IOException, ClassNotFoundException {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        String basePackage = path.split("/\\*")[0];
        Pattern checkPattern = Pattern.compile("(" + packageName.replace(".", "\\/")
                .replace("**", "[A-Za-z0-9$_/]+")
                .replace("*", "[A-Za-z0-9$_]+") + "[A-Za-z0-9$_/]+)\\.class$");

        Enumeration<URL> resources = classLoader.getResources(basePackage);
        Set<Class<?>> classes = new HashSet<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if ("file".equals(resource.getProtocol())) {
                String decodedPath = URLDecoder.decode(resource.getFile(), "UTF-8");
                classes.addAll(findClassesLocal(checkPattern, new File(decodedPath), checker));
            } else if ("jar".equals(resource.getProtocol())) {
                JarURLConnection jarURLConnection = (JarURLConnection) resource.openConnection();
                classes.addAll(findClassesJar(checkPattern, jarURLConnection.getJarFile(), checker));
            }
        }
        return classes;
    }

    private static Set<Class<?>> findClassesLocal(Pattern checkPattern, File directory, Function<Class<?>, Boolean> checker) throws ClassNotFoundException, IOException {
        Set<Class<?>> classes = new HashSet<>();
        if (!directory.exists()) {
            return classes;
        }

        Files.walk(directory.toPath())
                .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".class"))
                .forEach(path -> {
                    try {
                        String pathUrl = path.toUri().toURL().toString();
                        Matcher matcher = checkPattern.matcher(pathUrl);
                        if (matcher.find()) {
                            String className = matcher.group(1).replace("/", ".");
                            Class<?> clazz = Class.forName(className);
                            if (checker.apply(clazz)) { // check annotation
                                classes.add(clazz);
                            }
                        }
                    } catch (ClassNotFoundException | MalformedURLException e) {
                        // ignore
                    }
                });
        return classes;
    }

    private static Set<Class<?>> findClassesJar(Pattern checkPattern, JarFile jarFile, Function<Class<?>, Boolean> checker) throws ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<>();
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class")) {
                Matcher matcher = checkPattern.matcher(entry.getName());
                if (matcher.find()) {
                    String className = matcher.group(1).replace("/", ".");
                    Class<?> clazz;
                    try {
                        clazz = Class.forName(className);
                    }catch (ClassNotFoundException e){
                        clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                    }
                    if (clazz != null && checker.apply(clazz)) {
                        classes.add(clazz);
                    }
                }
            }
        }
        return classes;
    }
}
