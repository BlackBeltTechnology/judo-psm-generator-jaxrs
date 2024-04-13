package hu.blackbelt.judo.psm.generator.jaxrs.api;

/*-
 * #%L
 * JUDO PSM Generator Jakarta RESTful Web Services API
 * %%
 * Copyright (C) 2018 - 2023 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import com.github.jknack.handlebars.internal.lang3.StringUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import hu.blackbelt.judo.generator.commons.StaticMethodValueResolver;
import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.psm.PsmUtils;
import hu.blackbelt.judo.meta.psm.derived.StaticData;
import hu.blackbelt.judo.meta.psm.derived.StaticNavigation;
import hu.blackbelt.judo.meta.psm.namespace.*;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.github.jknack.handlebars.internal.lang3.StringUtils.capitalize;
import static hu.blackbelt.judo.psm.generator.jaxrs.api.ObjectTypeHelper.getEntity;
import static hu.blackbelt.judo.psm.generator.jaxrs.api.ObjectTypeHelper.isEntity;

@TemplateHelper
public class JavaNamespaceHelper extends StaticMethodValueResolver {

    public static final String DEFAULT_TRANSFER_OBJECT_TYPES = "_default_transferobjecttypes";

    public static String safeName(String str) {
        if (Arrays.asList(
                "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
                "continue", "default", "do", "double", "else", "enum", "exports", "extends",
                "final", "finally", "float", "for", "if", "implements", "import", "instanceof",
                "long", "module", "native", "new", "package", "private", "protected",
                "public", "requires", "return", "short", "static", "strictfp", "super",
                "switch", "synchronized", "this", "throw", "throws", "transient", "try",
                "void", "volatile", "while", "true", "null", "false", "var", "const", "goto",
                "class", "Class", "int", "interface", "default").contains(str)) {
            return str + "_";
        } else {
            return str;
        }
    }

    @Builder
    @EqualsAndHashCode
    @Getter
    private static final class FqNameKey {
        private Namespace namespace;
        private String separator;
        @Builder.Default
        private Boolean safeName = false;
    }

    private static CacheLoader<FqNameKey, String> fqNameKeyStringCacheLoader = new CacheLoader<>() {
        @Override
        public String load(FqNameKey key) {
            return fqNameForCache(key.getNamespace(), key.getSeparator(), key.getSafeName());
        }
    };

    private static LoadingCache<FqNameKey, String> fqNameKeyStringLoadingCache =
            CacheBuilder.newBuilder()
                    .build(fqNameKeyStringCacheLoader);
    public static String fqName(final Namespace namespace, String separator, boolean safeName) {
        try {
            return fqNameKeyStringLoadingCache.get(FqNameKey.builder()
                    .namespace(namespace)
                    .separator(separator)
                    .safeName(safeName)
                    .build());
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static String fqNameForCache(final Namespace namespace, String separator, boolean safeName) {
        if (namespace instanceof Model) {
            if (safeName) {
                return safeName(namespace.getName().toLowerCase());
            } else {
                return namespace.getName();
            }
        } else if (namespace instanceof Package) {
            final Optional<Namespace> containerNamespace = PsmUtils.getNamespaceOfPackage((Package) namespace);
            if (containerNamespace.isPresent()) {
                if (safeName) {
                    return fqName(containerNamespace.get(), separator, safeName) + separator + safeName(namespace.getName().toLowerCase());
                } else {
                    return fqName(containerNamespace.get(), separator, safeName) + separator + namespace.getName();
                }
            } else {
                // relative path is returned
                if (safeName) {
                    return separator + safeName(namespace.getName().toLowerCase());
                } else {
                    return separator + namespace.getName();
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid namespace");
        }
    }

    public static String namedElementOriginalName(NamedElement namedElement) {
        String name = namedElement.getName();
        Optional<Annotation> originalSource = namedElement.getAnnotations().stream()
                .filter(a -> a.getName().equals("OriginalSource")).findFirst();
        if (originalSource.isPresent()) {
            Optional<AnnotationDetail> detail = originalSource.get().getDetails().stream()
                    .filter(d -> d.getName().equals("Name")).findFirst();
            if (detail.isPresent()) {
                name = detail.get().getValue();
            }
        }
        return name;
    }

    public static String safeNamedElementOriginalNameWithSeparator(NamedElement namedElement, String separator) {
        return Arrays.stream(namedElementOriginalName(namedElement).split("::")).map(s -> safeName(s.toLowerCase())).collect(Collectors.joining(separator));
    }

    public static String safeNamedElementOriginalNameForClassNames(NamedElement namedElement) {
        return Arrays.stream(namedElementOriginalName(namedElement).split("::")).map(s -> StringUtils.capitalize(safeName(s))).collect(Collectors.joining(""));
    }

    public static String namedElementPackageName(NamedElement namedElement) {
        return Arrays.stream(fqName((Namespace) namedElement.eContainer(), ".", true).toLowerCase().split("\\.")).filter(name -> !DEFAULT_TRANSFER_OBJECT_TYPES.equals(name)).collect(Collectors.joining("."));
    }

    public static String namedElementParentPath(NamedElement namedElement) {
        return Arrays.stream(fqName((Namespace) namedElement.eContainer(), "/", true).toLowerCase().split("/")).filter(name -> !DEFAULT_TRANSFER_OBJECT_TYPES.equals(name)).collect(Collectors.joining("/"));
    }

    public static String namedElementLogicalName(NamedElement namedElement) {
        return fqName((Namespace) namedElement.eContainer(), PsmUtils.NAMESPACE_SEPARATOR, false) +
                PsmUtils.NAMESPACE_SEPARATOR + namedElement.getName();
    }

    public static String namedElementAsmName(NamedElement namedElement) {
        String name = namedElement.getName();
        if ((namedElement instanceof StaticData) | (namedElement instanceof StaticNavigation)) {
            name = capitalize(namedElement.getName());
        }
        return fqName((Namespace) namedElement.eContainer(), ".", false) + '.' + name;
    }

    public static String namedElementVariableName(NamedElement namedElement) {
        return safeName(StringUtils.uncapitalize(namedElement.getName()));
    }

    private static CacheLoader<TransferObjectRelation, String> relationAsmFqNameCacheLoader = new CacheLoader<>() {
        @Override
        public String load(TransferObjectRelation key) {
            return relationAsmFqNameForCache(key);
        }
    };

    private static LoadingCache<TransferObjectRelation, String> relationAsmFqNameLoadingCache =
            CacheBuilder.newBuilder()
                    .build(relationAsmFqNameCacheLoader);

    public static String relationAsmFqName(TransferObjectRelation transferObjectRelation) {
        try {
            return relationAsmFqNameLoadingCache.get(transferObjectRelation);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static String relationAsmFqNameForCache(TransferObjectRelation transferObjectRelation) {
        TransferObjectType transferObjectType = (TransferObjectType) transferObjectRelation.eContainer();
        return fqName(transferObjectType.getNamespace(), ".", false) + '.' + transferObjectType.getName() + "#" + transferObjectRelation.getName();
    }

    private static CacheLoader<TransferObjectType, String> classifierAsmFqNameCacheLoader = new CacheLoader<>() {
        @Override
        public String load(TransferObjectType key) {
            return classifierAsmFqNameForCache(key);
        }
    };

    private static LoadingCache<TransferObjectType, String> classifierAsmFqNameLoadingCache =
            CacheBuilder.newBuilder()
                    .build(classifierAsmFqNameCacheLoader);

    public static String classifierAsmFqName(TransferObjectType transferObjectType) {
        try {
            return classifierAsmFqNameLoadingCache.get(transferObjectType);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static String classifierAsmFqNameForCache(TransferObjectType transferObjectType) {
        return fqName(transferObjectType.getNamespace(), ".", false) + '.' + transferObjectType.getName();
    }

    public static String logicalFullName(TransferObjectType transferObjectType) {
        return Arrays.stream(classifierAsmFqName(transferObjectType).split("\\.")).map(s -> StringUtils.capitalize(s)).collect(Collectors.joining());
    }

    public static String attributeAsmFqName(TransferAttribute transferAttribute) {
        TransferObjectType transferObjectType = (TransferObjectType) transferAttribute.eContainer();
        NamedElement namedElement = transferObjectType;
        if (isEntity(transferObjectType)) {
            namedElement = getEntity(transferObjectType);
        }
        return fqName((Namespace) namedElement.eContainer(), ".", false) + '.' + namedElement.getName() + "#" + transferAttribute.getName();
    }

}
