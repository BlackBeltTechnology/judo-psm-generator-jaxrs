package hu.blackbelt.judo.psm.generator.jaxrs.api;

import hu.blackbelt.judo.generator.commons.StaticMethodValueResolver;
import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.psm.data.AssociationEnd;
import hu.blackbelt.judo.meta.psm.derived.ReferenceAccessor;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferOperation;

import java.util.List;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.psm.generator.jaxrs.api.JavaApiHelper.namedElementApiFqName;
import static hu.blackbelt.judo.psm.generator.jaxrs.api.ModelHelper.isMapped;

@TemplateHelper
public class RelationHelper extends StaticMethodValueResolver {
    public static String relationTargetTypeDefinition(TransferObjectRelation transferObjectRelation) {
        String relatedType = relationTargetBareTypeDefinition(transferObjectRelation);
        if (relatedType.contains("._optional_transferobjecttypes")) {
            relatedType = relatedType.replaceAll("._optional_transferobjecttypes", "");
        }
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

    public static List<TransferOperation> allOperations(TransferObjectType transferObjectType) {
        return transferObjectType.getOperations().stream()
                .filter(o -> o.getBehaviour() == null)
                .collect(Collectors.toList());
    }

    public static List<TransferObjectRelation> allEmbeddedMappedRelation(TransferObjectType transferObjectType) {
        return transferObjectType.getRelations().stream()
                .filter(r -> r.isEmbedded()
                        && !isParameterizedRelation(r)
                        && isMapped(r.getTarget()))
                .collect(Collectors.toList());
    }

    public static List<TransferObjectRelation> allRelation(TransferObjectType transferObjectType) {
        return transferObjectType.getRelations();
    }

    public static List<TransferObjectRelation> allSingleEmbeddedMappedRelation(TransferObjectType transferObjectType) {
        return transferObjectType.getRelations().stream()
                .filter(r -> r.isEmbedded()
                        && !isParameterizedRelation(r)
                        && isMapped(r.getTarget())
                        && !r.isCollection())
                .collect(Collectors.toList());
    }

    public static boolean relationIsEmbeddedAndMappedRelationIsAssociation(TransferObjectRelation transferObjectRelation) {
        return transferObjectRelation.isEmbedded()
                && (transferObjectRelation.getBinding() instanceof AssociationEnd);
    }

}
