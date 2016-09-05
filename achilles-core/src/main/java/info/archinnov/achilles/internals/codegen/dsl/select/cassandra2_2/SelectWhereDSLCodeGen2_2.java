/*
 * Copyright (C) 2012-2016 DuyHai DOAN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.archinnov.achilles.internals.codegen.dsl.select.cassandra2_2;

import static info.archinnov.achilles.internals.parser.TypeUtils.*;
import static info.archinnov.achilles.internals.parser.TypeUtils.ABSTRACT_SELECT_WHERE_JSON;
import static info.archinnov.achilles.internals.utils.NamingHelper.upperCaseFirst;

import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Modifier;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import info.archinnov.achilles.internals.codegen.dsl.AbstractDSLCodeGen;
import info.archinnov.achilles.internals.codegen.dsl.select.SelectWhereDSLCodeGen;
import info.archinnov.achilles.internals.codegen.meta.EntityMetaCodeGen;
import info.archinnov.achilles.internals.parser.context.GlobalParsingContext;

public class SelectWhereDSLCodeGen2_2 extends SelectWhereDSLCodeGen {

    @Override
    public List<TypeSpec> augmentSelectClass(GlobalParsingContext context,
                                             EntityMetaCodeGen.EntityMetaSignature signature,
                                             List<FieldSignatureInfo> partitionKeys,
                                             List<FieldSignatureInfo> clusteringCols) {
        final ClassSignatureParams jsonClassSignatureParams = ClassSignatureParams.of(SELECT_DSL_SUFFIX,
                SELECT_WHERE_JSON_DSL_SUFFIX, SELECT_END_JSON_DSL_SUFFIX,
                ABSTRACT_SELECT_WHERE_PARTITION_JSON, ABSTRACT_SELECT_WHERE_JSON);

        final Optional<FieldSignatureInfo> firstClustering = clusteringCols.stream().limit(1).findFirst();

        return buildWhereClassesInternal(signature, context.selectWhereDSLCodeGen(),
                partitionKeys, clusteringCols,
                firstClustering, jsonClassSignatureParams);

    }

    @Override
    public void augmentRelationClassForWhereClause(TypeSpec.Builder relationClassBuilder, FieldSignatureInfo fieldInfo,
                                                   ClassSignatureInfo nextSignature) {
        final String methodName = "Eq_FromJson";
        final MethodSpec fromJsonMethod = MethodSpec.methodBuilder(methodName)
                .addJavadoc("Generate a SELECT ... FROM ... WHERE ... <strong>$L $L </strong>", fieldInfo.quotedCqlColumn, " = fromJson(?)")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(STRING, fieldInfo.fieldName)
                .addStatement("where.and($T.eq($S, $T.fromJson($T.bindMarker($S))))",
                        QUERY_BUILDER, fieldInfo.quotedCqlColumn, QUERY_BUILDER, QUERY_BUILDER, fieldInfo.quotedCqlColumn)
                .addStatement("boundValues.add($N)", fieldInfo.fieldName)
                .addStatement("encodedValues.add($N)", fieldInfo.fieldName)
                .returns(nextSignature.returnClassType)
                .addStatement("return new $T(where)", nextSignature.returnClassType)
                .build();

        relationClassBuilder.addMethod(fromJsonMethod);
    }
}