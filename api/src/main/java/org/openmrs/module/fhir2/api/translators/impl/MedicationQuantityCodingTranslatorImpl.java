/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.Concept;
import org.openmrs.module.fhir2.FhirConstants;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of Coding Translator that maps a Medication Quantity Concept to/from a
 * Coding in FHIR with a preferred set of systems and codes to prioritize. This will first favor
 * using RxNorm as the coding system. If this is not present on the OpenMRS concept, it will next
 * favor SNOMED-CT. Finally, if neither are present, it will favor the Concept UUID with a null
 * system.
 */
@Component
public class MedicationQuantityCodingTranslatorImpl extends BaseCodingTranslator {
	
	@Override
	public Coding toFhirResource(@Nonnull Concept concept) {
		CodeableConcept codeableConcept = conceptTranslator.toFhirResource(concept);
		if (codeableConcept == null) {
			return null;
		}
		
		Coding coding = getCodingForSystem(codeableConcept, FhirConstants.RX_NORM_SYSTEM_URI);
		if (coding == null) {
			coding = getCodingForSystem(codeableConcept, FhirConstants.SNOMED_SYSTEM_URI);
		}
		if (coding == null) {
			coding = getCodingForSystem(codeableConcept, null);
		}
		if (coding == null) {
			coding = codeableConcept.getCodingFirstRep();
		}
		
		coding.setDisplay(codeableConcept.getCoding().stream().filter(c -> c.getSystem() == null || c.getSystem().isEmpty())
		        .findFirst().map(Coding::getDisplay).orElse(null));
		
		return coding;
	}
}
