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

import hu.blackbelt.judo.generator.commons.ModelCache;
import hu.blackbelt.judo.generator.commons.StaticMethodValueResolver;
import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.psm.accesspoint.AbstractActorType;
import hu.blackbelt.judo.meta.psm.derived.ReferenceAccessor;
import hu.blackbelt.judo.meta.psm.namespace.Model;
import hu.blackbelt.judo.meta.psm.namespace.NamedElement;
import hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferOperation;
import hu.blackbelt.judo.meta.psm.support.PsmModelResourceSupport;
import hu.blackbelt.judo.meta.psm.type.EnumerationType;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.psm.generator.jaxrs.api.ObjectTypeHelper.getRealm;
import static hu.blackbelt.judo.psm.generator.jaxrs.api.ObjectTypeHelper.isRangeInputType;

@TemplateHelper
public class ModelHelper extends StaticMethodValueResolver {

    /**
     * Returns the resource support of the model's ResourceSet. Building it mutates the ResourceSet (URI handlers,
     * resource creation), so it is built once per generation run (see {@link ModelCache}) and always under the
     * class lock.
     */
    public static PsmModelResourceSupport modelWrapper(Model model) {
        Resource resource = model.eResource();
        return cached(model, Arrays.asList("jaxrs.modelWrapper", resource.getURI()), () -> {
            synchronized (ModelHelper.class) {
                return PsmModelResourceSupport.psmModelResourceSupportBuilder()
                        .resourceSet(resource.getResourceSet()).uri(resource.getURI())
                        .build();
            }
        });
    }

    /**
     * Memoizes {@code loader} for the current generation run of the anchor's ResourceSet (see {@link ModelCache});
     * outside a run the loader is called directly. Package-private: not a template helper.
     */
    static <T> T cached(EObject anchor, Object key, Supplier<T> loader) {
        Resource resource = anchor == null ? null : anchor.eResource();
        return ModelCache.get(resource == null ? null : resource.getResourceSet(), key, loader);
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

    public static boolean isDerivedRelation(TransferObjectRelation transferObjectRelation) {
        return transferObjectRelation.getBinding() != null && transferObjectRelation.getBinding() instanceof ReferenceAccessor;
    }

    public static boolean isMapped(TransferObjectType transferObjectType) {
        return (transferObjectType instanceof MappedTransferObjectType);
    }

    public static Set<TransferObjectType> getAllExposedTransferObjectTypesFromAccessPoint(final TransferObjectType accessPoint) {

        final Set<TransferObjectType> foundTransferObjectTypes = new HashSet<>();
        if (!accessPoint.getOperations().isEmpty()) {
            foundTransferObjectTypes.add(accessPoint);
        }
        addTransferObjectTypesExposedByTransferObjectType(accessPoint, foundTransferObjectTypes);
        return inModelPathOrder(foundTransferObjectTypes.stream().filter(t -> !t.getOperations().isEmpty()));
    }

    /**
     * Ordered by model path (names of the containers and the type): the sets are built from {@code HashSet}s of
     * model objects, whose iteration order (identity hashes) changed between runs and reordered the generated
     * application config. The key needs no template context, so it also works in factory expressions.
     */
    private static Set<TransferObjectType> inModelPathOrder(java.util.stream.Stream<TransferObjectType> types) {
        return types.distinct()
                .sorted(java.util.Comparator.comparing(ModelHelper::modelPath))
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private static String modelPath(org.eclipse.emf.ecore.EObject element) {
        StringBuilder path = new StringBuilder();
        for (org.eclipse.emf.ecore.EObject e = element; e != null; e = e.eContainer()) {
            String name = e instanceof hu.blackbelt.judo.meta.psm.namespace.NamedElement n ? n.getName() : e.eClass().getName();
            path.insert(0, "::").insert(0, name);
        }
        return path.toString();
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
        return modelWrapper(model).getStreamOfPsmAccesspointAbstractActorType()
                .flatMap(access -> getAllExposedTransferObjectTypesFromAccessPoint(access).stream())
                .collect(Collectors.collectingAndThen(Collectors.toList(), l -> inModelPathOrder(l.stream())));
    }

    public static List<AbstractActorType> allAccessPointActor(Model model) {
        return modelWrapper(model).getStreamOfPsmAccesspointAbstractActorType().toList();
    }

    public static List<AbstractActorType> allAccessPointActorWithRealm(Model model) {
        return modelWrapper(model)
                .getStreamOfPsmAccesspointAbstractActorType()
                .filter(actorType -> getRealm(actorType) != null)
                .toList();
    }

    public static List<TransferObjectType> allTransferObject(Model model) {
        return modelWrapper(model)
                .getStreamOfPsmServiceTransferObjectType()
                .filter(transferObjectType -> !transferObjectType.isQueryCustomizer() && !isRangeInputType(transferObjectType))
                .collect(Collectors.toList());
    }

    public static List<EnumerationType> allEnumType(Model model) {
        return modelWrapper(model).getStreamOfPsmTypeEnumerationType()
                .collect(Collectors.toList());
    }

    public static List<TransferObjectType> allRange(Model model) {
        return modelWrapper(model).getStreamOfPsmServiceTransferObjectType()
                .filter(transferObjectType -> isRangeInputType(transferObjectType))
                .toList();
    }

    public static List<TransferObjectType> allQueryCustomizer(Model model) {
        return modelWrapper(model)
                .getStreamOfPsmServiceTransferObjectType()
                .filter(transferObjectType -> transferObjectType.isQueryCustomizer())
                .toList();
    }
}
