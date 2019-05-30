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

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.peterbencze.serritor.api.helper.JsonReaderWriter;
import com.github.peterbencze.serritor.api.web.AccessControlConfiguration;
import com.github.peterbencze.serritor.api.web.AccessControlConfiguration.AccessControlConfigurationBuilder;
import com.github.peterbencze.serritor.api.web.ServerConfiguration;
import com.github.peterbencze.serritor.api.web.ServerConfiguration.ServerConfigurationBuilder;
import com.github.peterbencze.serritor.api.web.SslContextConfiguration;
import com.github.peterbencze.serritor.api.web.User;
import com.github.peterbencze.serritor.internal.util.KeyFactory;
import com.github.peterbencze.serritor.internal.web.http.CsrfFilter;
import com.github.peterbencze.serritor.internal.web.http.auth.JwtAuthenticator;
import com.github.peterbencze.serritor.internal.web.http.dto.ErrorDto;
import com.github.peterbencze.serritor.internal.web.http.dto.JwtDto;
import com.github.peterbencze.serritor.internal.web.http.dto.LoginDto;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.awaitility.Awaitility;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Integration test cases for the crawler's web API.
 */
public final class WebApiIT {

    private static final String USERNAME = "test-username";
    private static final String PASSWORD = "test-password";

    private static final String KEY_STORE_PATH =
            WebApiIT.class.getClassLoader().getResource("keystore.jks").getFile();
    private static final SslContextConfiguration SSL_CONTEXT_CONFIG =
            new SslContextConfiguration(KEY_STORE_PATH, PASSWORD);

    private static final User ROOT_USER = new User(USERNAME, PASSWORD);

    private static final String SECRET_KEY = KeyFactory.createKey("HmacSHA256");
    private static final JWTVerifier JWT_VERIFIER =
            JWT.require(Algorithm.HMAC256(SECRET_KEY)).build();

    private static final CookieStore COOKIE_STORE = new BasicCookieStore();

    private static final ObjectMapper OBJECT_MAPPER = JsonReaderWriter.getObjectMapper();

    private static CloseableHttpClient HTTP_CLIENT;

