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

package de.gematik.demis.context.enrichment.service.services;

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

import static de.gematik.demis.context.enrichment.service.services.EnrichmentService.AccessTokenType.AUTHENTICATOR_TOKEN;
import static de.gematik.demis.context.enrichment.service.services.EnrichmentService.AccessTokenType.BUNDID_TOKEN;
import static de.gematik.demis.context.enrichment.service.services.EnrichmentService.AccessTokenType.HOSPITAL_TOKEN;
import static de.gematik.demis.context.enrichment.service.services.EnrichmentService.AccessTokenType.LAB_TOKEN;
import static de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum.ACCOUNT_SOURCE;
import static de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum.ISS;

import de.gematik.demis.context.enrichment.service.services.strategies.BundIdIdpStrategy;
import de.gematik.demis.context.enrichment.service.services.strategies.CertificateStrategy;
import de.gematik.demis.context.enrichment.service.services.strategies.GematikIdpStrategy;
import de.gematik.demis.context.enrichment.service.services.strategies.TokenProcessStrategy;
import de.gematik.demis.context.enrichment.service.utils.JwtUtils;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Provenance;
import org.springframework.stereotype.Service;

/** Service used to enrich the FHIR notification with context information */
@Service
@RequiredArgsConstructor
public class EnrichmentService {

  private final GematikIdpStrategy gematikIdpStrategy;
  private final CertificateStrategy certificateStrategy;
  private final BundIdIdpStrategy bundIdIdpStrategy;

  /**
   * Create provenance resource from token
   *
   * @param compositionId
   * @param token
   * @return
   */
  public Provenance addContextInformation(String compositionId, String token) {
    Map<String, Object> claims = JwtUtils.getClaimMap(token);
    TokenProcessStrategy strategy =
        switch (getTypeOfToken(claims)) {
          case AUTHENTICATOR_TOKEN -> gematikIdpStrategy;
          case HOSPITAL_TOKEN, LAB_TOKEN -> certificateStrategy;
          case BUNDID_TOKEN -> bundIdIdpStrategy;
        };
    return strategy.createProvenanceResource(claims, compositionId);
  }

  private AccessTokenType getTypeOfToken(Map<String, Object> claims) {
    if (!claims.containsKey(ISS.getName())) {
      throw new IllegalArgumentException("Missing values for claims: " + ISS);
    }
    if (claims.get(ISS.getName()).toString().endsWith("PORTAL")) {
      return claims.get(ACCOUNT_SOURCE.getName()).toString().equals("gematik")
          ? AUTHENTICATOR_TOKEN
          : BUNDID_TOKEN;
    }
    if (claims.get(ISS.getName()).toString().endsWith("HOSPITAL")) {
      return HOSPITAL_TOKEN;
    }
    if (claims.get(ISS.getName()).toString().endsWith("LAB")) {
      return LAB_TOKEN;
    }
    throw new IllegalArgumentException("Unknown token issuer");
  }

  enum AccessTokenType {
    AUTHENTICATOR_TOKEN,
    HOSPITAL_TOKEN,
    LAB_TOKEN,
    BUNDID_TOKEN
  }
}
