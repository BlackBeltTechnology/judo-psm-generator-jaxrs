package hu.blackbelt.judo.psm.generator.jackson.osgi;

/*-
 * #%L
 * JUDO PSM Generator Jackson REST Implementation
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
import hu.blackbelt.judo.meta.psm.service.TransferOperation;

import static hu.blackbelt.judo.psm.generator.jackson.api.JavaApiHelper.*;
import static hu.blackbelt.judo.psm.generator.jackson.api.JavaNamespaceHelper.*;

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

    public static String namedElementOsgiFqName(NamedElement namedElement) {
        return namedElementOsgiPackageName(namedElement) + "." + safeName(namedElement.getName());
    }
}
