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
 * Copyright (C) 2025 - 2026 gematik GmbH
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
 * For additional notes and disclaimer from gematik and in case of changes by gematik,
 * find details in the "Readme" file.
 * #L%
 */

import static lombok.AccessLevel.PRIVATE;

import java.util.Base64;
import java.util.Map;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public class JwtUtils {

  private static final JsonMapper mapper = new JsonMapper();
  private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE =
      new TypeReference<>() {};

  public static Map<String, Object> getClaimMap(String token) {
    if (token.startsWith("Bearer ")) {
      token = token.replace("Bearer ", "");
    }
    String s = token.split("\\.")[1];

    try {
      return mapper.readValue(new String(Base64.getUrlDecoder().decode(s)), MAP_TYPE_REFERENCE);
    } catch (IllegalArgumentException e) {
      log.warn("Error while decoding token-payload: '{}' - trying other Decoder", s);
      try {
        return mapper.readValue(new String(Base64.getDecoder().decode(s)), MAP_TYPE_REFERENCE);
      } catch (IllegalArgumentException ex) {
        log.error("Other attempt to decode token-payload failed as well");
        throw ex;
      }
    }
  }
}
