package hu.blackbelt.judo.psm.generator.jaxrs.api;

import hu.blackbelt.judo.generator.commons.StaticMethodValueResolver;
import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.psm.derived.DataProperty;
import hu.blackbelt.judo.meta.psm.derived.PrimitiveAccessor;
import hu.blackbelt.judo.meta.psm.namespace.Annotation;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import org.eclipse.emf.common.util.EList;

import static hu.blackbelt.judo.psm.generator.jaxrs.api.JavaApiHelper.*;

@TemplateHelper
public class AttributeHelper extends StaticMethodValueResolver {
    public static String attributeTargetTypeDefinition(TransferAttribute transferAttribute) {
        String relatedType = attributeTargetBareTypeDefinition(transferAttribute);
        if (isOptionalAttribute(transferAttribute)) {
            relatedType = "java.util.Optional<" + relatedType + ">";
        }
        return relatedType;
    }

    public static String attributeTargetBareTypeDefinition(TransferAttribute transferAttribute) {
        return primitiveDataTypeDefinition(transferAttribute.getDataType());
    }

    public static boolean isOptionalAttribute(TransferAttribute transferAttribute) {
        return hu.blackbelt.judo.psm.generator.jaxrs.api.StoredVariableHelper.isGenerateOptionalTypes() && !transferAttribute.isRequired();
    }


    public static boolean isQueryOrDefaultValueOrDataPropertyAttribute(TransferAttribute transferAttribute) {
        return (isParametrizedAttribute(transferAttribute)
                || hasQueryWithoutParamAnnotation(transferAttribute)
                || hasDefaultValueAnnotation(transferAttribute))
                || isDefaultValueAttribute(transferAttribute)
                || isDataProperty(transferAttribute);
    }

    public static boolean isRequestAttribute(TransferAttribute transferAttribute) {
        return (isParametrizedAttribute(transferAttribute)
                || hasQueryWithoutParamAnnotation(transferAttribute)
                || hasDefaultValueAnnotation(transferAttribute))
                || isDefaultValueAttribute(transferAttribute);
    }

    public static boolean isResponseAttribute(TransferAttribute transferAttribute) {
        return (isParametrizedAttribute(transferAttribute)
                || hasDefaultValueAnnotation(transferAttribute))
                || isDefaultValueAttribute(transferAttribute);
    }

    public static boolean isParametrizedAttribute(TransferAttribute transferAttribute) {
        return transferAttribute.getBinding() != null
                && (transferAttribute.getBinding() instanceof PrimitiveAccessor)
                && ((PrimitiveAccessor) transferAttribute.getBinding()).getGetterExpression().getParameterType() != null;
    }

    public static boolean hasQueryWithoutParamAnnotation(TransferAttribute transferAttribute) {
        return transferAttribute.getBinding() != null
                && (transferAttribute.getBinding() instanceof PrimitiveAccessor)
                && hasAnnotation(transferAttribute.getBinding().getAnnotations(), "QueryWithoutParameter");
    }

    public static boolean isDefaultValueAttribute(TransferAttribute transferAttribute) {
        return transferAttribute.getName().contains("_default_");
    }

    public static boolean hasDefaultValueAnnotation(TransferAttribute transferAttribute) {
        return transferAttribute.getBinding() != null
                && (transferAttribute.getBinding() instanceof PrimitiveAccessor)
                && hasAnnotation(transferAttribute.getBinding().getAnnotations(), "DefaultValue")
                || hasAnnotation(transferAttribute.getAnnotations(), "TransferObjectAttributeWithDefaultValue");
    }

    public static boolean hasAnnotation(EList<Annotation> annotations, String name){
        return annotations.stream().anyMatch(ann -> name.equals(ann.getName()));
    }
    
    public static boolean isDataProperty(TransferAttribute transferAttribute) {
        return transferAttribute.getBinding() != null
                && transferAttribute.getBinding() instanceof DataProperty;
    }

}
