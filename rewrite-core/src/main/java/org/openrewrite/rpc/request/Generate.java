/*
 * Copyright 2025 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.rpc.request;

import io.moderne.jsonrpc.JsonRpcMethod;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.openrewrite.*;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Value
public class Generate implements RpcRequest {
    String id;

    /**
     * An ID of the p value stored in the caller's local object cache.
     */
    String p;

    @RequiredArgsConstructor
    public static class Handler extends JsonRpcMethod<Generate> {
        private final Map<String, Object> localObjects;
        private final Map<String, Recipe> preparedRecipes;
        private final Map<Recipe, Cursor> recipeCursors;
        private final Function<String, ?> getObject;

        @Override
        protected Object handle(Generate request) throws Exception {
            ExecutionContext ctx = (ExecutionContext) getObject.apply(request.getP());
            Recipe recipe = preparedRecipes.get(request.getId());
            if (recipe instanceof ScanningRecipe) {
                //noinspection unchecked
                ScanningRecipe<Object> scanningRecipe = (ScanningRecipe<Object>) recipe;
                Object acc = scanningRecipe.getAccumulator(recipeCursors.computeIfAbsent(recipe,
                        r -> new Cursor(null, Cursor.ROOT_VALUE)), ctx);
                Collection<? extends SourceFile> generated = scanningRecipe.generate(acc, ctx);
                generated.forEach(g -> localObjects.put(g.getId().toString(), g));
                return generated.stream()
                        .map(SourceFile::getId)
                        .collect(Collectors.toList());
            }
            return emptyList();
        }
    }
}
