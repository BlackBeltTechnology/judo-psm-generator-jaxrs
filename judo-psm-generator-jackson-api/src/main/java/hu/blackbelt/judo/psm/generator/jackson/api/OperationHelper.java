package hu.blackbelt.judo.psm.generator.jackson.api;

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


import hu.blackbelt.judo.generator.commons.StaticMethodValueResolver;
import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;

import hu.blackbelt.judo.meta.psm.PsmUtils;
import hu.blackbelt.judo.meta.psm.namespace.Model;
import hu.blackbelt.judo.meta.psm.namespace.NamedElement;
import hu.blackbelt.judo.meta.psm.namespace.Namespace;
import hu.blackbelt.judo.meta.psm.service.*;
import hu.blackbelt.judo.meta.psm.type.Cardinality;

import static hu.blackbelt.judo.psm.generator.jackson.api.JavaNamespaceHelper.*;
import static hu.blackbelt.judo.psm.generator.jackson.api.ObjectTypeHelper.getEntity;
import static hu.blackbelt.judo.psm.generator.jackson.api.ObjectTypeHelper.isEntity;


@TemplateHelper
public class OperationHelper extends StaticMethodValueResolver {

    public static Boolean hasReturn(TransferOperation transferOperation) {
        return transferOperation.getOutput() != null;
    }

    public static String operationName(TransferOperation transferOperation) {
        return transferOperation.getName();
    }

    public static Boolean isBoundOperation(TransferOperation transferOperation) {
        return transferOperation instanceof BoundTransferOperation;
    }

    public static Boolean isCustomOperation(TransferOperation transferOperation) {
        return transferOperation.getImplementation() != null &&
                transferOperation.getImplementation().isCustomImplementation();
    }

    public static Boolean hasParam(TransferOperation transferOperation) {
        return transferOperation.getInput() != null;
    }

    public static boolean hasFaults(TransferOperation transferOperation) {
        return transferOperation.getFaults().size() > 0;
    }

    public static String operationAsmFqName(TransferOperation transferOperation) {
        TransferObjectType transferObjectType = (TransferObjectType) transferOperation.eContainer();
        NamedElement namedElement = transferObjectType;
        if (isEntity(transferObjectType) && isBoundOperation(transferOperation)) {
            namedElement = getEntity(transferObjectType);
        }
        return fqName((Namespace) namedElement.eContainer(), ".", false) + '.' + namedElement.getName() + "#" + transferOperation.getName();
    }

