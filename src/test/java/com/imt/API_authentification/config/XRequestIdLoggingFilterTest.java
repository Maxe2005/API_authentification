package com.imt.API_authentification.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Story 2.1 : le filtre doit poser l'identifiant de corrélation X-Request-Id dans le MDC pendant
 * tout le traitement de la requête (afin que chaque log d'API_authentification le porte) et le
 * retirer en fin de requête, y compris si le header est absent (aucune clé ne doit être posée).
 */
class XRequestIdLoggingFilterTest {

    private static final String X_REQUEST_ID = "X-Request-Id";

    private final XRequestIdLoggingFilter filter = new XRequestIdLoggingFilter();

    @AfterEach
    void cleanUp() {
        MDC.remove(X_REQUEST_ID);
    }

    @Test
    void putsRequestIdInMdc_forWholeChain_andRemovesItAfter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(X_REQUEST_ID, "correlation-42");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain =
                (req, res) -> assertEquals("correlation-42", MDC.get(X_REQUEST_ID));

        filter.doFilterInternal(request, response, chain);

        assertNull(MDC.get(X_REQUEST_ID));
    }

    @Test
    void doesNotPutMdcKey_whenHeaderAbsent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> assertNull(MDC.get(X_REQUEST_ID));

        filter.doFilterInternal(request, response, chain);

        assertNull(MDC.get(X_REQUEST_ID));
    }
}