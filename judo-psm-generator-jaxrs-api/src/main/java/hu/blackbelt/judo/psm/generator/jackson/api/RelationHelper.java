package hu.blackbelt.judo.psm.generator.jaxrs.api;

import hu.blackbelt.judo.generator.commons.StaticMethodValueResolver;
import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.psm.data.AssociationEnd;
import hu.blackbelt.judo.meta.psm.data.Containment;
import hu.blackbelt.judo.meta.psm.derived.ReferenceAccessor;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferOperation;

import java.util.List;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.psm.generator.jaxrs.api.JavaApiHelper.namedElementApiFqName;
import static hu.blackbelt.judo.psm.generator.jaxrs.api.ModelHelper.isMapped;
import static hu.blackbelt.judo.psm.generator.jaxrs.api.ObjectTypeHelper.isRangeInputType;

@TemplateHelper
public class RelationHelper extends StaticMethodValueResolver {
    public static String relationTargetTypeDefinition(TransferObjectRelation transferObjectRelation, boolean stored) {
        String relatedType = relationTargetBareTypeDefinition(transferObjectRelation);
        if (!stored && !transferObjectRelation.getTarget().isQueryCustomizer() && !isRangeInputType(transferObjectRelation.getTarget())) {
            relatedType = relatedType + "NotStored";
        }
        if (transferObjectRelation.isCollection()) {
            relatedType = "java.util.List<" + relatedType + ">";
        } else if (isRelationOptionalType(transferObjectRelation)) {
            relatedType = "java.util.Optional<" + relatedType + ">";
        }
        return relatedType;
    }

    public static String relationTargetTypeDefinitionForResponse(TransferObjectRelation transferObjectRelation) {
        String relatedType = relationTargetBareTypeDefinition(transferObjectRelation) + "ForResponse";
        if (transferObjectRelation.isCollection()) {
            relatedType = "java.util.List<" + relatedType + ">";
        } else if (isRelationOptionalType(transferObjectRelation)) {
            relatedType = "java.util.Optional<" + relatedType + ">";
        }
        return relatedType;
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

    public static List<TransferObjectRelation> allEmbeddedOrMappedRelation(TransferObjectType transferObjectType) {
        return transferObjectType.getRelations().stream()
                .filter(r -> r.isEmbedded() || (r.getBinding() != null && (r.getBinding() instanceof AssociationEnd || r.getBinding() instanceof Containment)))
                .collect(Collectors.toList());
    }
}
