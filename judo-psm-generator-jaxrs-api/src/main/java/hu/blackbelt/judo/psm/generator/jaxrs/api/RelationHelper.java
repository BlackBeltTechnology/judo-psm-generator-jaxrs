package hu.blackbelt.judo.psm.generator.jaxrs.api;

import hu.blackbelt.judo.generator.commons.StaticMethodValueResolver;
import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.psm.data.AssociationEnd;
import hu.blackbelt.judo.meta.psm.data.Containment;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.derived.ReferenceAccessor;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferOperation;

import java.util.List;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.psm.generator.jaxrs.api.JavaApiHelper.namedElementApiFqName;
import static hu.blackbelt.judo.psm.generator.jaxrs.api.ModelHelper.isMapped;
import static hu.blackbelt.judo.psm.generator.jaxrs.api.ObjectTypeHelper.getEntity;
import static hu.blackbelt.judo.psm.generator.jaxrs.api.ObjectTypeHelper.isRangeInputType;

@TemplateHelper
public class RelationHelper extends StaticMethodValueResolver {
    public static String decorateWrapperType(TransferObjectRelation transferObjectRelation, String type) {
        String relatedType = type;
        if (transferObjectRelation.isCollection()) {
            relatedType = "java.util.Optional<java.util.List<" + relatedType + ">>";
        } else if (isRelationOptionalType(transferObjectRelation)) {
            relatedType = "java.util.Optional<" + relatedType + ">";
        }
        return relatedType;
    }

    public static String relationTargetTypeDefinitionForCreateRequest(TransferObjectRelation transferObjectRelation) {
        String relatedType = relationTargetBareTypeDefinition(transferObjectRelation);
        if (!transferObjectRelation.getTarget().isQueryCustomizer()) {
            relatedType = relatedType + "CreateRequest";
        } else if (!transferObjectRelation.getTarget().isQueryCustomizer() && !isRangeInputType(transferObjectRelation.getTarget())) {
            relatedType = relatedType + "Request";
        } else if (!transferObjectRelation.getTarget().isQueryCustomizer() && isRangeInputType(transferObjectRelation.getTarget())) {
            relatedType = relatedType + "RangeRequest";
        }
        return decorateWrapperType(transferObjectRelation, relatedType);
    }

    public static String relationTargetTypeDefinitionForRequest(TransferObjectRelation transferObjectRelation) {
        String relatedType = relationTargetBareTypeDefinition(transferObjectRelation);
        if (!transferObjectRelation.getTarget().isQueryCustomizer()) {
            relatedType = relatedType + "Request";
        }
        return decorateWrapperType(transferObjectRelation, relatedType);
    }

    public static String relationTargetTypeDefinitionForRangeRequest(TransferObjectRelation transferObjectRelation) {
        String relatedType = relationTargetBareTypeDefinition(transferObjectRelation);
        if (!transferObjectRelation.getTarget().isQueryCustomizer() && !isRangeInputType(transferObjectRelation.getTarget())) {
            relatedType = relatedType + "Request";
        } else if (!transferObjectRelation.getTarget().isQueryCustomizer() && isRangeInputType(transferObjectRelation.getTarget())) {
            relatedType = relatedType + "RangeRequest";
        }
        return decorateWrapperType(transferObjectRelation, relatedType);
    }

    public static String relationTargetTypeDefinitionForOperationInput(TransferObjectRelation transferObjectRelation) {
        String relatedType = relationTargetBareTypeDefinition(transferObjectRelation) + "OperationInput";
        return decorateWrapperType(transferObjectRelation, relatedType);
    }

    public static String relationTargetTypeDefinitionForResponse(TransferObjectRelation transferObjectRelation) {
        String relatedType = relationTargetBareTypeDefinition(transferObjectRelation) + "Response";
        return decorateWrapperType(transferObjectRelation, relatedType);
    }

    public static String relationTargetBareTypeDefinition(TransferObjectRelation transferObjectRelation) {
        return namedElementApiFqName(transferObjectRelation.getTarget());
    }

    public static boolean isRelationOptionalType(TransferObjectRelation transferObjectRelation) {
        return hu.blackbelt.judo.psm.generator.jaxrs.api.StoredVariableHelper.isGenerateOptionalTypes() && !transferObjectRelation.isRequired();
    }

    public static boolean isParameterizedRelation(TransferObjectRelation transferObjectRelation) {
        return transferObjectRelation.getBinding() != null
                && (transferObjectRelation.getBinding() instanceof ReferenceAccessor)
                && ((ReferenceAccessor) transferObjectRelation.getBinding()).getGetterExpression().getParameterType() != null;
    }

    public static boolean mappedRelationIsAssociation(TransferObjectRelation transferObjectRelation) {
        return transferObjectRelation.getBinding() != null
                && (transferObjectRelation.getBinding() instanceof AssociationEnd);
    }

    /*
    public static List<TransferObjectRelation> allEmbeddedOrMappedRelation(TransferObjectType transferObjectType) {
        return transferObjectType.getRelations().stream()
                .filter(r -> r.isEmbedded()
                        || (r.isEmbedded() && r.getBinding() != null && r.getBinding() instanceof AssociationEnd)
                        || (r.getBinding() != null && r.getBinding() instanceof Containment))
                .collect(Collectors.toList());
    }

     */

    public static List<TransferObjectRelation> allEmbeddedOrMappedRelation(TransferObjectType transferObjectType) {
        return transferObjectType.getRelations().stream()
                .filter(r -> r.isEmbedded()
                        && !isParameterizedRelation(r)
                        && isMapped(r.getTarget()))
                .collect(Collectors.toList());
    }

    public static List<TransferObjectRelation> allEmbeddedOrMappedRelationForCreate(TransferObjectType transferObjectType) {
        return transferObjectType.getRelations().stream()
                .filter(r ->!isParameterizedRelation(r) && (r.isEmbedded()
                        || (r.getBinding() != null && (r.getBinding() instanceof AssociationEnd || r.getBinding() instanceof Containment))))
                .collect(Collectors.toList());
    }
}
