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

package de.gematik.demis.context.enrichment.service.utils.enums;

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

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum OidEnum {
  LABOR("labor", "laboratory"),
  BETRIEBSSTAETTE_ARZT("1.2.276.0.76.4.50", "physicianOffice"),
  ZAHNARZTPRAXIS("1.2.276.0.76.4.51", "dentalSurgery"),
  BETRIEBSSTAETTE_PSYCHOTHERAPEUT("1.2.276.0.76.4.52", "psycFacility"),
  KRANKENHAUS("1.2.276.0.76.4.53", "hospital"),
  OEFFENTLICHE_APOTHEKE("1.2.276.0.76.4.54", "othMedFacility"),
  KRANKENHAUSAPOTHEKE("1.2.276.0.76.4.55", "othMedFacility"),
  BUNDESWEHRAPOTHEKE("1.2.276.0.76.4.56", "othMedFacility"),
  BETRIEBSSTAETTE_MOBILE_EINRICHTUNG_RETTUNGSDIENST("1.2.276.0.76.4.57", "emResServ"),
  BETRIEBSSTAETTE_GESUNDHEITS_KRANKEN_UND_ALTENPFLEGE("1.2.276.0.76.4.245", "housingFacility"),
  BETRIEBSSTAETTE_GEBURTSHILFE("1.2.276.0.76.4.246", "maternity"),
  BETRIEBSSTAETTE_PHYSIOTHERAPIE("1.2.276.0.76.4.247", "othMedPractice"),
  BETRIEBSSTAETTE_SANITAETSDIENST_BUNDESWEHR("1.2.276.0.76.4.254", "othMedFacility"),
  BETRIEBSSTAETTE_OEFFENTLICHER_GESUNDHEITSDIENST("1.2.276.0.76.4.255", "medFacPHA"),
  BETRIEBSSTAETTE_ARBEITSMEDIZIN("1.2.276.0.76.4.256", "othMedFacility"),
  BETRIEBSSTAETTE_VORSORGE_UND_REHABILITATION("1.2.276.0.76.4.257", "prevCareRehab");

  private String oid;
  private String code;

  OidEnum(String oid, String code) {
    this.oid = oid;
    this.code = code;
  }

  public static OidEnum getValueByOid(String oid) {
    return Arrays.stream(OidEnum.values())
        .filter(o -> o.getOid().equals(oid))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown OID: " + oid));
  }
}
