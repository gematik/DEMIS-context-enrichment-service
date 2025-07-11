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

import static de.gematik.demis.context.enrichment.service.utils.TestDataParser.TokenType.AUTHENTICATOR;
import static de.gematik.demis.context.enrichment.service.utils.TestDataParser.getTokenClaimsFromResources;
import static de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum.ACCOUNT_TYPE;
import static de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum.SUB;
import static de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum.USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Map;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Provenance.ProvenanceAgentComponent;
import org.junit.jupiter.api.Test;

class GematikIdpStrategyTest {

  private GematikIdpStrategy underTest = new GematikIdpStrategy();
  private Map<String, Object> tokenClaims = getTokenClaimsFromResources(AUTHENTICATOR);

  @Test
  void shouldCreateAgentCorrectly() {
    ProvenanceAgentComponent agent = underTest.createAgent(tokenClaims);

    assertAll(
        () -> assertThat(agent.getExtension()).hasSize(2),
        () -> assertThat(agent.getType()).isNotNull(),
        () -> assertThat(agent.getRole()).isNotNull(),
        () ->
            assertThat(agent.getExtension())
                .map(e -> (CodeableConcept) e.getValue())
                .map(c -> c.getCoding().getFirst().getCode())
                .contains("substantial"),
        () ->
            assertThat(agent.getWho().getType()).isEqualTo(tokenClaims.get(ACCOUNT_TYPE.getName())),
        () ->
            assertThat(agent.getWho().getIdentifier().getValue())
                .isEqualTo("urn:uuid:" + tokenClaims.get(SUB.getName())),
        () ->
            assertThat(agent.getOnBehalfOf().getIdentifier().getSystem())
                .isEqualTo("https://gematik.de/fhir/sid/telematik-id"),
        () ->
            assertThat(agent.getOnBehalfOf().getIdentifier().getValue())
                .isEqualTo(tokenClaims.get(USERNAME.getName())));
  }
}
