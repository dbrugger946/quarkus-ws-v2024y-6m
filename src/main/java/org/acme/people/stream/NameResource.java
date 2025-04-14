package org.acme.people.stream;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.reactivestreams.Publisher;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * A simple resource retrieving the in-memory "my-data-stream" and sending the items as server-sent events.
 */
@Path("/names")
public class NameResource {

    @Inject
    @Channel("my-data-stream") Publisher<String> names; 

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Publisher<String> stream() { 
        return names;
    }
}