package com.example.taskmanager.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest
{
        private static final String SECRET = "test-secret-key-for-unit-tests-please-change-1234567890-abcdefgh";
        private JwtService jwtService;

        @BeforeEach
        void setUp()
        {
                jwtService = new JwtService(SECRET, 3_600_000L);
        }

        @Test
        void generateToken_thenExtractUsername_roundTrips()
        {
                String token = jwtService.generateToken("alice");

                assertThat(token).isNotBlank();
                assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
        }

        @Test
        void isTokenValid_freshToken_returnsTrue()
        {
                String token = jwtService.generateToken("bob");

                assertThat(jwtService.isTokenValid(token, "bob")).isTrue();
        }

        @Test
        void isTokenValid_wrongUsername_returnsFalse()
        {
                String token = jwtService.generateToken("bob");

                assertThat(jwtService.isTokenValid(token, "carol")).isFalse();
        }

        @Test
        void isTokenValid_malformedToken_returnsFalse()
        {
                assertThat(jwtService.isTokenValid("not-a-real-token", "bob")).isFalse();
        }

        @Test
        void isTokenValid_expiredToken_returnsFalse() throws InterruptedException
        {
                JwtService shortLived = new JwtService(SECRET, 1L);
                String token = shortLived.generateToken("dave");
                Thread.sleep(20L);

                assertThat(shortLived.isTokenValid(token, "dave")).isFalse();
        }
}
