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
 * #L%
 */

import static java.nio.charset.StandardCharsets.UTF_8;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import java.io.File;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r4.model.Provenance;

public class TestDataParser {

  public static final String RPS_BUNDLE_COMPOSITION_ID = "d95529ba-0d70-426f-8f77-7606f168268e";
  private static final FhirParser parser = new FhirParser(FhirContext.forR4());

  public static Provenance getProvenance() {
    return (Provenance) parser.parseFromJson(getProvenanceString());
  }

  @SneakyThrows
  public static String getProvenanceString() {
    return FileUtils.readFileToString(
        new File("src/test/resources/provenances/provenance.json"), UTF_8);
  }

  public static Map<String, Object> getTokenClaimsFromResources(TokenType type) {
    return JwtUtils.getClaimMap(getTokenFromResources(type));
  }

  @SneakyThrows
  public static String getTokenFromResources(TokenType type) {
    String path =
        switch (type) {
          case AUTHENTICATOR -> "src/test/resources/tokens/authenticatorToken.txt";
          case LAB -> "src/test/resources/tokens/labToken.txt";
          case HOSPITAL -> "src/test/resources/tokens/hospitalToken.txt";
          case BUNDID_ID -> "src/test/resources/tokens/bundIdPersoToken.txt";
          case BUNDID_USERNAME_PASSWORD ->
              "src/test/resources/tokens/bundIdUsernamePasswordToken.txt";
        };
    return FileUtils.readFileToString(new File(path), UTF_8);
  }

  public enum TokenType {
    AUTHENTICATOR,
    LAB,
    HOSPITAL,
    BUNDID_ID,
    BUNDID_USERNAME_PASSWORD
  }
}
