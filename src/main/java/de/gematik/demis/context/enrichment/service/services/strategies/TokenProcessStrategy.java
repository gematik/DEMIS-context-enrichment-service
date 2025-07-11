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

package de.gematik.demis.context.enrichment.service.services.strategies;

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

import static de.gematik.demis.context.enrichment.service.exceptions.ErrorCode.MISSING_CLAIMS;
import static de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum.AZP;
import static io.micrometer.common.util.StringUtils.isBlank;
import static org.hl7.fhir.r4.model.Provenance.ProvenanceEntityRole.SOURCE;

import de.gematik.demis.context.enrichment.service.exceptions.CesServiceException;
import de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum;
import de.gematik.demis.context.enrichment.service.utils.fhir.ProvenanceBuilder;
import java.util.List;
import java.util.Map;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Provenance.ProvenanceAgentComponent;
import org.hl7.fhir.r4.model.Provenance.ProvenanceEntityComponent;
import org.hl7.fhir.r4.model.Reference;

/**
 * All classes that extends this class are used to create Provenance resource depending on token
 * type
 */
public abstract class TokenProcessStrategy {

  protected static final String GEMATIK_IDENTIFIER_SYSTEM =
      "https://gematik.de/fhir/sid/telematik-id";
  protected static final String ACCOUNT_SOURCE_CERTIFICATES =
      "https://demis.rki.de/fhir/NamingSystem/DemisParticipantId";
  protected static final String BUNDID_IDENTIFIER = "https://demis.rki.de/fhir/sid/BundIdBPK2";

  /**
   * Create Provenance resource with the given token and compositionId
   *
   * @param claims claims of Bearer token
   * @param compositionId Id of the composition in bundle
   */
  public final Provenance createProvenanceResource(
      Map<String, Object> claims, String compositionId) {
    checkForNeededClaims(claims);
    return this.process(claims, compositionId);
  }

  private void checkForNeededClaims(Map<String, Object> claims) {
    List<TokenClaimsEnum> missingClaims =
        getNeededClaims().stream()
            .filter(
                claim ->
                    !claims.containsKey(claim.getName())
                        || isBlank(claims.get(claim.getName()).toString()))
            .toList();
    if (!missingClaims.isEmpty()) {
      throw new CesServiceException(MISSING_CLAIMS, "Missing values for claims: " + missingClaims);
    }
  }

  private Provenance process(Map<String, Object> claims, String compositionId) {
    ProvenanceBuilder provenanceBuilder = new ProvenanceBuilder();
    ProvenanceAgentComponent agent = createAgent(claims);
    ProvenanceEntityComponent entity = createEntity(claims);
    provenanceBuilder.setDefaults();
    provenanceBuilder.setTargetCompositionId(compositionId);
    provenanceBuilder.addAgent(agent);
    provenanceBuilder.addEntity(entity);
    return provenanceBuilder.build();
  }

  public ProvenanceEntityComponent createEntity(Map<String, Object> claims) {
    ProvenanceEntityComponent entity = new ProvenanceEntityComponent();
    entity.setRole(SOURCE);
    entity.setWhat(
        new Reference()
            .setType("Endpoint")
            .setIdentifier(
                new Identifier()
                    .setValue(claims.get(AZP.getName()).toString())
                    .setSystem("https://demis.rki.de/fhir/sid/DemisClientId")
                    .setType(
                        new CodeableConcept(
                            new Coding()
                                .setCode("RI")
                                .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")))));
    return entity;
  }

  /**
   * Method to create ProvenanceAgentComponent
   *
   * @param claims map of claims from token
   * @return ProvenanceAgentComponent
   */
  abstract ProvenanceAgentComponent createAgent(Map<String, Object> claims);

  /**
   * Method so each strategy can define which claims are needed
   *
   * @return needed claims
   */
  abstract List<TokenClaimsEnum> getNeededClaims();
}
