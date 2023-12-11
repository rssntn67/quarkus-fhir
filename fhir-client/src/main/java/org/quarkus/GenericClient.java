package org.quarkus;

import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.DomainResource;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.client.api.IBasicClient;

public interface GenericClient<T extends DomainResource> extends IBasicClient {

    @Read()
    T read(@IdParam IIdType theId);

    @Search()
    Bundle search(String code);

}