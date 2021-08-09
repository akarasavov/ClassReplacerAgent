package atk.agent;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AgentArgumentsTest
{

    @Test
    void shouldParseCorrectlyInputString()
    {
        final var arguments = AgentArguments.from( "classNameSelector=/a/b/c,overrideJar=/override/path,logsDir=/logs" );
        final var expected = new AgentArguments( "/a/b/c", "/override/path", Optional.of( Path.of( "/logs" ) ) );
        assertEquals( expected, arguments );
    }

    @Test
    void shouldFailIfMandatoryParameterIsMissing()
    {
        assertThrows( IllegalArgumentException.class, () -> AgentArguments.from( "classNameSelector=/a/b/c,logsDir=/logs" ) );
        assertThrows( IllegalArgumentException.class, () -> AgentArguments.from( "overrideJar=/a/b/c,logsDir=/logs" ) );
        assertThrows( IllegalArgumentException.class, () -> AgentArguments.from( null ) );
        assertThrows( IllegalArgumentException.class, () -> AgentArguments.from( "" ) );
    }
}
