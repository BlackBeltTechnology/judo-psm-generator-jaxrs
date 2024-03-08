package hu.blackbelt.judo.psm.generator.jaxrs.api;

/*-
 * #%L
 * JUDO PSM Generator SDK Core API
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
import hu.blackbelt.judo.meta.psm.accesspoint.AbstractActorType;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.namespace.Model;
import hu.blackbelt.judo.meta.psm.namespace.NamedElement;
import hu.blackbelt.judo.meta.psm.service.*;
import hu.blackbelt.judo.psm.generator.jaxrs.api.OperationHelper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static hu.blackbelt.judo.psm.generator.jaxrs.api.JavaNamespaceHelper.logicalFullName;
import static hu.blackbelt.judo.psm.generator.jaxrs.api.JavaNamespaceHelper.safeName;
import static hu.blackbelt.judo.psm.generator.jaxrs.api.ModelHelper.*;

@TemplateHelper
public class ObjectTypeHelper extends StaticMethodValueResolver {

    @Override
    public Object resolve(final Object context) {
        return UNRESOLVED;
    }

    @Override
    public Set<Map.Entry<String, Object>> propertySet(Object context) {
        return new HashSet<>();
    }

    public static boolean isActorType(TransferObjectType transferObjectType) {
        return transferObjectType instanceof AbstractActorType;
    }

    public static String getRealm(AbstractActorType actorType) {
        return actorType.getRealm();
    }

    public static EntityType getEntity(TransferObjectType transferObjectType) {
        Model model = getSpecifiedContainer(transferObjectType, Model.class);
        return modelWrapper(model).getStreamOfPsmDataEntityType()
                .filter(e -> e.getDefaultRepresentation() == transferObjectType).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Entity representation not found: " + transferObjectType.getName()));
    }

    public static boolean isEntity(TransferObjectType transferObjectType) {
        Model model = getSpecifiedContainer(transferObjectType, Model.class);
        return modelWrapper(model).getStreamOfPsmDataEntityType()
                .filter(e -> e.getDefaultRepresentation() == transferObjectType).findFirst().isPresent();
    }

    public static boolean hasCustomOperation(TransferObjectType transferObjectType) {
        return transferObjectType.getOperations().stream().filter(OperationHelper::isCustomOperation).count() > 0;
    }

    public static boolean isRangeInputType(TransferObjectType transferObjectType) {
        Model model = getSpecifiedContainer(transferObjectType, Model.class);
        return modelWrapper(model)
                .getStreamOfPsmServiceTransferOperation()
                .anyMatch(o ->
                        o.getBehaviour() != null
                                && o.getBehaviour().getBehaviourType() == TransferOperationBehaviourType.GET_RANGE
                                && o.getInput() != null
                                && o.getInput().getType().equals(transferObjectType)
                );
    }

    public static String classNameForCreateRequestPostfix(TransferObjectType transferObjectType) {
        if (!transferObjectType.isQueryCustomizer() && !isRangeInputType(transferObjectType)) {
            return "CreateRequest";
        } else if (!transferObjectType.isQueryCustomizer()) {
            return "Request";
        }
        return "";
    }

    public static String classNameForRangeRequestPostfix(TransferObjectType transferObjectType) {
        if (!transferObjectType.isQueryCustomizer()) {
            return "RangeRequest";
        } else if (transferObjectType.isQueryCustomizer()) {
            return "Request";
        }
        return "";
    }

    public static String classNameForRequestPostfix(TransferObjectType transferObjectType) {
        if (!transferObjectType.isQueryCustomizer()) {
            return  "Request";
        }
        return "";
    }

    public static String classNameForResponsePostfix(TransferObjectType transferObjectType) {
        return  "Response";
    }

    public static String classNameForCreateRequest(TransferObjectType transferObjectType) {
        return StringUtils.capitalize(safeName(transferObjectType.getName()) + classNameForCreateRequestPostfix(transferObjectType));
    }

    public static String classNameForRangeRequest(TransferObjectType transferObjectType) {
        return StringUtils.capitalize(safeName(transferObjectType.getName() + classNameForRangeRequestPostfix(transferObjectType)));
    }

    public static String classNameForRequest(TransferObjectType transferObjectType) {
        return StringUtils.capitalize(safeName(transferObjectType.getName() + classNameForRequestPostfix(transferObjectType)));
    }

    public static String classNameForResponse(TransferObjectType transferObjectType) {
        return StringUtils.capitalize(safeName(transferObjectType.getName() + classNameForResponsePostfix(transferObjectType)));
    }

    public static String classNameLogicalFullNameForRangeRequest(TransferObjectType transferObjectType) {
        return logicalFullName(transferObjectType) + classNameForRangeRequestPostfix(transferObjectType);
    }

    public static String classNameLogicalFullNameForRequest(TransferObjectType transferObjectType) {
        return logicalFullName(transferObjectType) + classNameForRequestPostfix(transferObjectType);
    }

    public static String classNameLogicalFullNameForCreateRequest(TransferObjectType transferObjectType) {
        return logicalFullName(transferObjectType) + classNameForCreateRequestPostfix(transferObjectType);
    }

    public static String classNameLogicalFullNameForResponse(TransferObjectType transferObjectType) {
        return logicalFullName(transferObjectType) + classNameForResponsePostfix(transferObjectType);
    }

}
