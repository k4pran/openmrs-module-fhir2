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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.FhirTestConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class EpisodeOfCareTranslatorImplTest {
	
	private static final String PATIENT_PROGRAM_UUID = "05a29f94-c0ed-11e2-94be-8c13b969e334";
	
	private static final String PATIENT_UUID = "8549f706-7e85-4c1d-9424-217d50a2988b";
	
	private static final String PATIENT_URI = FhirConstants.PATIENT + "/" + PATIENT_UUID;
	
	private static final String PATIENT_IDENTIFIER = "100024L";
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	private EpisodeOfCareTranslatorImpl episodeOfCareTranslator;
	
	@Before
	public void setup() {
		episodeOfCareTranslator = new EpisodeOfCareTranslatorImpl();
		episodeOfCareTranslator.setConceptTranslator(conceptTranslator);
		episodeOfCareTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
	}
	
	@Test
	public void shouldTranslateOpenmrsPatientToFhirPatient() {
		PatientProgram patientProgram = new PatientProgram();
		
		org.openmrs.Patient expectedPatient = new org.openmrs.Patient();
		expectedPatient.setUuid(PATIENT_UUID);
		
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifier(PATIENT_IDENTIFIER);
		expectedPatient.addIdentifier(patientIdentifier);
		
		patientProgram.setPatient(expectedPatient);
		
		Reference patientReference = new Reference().setReference(PATIENT_URI).setType(FhirTestConstants.PATIENT)
		        .setIdentifier(new Identifier().setValue(PATIENT_IDENTIFIER));
		patientReference.setId(PATIENT_UUID);
		
		when(patientReferenceTranslator.toFhirResource(expectedPatient)).thenReturn(patientReference);
		
		EpisodeOfCare episodeOfCare = episodeOfCareTranslator.toFhirResource(patientProgram);
		
		assertThat(episodeOfCare, notNullValue());
		assertThat(episodeOfCare.getPatient(), notNullValue());
		assertThat(episodeOfCare.getPatient().getId(), equalTo(expectedPatient.getUuid()));
		assertThat(episodeOfCare.getPatient().getIdentifier().getValue(),
		    equalTo(expectedPatient.getPatientIdentifier().getIdentifier()));
	}
	
	@Test
	public void shouldTranslateFhirPatientToOpenmrsPatient() {
		EpisodeOfCare episodeOfCare = new EpisodeOfCare();
		Patient expectedPatient = new Patient();
		expectedPatient.setUuid(PATIENT_UUID);
		
		Reference patientReference = new Reference().setReference(PATIENT_URI).setType(FhirTestConstants.PATIENT)
		        .setIdentifier(new Identifier().setValue(PATIENT_IDENTIFIER));
		patientReference.setId(PATIENT_UUID);
		episodeOfCare.setPatient(patientReference);
		
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifier(PATIENT_IDENTIFIER);
		expectedPatient.addIdentifier(patientIdentifier);
		
		when(patientReferenceTranslator.toOpenmrsType(patientReference)).thenReturn(expectedPatient);
		
		PatientProgram patientProgram = episodeOfCareTranslator.toOpenmrsType(episodeOfCare);
		
		assertThat(patientProgram, notNullValue());
		assertThat(patientProgram.getPatient(), notNullValue());
		assertThat(patientProgram.getPatient().getUuid(), equalTo(episodeOfCare.getPatient().getId()));
		assertThat(patientProgram.getPatient().getPatientIdentifier().getIdentifier(),
		    equalTo(episodeOfCare.getPatient().getIdentifier().getValue()));
	}
	
	@Test
	public void shouldTranslateProgramConceptToEpisodeOfCareType() {
		PatientProgram patientProgram = new PatientProgram();
		Program program = new Program();
		Concept concept = new Concept();
		CodeableConcept codeableConcept = new CodeableConcept();
		
		program.setConcept(concept);
		patientProgram.setProgram(program);
		
		when(conceptTranslator.toFhirResource(concept)).thenReturn(codeableConcept);
		
		EpisodeOfCare episodeOfCare = episodeOfCareTranslator.toFhirResource(patientProgram);
		
		assertThat(episodeOfCare, notNullValue());
		assertThat(episodeOfCare.getType().size(), is(1));
		assertThat(episodeOfCare.getType().get(0), is(codeableConcept));
	}
	
	@Test
	public void shouldHandleNullProgram() {
		PatientProgram patientProgram = new PatientProgram();
		EpisodeOfCare episodeOfCare = episodeOfCareTranslator.toFhirResource(patientProgram);
		
		assertThat(episodeOfCare, notNullValue());
		assertThat(episodeOfCare.getType().size(), is(0));
	}
	
	@Test
	public void shouldHandleNullCodeableConcept() {
		PatientProgram patientProgram = new PatientProgram();
		Program program = new Program();
		Concept concept = new Concept();
		
		program.setConcept(concept);
		patientProgram.setProgram(program);
		
		when(conceptTranslator.toFhirResource(concept)).thenReturn(null);
		
		EpisodeOfCare episodeOfCare = episodeOfCareTranslator.toFhirResource(patientProgram);
		
		assertThat(episodeOfCare, notNullValue());
		assertThat(episodeOfCare.getType().size(), is(0));
	}
	
	@Test
	public void shouldTranslateEpisodeOfCareTypeToProgramConcept() {
		EpisodeOfCare episodeOfCare = new EpisodeOfCare();
		CodeableConcept codeableConcept = new CodeableConcept();
		episodeOfCare.setType(Collections.singletonList(codeableConcept));
		
		Concept expectedConcept = new Concept();
		
		when(conceptTranslator.toOpenmrsType(codeableConcept)).thenReturn(expectedConcept);
		
		PatientProgram patientProgram = episodeOfCareTranslator.toOpenmrsType(episodeOfCare);
		
		assertThat(patientProgram, notNullValue());
		assertThat(patientProgram.getProgram().getConcept(), is(expectedConcept));
	}
	
	@Test
	public void shouldHandleEpisodeOfCareWithNullType() {
		EpisodeOfCare episodeOfCare = new EpisodeOfCare();
		episodeOfCare.setType(null);
		PatientProgram patientProgram = episodeOfCareTranslator.toOpenmrsType(episodeOfCare);
		assertThat(patientProgram.getProgram(), is(nullValue()));
	}
	
	@Test
	public void shouldHandleEpisodeOfCareWithEmptyTypeList() {
		EpisodeOfCare episodeOfCare = new EpisodeOfCare();
		episodeOfCare.setType(Collections.emptyList());
		PatientProgram patientProgram = episodeOfCareTranslator.toOpenmrsType(episodeOfCare);
		assertThat(patientProgram.getProgram(), is(nullValue()));
	}
	
	@Test
	public void shouldTranslateOpenmrsActivePatientProgramToFhirActiveStatus() {
		PatientProgram patientProgram = new PatientProgram();
		patientProgram.setDateEnrolled(Date.from(Instant.now()));
		
		EpisodeOfCare episodeOfCare = episodeOfCareTranslator.toFhirResource(patientProgram);
		
		assertThat(episodeOfCare, notNullValue());
		assertThat(patientProgram.getActive(), is(true));
		assertThat(episodeOfCare.getStatus(), is(EpisodeOfCare.EpisodeOfCareStatus.ACTIVE));
	}
	
	@Test
	public void shouldTranslateInProgressEpisodeOfCareToActivePatientProgram() {
		EpisodeOfCare episodeOfCare = new EpisodeOfCare();
		episodeOfCare.setStatus(EpisodeOfCare.EpisodeOfCareStatus.ACTIVE);
		
		Period period = new Period();
		period.setStart(new Date());
		episodeOfCare.setPeriod(period);
		
		PatientProgram patientProgram = episodeOfCareTranslator.toOpenmrsType(episodeOfCare);
		
		assertThat(patientProgram, notNullValue());
		assertThat(episodeOfCare.getStatus(), is(EpisodeOfCare.EpisodeOfCareStatus.ACTIVE));
		assertThat(patientProgram.getActive(), is(true));
	}
	
	@Test
	public void shouldTranslateVoidedPatientProgramToFinishesStatus() {
		PatientProgram patientProgram = new PatientProgram();
		patientProgram.setDateEnrolled(Date.from(Instant.now()));
		patientProgram.setVoided(true);
		
		EpisodeOfCare episodeOfCare = episodeOfCareTranslator.toFhirResource(patientProgram);
		
		assertThat(episodeOfCare, notNullValue());
		assertThat(episodeOfCare.getStatus(), is(EpisodeOfCare.EpisodeOfCareStatus.FINISHED));
		assertThat(patientProgram.getActive(), is(false));
	}
	
	@Test
	public void shouldTranslateFhirPeriodToOpenmrsEnrollment() {
		PatientProgram patientProgram = new PatientProgram();
		patientProgram.setDateEnrolled(Date.from(Instant.now().minus(24, ChronoUnit.HOURS)));
		patientProgram.setDateCompleted(Date.from(Instant.now()));
		
		EpisodeOfCare episodeOfCare = episodeOfCareTranslator.toFhirResource(patientProgram);
		
		assertThat(episodeOfCare, notNullValue());
		assertThat(patientProgram.getDateEnrolled(), is(episodeOfCare.getPeriod().getStart()));
		assertThat(patientProgram.getDateCompleted(), is(episodeOfCare.getPeriod().getEnd()));
	}
	
	@Test
	public void shouldTranslateEpisodeOfCarePeriodToPatientProgramEnrollment() {
		EpisodeOfCare episodeOfCare = new EpisodeOfCare();
		
		Period period = new Period();
		period.setStart(Date.from(Instant.now().minus(24, ChronoUnit.HOURS)));
		period.setEnd(Date.from(Instant.now()));
		
		episodeOfCare.setPeriod(period);
		
		PatientProgram patientProgram = episodeOfCareTranslator.toOpenmrsType(episodeOfCare);
		
		assertThat(patientProgram, notNullValue());
		assertThat(patientProgram.getDateEnrolled(), is(episodeOfCare.getPeriod().getStart()));
		assertThat(patientProgram.getDateCompleted(), is(episodeOfCare.getPeriod().getEnd()));
	}
	
	@Test
	public void shouldTranslateEpisodeOfCareIdToPatientProgramUUID() {
		EpisodeOfCare episodeOfCare = new EpisodeOfCare();
		episodeOfCare.setId(PATIENT_PROGRAM_UUID);
		
		PatientProgram resultProgram = episodeOfCareTranslator.toOpenmrsType(episodeOfCare);
		
		assertThat(resultProgram, notNullValue());
		assertThat(resultProgram.getUuid(), is(episodeOfCare.getId()));
	}
}
