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

import com.github.jknack.handlebars.internal.lang3.StringUtils;
import hu.blackbelt.judo.dao.api.DAO;
import hu.blackbelt.judo.generator.commons.StaticMethodValueResolver;
import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.derived.DataProperty;
import hu.blackbelt.judo.meta.psm.derived.PrimitiveAccessor;
import hu.blackbelt.judo.meta.psm.derived.ReferenceAccessor;
import hu.blackbelt.judo.meta.psm.measure.MeasuredType;
import hu.blackbelt.judo.meta.psm.namespace.*;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;
import hu.blackbelt.judo.meta.psm.type.*;
import hu.blackbelt.judo.psm.generator.jaxrs.api.StoredVariableHelper;
import org.eclipse.emf.common.util.EList;

import java.util.Optional;

import static hu.blackbelt.judo.psm.generator.jaxrs.api.JavaNamespaceHelper.*;

@TemplateHelper
public class JavaApiHelper extends StaticMethodValueResolver {

    public static final String REST = "rest";

    public static String namespaceElementApiFqPath(NamespaceElement namespaceElement) {
        return ((hu.blackbelt.judo.psm.generator.jaxrs.api.StoredVariableHelper.getApiPrefixLocal().equals("")
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

    public static String namedElementApiRestParentPath(NamedElement namedElement) {
        return apiPackageName().replaceAll("\\.", "/" ) + REST + "/" + namedElementParentPath(namedElement);
    }

    public static String applicationClassName(NamedElement namedElement) {
        return StringUtils.capitalize(safeName(namedElement.getName()) + "ApplicationConfig" );
    }

    public static String className(NamedElement namedElement) {
        return StringUtils.capitalize(safeName(namedElement.getName()));
    }

    public static String firstToLower(String input) {
        return StringUtils.uncapitalize(safeName(input));
    }

    public static String applicationFqName(NamedElement namedElement) {
        return namedElementApiPackageName(namedElement) + "." + applicationClassName(namedElement);
    }

    public static String namedElementApiRestFqName(NamedElement namedElement) {
        return apiPackageName() + REST + "." + namedElementPackageName(namedElement) +
                "." + className(namedElement);
    }

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
        return StoredVariableHelper.isGenerateOptionalTypes() && !transferObjectRelation.isRequired();
    }

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

    public static String primitiveDataTypeDefinition(Primitive dataType) {
        if (dataType instanceof StringType) {
            return "java.lang.String";
        } else if (dataType instanceof PasswordType) {
            return "java.lang.String";
        } else if (dataType instanceof BooleanType) {
            return "java.lang.Boolean";
        } else if (dataType instanceof DateType) {
            return "java.time.LocalDate";
        } else if (dataType instanceof TimestampType) {
            return "java.time.LocalDateTime";
        } else if (dataType instanceof TimeType) {
            return "java.time.LocalTime";
        } else if (dataType instanceof NumericType) {
            NumericType numericType = (NumericType) dataType;
            if (numericType.isInteger()) {
                if (numericType.getPrecision() <= 9 && numericType.getPrecision() > 0) {
                    return  "java.lang.Integer";
                } else if (numericType.getPrecision() <= 19 && numericType.getPrecision() > 9) {
                    return  "java.lang.Long";
                } else {
                    return  "java.math.BigDecimal";
                }
            } else if (numericType.isDecimal()) {
                if (numericType.getPrecision() <= 7 && numericType.getPrecision() > 0 && numericType.getScale() <= 4) {
                    return "java.lang.Float";
                } else if (numericType.getPrecision() <= 15 && numericType.getPrecision() > 7 && numericType.getScale() <= 4) {
                    return "java.lang.Double";
                } else {
                    return "java.math.BigDecimal";
                }
            }
        } else if (dataType instanceof EnumerationType) {
            return enumFqName((EnumerationType) dataType);
        } else if (dataType instanceof MeasuredType) {
            if (dataType.isInteger()) {
                return  "java.lang.Integer";
            } else if (dataType.isInteger()) {
                return "java.math.BigDecimal";
            }
        } else if (dataType instanceof BinaryType) {
            return "hu.blackbelt.judo.dispatcher.api.FileType";
        }
        return "java.lang.Object";
    }

    public static boolean isOptionalAttribute(TransferAttribute transferAttribute) {
        return StoredVariableHelper.isGenerateOptionalTypes() && !transferAttribute.isRequired();
    }

    public static String enumFqName(EnumerationType enumerationType) {
        return enumPackageName(enumerationType) + "." + safeName(enumerationType.getName());
    }

    public static String enumPackageName(EnumerationType enumerationType) {
        return apiPackageName() + namedElementPackageName(enumerationType);
    }

    public static String enumName(EnumerationType enumerationType) {
        return safeName(enumerationType.getName());
    }

    public static boolean isParameterizedRelation(TransferObjectRelation transferObjectRelation) {
        return transferObjectRelation.getBinding() != null
                && (transferObjectRelation.getBinding() instanceof ReferenceAccessor)
                && ((ReferenceAccessor) transferObjectRelation.getBinding()).getGetterExpression().getParameterType() != null;
    }

    public static boolean isQueryOrDefaultValueOrNotAttributeAttribute(TransferAttribute transferAttribute) {
        return (isParametrizedAttribute(transferAttribute)
                || hasQueryWithoutParamAnnotation(transferAttribute)
                || hasDefaultValueAnnotation(transferAttribute))
                || isDefaultValueAttribute(transferAttribute)
                || isDataProperty(transferAttribute);
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

    // TODO: add new name
    public static boolean isDataProperty(TransferAttribute transferAttribute) {
        return transferAttribute.getBinding() != null
                && transferAttribute.getBinding() instanceof DataProperty;
    }

    public static String queryCustomizerPackageName(TransferObjectType transferObjectType) {
        return apiPackageName() + namedElementPackageName(transferObjectType);
    }

    public static String classFqName(TransferObjectType transferObjectType) {
        String name = namedElementApiFqName(transferObjectType);
        Optional<Annotation> parameterObject = transferObjectType.getAnnotations().stream()
                .filter(a -> a.getName().equals("ParameterObject")).findFirst();
        if (parameterObject.isPresent()) {
            name = name + "Parameter";
        }

        return safeName(name);
    }

    public static String queryCustomizerName(TransferObjectType transferObjectType) {
        return StringUtils.capitalize(safeName(transferObjectType.getName()));
    }

    public static boolean isSeek(TransferObjectType transferObjectType) {
        return transferObjectType.isQueryCustomizer()
                && transferObjectType.getName().startsWith("_Seek");
    }

}
