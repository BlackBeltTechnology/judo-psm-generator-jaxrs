package hu.blackbelt.judo.psm.generator.jackson.api;

/*-
 * #%L
 * JUDO PSM Generator Jackson REST API
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
import hu.blackbelt.judo.meta.psm.service.TransferOperation;
import org.eclipse.emf.ecore.ENamedElement;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.jknack.handlebars.internal.lang3.StringUtils.capitalize;
import static hu.blackbelt.judo.psm.generator.jackson.api.ObjectTypeHelper.*;

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
                "class", "Class", "int", "interface").contains(str)) {
            return str + "_";
        } else {
            return str;
        }
    }

    public static String fqName(final Namespace namespace, String separator, boolean safeName) {
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

    public static String relationAsmFqName(TransferObjectRelation transferObjectRelation) {
        TransferObjectType transferObjectType = (TransferObjectType) transferObjectRelation.eContainer();
        return fqName(transferObjectType.getNamespace(), ".", false) + '.' + transferObjectType.getName() + "#" + transferObjectRelation.getName();
    }

    public static String classifierAsmFqName(TransferObjectType transferObjectType) {
        return fqName(transferObjectType.getNamespace(), ".", false) + '.' + transferObjectType.getName();
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
