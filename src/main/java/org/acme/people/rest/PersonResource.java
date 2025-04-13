package org.acme.people.rest;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.acme.people.model.DataTable;
import org.acme.people.model.EyeColor;
import org.acme.people.model.Person;
import org.acme.people.utils.CuteNameGenerator;

import io.quarkus.panache.common.Parameters;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.inject.Inject;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.POST;

@Path("/person")
@ApplicationScoped
public class PersonResource {

    @Inject EventBus bus;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Person> getAll() {
        return Person.listAll();
    }

    // TODO: add basic queries

    @GET
    @Path("/eyes/{color}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Person> findByColor(@PathParam(value = "color") EyeColor color) {
        return Person.findByColor(color);
    }

    @GET
    @Path("/birth/before/{year}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Person> getBeforeYear(@PathParam(value = "year") int year) {
        return Person.getBeforeYear(year);
    }


    // TODO: add datatable query

    @GET
    @Path("/datatable")
    @Produces(MediaType.APPLICATION_JSON)
    public DataTable datatable(
        @QueryParam(value = "draw") int draw,
        @QueryParam(value = "start") int start,
        @QueryParam(value = "length") int length,
        @QueryParam(value = "search[value]") String searchVal

        ) {
            // TODO: Begin result
            DataTable result = new DataTable();
            result.setDraw(draw); 


            // TODO: Filter based on 
            
            PanacheQuery<Person> filteredPeople;

            if (searchVal != null && !searchVal.isEmpty()) { 
                filteredPeople = Person.<Person>find("name like :search",
                    Parameters.with("search", "%" + searchVal + "%"));
            } else {
                filteredPeople = Person.findAll();
            }

            // TODO: Page and return

            int page_number = start / length;
            filteredPeople.page(page_number, length);

            result.setRecordsFiltered(filteredPeople.count());
            result.setData(filteredPeople.list());
            result.setRecordsTotal(Person.count());

            return result;

    }

    @POST
    @Path("/{name}")
    public Uni<Person> addPerson(String name) {
          return bus.<Person>request("add-person", name)
                .onItem().transform(response -> response.body());
    }

    @GET
    @Path("/name/{name}")
    public Person byName(String name) {
        return Person.find("name", name).firstResult();
    }


    // TODO: Add lifecycle hook
    @Transactional
    void onStart(@Observes StartupEvent ev) {
        for (int i = 0; i < 1000; i++) {
            String name = CuteNameGenerator.generate();
            LocalDate birth = LocalDate.now().plusWeeks(Math.round(Math.floor(Math.random() * 40 * 52 * -1)));
            EyeColor color = EyeColor.values()[(int)(Math.floor(Math.random() * EyeColor.values().length))];
            Person p = new Person();
            p.birth = birth;
            p.eyes = color;
            p.name = name;
            Person.persist(p);
        }
    }
}