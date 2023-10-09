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

import hu.blackbelt.judo.generator.commons.StaticMethodValueResolver;
import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.psm.derived.StaticData;
import hu.blackbelt.judo.meta.psm.derived.StaticNavigation;
import hu.blackbelt.judo.meta.psm.namespace.*;
import hu.blackbelt.judo.meta.psm.service.*;
import hu.blackbelt.judo.meta.psm.support.PsmModelResourceSupport;
import hu.blackbelt.judo.meta.psm.type.*;
import org.eclipse.emf.ecore.EObject;

import java.util.*;
import java.util.stream.Collectors;

@TemplateHelper
public class ModelHelper extends StaticMethodValueResolver {

    public synchronized static PsmModelResourceSupport modelWrapper(Model model) {
        PsmModelResourceSupport modelWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(model.eResource().getResourceSet()).uri(model.eResource().getURI())
                .build();
        return modelWrapper;
    }

    @SuppressWarnings("unchecked")
    public static <T extends EObject> T getSpecifiedContainer(EObject object, Class<T> clazz) {
        EObject root = object;
        while (root.eContainer() != null) {
            if (clazz.isAssignableFrom(root.getClass())) {
                return (T) root;
            }
            root = root.eContainer();
        }
        if (clazz.isAssignableFrom(root.getClass())) {
            return (T) root;
        }
        return null;
    }

    public static List<TransferObjectType> allUnmappedTransferObjectType(Model model) {
        return modelWrapper(model).getStreamOfPsmServiceUnmappedTransferObjectType().collect(Collectors.toList());
    }

    public static List<TransferObjectType> allTransferObjectType(Model model) {
        return modelWrapper(model).getStreamOfPsmServiceTransferObjectType().collect(Collectors.toList());
    }


    public static List<StaticNavigation> allStaticNavigation(Model model) {
        return modelWrapper(model).getStreamOfPsmDerivedStaticNavigation()
                .collect(Collectors.toList());
    }

    public static List<StaticData> allStaticData(Model model) {
        return modelWrapper(model).getStreamOfPsmDerivedStaticData()
                .collect(Collectors.toList());
    }

    public static List<EnumerationType> allEnumType(Model model) {
        return modelWrapper(model).getStreamOfPsmTypeEnumerationType()
                .collect(Collectors.toList());
    }

    public static String documentation(NamedElement namedElement) {
        return namedElement.getDocumentation();
    }

    public static boolean hasDocumentation(NamedElement namedElement) {
        if (namedElement == null) {
            return false;
        }
        return namedElement.getDocumentation() != null &&
                namedElement.getDocumentation().trim().length() > 0;
    }

    public static String jclImplementation(TransferOperation transferOperation) {
        return transferOperation.getImplementation().getBody();
    }

    public static boolean hasJclImplementation(TransferOperation operation) {
        if (operation == null) {
            return false;
        }

        return operation.getImplementation().getBody() != null &&
                operation.getImplementation().getBody().trim().length() > 0;
    }

    public static List<TransferObjectType> allMappedTransferObjectType(Model model) {
        return modelWrapper(model).getStreamOfPsmServiceMappedTransferObjectType().collect(Collectors.toList());
    }

    public static List<TransferOperation> allCustomizableOperations(Model model) {
        return modelWrapper(model).getStreamOfPsmServiceTransferOperation()
                .filter(o -> o.getBehaviour() == null)
                .collect(Collectors.toList());
    }

}
