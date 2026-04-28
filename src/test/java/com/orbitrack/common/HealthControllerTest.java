package com.orbitrack.common;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.orbitrack.auth.service.AuthService;
import com.orbitrack.auth.service.JwtService;
import com.orbitrack.auth.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Import(HealthControllerTest.TestProtectedControllerConfig.class)
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class
})
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private JwtService jwtService;


    @Test
    void shouldReturnHealthStatus() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"data\":\"OK\"}"));
    }
    @Test
    void shouldRequireAuthenticationForProtectedEndPoint() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }
    @Test
    void shouldAllowAuthenticatedRequestWithValidJwt() throws Exception {
        given(jwtService.extractUsername("valid-token")).willReturn("user@example.com");
        given(userDetailsService.loadUserByUsername(
                "user@example.com"))
                .willReturn(new User(
                        "username@example.com",
                        "encoded-password",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                ));
        given(jwtService.isTokenValid(
                org.mockito.ArgumentMatchers.eq("valid-token"),
                org.mockito.ArgumentMatchers.any(org.springframework.security.core.userdetails.UserDetails.class)
        )).willReturn(true);

        mockMvc.perform(get("/api/test/protected")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("protected-ok"));
    }
    @TestConfiguration
    static class TestProtectedControllerConfig {
        @Bean
        TestProtectedController testProtectedController() {
            return new TestProtectedController();
        }
    }
    @RestController
    static class TestProtectedController {
        @GetMapping("/api/test/protected")
        public String protectedEndPoint() {
            return "protected-ok";
        }
    }
}

