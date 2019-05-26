/*
 * Copyright 2019 Peter Bencze.
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

package com.github.peterbencze.serritor.it.web;

import com.github.peterbencze.serritor.api.CrawlerConfiguration;
import com.github.peterbencze.serritor.api.CrawlerConfiguration.CrawlerConfigurationBuilder;
import com.github.peterbencze.serritor.api.CrawlerWithSecuredWebApi;
import com.github.peterbencze.serritor.api.web.AccessControlConfiguration;
import com.github.peterbencze.serritor.api.web.ServerConfiguration;
import com.github.peterbencze.serritor.api.web.WebApiException;
import com.github.peterbencze.serritor.api.web.http.HttpMethod;
import com.github.peterbencze.serritor.api.web.socket.WebSocketHandler;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.commons.io.IOUtils;
import org.awaitility.Awaitility;

/**
 * A crawler implementation with secured web API support that is used by the tests.
 */
public final class TestCrawlerWithSecuredWebApi extends CrawlerWithSecuredWebApi {

    private static final CrawlerConfiguration CRAWLER_CONFIG =
            new CrawlerConfigurationBuilder().build();

    private static final RetryPolicy<Object> RETRY_POLICY = new RetryPolicy<>()
            .handle(WebApiException.class)
            .withDelay(Duration.ofSeconds(1))
            .withMaxRetries(10);

    private AtomicBoolean isServerStarted;
    private AtomicBoolean isStopAllowed;

    public TestCrawlerWithSecuredWebApi(
            final ServerConfiguration serverConfig,
            final AccessControlConfiguration accessControlConfig) {
        super(serverConfig, accessControlConfig, CRAWLER_CONFIG);

        isServerStarted = new AtomicBoolean();
        isStopAllowed = new AtomicBoolean();

        addHttpEndpoint(HttpMethod.GET, "/api/http/test",
                (request, response) -> IOUtils.write("It works!", response.getWriter()));

        addHttpEndpoint(HttpMethod.GET, "/api/http/test-with-role",
                Collections.singleton("test-role"),
                (request, response) -> IOUtils.write("It works!", response.getWriter()));

        addHttpEndpoint(HttpMethod.POST, "/api/http/test-csrf",
                (request, response) -> IOUtils.write("It works!", response.getWriter()));

        addWebSocketEndpoint("/api/ws/test", new WebSocketHandler() {
        });

        addWebSocketEndpoint("/api/ws/test-with-role", Collections.singleton("test-role"),
                new WebSocketHandler() {
                });
    }

    public AtomicBoolean isServerStarted() {
        return isServerStarted;
    }

    public void allowStop() {
        isStopAllowed.set(true);
    }

    @Override
    protected void onStart() {
        Failsafe.with(RETRY_POLICY).run(() -> {
            super.onStart();
            isServerStarted.set(true);
        });
    }

    @Override
    protected void onStop() {
        Awaitility.await().untilTrue(isStopAllowed);
        super.onStop();
    }
}
