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
 * #L%
 */

import static de.gematik.demis.context.enrichment.service.services.strategies.AccountTypesEnum.ORGANISATION;
import static de.gematik.demis.context.enrichment.service.services.strategies.CertificateStrategy.ACCOUNT_SOURCE_CERTIFICATES;
import static de.gematik.demis.context.enrichment.service.utils.TestDataParser.getTokenClaimsFromResources;
import static de.gematik.demis.context.enrichment.service.utils.enums.OidEnum.KRANKENHAUS;
import static de.gematik.demis.context.enrichment.service.utils.enums.OidEnum.LABOR;
import static de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum.PREFERRED_USERNAME;
import static de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum.SUB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import de.gematik.demis.context.enrichment.service.utils.TestDataParser.TokenType;
import java.util.List;
import java.util.Map;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Provenance.ProvenanceAgentComponent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CertificateStrategyTest {

  private CertificateStrategy underTest = new CertificateStrategy();

  @ParameterizedTest(name = "Type: {0}-token")
  @EnumSource(
      value = TokenType.class,
      names = {"LAB", "HOSPITAL"})
  void shouldCreateAgentCorrectly(TokenType tokenType) {
    Map<String, Object> tokenClaims = getTokenClaimsFromResources(tokenType);
    ProvenanceAgentComponent agent = underTest.createAgent(tokenClaims);

    assertAll(
        () -> assertThat(agent.getExtension()).hasSize(2),
        () -> assertThat(agent.getType()).isNotNull(),
        () -> assertThat(agent.getRole()).isNotNull(),
        () ->
            assertThat(agent.getExtension())
                .map(e -> (CodeableConcept) e.getValue())
                .map(c -> c.getCoding().getFirst().getCode())
                .containsExactlyInAnyOrderElementsOf(
                    List.of("substantial", getCodeForTokenType(tokenType))),
        () -> assertThat(agent.getWho().getType()).isEqualTo(ORGANISATION.getDisplayName()),
        () ->
            assertThat(agent.getWho().getIdentifier().getValue())
                .isEqualTo("urn:uuid:" + tokenClaims.get(SUB.getName())),
        () ->
            assertThat(agent.getOnBehalfOf().getIdentifier().getSystem())
                .isEqualTo(ACCOUNT_SOURCE_CERTIFICATES),
        () ->
            assertThat(agent.getOnBehalfOf().getIdentifier().getValue())
                .isEqualTo(tokenClaims.get(PREFERRED_USERNAME.getName())),
        () -> assertThat(agent.getExtension()).hasSize(2));
  }

  private String getCodeForTokenType(TokenType tokenType) {
    return tokenType == TokenType.HOSPITAL ? KRANKENHAUS.getCode() : LABOR.getCode();
  }
}
