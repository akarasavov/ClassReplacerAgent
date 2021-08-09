package atk.agent;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class AgentArguments
{
    private final String classNameSelector;
    private final Path overrideJar;
    private final Optional<Path> logsDir;

    AgentArguments( String classNameSelector, String overrideJarPath, Optional<Path> logsDir )
    {
        this.classNameSelector = validateNotNull( classNameSelector, Parameters.CLASS_NAME_SELECTOR.value + " is not set" );
        this.overrideJar = Path.of( validateNotNull( overrideJarPath, Parameters.OVERRIDE_JAR.value + " is not set" ) );
        this.logsDir = logsDir;
    }

    private <T> T validateNotNull( T value, String message )
    {
        if ( value == null )
        {
            throw new IllegalArgumentException( message );
        }
        return value;
    }

    public boolean matchPathSelector( String className )
    {
        return className.contains( classNameSelector );
    }

    public Path getOverrideJar()
    {
        return overrideJar;
    }

    public Optional<Path> getLogsDir()
    {
        return logsDir;
    }

    private enum Parameters
    {
        CLASS_NAME_SELECTOR( "classNameSelector" ),
        OVERRIDE_JAR( "overrideJar" ),
        LOGS_DIR( "logsDir" );

        private final String value;

        Parameters( String value )
        {
            this.value = value;
        }

        public static Optional<Parameters> getByValue( String value )
        {
            return Arrays.stream( values() ).filter( v -> v.value.equals( value ) ).findFirst();
        }
    }

    public static AgentArguments from( String args )
    {
        if ( args == null )
        {
            throw new IllegalArgumentException( "Mandatory parameters are not defined" );
        }
        Map<Parameters,String> parameterValueMap = new HashMap<>();
        for ( String token : args.trim().split( "," ) )
        {
            final String[] parameterAndValue = token.split( "=" );
            if ( parameterAndValue.length != 2 )
            {
                throw new IllegalArgumentException( "Arguments should be defined in format arg1=arg1Value,arg2=arg2Value" );
            }
            final Parameters parameter = Parameters.getByValue( parameterAndValue[0] )
                                                   .orElseThrow( () -> new IllegalArgumentException( parameterAndValue[0] + " is unknown parameter" ) );
            parameterValueMap.put( parameter, parameterAndValue[1] );
        }
        return new AgentArguments( parameterValueMap.get( Parameters.CLASS_NAME_SELECTOR ),
                                   parameterValueMap.get( Parameters.OVERRIDE_JAR ),
                                   Optional.ofNullable( parameterValueMap.get( Parameters.LOGS_DIR ) ).map( Path::of ) );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        AgentArguments that = (AgentArguments) o;
        return Objects.equals( classNameSelector, that.classNameSelector ) && Objects.equals( overrideJar, that.overrideJar );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( classNameSelector, overrideJar );
    }
}
