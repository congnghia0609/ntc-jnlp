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
package com.ntc.jnlp;

import com.ntc.jvertx.CommonVX;
import com.ntc.vntok.VnTok;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nghiatc
 * @since Jan 14, 2021
 */
public class VTVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(VTVerticle.class);

    private VnTok vntok;

    public VTVerticle() {
        try {
            System.out.println("Constructor VTVerticle...");
            this.vntok = new VnTok();
        } catch (Exception e) {
            log.error("VTVerticle " + e.getMessage(), e);
        }
    }

    @Override
    public void start(Promise<Void> promise) throws Exception {
        System.out.println("Start VTVerticle");
        vertx.eventBus().consumer(CommonVX.VT_QUEUE, this::onMessage);
        promise.complete();
    }

    public void onMessage(Message<JsonObject> message) {
        try {
            if (!message.headers().contains("action")) {
                log.error("No action header specified for message with headers {} and body {}",
                        message.headers(), message.body().encodePrettily());
                message.fail(CommonVX.NO_ACTION, "No action header specified");
                return;
            }
            String action = message.headers().get("action");
            switch (action) {
                // PublicService
                case "vntok":
                    vntokService(message);
                    break;

                default:
                    message.fail(CommonVX.BAD_ACTION, "Bad action: " + action);
            }
        } catch (Exception e) {
            log.error("onMessage " + e.getMessage());
        }
    }

    public void vntokService(Message<JsonObject> message) {
        try {
            JsonObject params = message.body();
            String s = params.getString("s", "");
            String vts = vntok.tokenizeSentence(s);

            // 1. Render data
            JsonObject vt = new JsonObject();
            vt.put("vt", vts);
            JsonObject resp = new JsonObject();
            resp.put("data", vt);
            message.reply(resp);
        } catch (Exception e) {
            log.error("vntokService: " + e.getMessage(), e);
        }
    }
}
