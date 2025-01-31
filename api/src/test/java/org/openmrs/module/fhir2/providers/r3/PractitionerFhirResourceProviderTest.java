/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.convertors.conv30_40.Practitioner30_40;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.openmrs.module.fhir2.api.search.param.PractitionerSearchParams;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

@RunWith(MockitoJUnitRunner.class)
public class PractitionerFhirResourceProviderTest extends BaseFhirR3ProvenanceResourceTest<org.hl7.fhir.r4.model.Practitioner> {
	
	private static final String PRACTITIONER_UUID = "48fb709b-48aa-4902-b681-926df5156e88";
	
	private static final String WRONG_PRACTITIONER_UUID = "f8bc0122-21db-4e91-a5d3-92ae01cafe92";
	
	private static final String GIVEN_NAME = "James";
	
	private static final String FAMILY_NAME = "pope";
	
	private static final String WRONG_NAME = "wrong name";
	
	private static final String PRACTITIONER_IDENTIFIER = "nurse";
	
	private static final String WRONG_PRACTITIONER_IDENTIFIER = "wrong identifier";
	
	private static final String PRACTITIONER_GIVEN_NAME = "John";
	
	private static final String WRONG_GIVEN_NAME = "Wrong given name";
	
	private static final String PRACTITIONER_FAMILY_NAME = "Doe";
	
	private static final String WRONG_FAMILY_NAME = "Wrong family name";
	
	private static final String CITY = "Indianapolis";
	
	private static final String WRONG_CITY = "Wrong city";
	
	private static final String STATE = "IN";
	
	private static final String WRONG_STATE = "Wrong state";
	
	private static final String POSTAL_CODE = "46202";
	
	private static final String WRONG_POSTAL_CODE = "Wrong postal code";
	
	private static final String COUNTRY = "USA";
	
	private static final String WRONG_COUNTRY = "Wrong country";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final String WRONG_LAST_UPDATED_DATE = "2020-09-09";
	
	private static final int PREFERRED_PAGE_SIZE = 10;
	
	private static final int COUNT = 1;
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirPractitionerService practitionerService;
	
	private PractitionerFhirResourceProvider resourceProvider;
	
	private org.hl7.fhir.r4.model.Practitioner practitioner;
	
	@Before
	public void setup() {
		resourceProvider = new PractitionerFhirResourceProvider();
		resourceProvider.setPractitionerService(practitionerService);
	}
	
