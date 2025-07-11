package de.gematik.demis.context.enrichment.service.api;

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

import static de.gematik.demis.context.enrichment.service.exceptions.ErrorCode.INVALID_COMPOSITION_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.gematik.demis.context.enrichment.service.exceptions.CesServiceException;
import de.gematik.demis.context.enrichment.service.services.EnrichmentService;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.Provenance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = EnrichmentController.class)
class EnrichmentControllerTest {

  private static final String CONTENT_HELLO_WORLD = "{\"Hello\": \"World\"}";
  @Autowired protected MockMvc mockMvc;
  @MockitoBean private EnrichmentService enrichmentService;
  @MockitoBean private FhirParser fhirParser;

  @BeforeEach
  void setUp() {
    lenient()
        .when(enrichmentService.addContextInformation(any(), any()))
        .thenReturn(new Provenance());
    lenient().when(fhirParser.encodeToJson(any())).thenReturn(CONTENT_HELLO_WORLD);
  }

  @Test
  @SneakyThrows
  void sendRequestWithoutBodyAndExpectError400() {
    mockMvc
        .perform(post("/enrichment").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(400));
  }

  @Test
  @SneakyThrows
  void sendRequestWithoutAuthHeaderAndExpect401() {
    mockMvc
        .perform(
            post("/enrichment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CONTENT_HELLO_WORLD))
        .andExpect(status().is(401));
  }

  @Test
  @SneakyThrows
  void sendRequestSuccessfully() {
    mockMvc
        .perform(
            post("/enrichment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CONTENT_HELLO_WORLD)
                .header(AUTHORIZATION, "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.Hello").value("World"));
  }

  @Test
  @SneakyThrows
  void shouldReturnBadRequestIfCesServiceExceptionBeenThrown() {
    when(enrichmentService.addContextInformation(any(), any()))
        .thenThrow(new CesServiceException(INVALID_COMPOSITION_ID, "Ces service exception"));

    mockMvc
        .perform(
            post("/enrichment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CONTENT_HELLO_WORLD)
                .header(AUTHORIZATION, "Bearer token"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  void sendRequestWithEmptyBodyAndExpectError400() {
    mockMvc
        .perform(
            post("/enrichment")
                .contentType(MediaType.APPLICATION_JSON)
                .content("")
                .header(AUTHORIZATION, "Bearer token"))
        .andExpect(status().is(400));
  }
}
