package org.openmrs.module.fhir2.api.dao.impl;

import javax.annotation.Nonnull;

import java.util.List;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.openmrs.PatientProgram;
import org.openmrs.module.fhir2.api.dao.FhirEpisodeOfCareDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirEpisodeOfCareDaoImpl extends BaseEncounterDao<PatientProgram> implements FhirEpisodeOfCareDao {
	
	@Override
	public List<String> getSearchResultUuids(@Nonnull SearchParameterMap theParams) {
		return null;
	}
	
	@Override
	protected void handleDate(Criteria criteria, DateRangeParam dateRangeParam) {
		
	}
	
	@Override
	protected void handleEncounterType(Criteria criteria, TokenAndListParam tokenAndListParam) {
		
	}
	
	@Override
	protected void handleParticipant(Criteria criteria, ReferenceAndListParam referenceAndListParam) {
		
	}
}
