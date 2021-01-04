/*
 * Copyright 2020-2021 the original author or authors from the JHipster project.
 *
 * This file is part of the JHipster project, see https://www.jhipster.tech/
 * for more information.
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
package tech.jhipster.controlcenter.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Test class for the {@link AudienceValidator} utility class.
 */
public class AudienceValidatorTest {
    private final AudienceValidator validator = new AudienceValidator(Arrays.asList("api://default"));

    @Test
    @SuppressWarnings("unchecked")
    public void testInvalidAudience() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("aud", "bar");
        Jwt badJwt = mock(Jwt.class);
        when(badJwt.getAudience()).thenReturn(new ArrayList(claims.values()));
        assertThat(validator.validate(badJwt).hasErrors()).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testValidAudience() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("aud", "api://default");
        Jwt jwt = mock(Jwt.class);
        when(jwt.getAudience()).thenReturn(new ArrayList(claims.values()));
        assertThat(validator.validate(jwt).hasErrors()).isFalse();
    }
}
