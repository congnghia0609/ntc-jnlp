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

import com.ntc.jnlp.LDVerticle;
import com.ntc.jnlp.SDVerticle;
import com.ntc.jnlp.VTVerticle;
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
        ldPromise.future().compose(idld -> { // id: 9d61936e-8e08-46ca-950e-2d85d4580acb
            
            // 2. SDVerticle Vert
            Promise<String> sdPromise = Promise.promise();
            vertx.deployVerticle(
                    SDVerticle.class,
                    new DeploymentOptions().setInstances(2).setWorkerPoolSize(100),
                    sdPromise);
            return sdPromise.future().compose(idsd -> {
                
                // 3. VTVerticle Vert
                Promise<String> vtPromise = Promise.promise();
                vertx.deployVerticle(
                        VTVerticle.class,
                        new DeploymentOptions().setInstances(2).setWorkerPoolSize(100),
                        vtPromise);
                return vtPromise.future().compose(idvt -> {
                    
                    // 4. Http Vert
                    Promise<String> httpPromise = Promise.promise();
                    vertx.deployVerticle(
                            HttpServerVerticle.class,
                            new DeploymentOptions().setInstances(2).setWorkerPoolSize(100),
                            httpPromise);

                    return httpPromise.future();
                });
            });
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete();
            } else {
                promise.fail(ar.cause());
            }
        });
    }
    
}
