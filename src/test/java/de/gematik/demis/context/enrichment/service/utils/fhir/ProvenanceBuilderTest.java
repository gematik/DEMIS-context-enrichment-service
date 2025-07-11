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

import static org.assertj.core.api.Assertions.*;

import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.Provenance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProvenanceBuilderTest {

  private static final String COMPOSITION_ID = "1b22c8d8-f1cb-311a-991a-8b488979af8e";
  private static final String COMPOSITION_ID_WITH_COMPOSITION = "Composition/" + COMPOSITION_ID;

  @ParameterizedTest
  @ValueSource(strings = {COMPOSITION_ID})
  void createCompositionRight(String validCompositionId) {
    ProvenanceBuilder provenanceBuilder = new ProvenanceBuilder();
    provenanceBuilder.setTargetCompositionId(validCompositionId);
    Provenance provenance = provenanceBuilder.build();
    assertThat(provenance.getTarget().getFirst().getReference())
        .isEqualTo("Composition/" + COMPOSITION_ID);
  }

  @Test
  void createCompositionRightIfCompositionAlreadyContained() {
    ProvenanceBuilder provenanceBuilder = new ProvenanceBuilder();
    provenanceBuilder.setTargetCompositionId(COMPOSITION_ID);
    Provenance provenance = provenanceBuilder.build();
    assertThat(provenance.getTarget().getFirst().getReference())
        .isEqualTo(COMPOSITION_ID_WITH_COMPOSITION);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "üäöl=)!)§&$",
        "Composition",
        "Composition/",
        "https://demis.rki.de/fhir/Composition/1b22c8d8-f1cb-311a-991a-8b488979af8e",
        "Composition/1b22c8d8-b9862-311a-991a-8b488979af8e",
        COMPOSITION_ID_WITH_COMPOSITION
      })
  @SneakyThrows
  void createCompositionRightEvenIfCompositionIdUnlikeUUID(String compositionIdUnlikeUUID) {
    ProvenanceBuilder provenanceBuilder = new ProvenanceBuilder();
    provenanceBuilder.setTargetCompositionId(compositionIdUnlikeUUID);
    Provenance provenance = provenanceBuilder.build();
    assertThat(provenance.getTarget().getFirst().getReference())
        .isEqualTo("Composition/" + compositionIdUnlikeUUID);
  }
}
