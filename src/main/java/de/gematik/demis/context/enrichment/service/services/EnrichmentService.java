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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 * #L%
 */

import static de.gematik.demis.context.enrichment.service.services.EnrichmentService.AccessTokenType.AUTHENTICATOR_TOKEN;
import static de.gematik.demis.context.enrichment.service.services.EnrichmentService.AccessTokenType.BUNDID_TOKEN;
import static de.gematik.demis.context.enrichment.service.services.EnrichmentService.AccessTokenType.HOSPITAL_TOKEN;
import static de.gematik.demis.context.enrichment.service.services.EnrichmentService.AccessTokenType.LAB_TOKEN;
import static de.gematik.demis.context.enrichment.service.services.EnrichmentService.AccessTokenType.MUK_TOKEN;
import static de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum.ACCOUNT_SOURCE;
import static de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum.ISS;

import com.apicatalog.jsonld.StringUtils;
import de.gematik.demis.context.enrichment.service.services.strategies.CertificateStrategy;
import de.gematik.demis.context.enrichment.service.services.strategies.GematikIdpStrategy;
import de.gematik.demis.context.enrichment.service.services.strategies.OzgStrategy;
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

  public static final String PORTAL_REALM_NAME = "PORTAL";
  public static final String TOKEN_EXCHANGE_REALM_NAME = "INSTITUTIONS-TI";
  public static final String HOSPITAL_REALM_NAME = "HOSPITAL";
  public static final String LAB_REAM_NAME = "LAB";
  private final GematikIdpStrategy gematikIdpStrategy;
  private final CertificateStrategy certificateStrategy;
  private final OzgStrategy ozgStrategy;

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
          case BUNDID_TOKEN -> ozgStrategy;
          case MUK_TOKEN -> ozgStrategy;
        };
    return strategy.createProvenanceResource(claims, compositionId);
  }

  private AccessTokenType getTypeOfToken(Map<String, Object> claims) {
    if (checkIssuerIsPresent(claims)) {
      throw new IllegalArgumentException("Missing or null value for claim: " + ISS.getName());
    }
    String realmName = getRealmNameFromIssuer(claims);
    return switch (realmName) {
      case PORTAL_REALM_NAME -> handlePortalRealm(claims);
      case TOKEN_EXCHANGE_REALM_NAME -> AUTHENTICATOR_TOKEN;
      case HOSPITAL_REALM_NAME -> HOSPITAL_TOKEN;
      case LAB_REAM_NAME -> LAB_TOKEN;
      default -> throw new IllegalArgumentException("Unknown token issuer: " + realmName);
    };
  }

  private AccessTokenType handlePortalRealm(Map<String, Object> claims) {
    String accountSource = (String) claims.get(ACCOUNT_SOURCE.getName());
    if (StringUtils.isBlank(accountSource)) {
      throw new IllegalArgumentException(
          "Missing or null value for claim: " + ACCOUNT_SOURCE.getName());
    }
    return switch (accountSource) {
      case "gematik" -> AUTHENTICATOR_TOKEN;
      case "bundid" -> BUNDID_TOKEN;
      case "muk" -> MUK_TOKEN;
      default ->
          throw new IllegalArgumentException(
              "Unknown account source in portal realm: " + accountSource);
    };
  }

  private static boolean checkIssuerIsPresent(Map<String, Object> claims) {
    return !claims.containsKey(ISS.getName())
        || claims.get(ISS.getName()) == null
        || claims.get(ISS.getName()).toString().isEmpty();
  }

  private static String getRealmNameFromIssuer(Map<String, Object> claims) {
    String value = claims.get(ISS.getName()).toString();
    return value.substring(value.lastIndexOf("/") + 1);
  }

  enum AccessTokenType {
    AUTHENTICATOR_TOKEN,
    HOSPITAL_TOKEN,
    LAB_TOKEN,
    BUNDID_TOKEN,
    MUK_TOKEN
  }
}
