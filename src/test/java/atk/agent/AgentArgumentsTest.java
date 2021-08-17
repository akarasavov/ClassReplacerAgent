package atk.agent;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AgentArgumentsTest
{

    @Test
    void shouldParseCorrectlyInputString()
    {
        final var now = Instant.now();
        final var arguments = AgentArguments.from( "classNameSelector=/a/b/c,classCount=3,overrideJar=/override/path,logsDir=/logs", now );
        final var expected = new AgentArguments( "/a/b/c", "/override/path", "3", Optional.of( Path.of( "/logs" ) ), now );
        assertEquals( expected, arguments );
    }

    @Test
    void shouldFailIfMandatoryParameterIsMissing()
    {
        final var now = Instant.now();
        assertThrows( IllegalArgumentException.class, () -> AgentArguments.from( "classNameSelector=/a/b/c,overrideJar=/override/path,logsDir=/logs", now ) );
        assertThrows( IllegalArgumentException.class, () -> AgentArguments.from( "classNameSelector=/a/b/c,logsDir=/logs", now ) );
        assertThrows( IllegalArgumentException.class, () -> AgentArguments.from( "overrideJar=/a/b/c,logsDir=/logs", now ) );
        assertThrows( IllegalArgumentException.class, () -> AgentArguments.from( null, now ) );
        assertThrows( IllegalArgumentException.class, () -> AgentArguments.from( "", now ) );
    }
}
