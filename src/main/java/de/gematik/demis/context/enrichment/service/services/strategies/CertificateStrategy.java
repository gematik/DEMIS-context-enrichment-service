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
import static de.gematik.demis.context.enrichment.service.utils.enums.AssuranceLevelEnum.SUBSTANTIAL;
import static de.gematik.demis.context.enrichment.service.utils.enums.OidEnum.KRANKENHAUS;
import static de.gematik.demis.context.enrichment.service.utils.enums.OidEnum.LABOR;
import static de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum.ISS;
import static de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum.PREFERRED_USERNAME;
import static de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum.SUB;

import de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum;
import de.gematik.demis.context.enrichment.service.utils.fhir.AgentBuilder;
import java.util.List;
import java.util.Map;
import org.hl7.fhir.r4.model.Provenance.ProvenanceAgentComponent;
import org.springframework.stereotype.Service;

@Service
public class CertificateStrategy extends TokenProcessStrategy {

  @Override
  List<TokenClaimsEnum> getNeededClaims() {
    return List.of(SUB, PREFERRED_USERNAME);
  }

  @Override
  ProvenanceAgentComponent createAgent(Map<String, Object> claims) {
    AgentBuilder agentBuilder = new AgentBuilder();
    agentBuilder.setDefaults();
    agentBuilder.addAssuranceExtension(SUBSTANTIAL.getValues().getFirst());
    agentBuilder.addOrganizationExtension(
        claims.get(ISS.getName()).toString().endsWith("HOSPITAL")
            ? KRANKENHAUS.getOid()
            : LABOR.getOid());
    agentBuilder.setAccountType(ORGANISATION.getDisplayName());
    agentBuilder.setKeycloakUserId(claims.get(SUB.getName()).toString());
    agentBuilder.setUserName(claims.get(PREFERRED_USERNAME.getName()).toString());
    agentBuilder.setAccountIdentifierSystem(ACCOUNT_SOURCE_CERTIFICATES);
    return agentBuilder.build();
  }
}
