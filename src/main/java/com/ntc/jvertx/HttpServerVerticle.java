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

import com.ntc.configer.NConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nghiatc
 * @since Jan 15, 2021
 */
public class HttpServerVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(HttpServerVerticle.class);

    @Override
    public void start(Promise<Void> promise) throws Exception {
        System.out.println("Start HttpServerVerticle");
        HttpServer server = vertx.createHttpServer();

        // Init Router
        Router router = Router.router(vertx);
        // https://github.com/vert-x3/vertx-auth/tree/master/vertx-auth-jwt/src/main/java/examples
        // Module util http.
        // Cookie.
        //router.route().handler(CookieHandler.create()); // Default.
        // Cross Origin Resource Sharing: https://vertx.io/docs/vertx-web/java/#_cors_handling
        //router.route().handler(CorsHandler.create("*").allowedMethods(new HashSet<>(Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS))));
        // Session.
        //router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)).setSessionTimeout(SESSION_TIMEOUT).setSessionCookieName(SESSION_COOKIE_NAME));
        // BodyHandler & Uploads.
        router.route().handler(BodyHandler.create().setUploadsDirectory("tmp"));
        // Static file.
        //router.get("/static/*").handler(StaticHandler.create().setWebRoot("./public/").setCachingEnabled(true).setCacheEntryTimeout(600000).setMaxCacheSize(50*1024*1024));
        // Vue Front-End Website.
        //router.get("/*").handler(StaticHandler.create("./vue/dist/"));
        //router.get("/").handler(context -> context.reroute("index.html"));

        //// Language Detect API
        Router ldRouter = Router.router(vertx);
        // Public Zone.
        ldRouter.post("/post").handler(this::ldHandler);
        router.mountSubRouter("/ld/v1", ldRouter);
        
        //// Sentence Detect API
        Router sdRouter = Router.router(vertx);
        // Public Zone.
        sdRouter.post("/post").handler(this::sdHandler);
        router.mountSubRouter("/sd/v1", sdRouter);
        
        //// Vietnamese Tokenizer API
        Router vtRouter = Router.router(vertx);
        // Public Zone.
        vtRouter.post("/post").handler(this::vtHandler);
        router.mountSubRouter("/vt/v1", vtRouter);

        int portNumber = NConfig.getConfig().getInt("webserver.port", 8787);
        server.requestHandler(router)
            .listen(portNumber, ar -> {
                if (ar.succeeded()) {
                    System.out.println("HTTP server running on port " + portNumber);
                    log.info("HTTP server running on port " + portNumber);
                    promise.complete();
                } else {
                    log.error("Could not start a HTTP server", ar.cause());
                    promise.fail(ar.cause());
                }
            });
    }
    
    protected JsonObject getParams(RoutingContext context) {
        JsonObject params = new JsonObject();
        // get params standard.
        MultiMap mm = context.request().params();
        //System.out.println("mm: " + mm.toString());
        if (!mm.isEmpty()) {
            mm.names().forEach((key) -> {
                params.put(key, mm.get(key));
            });
        }
        try {
            // get params in body.
            JsonObject body = context.getBodyAsJson();
            //System.out.println("body: " + body.toString());
            params.mergeIn(body);
        } catch (Exception e) {
        }
        //System.out.println("params: " + params.toString());
        return params;
    }
    
    private void printJson(RoutingContext context, JsonObject resp) {
        context.response().setStatusCode(200);
        context.response().putHeader("Content-Type", "application/json;charset=utf-8");
        context.response().end(resp.encode());
    }
    
    public void ldHandler(RoutingContext context) {
        try {
            //System.out.println("In ldHandler");
            JsonObject resp = new JsonObject();
            resp.put("err", -1);
            resp.put("msg", "Execute fail. Please try again.");
            JsonObject params = getParams(context);
            // 1. Validate params.
            String s = params.getString("s", "");
            if (s.isEmpty()) {
                resp.put("err", -1);
                resp.put("msg", "Params invalid");
                printJson(context, resp);
                return;
            }

            // Z. Language detector
            DeliveryOptions options = new DeliveryOptions().addHeader("action", "ld");
            vertx.eventBus().request(CommonVX.LD_QUEUE, params, options, reply -> {
                if (reply.succeeded()) {
                    JsonObject body = (JsonObject) reply.result().body();
                    JsonObject data = body.getJsonObject("data");
                    //System.out.println("home: " + home.toString());
                    resp.put("err", 0);
                    resp.put("msg", "Language detector successful");
                    resp.put("data", data);
                    printJson(context, resp);
                } else {
                    resp.put("err", -1);
                    resp.put("msg", reply.cause().getMessage());
                    printJson(context, resp);
                }
            });
        } catch (Exception e) {
            log.error("ldHandler " + e.getMessage(), e);
        }
    }
    
    public void sdHandler(RoutingContext context) {
        try {
            //System.out.println("In sdHandler");
            JsonObject resp = new JsonObject();
            resp.put("err", -1);
            resp.put("msg", "Execute fail. Please try again.");
            JsonObject params = getParams(context);
            // 1. Validate params.
            String s = params.getString("s", "");
            if (s.isEmpty()) {
                resp.put("err", -1);
                resp.put("msg", "Params invalid");
                printJson(context, resp);
                return;
            }

            // Z. Sentence detector
            DeliveryOptions options = new DeliveryOptions().addHeader("action", "visd");
            vertx.eventBus().request(CommonVX.SD_QUEUE, params, options, reply -> {
                if (reply.succeeded()) {
                    JsonObject body = (JsonObject) reply.result().body();
                    JsonObject data = body.getJsonObject("data");
                    //System.out.println("home: " + home.toString());
                    resp.put("err", 0);
                    resp.put("msg", "Sentence detector successful");
                    resp.put("data", data);
                    printJson(context, resp);
                } else {
                    resp.put("err", -1);
                    resp.put("msg", reply.cause().getMessage());
                    printJson(context, resp);
                }
            });
        } catch (Exception e) {
            log.error("sdHandler " + e.getMessage(), e);
        }
    }
    
    public void vtHandler(RoutingContext context) {
        try {
            //System.out.println("In vtHandler");
            JsonObject resp = new JsonObject();
            resp.put("err", -1);
            resp.put("msg", "Execute fail. Please try again.");
            JsonObject params = getParams(context);
            // 1. Validate params.
            String s = params.getString("s", "");
            if (s.isEmpty()) {
                resp.put("err", -1);
                resp.put("msg", "Params invalid");
                printJson(context, resp);
                return;
            }

            // Z. Sentence detector
            DeliveryOptions options = new DeliveryOptions().addHeader("action", "vntok");
            vertx.eventBus().request(CommonVX.VT_QUEUE, params, options, reply -> {
                if (reply.succeeded()) {
                    JsonObject body = (JsonObject) reply.result().body();
                    JsonObject data = body.getJsonObject("data");
                    //System.out.println("home: " + home.toString());
                    resp.put("err", 0);
                    resp.put("msg", "Tokenizer successful");
                    resp.put("data", data);
                    printJson(context, resp);
                } else {
                    resp.put("err", -1);
                    resp.put("msg", reply.cause().getMessage());
                    printJson(context, resp);
                }
            });
        } catch (Exception e) {
            log.error("vtHandler " + e.getMessage(), e);
        }
    }
}
