package com.million.learnify;

import java.io.IOException;

interface BackendTokenProvider {
    String getIdToken(boolean forceRefresh) throws IOException;

    void signOut();
}
