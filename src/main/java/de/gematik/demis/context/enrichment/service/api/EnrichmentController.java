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

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import de.gematik.demis.context.enrichment.service.services.EnrichmentService;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Provenance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint for the context enrichment service. */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class EnrichmentController {

  private final EnrichmentService enrichmentService;
  private final FhirParser fhirParser;

  /**
   * Endpoint to create provenance resource from token.
   *
   * @param compositionId id of composition to reference
   * @param token user token
   * @return the enriched FHIR notification
   */
  @PostMapping(
      path = "/enrichment",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<String> enrichBundle(
      @RequestBody @NotBlank final String compositionId,
      @RequestHeader(value = AUTHORIZATION) final String token) {
    log.debug("Received request notification");
    Provenance provenanceResource = enrichmentService.addContextInformation(compositionId, token);
    return ok(fhirParser.encodeToJson(provenanceResource));
  }
}
