/*
 * Copyright [2024], gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.demis.context.enrichment.service.utils;

/*-
 * #%L
 * context-enrichment-service
 * %%
 * Copyright (C) 2025 gematik GmbH
 * %%
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 * #L%
 */

import static java.util.Base64.getEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public class TestJwtCreator {

  public static final String FAKE_SIG =
      "K-719cdmrGR3xm0ELpGrB-DvysurKawN-HcvM6QmyZn38glIfu5xA4wcOybh9XU2fC0robuqafcdQCWk2ZeT_0j48E1b9MHHF_gsvyIe3t8DyK-uhWE2T6bNRVF3twBtmWC5mbU93KqSpcXdmo6es1eVdmA49iyUyRHoRsIKwdX6v5Hyq0nfFOg7zR7-R0QpQmh-haX7cYijaQSi7LqajDhmoHDffOENDwyNsW72_wYaTVYCM0DL4vSVSBqWmaDDBBXkkUkCCO8n-IRfWOrnlm_1hJNhnwIqii9MNDh5U2RG280GJaMUNvyIaMHi-Qbzf2sHzzzgWEruGwD1vMQpDw";
  Map<String, Object> header =
      Map.of(
          "alg", "RS256",
          "typ", "JWT",
          "kid", "rRWp1IF_HMCA0dQiTp6_eLT3JIwf5UgjP73NGfqDbYM");
  Map<String, Object> claims;

  public TestJwtCreator() {
    this.claims = new HashMap<>();
    claims.put("exp", 1712308787);
    claims.put("iat", 1712308487);
    claims.put("auth_time", 1712306569);
    claims.put("jti", "56993e46-e9b0-4d4a-8564-b4b37bacab9d");
    claims.put("iss", "https://auth.ingress.local/realms/PORTAL");
    claims.put("aud", "account");
    claims.put("sub", "9569bc96-6c32-469c-9907-f8579ef3df70");
    claims.put("typ", "Bearer");
    claims.put("azp", "meldeportal");
    claims.put("nonce", "301666018a4649d6cfa3289c87c525359ahKFnTN6");
    claims.put("session_state", "636d2314-8348-463e-8649-215c5635148c");
    claims.put("acr", "1");
    claims.put("allowed-origins", List.of("https://portal.ingress.local"));
    claims.put(
        "realm_access",
        Map.of(
            "roles",
            List.of(
                "bed-occupancy-sender",
                "disease-notification-sender",
                "offline_access",
                "default-roles-portal",
                "uma_authorization",
                "pathogen-notification-sender",
                "vaccine-injury-sender")));
    claims.put(
        "resource_access",
        Map.of(
            "account",
            Map.of("roles", List.of("manage-account", "manage-account-links", "view-profile"))));
    claims.put("scope", "openid email profile");
    claims.put("sid", "636d2314-8348-463e-8649-215c5635148c");
    claims.put("ik", "123494546");
    claims.put("email_verified", false);
    claims.put("professionOid", "1.2.276.0.76.4.53");
    claims.put("organizationName", "Krankenhaus Melissa David TEST-ONLY");
    claims.put("accountType", "organization");
    claims.put("accountSource", "gematik");
    claims.put("accountIsTemporary", true);
    claims.put("accountIdentifier", "https://gematik.de/fhir/sid/telematik-id|5-2-123494546");
    claims.put("preferred_username", "5-2-123494546");
    claims.put("username", "5-2-123494546");
    claims.put("levelOfAssurance", "STORK-QAA-Level-3");
  }

  public TestJwtCreator removeClaim(String name) {
    claims.remove(name);
    return this;
  }

  public TestJwtCreator addClaim(String name, Object value) {
    this.claims.put(name, value);
    return this;
  }

  public Map<String, Object> createClaims() {
    return claims;
  }

  @SneakyThrows
  public String createToken() {
    ObjectMapper mapper = new ObjectMapper();
    String baseHeader =
        new String(getEncoder().encode(mapper.writeValueAsString(header).getBytes()));
    String payload = new String(getEncoder().encode(mapper.writeValueAsString(claims).getBytes()));
    return String.join(".", List.of(baseHeader, payload, FAKE_SIG));
  }
}