    public static Boolean isStateful(TransferOperation transferOperation) {
        if (transferOperation.getImplementation() != null) {
            return transferOperation.getImplementation().isStateful();
        }
        if (transferOperation.getBehaviour() == null) {
            return true;
        }
        if (transferOperation.getBehaviour() != null) {
            TransferOperationBehaviourType behaviourType = transferOperation.getBehaviour().getBehaviourType();
            if (behaviourType == TransferOperationBehaviourType.VALIDATE_CREATE) {
                return false;
            } else if (behaviourType == TransferOperationBehaviourType.VALIDATE_UPDATE) {
                return false;
            } else if (behaviourType == TransferOperationBehaviourType.LIST) {
                return false;
            } else if (behaviourType == TransferOperationBehaviourType.GET_RANGE) {
                return false;
            } else if (behaviourType == TransferOperationBehaviourType.GET_TEMPLATE) {
                return false;
            } else if (behaviourType == TransferOperationBehaviourType.GET_PRINCIPAL) {
                return false;
            } else if (behaviourType == TransferOperationBehaviourType.GET_METADATA) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static Boolean isStateless(TransferOperation transferOperation) {
        return !isStateful(transferOperation);
    }

    public static Boolean isStatelessAndHasNoInputParameter(TransferOperation transferOperation) {
        return isStateless(transferOperation) && transferOperation.getInput() == null;
    }

    public static String toJAXRSPath(TransferOperation transferOperation, Model model) {
        String path = operationAsmFqName(transferOperation);
        TransferOperationBehaviour behaviour = transferOperation.getBehaviour();

        if (behaviour != null && behaviour.getBehaviourType() != null) {
            path = getJAXRSPath(transferOperation, behaviour.getBehaviourType(), behaviour.getOwner(), path);
        }

        return path.replaceAll("^" + model.getName() + "\\.", "").replaceAll("\\.", "/").replaceAll("#", "/").replaceAll("_default_transferobjecttypes/", "");
    }

    private static String getJAXRSPath(TransferOperation transferOperation, TransferOperationBehaviourType behaviourType, NamedElement owner, String path) {
        if (behaviourType == TransferOperationBehaviourType.LIST) {
            if (((TransferObjectRelation) owner).getCardinality().getUpper() == -1) {
                path = relationAsmFqName((TransferObjectRelation) owner) + "/~list";
            } else {
                path = relationAsmFqName((TransferObjectRelation) owner) + "/~get";
            }
        } else if (behaviourType == TransferOperationBehaviourType.CREATE_INSTANCE) {
            if (isBoundOperation(transferOperation)) {
                path = classifierAsmFqName((TransferObjectType) owner.eContainer()) + "/~update/" + owner.getName() + "/~create";
            } else {
                path = relationAsmFqName((TransferObjectRelation) owner) + "/~create/";
            }
        } else if (behaviourType == TransferOperationBehaviourType.VALIDATE_CREATE) {
            if (isBoundOperation(transferOperation)) {
                path = classifierAsmFqName((TransferObjectType) owner.eContainer()) + "/~update/" + owner.getName() + "/~validate";
            } else {
                path = relationAsmFqName((TransferObjectRelation) owner) + "/~validate/";
            }
        } else if (behaviourType == TransferOperationBehaviourType.REFRESH) {
            path = classifierAsmFqName((TransferObjectType) owner) + "/~get/";
        } else if (behaviourType == TransferOperationBehaviourType.UPDATE_INSTANCE) {
            path = classifierAsmFqName((TransferObjectType) owner) + "/~update/";
        } else if (behaviourType == TransferOperationBehaviourType.VALIDATE_UPDATE) {
            path = classifierAsmFqName((TransferObjectType) owner) + "/~validate/";
        } else if (behaviourType == TransferOperationBehaviourType.DELETE_INSTANCE) {
            path = classifierAsmFqName((TransferObjectType) owner) + "/~delete/";
        } else if (behaviourType == TransferOperationBehaviourType.SET_REFERENCE) {
            path = classifierAsmFqName((TransferObjectType) owner.eContainer()) + "/~update/" + owner.getName() + "/~set";
        } else if (behaviourType == TransferOperationBehaviourType.UNSET_REFERENCE) {
            path = classifierAsmFqName((TransferObjectType) owner.eContainer()) + "/~update/" + owner.getName() + "/~unset";
        } else if (behaviourType == TransferOperationBehaviourType.ADD_REFERENCE) {
            path = classifierAsmFqName((TransferObjectType) owner.eContainer()) + "/~update/" + owner.getName() + "/~add";
        } else if (behaviourType == TransferOperationBehaviourType.REMOVE_REFERENCE) {
            path = classifierAsmFqName((TransferObjectType) owner.eContainer()) + "/~update/" + owner.getName() + "/~remove";
        } else if (behaviourType == TransferOperationBehaviourType.GET_RANGE) {
            if (owner instanceof TransferObjectRelation) {
                path = relationAsmFqName((TransferObjectRelation) owner) + "/~range/";
            } else {
                path = operationAsmFqName((TransferOperation) owner) + "/~range/";
            }
        } else if (behaviourType == TransferOperationBehaviourType.GET_TEMPLATE) {
            path = classifierAsmFqName((TransferObjectType) owner) + "/~template/";
        } else if (behaviourType == TransferOperationBehaviourType.GET_PRINCIPAL) {
            path = classifierAsmFqName((TransferObjectType) owner) + "/~principal/";
        } else if (behaviourType == TransferOperationBehaviourType.GET_METADATA) {
            path = classifierAsmFqName((TransferObjectType) owner) + "/~meta/";
        } else if (behaviourType == TransferOperationBehaviourType.GET_UPLOAD_TOKEN) {
            path = attributeAsmFqName((TransferAttribute) owner) + "/~upload-token/";
        }
        return path;
    }

    public static Boolean isMany(Parameter parameter) {
        return parameter.getCardinality().getUpper() == -1;
    }

    public static Boolean operationOutputTypeDefined(TransferOperation transferOperation) {
        if (!hasReturn(transferOperation)) {
            return false;
        }
        return transferOperation.getOutput().getType() != null;
    }

}
