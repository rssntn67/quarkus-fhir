package org.quarkus;

import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.DomainResource;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.Practitioner;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public abstract class FhirClient<T extends DomainResource> {

    private final int queryLimit;
    private final IGenericClient iGenericClient;

    private final GenericClient<T> client;

    public static final String FHIR_MEDIA_TYPE="application/fhir+json";
    public static final Map<Class,String> RESOURCE_TYPE_MAP = new HashMap<>();

    static {
        RESOURCE_TYPE_MAP.put(Practitioner.class, "Practitioner");
    }

    @Inject
    FhirClient(String serverBase, Integer timeout, Integer queryLimit) {
        // Init Context
        this.queryLimit=queryLimit;
        FhirContext ctx = FhirContext.forR5();
        ctx.getRestfulClientFactory().setSocketTimeout(timeout);

        //Create a Generic Client without map
        iGenericClient = ctx.newRestfulGenericClient(serverBase);

        client = ctx.newRestfulClient(GenericClient.class, serverBase);
    }



    public T read(IIdType theId) {
        return client.read(theId);
    }


    public IIdType create(T t) {
        MethodOutcome outcome = iGenericClient.create()
                .resource(t)
                .prettyPrint()
                .encodedJson()
                .execute();

        return outcome.getId();
    }
    public T update(String id, T t) {
        IIdType idType = new IdType(RESOURCE_TYPE_MAP.get(t.getClass()), id);
        t.setId(idType.toString());
        iGenericClient.update().resource(t).execute();
        return t;
    }

    public Bundle getAll() {
        SortSpec sortSpec = new SortSpec("_lastUpdated",
                SortOrderEnum.DESC);
        return
                iGenericClient.search()
                        .forResource(Practitioner.class)
                        .count(queryLimit)
                        .offset(0)
                        .sort(sortSpec)
                        .returnBundle(Bundle.class)
                        .execute();
    }

    public OperationOutcome delete(T t) {
        MethodOutcome response =
                iGenericClient.delete().resourceById(new IdType(RESOURCE_TYPE_MAP.get(t.getClass()), t.getId())).execute();

        return (OperationOutcome) response.getOperationOutcome();
    }

    public Bundle search(TokenClientParam token, String code, T t) {
        return iGenericClient.search()
                .forResource(t.getClass())
                .where(token.exactly().identifier(code))
                .returnBundle(Bundle.class)
                .execute();
    }

}
