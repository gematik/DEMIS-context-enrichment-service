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
 * #L%
 */

import static de.gematik.demis.context.enrichment.service.utils.enums.AssuranceLevelEnum.NONE;
import static de.gematik.demis.context.enrichment.service.utils.enums.OidEnum.getValueByOid;
import static io.micrometer.common.util.StringUtils.isBlank;
import static java.util.Objects.isNull;

import de.gematik.demis.context.enrichment.service.utils.enums.AssuranceLevelEnum;
import de.gematik.demis.notification.builder.demis.fhir.notification.builder.technicals.InitializableFhirObjectBuilder;
import java.util.ArrayList;
import java.util.List;
import lombok.Setter;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Provenance.ProvenanceAgentComponent;
import org.hl7.fhir.r4.model.Reference;

/** Builder for creating a FhirAgentRessource */
@Setter
public class AgentBuilder implements InitializableFhirObjectBuilder {

  private CodeableConcept type;
  private List<CodeableConcept> roles;
  private List<Extension> extensions;
  private String accountType;
  private String keycloakUserId;
  private String userName;
  private String accountIdentifierSystem;

  /**
   * Sets all static default values
   *
   * @return the builder
   */
  @Override
  public InitializableFhirObjectBuilder setDefaults() {
    this.type =
        new CodeableConcept()
            .setCoding(
                List.of(
                    new Coding()
                        .setCode("performer")
                        .setSystem(
                            "http://terminology.hl7.org/CodeSystem/provenance-participant-type")));
    this.roles =
        List.of(
            new CodeableConcept()
                .setCoding(
                    List.of(
                        new Coding()
                            .setCode("AUT")
                            .setSystem(
                                "http://terminology.hl7.org/CodeSystem/v3-ParticipationType"))));
    return this;
  }

  /**
   * Builds the AgentResource regarding the given parameter in builder
   *
   * @return the builder
   */
  @Override
  public ProvenanceAgentComponent build() {
    ProvenanceAgentComponent agent = new ProvenanceAgentComponent();
    agent.setType(this.type);
    agent.setRole(this.roles);
    agent.setExtension(this.extensions);
    agent.setWho(getWho());
    agent.setOnBehalfOf(getOnBehalfOf());
    return agent;
  }

  /**
   * Adds specific assurance extension
   *
   * @param acr a number that corresponds to a specific level of assurance
   */
  public void addAssuranceExtension(String acr) {
    Extension extension = new Extension();
    extension.setUrl(
        "https://demis.rki.de/fhir/StructureDefinition/ProvenanceAgentLevelOfAssurance");
    Coding coding =
        new Coding()
            .setCode(getAssuranceLvl(acr))
            .setSystem("https://demis.rki.de/fhir/CodeSystem/levelOfAssurance");
    CodeableConcept codeableConcept = new CodeableConcept().setCoding(List.of(coding));
    extension.setValue(codeableConcept);
    this.addExtension(extension);
  }

  /**
   * Adds specific organization extension
   *
   * @param oid
   */
  public void addOrganizationExtension(String oid) {
    Extension extension = new Extension();
    extension.setUrl(
        "https://demis.rki.de/fhir/StructureDefinition/ProvenanceAgentOrganizationType");
    Coding coding =
        new Coding()
            .setCode(getValueByOid(oid).getCode())
            .setSystem("https://demis.rki.de/fhir/CodeSystem/organizationType");
    CodeableConcept codeableConcept = new CodeableConcept().setCoding(List.of(coding));
    extension.setValue(codeableConcept);
    this.addExtension(extension);
  }

  private void addExtension(Extension extension) {
    if (isNull(this.extensions)) {
      this.extensions = new ArrayList<>();
    }
    extensions.add(extension);
  }

  private String getAssuranceLvl(String acr) {
    for (AssuranceLevelEnum assuranceLevel : AssuranceLevelEnum.values()) {
      if (assuranceLevel.getValues().contains(acr)) {
        return assuranceLevel.getName();
      }
    }
    return NONE.getName();
  }

  private Reference getWho() {
    if (isBlank(this.accountType) || isBlank(this.keycloakUserId)) {
      throw new IllegalArgumentException("Missing value for keycloakUserId or accountType");
    }
    return new Reference()
        .setType(this.accountType)
        .setIdentifier(
            new Identifier()
                .setSystem("urn:ietf:rfc:3986")
                .setValue("urn:uuid:" + this.keycloakUserId)
                .setType(getDefaultType("AN")));
  }

  private Reference getOnBehalfOf() {
    if (isBlank(this.accountIdentifierSystem) || isBlank(this.userName)) {
      throw new IllegalArgumentException("Missing value for accountIdentifierSystem or userName");
    }
    return new Reference()
        .setType(this.accountType)
        .setIdentifier(
            new Identifier()
                .setSystem(this.accountIdentifierSystem)
                .setValue(this.userName)
                .setType(getDefaultType("PRN")));
  }

  private CodeableConcept getDefaultType(String code) {
    return new CodeableConcept()
        .setCoding(
            List.of(
                new Coding()
                    .setCode(code)
                    .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")));
  }
}
