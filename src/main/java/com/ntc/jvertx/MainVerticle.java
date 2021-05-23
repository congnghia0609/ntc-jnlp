/*
 * Copyright 2021 nghiatc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ntc.jvertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;

/**
 *
 * @author nghiatc
 * @since Jan 15, 2021
 */
public class MainVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> promise) throws Exception {
        // 1. Language Detector Vert
        Promise<String> ldPromise = Promise.promise();
        vertx.deployVerticle(
                LDVerticle.class,
                new DeploymentOptions().setInstances(2).setWorkerPoolSize(100), 
                ldPromise);
        ldPromise.future().compose(id -> { // id: 9d61936e-8e08-46ca-950e-2d85d4580acb

            // 2. Http Vert
            Promise<String> httpPromise = Promise.promise();
            vertx.deployVerticle(
                    HttpServerVerticle.class,
                    new DeploymentOptions().setInstances(2).setWorkerPoolSize(100),
                    httpPromise);

            return httpPromise.future();
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete();
            } else {
                promise.fail(ar.cause());
            }
        });
    }
    
}
