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

import static de.gematik.demis.context.enrichment.service.utils.TestDataParser.RPS_BUNDLE_COMPOSITION_ID;
import static de.gematik.demis.context.enrichment.service.utils.TestDataParser.TokenType.AUTHENTICATOR;
import static de.gematik.demis.context.enrichment.service.utils.TestDataParser.getTokenFromResources;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import de.gematik.demis.context.enrichment.service.services.strategies.BundIdIdpStrategy;
import de.gematik.demis.context.enrichment.service.services.strategies.CertificateStrategy;
import de.gematik.demis.context.enrichment.service.services.strategies.GematikIdpStrategy;
import de.gematik.demis.context.enrichment.service.utils.TestDataParser.TokenType;
import org.hl7.fhir.r4.model.Provenance;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnrichmentServiceTest {

  private final String EXAMPLE_COMPOSITION_ID = "d95529ba-0d70-426f-8f77-7606f168268e";
  private final Provenance provenance = Instancio.create(Provenance.class);
  private final String TOKEN = getTokenFromResources(AUTHENTICATOR);
  @Mock private GematikIdpStrategy gematikIdpStrategy;
  @Mock private CertificateStrategy certificateStrategy;
  @Mock private BundIdIdpStrategy bundIdIdpStrategy;
  @InjectMocks private EnrichmentService underTest;

  @BeforeEach
  void init() {
    lenient()
        .when(gematikIdpStrategy.createProvenanceResource(anyMap(), any()))
        .thenReturn(provenance);
    lenient()
        .when(certificateStrategy.createProvenanceResource(anyMap(), any()))
        .thenReturn(provenance);
  }

  @ParameterizedTest(name = "Type: {0}-token")
  @EnumSource(
      value = TokenType.class,
      names = {"LAB", "HOSPITAL"})
  @DisplayName("Should select certificate strategy with lab / hospital token")
  void shouldSelectCertificateStrategy(TokenType tokenType) {
    underTest.addContextInformation(EXAMPLE_COMPOSITION_ID, getTokenFromResources(tokenType));
    verify(certificateStrategy)
        .createProvenanceResource(anyMap(), endsWith(RPS_BUNDLE_COMPOSITION_ID));
  }

  @Test
  @DisplayName("Should select idp strategy with authenticator token")
  void shouldSelectAuthenticatorStrategy() {
    underTest.addContextInformation(EXAMPLE_COMPOSITION_ID, TOKEN);
    verify(gematikIdpStrategy)
        .createProvenanceResource(anyMap(), endsWith(RPS_BUNDLE_COMPOSITION_ID));
  }

  @ParameterizedTest(name = "Type: {0}-token")
  @EnumSource(
      value = TokenType.class,
      names = {"BUNDID_ID", "BUNDID_USERNAME_PASSWORD"})
  @DisplayName("Should select bundId strategy with perso / username-password token")
  void shouldSelectBundIdStrategy(TokenType tokenType) {
    underTest.addContextInformation(EXAMPLE_COMPOSITION_ID, getTokenFromResources(tokenType));
    verify(bundIdIdpStrategy)
        .createProvenanceResource(anyMap(), endsWith(RPS_BUNDLE_COMPOSITION_ID));
  }
}