	@Before
	public void initPractitioner() {
		HumanName name = new HumanName();
		name.addGiven(GIVEN_NAME);
		name.setFamily(FAMILY_NAME);
		
		Identifier theIdentifier = new Identifier();
		theIdentifier.setValue(PRACTITIONER_IDENTIFIER);
		
		practitioner = new org.hl7.fhir.r4.model.Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		practitioner.addName(name);
		practitioner.addIdentifier(theIdentifier);
		setProvenanceResources(practitioner);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Practitioner.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Practitioner.class.getName()));
	}
	
	@Test
	public void getPractitionerById_shouldReturnPractitioner() {
		IdType id = new IdType();
		id.setValue(PRACTITIONER_UUID);
		when(practitionerService.get(PRACTITIONER_UUID)).thenReturn(practitioner);
		
		Practitioner result = resourceProvider.getPractitionerById(id);
		assertThat(result.isResource(), is(true));
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getPractitionerByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_PRACTITIONER_UUID);
		assertThat(resourceProvider.getPractitionerById(idType).isResource(), is(true));
		assertThat(resourceProvider.getPractitionerById(idType), nullValue());
	}
	
	@Test
	public void findPractitionersByName_shouldReturnMatchingBundleOfPractitioners() {
		StringAndListParam nameParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(GIVEN_NAME)));
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, nameParam, null, null, null, null, null, null, null, null, null))).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(practitioner), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(nameParam, null, null, null, null, null, null,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PRACTITIONER));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void findPractitionersByWrongName_shouldReturnBundleWithEmptyEntries() {
		StringAndListParam nameParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(WRONG_NAME)));
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, nameParam, null, null, null, null, null, null, null, null, null)))
		            .thenReturn(new MockIBundleProvider<>(Collections.emptyList(), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(nameParam, null, null, null, null, null, null,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void findPractitionersByIdentifier_shouldReturnMatchingBundleOfPractitioners() {
		TokenAndListParam identifier = new TokenAndListParam().addAnd(new TokenOrListParam().add(PRACTITIONER_IDENTIFIER));
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(identifier, null, null, null, null, null, null, null, null, null, null)))
		            .thenReturn(
		                new MockIBundleProvider<>(Collections.singletonList(practitioner), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(identifier, null, null, null, null, null, null, null, null, null, null));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PRACTITIONER));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void findPractitionersByWrongIdentifier_shouldReturnBundleWithEmptyEntries() {
		TokenAndListParam identifier = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().add(WRONG_PRACTITIONER_IDENTIFIER));
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(identifier, null, null, null, null, null, null, null, null, null, null)))
		            .thenReturn(new MockIBundleProvider<>(Collections.emptyList(), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, identifier, null, null, null, null, null,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void findPractitionersByGivenName_shouldReturnMatchingBundleOfPractitioners() {
		StringAndListParam givenName = new StringAndListParam().addAnd(new StringParam(PRACTITIONER_GIVEN_NAME));
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, null, givenName, null, null, null, null, null, null, null, null))).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(practitioner), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, null, givenName, null, null, null, null,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PRACTITIONER));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void findPractitionersByWrongGivenName_shouldReturnBundleWithEmptyEntries() {
		StringAndListParam givenName = new StringAndListParam().addAnd(new StringParam(WRONG_GIVEN_NAME));
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, null, givenName, null, null, null, null, null, null, null, null)))
		            .thenReturn(new MockIBundleProvider<>(Collections.emptyList(), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, null, givenName, null, null, null, null,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void findPractitionersByFamilyName_shouldReturnMatchingBundleOfPractitioners() {
		StringAndListParam familyName = new StringAndListParam().addAnd(new StringParam(PRACTITIONER_FAMILY_NAME));
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, null, null, familyName, null, null, null, null, null, null, null)))
		            .thenReturn(
		                new MockIBundleProvider<>(Collections.singletonList(practitioner), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, null, null, familyName, null, null, null,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PRACTITIONER));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void findPractitionersByWrongFamilyName_shouldReturnBundleWithEmptyEntries() {
		StringAndListParam familyName = new StringAndListParam().addAnd(new StringParam(WRONG_FAMILY_NAME));
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, null, null, familyName, null, null, null, null, null, null, null)))
		            .thenReturn(new MockIBundleProvider<>(Collections.emptyList(), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, null, null, familyName, null, null, null,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void findPractitionersByAddressCity_shouldReturnMatchingBundleOfPractitioners() {
		StringAndListParam city = new StringAndListParam().addAnd(new StringParam(CITY));
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, null, null, null, city, null, null, null, null, null, null))).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(practitioner), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, null, null, null, city, null, null, null,
		    null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PRACTITIONER));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void findPractitionersByWrongAddressCity_shouldReturnBundleWithEmptyEntries() {
		StringAndListParam city = new StringAndListParam().addAnd(new StringParam(WRONG_CITY));
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, null, null, null, city, null, null, null, null, null, null)))
		            .thenReturn(new MockIBundleProvider<>(Collections.emptyList(), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, null, null, null, city, null, null, null,
		    null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void findPractitionersByAddressState_shouldReturnMatchingBundleOfPractitioners() {
		StringAndListParam state = new StringAndListParam().addAnd(new StringParam(STATE));
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, null, null, null, null, state, null, null, null, null, null))).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(practitioner), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, null, null, null, null, state, null, null,
		    null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PRACTITIONER));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void findPractitionersByWrongAddressState_shouldReturnBundleWithEmptyEntries() {
		StringAndListParam state = new StringAndListParam().addAnd(new StringParam(WRONG_STATE));
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, null, null, null, null, state, null, null, null, null, null)))
		            .thenReturn(new MockIBundleProvider<>(Collections.emptyList(), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, null, null, null, null, state, null, null,
		    null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void findPractitionersByAddressPostalCode_shouldReturnMatchingBundleOfPractitioners() {
		StringAndListParam postalCode = new StringAndListParam().addAnd(new StringParam(POSTAL_CODE));
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, null, null, null, null, null, postalCode, null, null, null, null)))
		            .thenReturn(
		                new MockIBundleProvider<>(Collections.singletonList(practitioner), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, null, null, null, null, null, postalCode,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PRACTITIONER));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void findPractitionersByWrongAddressPostalCode_shouldReturnBundleWithEmptyEntries() {
		StringAndListParam postalCode = new StringAndListParam().addAnd(new StringParam(WRONG_POSTAL_CODE));
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, null, null, null, null, null, postalCode, null, null, null, null)))
		            .thenReturn(new MockIBundleProvider<>(Collections.emptyList(), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, null, null, null, null, null, postalCode,
		    null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void findPractitionersByAddressCountry_shouldReturnMatchingBundleOfPractitioners() {
		StringAndListParam country = new StringAndListParam().addAnd(new StringParam(COUNTRY));
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, null, null, null, null, null, null, country, null, null, null))).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(practitioner), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, null, null, null, null, null, null, country,
		    null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PRACTITIONER));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void findPractitionersByWrongAddressCountry_shouldReturnBundleWithEmptyEntries() {
		StringAndListParam country = new StringAndListParam().addAnd(new StringParam(WRONG_COUNTRY));
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, null, null, null, null, null, null, country, null, null, null)))
		            .thenReturn(new MockIBundleProvider<>(Collections.emptyList(), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, null, null, null, null, null, null, country,
		    null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void findPractitionersByUUID_shouldReturnMatchingBundleOfPractitioners() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PRACTITIONER_UUID));
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, null, null, null, null, null, null, null, uuid, null, null))).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(practitioner), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, null, null, null, null, null, null, null,
		    uuid, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PRACTITIONER));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void findPractitionersByWrongUUID_shouldReturnBundleWithEmptyEntries() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(WRONG_PRACTITIONER_UUID));
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, null, null, null, null, null, null, null, uuid, null, null)))
		            .thenReturn(new MockIBundleProvider<>(Collections.emptyList(), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, null, null, null, null, null, null, null,
		    uuid, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void findPractitionersByLastUpdated_shouldReturnMatchingBundleOfPractitioners() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, null, null, null, null, null, null, null, null, lastUpdated, null)))
		            .thenReturn(
		                new MockIBundleProvider<>(Collections.singletonList(practitioner), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, null, null, null, null, null, null, null,
		    null, lastUpdated, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.iterator().next().fhirType(), is(FhirConstants.PRACTITIONER));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void findPractitionersByWrongLastUpdated_shouldReturnBundleWithEmptyEntries() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(WRONG_LAST_UPDATED_DATE)
		        .setLowerBound(WRONG_LAST_UPDATED_DATE);
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, null, null, null, null, null, null, null, null, lastUpdated, null)))
		            .thenReturn(new MockIBundleProvider<>(Collections.emptyList(), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, null, null, null, null, null, null, null,
		    null, lastUpdated, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void findPractitioners_shouldAddRelatedResourcesWhenReverseIncluded() {
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("Encounter:participant"));
		
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, null, null, null, null, null, null, null, null, null, revIncludes)))
		            .thenReturn(
		                new MockIBundleProvider<>(Arrays.asList(practitioner, new Encounter()), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, null, null, null, null, null, null, null,
		    null, null, revIncludes);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList.size(), equalTo(2));
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.PRACTITIONER));
		assertThat(resultList.get(1).fhirType(), is(FhirConstants.ENCOUNTER));
		assertThat(((Practitioner) resultList.get(0)).getId(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void findPractitioners_shouldNotAddRelatedResourcesForEmptyReverseInclude() {
		HashSet<Include> revIncludes = new HashSet<>();
		
		when(practitionerService.searchForPractitioners(
		    new PractitionerSearchParams(null, null, null, null, null, null, null, null, null, null, null))).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(practitioner), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForPractitioners(null, null, null, null, null, null, null, null,
		    null, null, revIncludes);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList.size(), equalTo(1));
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.PRACTITIONER));
		assertThat(((Practitioner) resultList.get(0)).getId(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void createPractitioner_shouldCreateNewPractitioner() {
		when(practitionerService.create(any(org.hl7.fhir.r4.model.Practitioner.class))).thenReturn(practitioner);
		
		MethodOutcome result = resourceProvider.createPractitioner(Practitioner30_40.convertPractitioner(practitioner));
		assertThat(result, notNullValue());
		assertThat(result.getCreated(), is(true));
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void updatePractitioner_shouldUpdatePractitioner() {
		when(practitionerService.update(eq(PRACTITIONER_UUID), any(org.hl7.fhir.r4.model.Practitioner.class)))
		        .thenReturn(practitioner);
		
		MethodOutcome result = resourceProvider.updatePractitioner(new IdType().setValue(PRACTITIONER_UUID),
		    Practitioner30_40.convertPractitioner(practitioner));
		assertThat(result, notNullValue());
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updatePractitioner_shouldThrowInvalidRequestForUuidMismatch() {
		when(practitionerService.update(eq(WRONG_PRACTITIONER_UUID), any(org.hl7.fhir.r4.model.Practitioner.class)))
		        .thenThrow(InvalidRequestException.class);
		
		resourceProvider.updatePractitioner(new IdType().setValue(WRONG_PRACTITIONER_UUID),
		    Practitioner30_40.convertPractitioner(practitioner));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updatePractitioner_shouldThrowInvalidRequestForMissingId() {
		org.hl7.fhir.r4.model.Practitioner noIdPractitioner = new org.hl7.fhir.r4.model.Practitioner();
		
		when(practitionerService.update(eq(PRACTITIONER_UUID), any(org.hl7.fhir.r4.model.Practitioner.class)))
		        .thenThrow(InvalidRequestException.class);
		
		resourceProvider.updatePractitioner(new IdType().setValue(PRACTITIONER_UUID),
		    Practitioner30_40.convertPractitioner(noIdPractitioner));
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void updatePractitioner_shouldThrowMethodNotAllowedIfDoesNotExist() {
		org.hl7.fhir.r4.model.Practitioner wrongPractitioner = new org.hl7.fhir.r4.model.Practitioner();
		wrongPractitioner.setId(WRONG_PRACTITIONER_UUID);
		
		when(practitionerService.update(eq(WRONG_PRACTITIONER_UUID), any(org.hl7.fhir.r4.model.Practitioner.class)))
		        .thenThrow(MethodNotAllowedException.class);
		
		resourceProvider.updatePractitioner(new IdType().setValue(WRONG_PRACTITIONER_UUID),
		    Practitioner30_40.convertPractitioner(wrongPractitioner));
	}
	
	@Test
	public void deletePractitioner_shouldDeletePractitioner() {
		OperationOutcome result = resourceProvider.deletePractitioner(new IdType().setValue(PRACTITIONER_UUID));
		
		assertThat(result, notNullValue());
		assertThat(result.getIssue(), notNullValue());
		assertThat(result.getIssueFirstRep().getSeverity(), equalTo(OperationOutcome.IssueSeverity.INFORMATION));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getCode(), equalTo("MSG_DELETED"));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getDisplay(),
		    equalTo("This resource has been deleted"));
	}
}