    @BeforeClass
    public static void beforeClass()
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                .build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        HTTP_CLIENT = HttpClients.custom()
                .setDefaultCookieStore(COOKIE_STORE)
                .setSSLSocketFactory(socketFactory)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
                .build();
    }

    @AfterClass
    public static void afterClass() throws IOException {
        HTTP_CLIENT.close();
    }

    @Test
    public void testHttpEndpointWhenEndpointExists() throws IOException {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        TestCrawlerWithWebApi crawler = new TestCrawlerWithWebApi(serverConfig);

        try {
            Executors.newSingleThreadExecutor().execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            HttpGet request = new HttpGet("http://localhost:8080/api/http/test");
            try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
                String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                Assert.assertEquals(HttpStatus.OK_200, response.getStatusLine().getStatusCode());
                Assert.assertEquals("It works!", content);
            }
        } finally {
            crawler.allowStop();
        }
    }

    @Test
    public void testHttpEndpointWhenEndpointDoesNotExist() throws IOException {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        TestCrawlerWithWebApi crawler = new TestCrawlerWithWebApi(serverConfig);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            HttpGet request = new HttpGet("http://localhost:8080/http/nonexistent");
            try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
                String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                ErrorDto errorDto = JsonReaderWriter.getObjectMapper()
                        .readValue(content, ErrorDto.class);

                Assert.assertEquals(HttpStatus.NOT_FOUND_404,
                        response.getStatusLine().getStatusCode());
                Assert.assertEquals(HttpStatus.NOT_FOUND_404, errorDto.getCode());
                Assert.assertEquals(HttpStatus.getMessage(HttpStatus.NOT_FOUND_404),
                        errorDto.getMessage());
            }
        } finally {
            crawler.allowStop();
            executor.shutdown();
        }
    }

    @Test
    public void testHttpEndpointWhenUsingSsl() throws IOException {
        ServerConfiguration serverConfig = new ServerConfigurationBuilder()
                .withSsl(SSL_CONTEXT_CONFIG)
                .build();
        TestCrawlerWithWebApi crawler = new TestCrawlerWithWebApi(serverConfig);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            HttpGet request = new HttpGet("https://localhost:8080/api/http/test");
            try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
                String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                Assert.assertEquals(HttpStatus.OK_200, response.getStatusLine().getStatusCode());
                Assert.assertEquals("It works!", content);
            }
        } finally {
            crawler.allowStop();
            executor.shutdown();
        }
    }

    @Test
    public void testHttpEndpointWhenNoJwtPresentInRequest() throws IOException {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        AccessControlConfiguration accessControlConfig =
                new AccessControlConfigurationBuilder(ROOT_USER).build();
        TestCrawlerWithSecuredWebApi crawler =
                new TestCrawlerWithSecuredWebApi(serverConfig, accessControlConfig);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            HttpGet request = new HttpGet("http://localhost:8080/api/http/test");
            try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
                String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                ErrorDto errorDto = JsonReaderWriter.getObjectMapper()
                        .readValue(content, ErrorDto.class);

                Assert.assertEquals(HttpStatus.UNAUTHORIZED_401,
                        response.getStatusLine().getStatusCode());
                Assert.assertEquals(HttpStatus.UNAUTHORIZED_401, errorDto.getCode());
                Assert.assertEquals(HttpStatus.getMessage(HttpStatus.UNAUTHORIZED_401),
                        errorDto.getMessage());
            }
        } finally {
            crawler.allowStop();
            executor.shutdown();
        }
    }

    @Test
    public void testHttpEndpointWhenInvalidJwtProvidedInHeader() throws IOException {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        AccessControlConfiguration accessControlConfig =
                new AccessControlConfigurationBuilder(ROOT_USER).build();
        TestCrawlerWithSecuredWebApi crawler =
                new TestCrawlerWithSecuredWebApi(serverConfig, accessControlConfig);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            HttpGet request = new HttpGet("http://localhost:8080/api/http/test");
            request.setHeader(HttpHeader.AUTHORIZATION.asString(), "Bearer invalid");
            try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
                String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                ErrorDto errorDto = JsonReaderWriter.getObjectMapper()
                        .readValue(content, ErrorDto.class);

                Assert.assertEquals(HttpStatus.UNAUTHORIZED_401,
                        response.getStatusLine().getStatusCode());
                Assert.assertEquals(HttpStatus.UNAUTHORIZED_401, errorDto.getCode());
                Assert.assertEquals(HttpStatus.getMessage(HttpStatus.UNAUTHORIZED_401),
                        errorDto.getMessage());
            }
        } finally {
            crawler.allowStop();
            executor.shutdown();
        }
    }

    @Test
    public void testHttpEndpointWhenInvalidJwtProvidedInCookie() throws IOException {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        AccessControlConfiguration accessControlConfig =
                new AccessControlConfigurationBuilder(ROOT_USER).build();
        TestCrawlerWithSecuredWebApi crawler =
                new TestCrawlerWithSecuredWebApi(serverConfig, accessControlConfig);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            HttpGet request = new HttpGet("http://localhost:8080/api/http/test");
            BasicClientCookie authCookie =
                    new BasicClientCookie(JwtAuthenticator.AUTH_COOKIE_NAME, "invalid");
            authCookie.setDomain("localhost");
            authCookie.setPath("/");
            COOKIE_STORE.addCookie(authCookie);

            try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
                String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                ErrorDto errorDto = JsonReaderWriter.getObjectMapper()
                        .readValue(content, ErrorDto.class);

                Assert.assertEquals(HttpStatus.UNAUTHORIZED_401,
                        response.getStatusLine().getStatusCode());
                Assert.assertEquals(HttpStatus.UNAUTHORIZED_401, errorDto.getCode());
                Assert.assertEquals(HttpStatus.getMessage(HttpStatus.UNAUTHORIZED_401),
                        errorDto.getMessage());
            }
        } finally {
            crawler.allowStop();
            executor.shutdown();
            COOKIE_STORE.clear();
        }
    }

    @Test
    public void testLoginWhenCredentialsAreCorrect() throws IOException {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        AccessControlConfiguration accessControlConfig =
                new AccessControlConfigurationBuilder(ROOT_USER)
                        .setSecretKey(SECRET_KEY)
                        .build();
        TestCrawlerWithSecuredWebApi crawler =
                new TestCrawlerWithSecuredWebApi(serverConfig, accessControlConfig);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            LoginDto loginDto = new LoginDto(USERNAME, PASSWORD);
            HttpPost request = new HttpPost("http://localhost:8080/api/auth");
            StringEntity entity = new StringEntity(OBJECT_MAPPER.writeValueAsString(loginDto),
                    ContentType.APPLICATION_JSON);
            request.setEntity(entity);

            try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
                String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JwtDto jwtDto = JsonReaderWriter.getObjectMapper().readValue(content, JwtDto.class);
                DecodedJWT decodedJwt = JWT_VERIFIER.verify(jwtDto.getJwt());

                Assert.assertEquals(HttpStatus.OK_200, response.getStatusLine().getStatusCode());
                Assert.assertEquals(USERNAME, jwtDto.getUsername());
                Assert.assertEquals(USERNAME, decodedJwt.getClaim("name").asString());
                Assert.assertEquals(jwtDto.getExpiryDate(), decodedJwt.getExpiresAt());
                Assert.assertEquals(jwtDto.getRoles(),
                        new HashSet<>(decodedJwt.getClaim("roles").asList(String.class)));
            }
        } finally {
            crawler.allowStop();
            executor.shutdown();
        }
    }

    @Test
    public void testLoginWhenCredentialsAreCorrectAndCookieAuthenticationIsEnabled()
            throws IOException {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        AccessControlConfiguration accessControlConfig =
                new AccessControlConfigurationBuilder(ROOT_USER)
                        .setSecretKey(SECRET_KEY)
                        .setCookieAuthenticationEnabled(true)
                        .build();
        TestCrawlerWithSecuredWebApi crawler =
                new TestCrawlerWithSecuredWebApi(serverConfig, accessControlConfig);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            LoginDto loginDto = new LoginDto(USERNAME, PASSWORD);
            HttpPost request = new HttpPost("http://localhost:8080/api/auth");
            StringEntity entity = new StringEntity(OBJECT_MAPPER.writeValueAsString(loginDto),
                    ContentType.APPLICATION_JSON);
            request.setEntity(entity);

            try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
                String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JwtDto jwtDto = JsonReaderWriter.getObjectMapper().readValue(content, JwtDto.class);
                DecodedJWT decodedJwt = JWT_VERIFIER.verify(jwtDto.getJwt());

                Optional<Cookie> jwtCookieOpt = COOKIE_STORE.getCookies()
                        .stream()
                        .filter(cookie -> JwtAuthenticator.AUTH_COOKIE_NAME.equals(cookie.getName()))
                        .findFirst();

                Optional<Cookie> csrfCookieOpt = COOKIE_STORE.getCookies()
                        .stream()
                        .filter(cookie -> CsrfFilter.CSRF_COOKIE_NAME.equals(cookie.getName()))
                        .findFirst();

                Assert.assertEquals(HttpStatus.OK_200, response.getStatusLine().getStatusCode());
                Assert.assertEquals(USERNAME, jwtDto.getUsername());
                Assert.assertEquals(USERNAME, decodedJwt.getClaim("name").asString());
                Assert.assertEquals(jwtDto.getExpiryDate(), decodedJwt.getExpiresAt());
                Assert.assertEquals(jwtDto.getRoles(),
                        new HashSet<>(decodedJwt.getClaim("roles").asList(String.class)));

                Assert.assertTrue(jwtCookieOpt.isPresent());
                Cookie jwtCookie = jwtCookieOpt.get();
                Assert.assertEquals(jwtDto.getJwt(), jwtCookie.getValue());
                Assert.assertEquals("/", jwtCookie.getPath());
                Assert.assertEquals(jwtDto.getExpiryDate(), jwtCookie.getExpiryDate());

                Assert.assertTrue(csrfCookieOpt.isPresent());
                Cookie csrfCookie = csrfCookieOpt.get();
                Assert.assertEquals(JwtAuthenticator.CSRF_TOKEN_BYTE_SIZE,
                        Base64.getUrlDecoder().decode(csrfCookie.getValue()).length);
                Assert.assertEquals("/", csrfCookie.getPath());
                Assert.assertEquals(jwtDto.getExpiryDate(), csrfCookie.getExpiryDate());
            }
        } finally {
            crawler.allowStop();
            executor.shutdown();
            COOKIE_STORE.clear();
        }
    }

    @Test
    public void testLoginWhenCredentialsAreIncorrect() throws IOException {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        AccessControlConfiguration accessControlConfig =
                new AccessControlConfigurationBuilder(ROOT_USER).build();
        TestCrawlerWithSecuredWebApi crawler =
                new TestCrawlerWithSecuredWebApi(serverConfig, accessControlConfig);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            LoginDto loginDto = new LoginDto(USERNAME, "wrong-password");
            HttpPost request = new HttpPost("http://localhost:8080/api/auth");
            StringEntity entity = new StringEntity(OBJECT_MAPPER.writeValueAsString(loginDto),
                    ContentType.APPLICATION_JSON);
            request.setEntity(entity);

            try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
                String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                ErrorDto errorDto = JsonReaderWriter.getObjectMapper()
                        .readValue(content, ErrorDto.class);

                Assert.assertEquals(HttpStatus.UNAUTHORIZED_401,
                        response.getStatusLine().getStatusCode());
                Assert.assertEquals(HttpStatus.UNAUTHORIZED_401, errorDto.getCode());
                Assert.assertEquals(HttpStatus.getMessage(HttpStatus.UNAUTHORIZED_401),
                        errorDto.getMessage());
            }
        } finally {
            crawler.allowStop();
            executor.shutdown();
        }
    }

    @Test
    public void testHttpEndpointWhenUserIsNotAuthorized() throws IOException {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        AccessControlConfiguration accessControlConfig =
                new AccessControlConfigurationBuilder(ROOT_USER)
                        .setCookieAuthenticationEnabled(true)
                        .build();
        TestCrawlerWithSecuredWebApi crawler =
                new TestCrawlerWithSecuredWebApi(serverConfig, accessControlConfig);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            // Log in
            LoginDto loginDto = new LoginDto(USERNAME, PASSWORD);
            HttpPost loginRequest = new HttpPost("http://localhost:8080/api/auth");
            StringEntity entity = new StringEntity(OBJECT_MAPPER.writeValueAsString(loginDto),
                    ContentType.APPLICATION_JSON);
            loginRequest.setEntity(entity);
            try (CloseableHttpResponse response = HTTP_CLIENT.execute(loginRequest)) {
                EntityUtils.consumeQuietly(response.getEntity());

                Assert.assertEquals(HttpStatus.OK_200, response.getStatusLine().getStatusCode());
            }

            // Try accessing endpoint
            HttpGet endpointRequest = new HttpGet("http://localhost:8080/api/http/test-with-role");
            try (CloseableHttpResponse response = HTTP_CLIENT.execute(endpointRequest)) {
                String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                ErrorDto errorDto = JsonReaderWriter.getObjectMapper()
                        .readValue(content, ErrorDto.class);

                Assert.assertEquals(HttpStatus.FORBIDDEN_403,
                        response.getStatusLine().getStatusCode());
                Assert.assertEquals(HttpStatus.FORBIDDEN_403, errorDto.getCode());
                Assert.assertEquals(HttpStatus.getMessage(HttpStatus.FORBIDDEN_403),
                        errorDto.getMessage());
            }
        } finally {
            crawler.allowStop();
            executor.shutdown();
            COOKIE_STORE.clear();
        }
    }

    @Test
    public void testHttpEndpointWhenUserIsAuthorized() throws IOException {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        User rootUser = new User(USERNAME, PASSWORD, Collections.singleton("test-role"));
        AccessControlConfiguration accessControlConfig =
                new AccessControlConfigurationBuilder(rootUser)
                        .setCookieAuthenticationEnabled(true)
                        .build();
        TestCrawlerWithSecuredWebApi crawler =
                new TestCrawlerWithSecuredWebApi(serverConfig, accessControlConfig);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            // Log in
            LoginDto loginDto = new LoginDto(USERNAME, PASSWORD);
            HttpPost loginRequest = new HttpPost("http://localhost:8080/api/auth");
            StringEntity entity = new StringEntity(OBJECT_MAPPER.writeValueAsString(loginDto),
                    ContentType.APPLICATION_JSON);
            loginRequest.setEntity(entity);
            try (CloseableHttpResponse response = HTTP_CLIENT.execute(loginRequest)) {
                EntityUtils.consumeQuietly(response.getEntity());

                Assert.assertEquals(HttpStatus.OK_200, response.getStatusLine().getStatusCode());
            }

            // Try accessing endpoint
            HttpGet endpointRequest = new HttpGet("http://localhost:8080/api/http/test-with-role");
            try (CloseableHttpResponse response = HTTP_CLIENT.execute(endpointRequest)) {
                String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                Assert.assertEquals(HttpStatus.OK_200, response.getStatusLine().getStatusCode());
                Assert.assertEquals("It works!", content);
            }
        } finally {
            crawler.allowStop();
            executor.shutdown();
            COOKIE_STORE.clear();
        }
    }

    @Test
    public void testHttpEndpointCsrfProtectionWhenHeaderIsNotPresentInRequest() throws IOException {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        AccessControlConfiguration accessControlConfig =
                new AccessControlConfigurationBuilder(ROOT_USER)
                        .setCookieAuthenticationEnabled(true)
                        .build();
        TestCrawlerWithSecuredWebApi crawler =
                new TestCrawlerWithSecuredWebApi(serverConfig, accessControlConfig);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            // Log in
            LoginDto loginDto = new LoginDto(USERNAME, PASSWORD);
            HttpPost loginRequest = new HttpPost("http://localhost:8080/api/auth");
            StringEntity entity = new StringEntity(OBJECT_MAPPER.writeValueAsString(loginDto),
                    ContentType.APPLICATION_JSON);
            loginRequest.setEntity(entity);
            try (CloseableHttpResponse response = HTTP_CLIENT.execute(loginRequest)) {
                EntityUtils.consumeQuietly(response.getEntity());

                Assert.assertEquals(HttpStatus.OK_200, response.getStatusLine().getStatusCode());
            }

            // Try accessing endpoint
            HttpPost endpointRequest = new HttpPost("http://localhost:8080/api/http/test-csrf");
            try (CloseableHttpResponse response = HTTP_CLIENT.execute(endpointRequest)) {
                String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                ErrorDto errorDto = JsonReaderWriter.getObjectMapper()
                        .readValue(content, ErrorDto.class);

                Assert.assertEquals(HttpStatus.UNAUTHORIZED_401,
                        response.getStatusLine().getStatusCode());
                Assert.assertEquals(HttpStatus.UNAUTHORIZED_401, errorDto.getCode());
                Assert.assertEquals("Missing or incorrect CSRF token", errorDto.getMessage());
            }
        } finally {
            crawler.allowStop();
            executor.shutdown();
            COOKIE_STORE.clear();
        }
    }

    @Test
    public void testHttpEndpointCsrfProtectionWhenHeaderIsPresentInRequest() throws IOException {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        AccessControlConfiguration accessControlConfig =
                new AccessControlConfigurationBuilder(ROOT_USER)
                        .setCookieAuthenticationEnabled(true)
                        .build();
        TestCrawlerWithSecuredWebApi crawler =
                new TestCrawlerWithSecuredWebApi(serverConfig, accessControlConfig);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            // Log in
            LoginDto loginDto = new LoginDto(USERNAME, PASSWORD);
            HttpPost loginRequest = new HttpPost("http://localhost:8080/api/auth");
            StringEntity entity = new StringEntity(OBJECT_MAPPER.writeValueAsString(loginDto),
                    ContentType.APPLICATION_JSON);
            loginRequest.setEntity(entity);
            try (CloseableHttpResponse response = HTTP_CLIENT.execute(loginRequest)) {
                EntityUtils.consumeQuietly(response.getEntity());

                Assert.assertEquals(HttpStatus.OK_200, response.getStatusLine().getStatusCode());
            }

            // Try accessing endpoint
            HttpPost endpointRequest = new HttpPost("http://localhost:8080/api/http/test-csrf");
            COOKIE_STORE.getCookies()
                    .stream()
                    .filter(cookie -> CsrfFilter.CSRF_COOKIE_NAME.equals(cookie.getName()))
                    .findFirst()
                    .ifPresent(cookie ->
                            endpointRequest.setHeader(CsrfFilter.CSRF_HEADER_NAME,
                                    cookie.getValue()));

            try (CloseableHttpResponse response = HTTP_CLIENT.execute(endpointRequest)) {
                String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                Assert.assertEquals(HttpStatus.OK_200, response.getStatusLine().getStatusCode());
                Assert.assertEquals("It works!", content);
            }
        } finally {
            crawler.allowStop();
            executor.shutdown();
            COOKIE_STORE.clear();
        }
    }

    @Test
    public void testWebSocketEndpointWhenEndpointExists() throws Exception {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        TestCrawlerWithWebApi crawler = new TestCrawlerWithWebApi(serverConfig);

        WebSocketClient wsClient = new WebSocketClient();
        WebSocketAdapter clientSocket = new WebSocketAdapter();
        ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
        // Must set the Origin header, otherwise the socket endpoint creation will fail
        upgradeRequest.setHeader(HttpHeader.ORIGIN.toString(), "localhost");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            wsClient.start();
            wsClient.connect(clientSocket, URI.create("ws://localhost:8080/api/ws/test"),
                    upgradeRequest);
            Awaitility.await().untilAsserted(() -> Assert.assertTrue(clientSocket.isConnected()));
        } finally {
            crawler.allowStop();
            executor.shutdown();
            wsClient.stop();
        }
    }

    @Test
    public void testWebSocketEndpointWhenEndpointDoesNotExist() throws Exception {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        TestCrawlerWithWebApi crawler = new TestCrawlerWithWebApi(serverConfig);

        WebSocketClient wsClient = new WebSocketClient();
        WebSocketAdapter clientSocket = new WebSocketAdapter();
        ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
        // Must set the Origin header, otherwise the socket endpoint creation will fail
        upgradeRequest.setHeader(HttpHeader.ORIGIN.toString(), "localhost");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            wsClient.start();
            wsClient.connect(clientSocket, URI.create("ws://localhost:8080/api/ws/nonexistent"),
                    upgradeRequest);
            Awaitility.await().pollDelay(1, TimeUnit.SECONDS).untilAsserted(() ->
                    Assert.assertTrue(clientSocket.isNotConnected()));
        } finally {
            crawler.allowStop();
            executor.shutdown();
            wsClient.stop();
        }
    }

    @Test
    public void testWebSocketEndpointWhenUsingSsl() throws Exception {
        ServerConfiguration serverConfig = new ServerConfigurationBuilder()
                .withSsl(SSL_CONTEXT_CONFIG)
                .build();
        TestCrawlerWithWebApi crawler = new TestCrawlerWithWebApi(serverConfig);

        SslContextFactory sslContextFactory = new SslContextFactory.Client(true);
        org.eclipse.jetty.client.HttpClient httpClient =
                new org.eclipse.jetty.client.HttpClient(sslContextFactory);
        httpClient.start();
        WebSocketClient wsClient = new WebSocketClient(httpClient);
        WebSocketAdapter clientSocket = new WebSocketAdapter();
        ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
        // Must set the Origin header, otherwise the socket endpoint creation will fail
        upgradeRequest.setHeader(HttpHeader.ORIGIN.toString(), "localhost");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            wsClient.start();
            wsClient.connect(clientSocket, URI.create("wss://localhost:8080/api/ws/test"),
                    upgradeRequest);
            Awaitility.await().untilAsserted(() -> Assert.assertTrue(clientSocket.isConnected()));
        } finally {
            crawler.allowStop();
            executor.shutdown();
            httpClient.stop();
            wsClient.stop();
        }
    }

    @Test
    public void testWebSocketEndpointWhenNoJwtIsPresentInRequest() throws Exception {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        AccessControlConfiguration accessControlConfig =
                new AccessControlConfigurationBuilder(ROOT_USER).build();
        TestCrawlerWithSecuredWebApi crawler =
                new TestCrawlerWithSecuredWebApi(serverConfig, accessControlConfig);

        WebSocketClient wsClient = new WebSocketClient();
        WebSocketAdapter clientSocket = new WebSocketAdapter();
        ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
        // Must set the Origin header, otherwise the socket endpoint creation will fail
        upgradeRequest.setHeader(HttpHeader.ORIGIN.toString(), "localhost");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            wsClient.start();
            wsClient.connect(clientSocket, URI.create("ws://localhost:8080/api/ws/test"),
                    upgradeRequest);
            Awaitility.await().pollDelay(1, TimeUnit.SECONDS).untilAsserted(() ->
                    Assert.assertTrue(clientSocket.isNotConnected()));
        } finally {
            crawler.allowStop();
            executor.shutdown();
            wsClient.stop();
        }
    }

    @Test
    public void testWebSocketEndpointWhenInvalidJwtIsProvidedInQueryParameter() throws Exception {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        AccessControlConfiguration accessControlConfig =
                new AccessControlConfigurationBuilder(ROOT_USER).build();
        TestCrawlerWithSecuredWebApi crawler =
                new TestCrawlerWithSecuredWebApi(serverConfig, accessControlConfig);

        WebSocketClient wsClient = new WebSocketClient();
        WebSocketAdapter clientSocket = new WebSocketAdapter();
        ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
        // Must set the Origin header, otherwise the socket endpoint creation will fail
        upgradeRequest.setHeader(HttpHeader.ORIGIN.toString(), "localhost");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            wsClient.start();
            wsClient.connect(clientSocket,
                    URI.create("ws://localhost:8080/test?access_token=invalid"), upgradeRequest);
            Awaitility.await().pollDelay(1, TimeUnit.SECONDS).untilAsserted(() ->
                    Assert.assertTrue(clientSocket.isNotConnected()));
        } finally {
            crawler.allowStop();
            executor.shutdown();
            wsClient.stop();
        }
    }

    @Test
    public void testWebSocketEndpointWhenUserIsNotAuthorized() throws Exception {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        AccessControlConfiguration accessControlConfig =
                new AccessControlConfigurationBuilder(ROOT_USER)
                        .setCookieAuthenticationEnabled(true)
                        .build();
        TestCrawlerWithSecuredWebApi crawler =
                new TestCrawlerWithSecuredWebApi(serverConfig, accessControlConfig);

        WebSocketClient wsClient = new WebSocketClient();
        WebSocketAdapter clientSocket = new WebSocketAdapter();
        ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
        // Must set the Origin header, otherwise the socket endpoint creation will fail
        upgradeRequest.setHeader(HttpHeader.ORIGIN.toString(), "localhost");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            // Log in
            LoginDto loginDto = new LoginDto(USERNAME, PASSWORD);
            HttpPost loginRequest = new HttpPost("http://localhost:8080/api/auth");
            StringEntity entity = new StringEntity(OBJECT_MAPPER.writeValueAsString(loginDto),
                    ContentType.APPLICATION_JSON);
            loginRequest.setEntity(entity);
            try (CloseableHttpResponse response = HTTP_CLIENT.execute(loginRequest)) {
                EntityUtils.consumeQuietly(response.getEntity());

                Assert.assertEquals(HttpStatus.OK_200, response.getStatusLine().getStatusCode());
            }

            // Try accessing endpoint
            upgradeRequest.setCookies(convertCookies(COOKIE_STORE.getCookies()));
            wsClient.start();
            wsClient.connect(clientSocket, URI.create("ws://localhost:8080/test-with-role"),
                    upgradeRequest);
            Awaitility.await().pollDelay(1, TimeUnit.SECONDS).untilAsserted(() ->
                    Assert.assertTrue(clientSocket.isNotConnected()));
        } finally {
            crawler.allowStop();
            executor.shutdown();
            wsClient.stop();
            COOKIE_STORE.clear();
        }
    }

    @Test
    public void testWebSocketEndpointWhenUserIsAuthorized() throws Exception {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        User rootUser = new User(USERNAME, PASSWORD, Collections.singleton("test-role"));
        AccessControlConfiguration accessControlConfig =
                new AccessControlConfigurationBuilder(rootUser)
                        .setCookieAuthenticationEnabled(true)
                        .build();
        TestCrawlerWithSecuredWebApi crawler =
                new TestCrawlerWithSecuredWebApi(serverConfig, accessControlConfig);

        WebSocketClient wsClient = new WebSocketClient();
        WebSocketAdapter clientSocket = new WebSocketAdapter();
        ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
        // Must set the Origin header, otherwise the socket endpoint creation will fail
        upgradeRequest.setHeader(HttpHeader.ORIGIN.toString(), "localhost");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            // Log in
            LoginDto loginDto = new LoginDto(USERNAME, PASSWORD);
            HttpPost loginRequest = new HttpPost("http://localhost:8080/api/auth");
            StringEntity entity = new StringEntity(OBJECT_MAPPER.writeValueAsString(loginDto),
                    ContentType.APPLICATION_JSON);
            loginRequest.setEntity(entity);
            try (CloseableHttpResponse response = HTTP_CLIENT.execute(loginRequest)) {
                Assert.assertEquals(HttpStatus.OK_200, response.getStatusLine().getStatusCode());

                // Try accessing endpoint
                upgradeRequest.setCookies(convertCookies(COOKIE_STORE.getCookies()));
                wsClient.start();
                wsClient.connect(clientSocket,
                        URI.create("ws://localhost:8080/api/ws/test-with-role"), upgradeRequest);
                Awaitility.await().untilAsserted(() ->
                        Assert.assertTrue(clientSocket.isConnected()));
            }
        } finally {
            crawler.allowStop();
            executor.shutdown();
            wsClient.stop();
            COOKIE_STORE.clear();
        }
    }

    @Test
    public void testWebSocketEndpointWhenOriginIsNotAllowed() throws Exception {
        ServerConfiguration serverConfig = new ServerConfigurationBuilder()
                .setCorsAllowedOrigins(Collections.singleton("http://example.com"))
                .build();
        TestCrawlerWithWebApi crawler = new TestCrawlerWithWebApi(serverConfig);

        WebSocketClient wsClient = new WebSocketClient();
        WebSocketAdapter clientSocket = new WebSocketAdapter();
        ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
        // Must set the Origin header, otherwise the socket endpoint creation will fail
        upgradeRequest.setHeader(HttpHeader.ORIGIN.toString(), "localhost");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            wsClient.start();
            wsClient.connect(clientSocket, URI.create("ws://localhost:8080/api/ws/test"),
                    upgradeRequest);
            Awaitility.await().pollDelay(1, TimeUnit.SECONDS).untilAsserted(() ->
                    Assert.assertTrue(clientSocket.isNotConnected()));
        } finally {
            crawler.allowStop();
            executor.shutdown();
            wsClient.stop();
        }
    }

    @Test
    public void testWebSocketSessionManagement() throws Exception {
        ServerConfiguration serverConfig = ServerConfiguration.createDefault();
        TestCrawlerWithWebApi crawler = new TestCrawlerWithWebApi(serverConfig);

        WebSocketClient wsClient = new WebSocketClient();
        WebSocketAdapter clientSocket = new WebSocketAdapter();
        ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
        // Must set the Origin header, otherwise the socket endpoint creation will fail
        upgradeRequest.setHeader(HttpHeader.ORIGIN.toString(), "localhost");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(crawler::start);
            Awaitility.await().atMost(30, TimeUnit.SECONDS).untilTrue(crawler.isServerStarted());

            wsClient.start();
            wsClient.connect(clientSocket, URI.create("ws://localhost:8080/api/ws/test"),
                    upgradeRequest);
            Awaitility.await().untilAsserted(() -> Assert.assertTrue(clientSocket.isConnected()));
            Assert.assertEquals(1, crawler.getOpenWebSocketSessionCount());

            wsClient.stop();
            Awaitility.await().untilAsserted(() ->
                    Assert.assertTrue(clientSocket.isNotConnected()));
            Assert.assertEquals(0, crawler.getOpenWebSocketSessionCount());
        } finally {
            crawler.allowStop();
            executor.shutdown();
            wsClient.stop();
        }
    }

    private static List<HttpCookie> convertCookies(final List<Cookie> cookiesToConvert) {
        return cookiesToConvert.stream()
                .map(cookieToConvert -> {
                    HttpCookie convertedCookie =
                            new HttpCookie(cookieToConvert.getName(), cookieToConvert.getValue());
                    convertedCookie.setDomain(cookieToConvert.getDomain());
                    convertedCookie.setPath(cookieToConvert.getPath());

                    return convertedCookie;
                })
                .collect(Collectors.toList());
    }
}
