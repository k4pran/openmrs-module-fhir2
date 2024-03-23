/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static lombok.AccessLevel.PACKAGE;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.openmrs.module.fhir2.api.FhirEpisodeOfCareService;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("episodeOfCareFhirR4ResourceProvider")
@R4Provider
@Setter(PACKAGE)
public class EpisodeOfCareFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirEpisodeOfCareService episodeOfCareService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return EpisodeOfCare.class;
	}
	
	@Read
	public EpisodeOfCare getEpisodeOfCareById(@IdParam IdType id) {
		EpisodeOfCare episodeOfCare = episodeOfCareService.get(id.getIdPart());
		if (episodeOfCare == null) {
			throw new ResourceNotFoundException("Could not find EpisodeOfCare with Id " + id.getIdPart());
		}
		return episodeOfCare;
	}
	
	@Create
	public MethodOutcome createEpisodeOfCare(@ResourceParam EpisodeOfCare episodeOfCare) {
		return FhirProviderUtils.buildCreate(episodeOfCareService.create(episodeOfCare));
	}
	
	@Update
	public MethodOutcome updateEpisodeOfCare(@IdParam IdType id, @ResourceParam EpisodeOfCare episodeOfCare) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("Id must be specified to update");
		}
		
		episodeOfCare.setId(id.getIdPart());
		
		return FhirProviderUtils.buildUpdate(episodeOfCareService.update(id.getIdPart(), episodeOfCare));
	}
	
	@Delete
	public OperationOutcome deleteEpisodeOfCare(@IdParam IdType id) {
		episodeOfCareService.delete(id.getIdPart());
		episodeOfCareService.delete(id.getIdPart());
		return FhirProviderUtils.buildDeleteR4();
	}
}
