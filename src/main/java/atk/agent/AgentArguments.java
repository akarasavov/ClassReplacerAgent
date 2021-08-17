package atk.agent;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class AgentArguments
{
    private final String classNameSelector;
    private final Path overrideJar;
    private final Integer classCount;
    private final Optional<Path> logFilePath;

    AgentArguments( String classNameSelector, String overrideJarPath, String classCount, Optional<Path> logsDir, Instant now )
    {
        this.classNameSelector = validateNotNull( classNameSelector, Parameters.CLASS_NAME_SELECTOR.value + " is not set" );
        this.overrideJar = Path.of( validateNotNull( overrideJarPath, Parameters.OVERRIDE_JAR.value + " is not set" ) );
        this.classCount = Integer.valueOf( validateNotNull( classCount, Parameters.CLASS_COUNT.value + " is not set" ) );
        this.logFilePath = logsDir.map( dir -> dir.resolve( "agent-" + now + ".log" ) );
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

    public Optional<Path> getLogFilePath()
    {
        return logFilePath;
    }

    public boolean canReplaceMoreClasses( int replacedClasses )
    {
        return classCount > replacedClasses;
    }

    private enum Parameters
    {
        CLASS_NAME_SELECTOR( "classNameSelector" ),
        OVERRIDE_JAR( "overrideJar" ),
        CLASS_COUNT( "classCount"),
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

    public static AgentArguments from( String args, Instant now )
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
                                   parameterValueMap.get( Parameters.CLASS_COUNT ),
                                   Optional.ofNullable( parameterValueMap.get( Parameters.LOGS_DIR ) ).map( Path::of ), now );
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
        return Objects.equals( classNameSelector, that.classNameSelector ) && Objects.equals( overrideJar, that.overrideJar ) &&
               Objects.equals( logFilePath, that.logFilePath );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( classNameSelector, overrideJar, logFilePath );
    }
}
