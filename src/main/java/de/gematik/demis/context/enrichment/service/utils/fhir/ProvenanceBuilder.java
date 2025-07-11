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

import static java.util.Objects.isNull;

import de.gematik.demis.notification.builder.demis.fhir.notification.builder.technicals.InitializableFhirObjectBuilder;
import de.gematik.demis.notification.builder.demis.fhir.notification.utils.Utils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Provenance.ProvenanceAgentComponent;
import org.hl7.fhir.r4.model.Provenance.ProvenanceEntityComponent;
import org.hl7.fhir.r4.model.Reference;

/** Builder for creating a FhirProvenanceRessource */
@Slf4j
@Setter
public class ProvenanceBuilder implements InitializableFhirObjectBuilder {

  private static final String COMPOSITION_PREFIX = "Composition/";
  private String id;
  private String targetCompositionId;
  private CodeableConcept activity;
  private Date recorded;
  private List<ProvenanceAgentComponent> agents;
  private List<ProvenanceEntityComponent> entities;

  /**
   * Sets all static default values
   *
   * @return the builder
   */
  @Override
  public ProvenanceBuilder setDefaults() {
    this.id = Utils.generateUuidString();
    this.recorded = Utils.getCurrentDate();
    this.activity =
        new CodeableConcept()
            .setCoding(
                List.of(
                    new Coding()
                        .setCode("CREATE")
                        .setSystem("http://terminology.hl7.org/CodeSystem/v3-DataOperation")));
    return this;
  }

  /**
   * Builds the resource regarding the given parameter
   *
   * @return
   */
  @Override
  public Provenance build() {
    Provenance provenance = new Provenance();
    provenance.setId(this.id);
    provenance.setMeta(getMeta());
    provenance.setTarget(getTarget());
    provenance.setRecorded(this.recorded);
    provenance.setActivity(this.activity);
    provenance.setAgent(this.agents);
    provenance.setEntity(this.entities);
    return provenance;
  }

  private List<Reference> getTarget() {
    return List.of(new Reference(COMPOSITION_PREFIX + targetCompositionId));
  }

  private Meta getMeta() {
    Meta meta = new Meta();
    meta.addProfile("https://demis.rki.de/fhir/StructureDefinition/DemisProvenance");
    return meta;
  }

  /**
   * Adds agent to Provenance
   *
   * @param agent
   */
  public void addAgent(ProvenanceAgentComponent agent) {
    if (isNull(this.agents)) {
      this.agents = new ArrayList<>();
    }
    this.agents.add(agent);
  }

  /**
   * Adds entity to Provenance
   *
   * @param entity
   */
  public void addEntity(ProvenanceEntityComponent entity) {
    if (isNull(this.entities)) {
      this.entities = new ArrayList<>();
    }
    this.entities.add(entity);
  }
}
