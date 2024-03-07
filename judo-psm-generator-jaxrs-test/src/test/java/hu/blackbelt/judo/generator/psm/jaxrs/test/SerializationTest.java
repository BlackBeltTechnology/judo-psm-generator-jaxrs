package hu.blackbelt.judo.generator.psm.jaxrs.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import hu.blackbelt.judo.psm.generator.jaxrs.test.api.actiongrouptest.CreatureRequest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SerializationTest {

    @Test
    public void testNullValueSerialization()
            throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        CreatureRequest creature = new CreatureRequest();

        String creatureAsString = mapper.writeValueAsString(creature);
        assertThat(creatureAsString, equalTo("{}"));

        creature.setName(Optional.empty());
        creatureAsString = mapper.writeValueAsString(creature);
        assertThat(creatureAsString, equalTo("{\"name\":null}"));

        creature.setName(Optional.of("Test"));
        creatureAsString = mapper.writeValueAsString(creature);
        assertThat(creatureAsString, equalTo("{\"name\":\"Test\"}"));
    }

    @Test
    public void testNullValueDeserialization()
            throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());

        CreatureRequest creature = mapper.readValue("{}", CreatureRequest.class);
        assertThat(creature.getName(), nullValue());

        creature = mapper.readValue("{\"name\":null}", CreatureRequest.class);
        assertThat(creature.getName(), equalTo(Optional.empty()));

        creature = mapper.readValue("{\"name\":\"Test\"}", CreatureRequest.class);
        assertThat(creature.getName(), equalTo(Optional.of("Test")));

    }

}
