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

package de.gematik.demis.context.enrichment.service.utils.fhir;

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

import static de.gematik.demis.context.enrichment.service.utils.enums.OidEnum.ZAHNARZTPRAXIS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Provenance.ProvenanceAgentComponent;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AgentBuilderTest {

  @Test
  void shouldCreateAgentSuccessfully() {
    assertDoesNotThrow(() -> Instancio.create(AgentBuilder.class).build());
  }

  private static Stream<Arguments> shouldThrowIfAccountTypeMissing() {
    return Stream.of(
        Arguments.of("accountType", "Missing value for keycloakUserId or accountType"),
        Arguments.of("keycloakUserId", "Missing value for keycloakUserId or accountType"),
        Arguments.of(
            "accountIdentifierSystem", "Missing value for accountIdentifierSystem or userName"),
        Arguments.of("userName", "Missing value for accountIdentifierSystem or userName"));
  }

  @MethodSource
  @ParameterizedTest(name = "{0} missing")
  @DisplayName("should throw exception if important value is missing")
  void shouldThrowIfAccountTypeMissing(String field, String message) {
    AgentBuilder builder = Instancio.of(AgentBuilder.class).ignore(field(field)).create();
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, builder::build);
    assertThat(ex.getMessage()).isEqualTo(message);
  }

  private static Stream<Arguments> shouldReturnCorrectAssuranceLevel() {
    return Stream.of(
        Arguments.of("STORK-QAA-Level-1", "low"),
        Arguments.of("STORK-QAA-Level-2", "none"),
        Arguments.of("STORK-QAA-Level-3", "substantial"),
        Arguments.of("STORK-QAA-Level-4", "high"),
        Arguments.of("STORK-QAA-Level-10", "none"),
        Arguments.of("asdf", "none"),
        Arguments.of("STORK-AA-Level-1", "none"),
        Arguments.of("stork-qaa-level-1", "none"));
  }

  @MethodSource
  @DisplayName("should set correct assurance level")
  @ParameterizedTest(name = "{0} -> {1}")
  void shouldReturnCorrectAssuranceLevel(String acr, String assurance) {
    AgentBuilder builder =
        Instancio.of(AgentBuilder.class)
            .generate(field("extensions"), gen -> gen.collection().size(0))
            .create();

    builder.addAssuranceExtension(acr);
    ProvenanceAgentComponent agent = builder.build();
    assertThat(
            agent
                .getExtension()
                .getFirst()
                .getValue()
                .getChildByName("coding")
                .getValues()
                .getFirst()
                .getChildByName("code")
                .getValues()
                .getFirst()
                .toString())
        .isEqualTo(assurance);
  }

  @Test
  @DisplayName("should set url for assurance correct to extension")
  void shouldAddUrlCorrectlyToAssuranceExtension() {
    AgentBuilder builder =
        Instancio.of(AgentBuilder.class)
            .generate(field("extensions"), gen -> gen.collection().size(0))
            .create();
    builder.addAssuranceExtension("0");
    ProvenanceAgentComponent agent = builder.build();
    assertThat(agent.getExtension().getFirst().getUrl())
        .isEqualTo("https://demis.rki.de/fhir/StructureDefinition/ProvenanceAgentLevelOfAssurance");
  }

  @Test
  @DisplayName("should set oid correct to extension")
  void shouldPutOidOnRightPlaceInExtension() {
    AgentBuilder builder =
        Instancio.of(AgentBuilder.class)
            .generate(field("extensions"), gen -> gen.collection().size(0))
            .create();
    builder.addOrganizationExtension(ZAHNARZTPRAXIS.getOid());
    ProvenanceAgentComponent agent = builder.build();
    assertThat(
            agent
                .getExtension()
                .getFirst()
                .getValue()
                .getChildByName("coding")
                .getValues()
                .getFirst()
                .getChildByName("code")
                .getValues()
                .getFirst()
                .toString())
        .isEqualTo(ZAHNARZTPRAXIS.getCode());
  }

  @Test
  @DisplayName("should set url for organization correct to extension")
  void shouldAddUrlCorrectlyToOrganizationExtension() {
    AgentBuilder builder =
        Instancio.of(AgentBuilder.class)
            .generate(field("extensions"), gen -> gen.collection().size(0))
            .create();
    builder.addOrganizationExtension(ZAHNARZTPRAXIS.getOid());
    ProvenanceAgentComponent agent = builder.build();
    assertThat(agent.getExtension().getFirst().getUrl())
        .isEqualTo("https://demis.rki.de/fhir/StructureDefinition/ProvenanceAgentOrganizationType");
  }

  @Test
  @DisplayName("should set accountType at correct place")
  void shouldPutAccountTypeCorrect() {
    String testAccountTypeValue = "accountType";
    AgentBuilder builder = Instancio.of(AgentBuilder.class).ignore(field("accountType")).create();
    builder.setAccountType(testAccountTypeValue);
    ProvenanceAgentComponent agent = builder.build();
    assertThat(agent.getWho().getType()).isEqualTo(testAccountTypeValue);
  }

  @Test
  @DisplayName("should set keycloakUserId at correct place")
  void shouldPutKeycloakUserIdCorrect() {
    String testKeycloakUserIdValue = "keycloakUserId";
    AgentBuilder builder =
        Instancio.of(AgentBuilder.class).ignore(field("keycloakUserId")).create();
    builder.setKeycloakUserId(testKeycloakUserIdValue);
    ProvenanceAgentComponent agent = builder.build();
    assertThat(agent.getWho().getIdentifier().getValue())
        .isEqualTo("urn:uuid:" + testKeycloakUserIdValue);
  }

  @Test
  @DisplayName("should set userName at correct place")
  void shouldPutUserNameCorrect() {
    String testUserNameValue = "userName";
    AgentBuilder builder = Instancio.of(AgentBuilder.class).ignore(field("userName")).create();
    builder.setUserName(testUserNameValue);
    ProvenanceAgentComponent agent = builder.build();
    assertThat(agent.getOnBehalfOf().getIdentifier().getValue()).isEqualTo(testUserNameValue);
  }

  @Test
  @DisplayName("should set accountIdentifierSystem at correct place")
  void shouldPutAccountSourceCorrect() {
    String testAccountIdentifierSystemValue = "accountSource";
    AgentBuilder builder =
        Instancio.of(AgentBuilder.class).ignore(field("accountIdentifierSystem")).create();
    builder.setAccountIdentifierSystem(testAccountIdentifierSystemValue);
    ProvenanceAgentComponent agent = builder.build();
    assertThat(agent.getOnBehalfOf().getIdentifier().getSystem())
        .isEqualTo(testAccountIdentifierSystemValue);
  }
}
