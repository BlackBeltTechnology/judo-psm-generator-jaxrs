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
import hu.blackbelt.judo.generator.commons.ThreadLocalContextHolder;
import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.psm.PsmUtils;
import hu.blackbelt.judo.meta.psm.derived.PrimitiveAccessor;
import hu.blackbelt.judo.meta.psm.derived.StaticData;
import hu.blackbelt.judo.meta.psm.derived.StaticNavigation;
import hu.blackbelt.judo.meta.psm.measure.MeasuredType;
import hu.blackbelt.judo.meta.psm.namespace.*;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferOperation;
import hu.blackbelt.judo.meta.psm.type.*;

import java.util.*;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.psm.generator.jackson.api.JavaNamespaceHelper.*;
import static hu.blackbelt.judo.psm.generator.jackson.api.ModelHelper.*;

@TemplateHelper
public class JavaApiHelper extends StaticMethodValueResolver {

    public static final String REST = "rest";

    public static String namespaceElementApiFqPath(NamespaceElement namespaceElement) {
        return ((StoredVariableHelper.getApiPrefixLocal().equals("")
                    ? ""
                    : StoredVariableHelper.getApiPrefixLocal() + ".")).replaceAll(".", "/")
                + fqName(namespaceElement.getNamespace(), "/", true);
    }

    public static String apiPackageName() {
        return (StoredVariableHelper.getApiPrefixLocal().equals("")
                ? ""
                : StoredVariableHelper.getApiPrefixLocal() + ".");
    }

    public static String namedElementApiPackageName(NamedElement namedElement) {
        return apiPackageName() + namedElementPackageName(namedElement);
    }

    public static String namedElementApiFqName(NamedElement namedElement) {
        return apiPackageName() + namedElementPackageName(namedElement) +
                "." + safeNamedElementOriginalNameForClassNames(namedElement);
    }

    public static String namedElementApiParentPath(NamedElement namedElement) {
        return apiPackageName().replaceAll("\\.", "/" ) + namedElementParentPath(namedElement);
    }

    public static String namedElementApiRestPackageName(NamedElement namedElement) {
        return apiPackageName() + REST + "." + namedElementPackageName(namedElement);
    }

    public static String namedElementApiRestFqName(NamedElement namedElement) {
        return apiPackageName() + REST + "." + namedElementPackageName(namedElement) +
                "." + safeNamedElementOriginalNameForClassNames(namedElement);
    }

    public static String namedElementApiRestParentPath(NamedElement namedElement) {
        return apiPackageName().replaceAll("\\.", "/" ) + REST + "/" + namedElementParentPath(namedElement);
    }

    public static String applicationClassName(TransferObjectType transferObjectType) {
        return StringUtils.capitalize(safeName(transferObjectType.getName()) + "ApplicationConfig" );
    }

    public static String className(TransferObjectType transferObjectType) {
        return StringUtils.capitalize(safeName(transferObjectType.getName()));
    }

    public static String applicationFqName(TransferObjectType transferObjectType) {
        return namedElementApiPackageName(transferObjectType) + "." + applicationClassName(transferObjectType);
    }

}
