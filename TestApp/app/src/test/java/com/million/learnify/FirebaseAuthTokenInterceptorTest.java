package com.million.learnify;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class FirebaseAuthTokenInterceptorTest {

    @Test
    public void attachesCurrentFirebaseTokenToProtectedRequest() throws Exception {
        FakeTokenProvider provider = new FakeTokenProvider("fresh-token");
        FakeChain chain = new FakeChain(response(200), response(200));

        new FirebaseAuthTokenInterceptor(provider).intercept(chain);

        assertEquals("Bearer fresh-token", chain.requests.get(0).header("Authorization"));
        assertEquals(1, provider.tokenRequests.size());
        assertFalse(provider.tokenRequests.get(0));
    }

    @Test
    public void refreshesTokenAndRetriesOnceAfterUnauthorizedResponse() throws Exception {
        FakeTokenProvider provider = new FakeTokenProvider("old-token", "refreshed-token");
        FakeChain chain = new FakeChain(response(401), response(200));

        Response result = new FirebaseAuthTokenInterceptor(provider).intercept(chain);

        assertEquals(200, result.code());
        assertEquals("Bearer old-token", chain.requests.get(0).header("Authorization"));
        assertEquals("Bearer refreshed-token", chain.requests.get(1).header("Authorization"));
        assertEquals(Arrays.asList(false, true), provider.tokenRequests);
        assertFalse(provider.signedOut);
    }

    @Test
    public void signsOutWhenRefreshedTokenIsRejected() throws Exception {
        FakeTokenProvider provider = new FakeTokenProvider("old-token", "refreshed-token");
        FakeChain chain = new FakeChain(response(401), response(401));

        Response result = new FirebaseAuthTokenInterceptor(provider).intercept(chain);

        assertEquals(401, result.code());
        assertTrue(provider.signedOut);
        assertEquals(2, chain.requests.size());
    }

    @Test
    public void signsOutAndFailsWhenFirebaseCannotRefreshToken() {
        FakeTokenProvider provider = new FakeTokenProvider("old-token");
        provider.failOnRefresh = true;
        FakeChain chain = new FakeChain(response(401));

        assertThrows(
                SessionAuthenticationException.class,
                () -> new FirebaseAuthTokenInterceptor(provider).intercept(chain));

        assertTrue(provider.signedOut);
        assertEquals(1, chain.requests.size());
    }

    @Test
    public void refusesProtectedRequestWithoutSignedInFirebaseUser() {
        FakeTokenProvider provider = new FakeTokenProvider();
        FakeChain chain = new FakeChain(response(200));

        assertThrows(
                SessionAuthenticationException.class,
                () -> new FirebaseAuthTokenInterceptor(provider).intercept(chain));

        assertTrue(chain.requests.isEmpty());
    }

    private static Response response(int code) {
        return new Response.Builder()
                .request(new Request.Builder().url("https://api.example.com/api/assistant/ask").build())
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(code == 200 ? "OK" : "Unauthorized")
                .body(ResponseBody.create("", null))
                .build();
    }

    private static final class FakeTokenProvider implements BackendTokenProvider {
        private final List<String> tokens;
        private final List<Boolean> tokenRequests = new ArrayList<>();
        private boolean signedOut;
        private boolean failOnRefresh;

        FakeTokenProvider(String... tokens) {
            this.tokens = Arrays.asList(tokens);
        }

        @Override
        public String getIdToken(boolean forceRefresh) throws IOException {
            tokenRequests.add(forceRefresh);
            if (forceRefresh && failOnRefresh) {
                throw new SessionAuthenticationException("Token refresh failed.");
            }
            int index = forceRefresh ? 1 : 0;
            if (index >= tokens.size()) {
                throw new SessionAuthenticationException("No Firebase user is signed in.");
            }
            return tokens.get(index);
        }

        @Override
        public void signOut() {
            signedOut = true;
        }
    }

    private static final class FakeChain implements Interceptor.Chain {
        private final List<Response> responses;
        private final List<Request> requests = new ArrayList<>();
        private int responseIndex;
        private Request request;

        FakeChain(Response... responses) {
            this.responses = Arrays.asList(responses);
            this.request = new Request.Builder()
                    .url("https://api.example.com/api/assistant/ask")
                    .post(okhttp3.RequestBody.create("", null))
                    .build();
        }

        @Override
        public Request request() {
            return request;
        }

        @Override
        public Response proceed(Request request) {
            this.request = request;
            requests.add(request);
            return responses.get(responseIndex++).newBuilder().request(request).build();
        }

        @Override
        public Connection connection() {
            return null;
        }

        @Override
        public Call call() {
            return null;
        }

        @Override
        public int connectTimeoutMillis() {
            return 0;
        }

        @Override
        public Interceptor.Chain withConnectTimeout(int timeout, TimeUnit unit) {
            return this;
        }

        @Override
        public int readTimeoutMillis() {
            return 0;
        }

        @Override
        public Interceptor.Chain withReadTimeout(int timeout, TimeUnit unit) {
            return this;
        }

        @Override
        public int writeTimeoutMillis() {
            return 0;
        }

        @Override
        public Interceptor.Chain withWriteTimeout(int timeout, TimeUnit unit) {
            return this;
        }
    }
}
