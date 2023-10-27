package hu.blackbelt.judo.psm.generator.jaxrs.impl;

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
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;

import static hu.blackbelt.judo.psm.generator.jaxrs.api.JavaApiHelper.*;
import static hu.blackbelt.judo.psm.generator.jaxrs.api.JavaNamespaceHelper.*;

@TemplateHelper
public class JavaImplHelper extends StaticMethodValueResolver {

    public static synchronized String getImplPrefixLocal() {
        return (String) ThreadLocalContextHolder.getVariable("implPrefix");
    }

    public static String implPackageName() {
        return (getImplPrefixLocal().equals("") ? "" : getImplPrefixLocal() + ".");
    }

    public static String namedElementImplParentPath(NamedElement namedElement) {
        return implPackageName().replaceAll("\\.", "/") + namedElementParentPath(namedElement);
    }

    public static String namedElementImplPackageName(NamedElement namedElement) {
        return implPackageName() + namedElementPackageName(namedElement);
    }

    public static String namedElementImplRestPackageName(NamedElement namedElement) {
        return implPackageName() + REST + "." + namedElementPackageName(namedElement);
    }

    public static String namedElementImplRestParentPath(NamedElement namedElement) {
        return implPackageName().replaceAll("\\.", "/") + REST + "/" + namedElementParentPath(namedElement);
    }

    public static String applicationImplClassName(NamedElement namedElement) {
        return applicationClassName(namedElement) + "Impl";
    }

    public static String implClassName(NamedElement namedElement) {
        return className(namedElement) + "Impl";
    }

    public static String variableName(NamedElement namedElement) {
        return (namedElementPackageName(namedElement) + "_" + implClassName(namedElement)).replaceAll("\\.", "_");
    }

    public static String applicationImplFqName(NamedElement namedElement) {
        return namedElementImplPackageName(namedElement) + "." + applicationImplClassName(namedElement);
    }

    public static String namedElementImplRestFqName(NamedElement namedElement) {
        return namedElementImplRestPackageName(namedElement) + "." + implClassName(namedElement);
    }

}
