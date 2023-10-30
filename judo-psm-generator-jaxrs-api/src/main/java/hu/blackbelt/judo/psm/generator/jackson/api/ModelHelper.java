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

import hu.blackbelt.judo.generator.commons.StaticMethodValueResolver;
import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.psm.PsmUtils;
import hu.blackbelt.judo.meta.psm.accesspoint.AbstractActorType;
import hu.blackbelt.judo.meta.psm.accesspoint.ActorType;
import hu.blackbelt.judo.meta.psm.accesspoint.MappedActorType;
import hu.blackbelt.judo.meta.psm.data.BoundOperation;
import hu.blackbelt.judo.meta.psm.derived.ReferenceAccessor;
import hu.blackbelt.judo.meta.psm.derived.StaticData;
import hu.blackbelt.judo.meta.psm.derived.StaticNavigation;
import hu.blackbelt.judo.meta.psm.namespace.*;
import hu.blackbelt.judo.meta.psm.service.*;
import hu.blackbelt.judo.meta.psm.support.PsmModelResourceSupport;
import hu.blackbelt.judo.meta.psm.type.*;
import jdk.dynalink.Operation;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.blackbelt.judo.psm.generator.jaxrs.api.JavaNamespaceHelper.*;

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

    public static Set<TransferObjectType> getAllExposedTransferObjectTypesFromAccessPointWithOperation(
            final TransferObjectType accessPoint) {

        final Set<TransferObjectType> foundTransferObjectTypes = new HashSet<>();
        if (!accessPoint.getOperations().isEmpty()) {
            foundTransferObjectTypes.add(accessPoint);
        }
        addExposedTransferObjectTypesWithOperation(accessPoint, foundTransferObjectTypes);
        return foundTransferObjectTypes;
    }

    private static void addExposedTransferObjectTypesWithOperation(final TransferObjectType type,
                                                                   final Set<TransferObjectType> foundTransferObjectTypes) {
        final Set<TransferObjectType> newTransferObjectTypes =
                type.getRelations().stream()
                        .map(TransferObjectRelation::getTarget).filter(t -> !foundTransferObjectTypes.contains(t))
                        .collect(Collectors.toSet());
        foundTransferObjectTypes.addAll(newTransferObjectTypes.stream().filter(t -> !t.getOperations().isEmpty()).collect(Collectors.toSet()));
        newTransferObjectTypes.forEach(t -> addExposedTransferObjectTypesWithOperation(t, foundTransferObjectTypes));
    }

    public static Set<TransferOperation> getAllExposedOperationsFromAccessPoint(
            final TransferObjectType accessPoint) {
        Set<TransferObjectType> types = getAllExposedTransferObjectTypesFromAccessPointWithOperation(accessPoint);
        return types.stream().flatMap(t -> getAllOperations(t).stream()).collect(Collectors.toSet());
    }

    public static boolean isDerivedRelation(TransferObjectRelation transferObjectRelation) {
        return transferObjectRelation.getBinding() != null && transferObjectRelation.getBinding() instanceof ReferenceAccessor;
    }

    public static boolean isMapped(TransferObjectType transferObjectType) {
        return (transferObjectType instanceof MappedTransferObjectType);
    }

    public static Set<TransferObjectType> getAllExposedTransferObjectTypesFromAccessPoint(
            final TransferObjectType accessPoint) {

        final Set<TransferObjectType> foundTransferObjectTypes = new HashSet<>();
        if (!accessPoint.getOperations().isEmpty()) {
            foundTransferObjectTypes.add(accessPoint);
        }
        addTransferObjectTypesExposedByTransferObjectType(accessPoint, foundTransferObjectTypes);
        return foundTransferObjectTypes.stream().filter(t -> !t.getOperations().isEmpty()).collect(Collectors.toSet());
    }

    private static void addTransferObjectTypesExposedByTransferObjectType(final TransferObjectType type,
                                                                          final Set<TransferObjectType> foundTransferObjectTypes) {
        final Set<TransferObjectType> newTransferObjectTypes = type.getRelations().stream()
                .filter(r -> !(!isMapped(r.getTarget()) && !r.isEmbedded() && isDerivedRelation(r)))
                .map(r -> (TransferObjectType) r.getTarget())
                .filter(t -> !foundTransferObjectTypes.contains(t))
                .collect(Collectors.toSet());
        foundTransferObjectTypes.addAll(newTransferObjectTypes);

        final Set<TransferObjectType> newTransferObjectTypesFromOperationsInput =
                type.getOperations().stream()
                        .filter(o -> o.getInput() != null)
                        .map(o -> (TransferObjectType) o.getInput().getType())
                        .filter(t -> !foundTransferObjectTypes.contains(t))
                        .collect(Collectors.toSet());
        newTransferObjectTypes.addAll(newTransferObjectTypesFromOperationsInput);
        foundTransferObjectTypes.addAll(newTransferObjectTypesFromOperationsInput);

        final Set<TransferObjectType> newTransferObjectTypesFromOperationsOutput =
                type.getOperations().stream()
                        .filter(o -> o.getOutput() != null)
                        .map(o -> (TransferObjectType) o.getOutput().getType())
                        .filter(t -> !foundTransferObjectTypes.contains(t))
                        .collect(Collectors.toSet());
        newTransferObjectTypes.addAll(newTransferObjectTypesFromOperationsOutput);
        foundTransferObjectTypes.addAll(newTransferObjectTypesFromOperationsOutput);

        newTransferObjectTypes.forEach(t -> addTransferObjectTypesExposedByTransferObjectType(t, foundTransferObjectTypes));
    }

    public static EList<TransferOperation> getAllOperations(final TransferObjectType transferObjectType) {
        final EList<TransferOperation> operationsCollected = new BasicEList<>();
        operationsCollected.addAll(transferObjectType.getOperations());
        return operationsCollected;
    }

    public static Set<TransferObjectType> allExposedTransferObjectWithOperation(Model model) {
        return modelWrapper(model).getStreamOfPsmAccesspointAbstractActorType().flatMap(access -> getAllExposedTransferObjectTypesFromAccessPoint(access).stream()).collect(Collectors.toSet());
    }

    public static List<AbstractActorType> allAccessPointActor(Model model) {
        return modelWrapper(model).getStreamOfPsmAccesspointAbstractActorType().toList();
    }

}
