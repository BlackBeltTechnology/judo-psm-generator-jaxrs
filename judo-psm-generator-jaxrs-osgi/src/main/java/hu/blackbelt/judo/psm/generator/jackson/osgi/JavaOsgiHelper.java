package hu.blackbelt.judo.psm.generator.jaxrs.osgi;

/*-
 * #%L
 * JUDO PSM Generator Jakarta RESTful Web Services Implementation
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
import hu.blackbelt.judo.generator.commons.ThreadLocalContextHolder;
import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.psm.derived.StaticData;
import hu.blackbelt.judo.meta.psm.derived.StaticNavigation;
import hu.blackbelt.judo.meta.psm.namespace.NamedElement;
import hu.blackbelt.judo.meta.psm.namespace.Namespace;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferOperation;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.psm.generator.jaxrs.api.JavaApiHelper.*;
import static hu.blackbelt.judo.psm.generator.jaxrs.api.JavaNamespaceHelper.*;

@TemplateHelper
public class JavaOsgiHelper extends StaticMethodValueResolver {

    public static synchronized String getOsgiPrefixLocal() {
        return (String) ThreadLocalContextHolder.getVariable("osgiPrefix");
    }

    public static String osgiPackageName() {
        return (getOsgiPrefixLocal().equals("") ? "" : getOsgiPrefixLocal() + ".");
    }

    public static String namedElementOsgiParentPath(NamedElement namedElement) {
        return osgiPackageName().replaceAll("\\.", "/" ) + namedElementParentPath(namedElement);
    }

    public static String namedElementOsgiPackageName(NamedElement namedElement) {
        return osgiPackageName() + namedElementPackageName(namedElement);
    }

    public static String namedElementOsgiRestPackageName(NamedElement namedElement) {
        return osgiPackageName() + REST + "." + namedElementPackageName(namedElement);
    }

    public static String namedElementOsgiRestFqName(NamedElement namedElement) {
        return osgiPackageName() + REST + "." + namedElementPackageName(namedElement) +
                "." + safeNamedElementOriginalNameForClassNames(namedElement);
    }

    public static String namedElementOsgiRestParentPath(NamedElement namedElement) {
        return osgiPackageName().replaceAll("\\.", "/" ) + REST + "/" + namedElementParentPath(namedElement);
    }

    public static String namedElementOsgiFqName(NamedElement namedElement) {
        return namedElementOsgiPackageName(namedElement) + "." + safeName(namedElement.getName());
    }

    public static String applicationOsgiClassName(NamedElement namedElement) {
        return applicationClassName(namedElement) + "Component" ;
    }

    public static String osgiClassName(NamedElement namedElement) {
        return className(namedElement) + "Component";
    }

    public static String namedElementOsgiApplicationPath(NamedElement namedElement) {
        return Arrays.stream(fqName((Namespace) namedElement.eContainer(), "/", false).split("/")).filter(name -> !DEFAULT_TRANSFER_OBJECT_TYPES.equals(name)).collect(Collectors.joining("/")) + "/" + className(namedElement);
    }

    public static String namedElementOsgiApplicationName(NamedElement namedElement) {
        return Arrays.stream(fqName((Namespace) namedElement.eContainer(), "/", false).split("/")).filter(name -> !DEFAULT_TRANSFER_OBJECT_TYPES.equals(name)).collect(Collectors.joining(".")) + "." + className(namedElement);
    }


}
