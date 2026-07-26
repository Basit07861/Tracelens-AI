package com.tracelens.security;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
class SecurityAndErrorIntegrationTest {

    private static final String TEST_USER_EMAIL =
            "security-test@example.com";

    private static final String FRONTEND_ORIGIN =
            "http://localhost:5173";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void actuatorHealthIsPublic() throws Exception {

        mockMvc.perform(
                        get("/actuator/health")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("UP")
                );
    }

    @Test
    void openApiSpecificationIsPublic()
            throws Exception {

        mockMvc.perform(
                        get("/v3/api-docs")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.info.title")
                                .value("TraceLens AI API")
                )
                .andExpect(
                        jsonPath(
                                "$.components.securitySchemes"
                                        + ".bearerAuth"
                                        + ".scheme"
                        ).value("bearer")
                );
    }

    @Test
    void swaggerUiIsPublic() throws Exception {

        mockMvc.perform(
                        get("/swagger-ui.html")
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl(
                                "/swagger-ui/index.html"
                        )
                );
    }

    @Test
    void systemStatusIsPublic() throws Exception {

        mockMvc.perform(
                        get("/api/system/status")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.data.applicationStatus")
                                .value("UP")
                )
                .andExpect(
                        jsonPath("$.data.databaseStatus")
                                .value("CONNECTED")
                );
    }

    @Test
    void actuatorInfoRemainsProtected()
            throws Exception {

        mockMvc.perform(
                        get("/actuator/info")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void currentUserRequiresAuthentication()
            throws Exception {

        mockMvc.perform(
                        get("/api/auth/me")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void caseListingRequiresAuthentication()
            throws Exception {

        mockMvc.perform(
                        get("/api/cases")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void caseCreationRequiresAuthentication()
            throws Exception {

        String requestBody =
                """
                {
                  "title": "Unauthorised case creation",
                  "description": "This request must be rejected.",
                  "priority": "HIGH"
                }
                """;

        mockMvc.perform(
                        post("/api/cases")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void invalidBearerTokenIsRejected()
            throws Exception {

        mockMvc.perform(
                        get("/api/auth/me")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer this-is-not-a-valid-jwt"
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void malformedRegistrationJsonReturnsSafeBadRequest()
            throws Exception {

        String malformedJson =
                """
                {
                  "fullName": "Security Test",
                  "email": "security-test@example.com",
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(malformedJson)
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Bad Request")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Request body is malformed "
                                                + "or contains "
                                                + "unsupported values"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/auth/register"
                                )
                );
    }

    @Test
    void registrationValidationReturnsFieldErrors()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("{}")
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Request validation failed"
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.fieldErrors.fullName"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.fieldErrors.email"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.fieldErrors.password"
                        ).exists()
                );
    }

    @Test
    void invalidCaseStatusReturnsBadRequest()
            throws Exception {

        mockMvc.perform(
                        get("/api/cases")
                                .with(
                                        investigatorJwt()
                                )
                                .param(
                                        "status",
                                        "NOT_A_REAL_STATUS"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Invalid value for "
                                                + "request parameter "
                                                + "'status'"
                                )
                );
    }

    @Test
    void negativePageReturnsBadRequest()
            throws Exception {

        mockMvc.perform(
                        get("/api/cases")
                                .with(
                                        investigatorJwt()
                                )
                                .param("page", "-1")
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Page number cannot be negative"
                                )
                );
    }

    @Test
    void unsupportedSortFieldReturnsBadRequest()
            throws Exception {

        mockMvc.perform(
                        get("/api/cases")
                                .with(
                                        investigatorJwt()
                                )
                                .param(
                                        "sortBy",
                                        "passwordHash"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Unsupported case sorting field"
                                )
                );
    }

    @Test
    void corsPreflightAllowsConfiguredFrontend()
            throws Exception {

        mockMvc.perform(
                        options("/api/cases")
                                .header(
                                        HttpHeaders.ORIGIN,
                                        FRONTEND_ORIGIN
                                )
                                .header(
                                        HttpHeaders
                                                .ACCESS_CONTROL_REQUEST_METHOD,
                                        "GET"
                                )
                                .header(
                                        HttpHeaders
                                                .ACCESS_CONTROL_REQUEST_HEADERS,
                                        HttpHeaders.AUTHORIZATION
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                HttpHeaders
                                        .ACCESS_CONTROL_ALLOW_ORIGIN,
                                FRONTEND_ORIGIN
                        )
                )
                .andExpect(
                        header().string(
                                HttpHeaders
                                        .ACCESS_CONTROL_ALLOW_METHODS,
                                containsString("GET")
                        )
                )
                .andExpect(
                        header().string(
                                HttpHeaders
                                        .ACCESS_CONTROL_ALLOW_HEADERS,
                                containsString(
                                        HttpHeaders.AUTHORIZATION
                                )
                        )
                );
    }

    private static org.springframework.test.web.servlet
            .request.RequestPostProcessor
            investigatorJwt() {

        return jwt().jwt(jwt -> jwt
                .subject(TEST_USER_EMAIL)
                .claim(
                        "role",
                        "INVESTIGATOR"
                )
        );
    }
}