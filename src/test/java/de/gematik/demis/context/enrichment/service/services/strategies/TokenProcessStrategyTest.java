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
import static de.gematik.demis.context.enrichment.service.utils.TestDataParser.TokenType.BUNDID_ID;
import static de.gematik.demis.context.enrichment.service.utils.TestDataParser.TokenType.BUNDID_USERNAME_PASSWORD;
import static de.gematik.demis.context.enrichment.service.utils.TestDataParser.TokenType.HOSPITAL;
import static de.gematik.demis.context.enrichment.service.utils.TestDataParser.TokenType.LAB;
import static de.gematik.demis.context.enrichment.service.utils.TestDataParser.getTokenClaimsFromResources;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hl7.fhir.r4.model.Provenance.ProvenanceEntityRole.SOURCE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.demis.context.enrichment.service.exceptions.CesServiceException;
import de.gematik.demis.context.enrichment.service.utils.TestJwtCreator;
import de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum;
import java.util.Map;
import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Provenance.ProvenanceEntityComponent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TokenProcessStrategyTest {

  final String COMPOSITION_ID = "85d9819c-ce0b-4ee3-872e-0017657886b7";

  public static Stream<Arguments> getStrategies() {
    return Stream.of(
        Arguments.of("lab", new CertificateStrategy(), getTokenClaimsFromResources(LAB)),
        Arguments.of("hospital", new CertificateStrategy(), getTokenClaimsFromResources(HOSPITAL)),
        Arguments.of(
            "authenticator", new GematikIdpStrategy(), getTokenClaimsFromResources(AUTHENTICATOR)),
        Arguments.of(
            "bundId_Perso", new BundIdIdpStrategy(), getTokenClaimsFromResources(BUNDID_ID)),
        Arguments.of(
            "bundId_UsernamePassword",
            new BundIdIdpStrategy(),
            getTokenClaimsFromResources(BUNDID_USERNAME_PASSWORD)));
  }

  @MethodSource("getStrategies")
  @ParameterizedTest(name = "Type: {0}-token")
  @DisplayName("Test each strategy to deliver agent entry and correct composition")
  void createAndAppendProvenanceResource(
      String testTypeName, TokenProcessStrategy underTest, Map<String, Object> claims) {
    Provenance provenance = underTest.createProvenanceResource(claims, COMPOSITION_ID);

    assertAll(
        "Check if general structure of ProvenanceResource is created successfully",
        () -> assertThat(provenance).isNotNull(),
        () -> assertThat(provenance.getAgent()).hasSize(1),
        () -> assertThat(provenance.getEntity()).hasSize(1),
        () ->
            assertThat(provenance.getTarget().getFirst().getReference())
                .isEqualTo("Composition/" + COMPOSITION_ID));
  }

  @MethodSource("getStrategies")
  @ParameterizedTest(name = "Type: {0}-token")
  @DisplayName("Test should throw exception if mandatory claim is missing in token")
  void shouldThrowExceptionIfAzpIsNotAvailableInJwt(
      String testTypeName, TokenProcessStrategy underTest, Map<String, Object> notNeededClaims) {
    for (TokenClaimsEnum claim : underTest.getNeededClaims()) {
      Map<String, Object> claims = new TestJwtCreator().removeClaim(claim.getName()).createClaims();

      CesServiceException ex =
          assertThrows(
              CesServiceException.class,
              () -> underTest.createProvenanceResource(claims, COMPOSITION_ID));
      assertThat(ex.getMessage()).isEqualTo("Missing values for claims: [" + claim + "]");
    }
  }

  @MethodSource("getStrategies")
  @ParameterizedTest(name = "Type: {0}-token")
  @DisplayName("Test should create entity correctly for each token type")
  void shouldCreateEntityCorrectly(
      String testTypeName, TokenProcessStrategy underTest, Map<String, Object> claims) {
    ProvenanceEntityComponent entity = underTest.createEntity(claims);

    assertAll(
        () -> assertThat(entity.getRole()).isEqualTo(SOURCE),
        () -> assertThat(entity.getWhat().getType()).isEqualTo("Endpoint"),
        () -> assertThat(entity.getWhat().getIdentifier().getValue()).isEqualTo(claims.get("azp")));
  }
}
