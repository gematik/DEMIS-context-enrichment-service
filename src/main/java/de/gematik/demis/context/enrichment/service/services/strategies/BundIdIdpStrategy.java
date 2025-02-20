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

import static de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum.ACCOUNT_SOURCE;
import static de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum.ACCOUNT_TYPE;
import static de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum.LEVEL_OF_ASSURANCE;
import static de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum.SUB;
import static de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum.USERNAME;

import de.gematik.demis.context.enrichment.service.utils.enums.TokenClaimsEnum;
import de.gematik.demis.context.enrichment.service.utils.fhir.AgentBuilder;
import java.util.List;
import java.util.Map;
import org.hl7.fhir.r4.model.Provenance.ProvenanceAgentComponent;
import org.springframework.stereotype.Service;

/** Service used to create Provenance resource */
@Service
public class BundIdIdpStrategy extends TokenProcessStrategy {

  @Override
  List<TokenClaimsEnum> getNeededClaims() {
    return List.of(LEVEL_OF_ASSURANCE, ACCOUNT_TYPE, SUB, USERNAME, ACCOUNT_SOURCE);
  }

  @Override
  public ProvenanceAgentComponent createAgent(Map<String, Object> claims) {
    AgentBuilder agentBuilder = new AgentBuilder();
    agentBuilder.setDefaults();
    agentBuilder.addAssuranceExtension(claims.get(LEVEL_OF_ASSURANCE.getName()).toString());
    agentBuilder.setAccountType(claims.get(ACCOUNT_TYPE.getName()).toString());
    agentBuilder.setKeycloakUserId(claims.get(SUB.getName()).toString());
    agentBuilder.setUserName(claims.get(USERNAME.getName()).toString());
    agentBuilder.setAccountIdentifierSystem(BUNDID_IDENTIFIER);
    return agentBuilder.build();
  }
}
