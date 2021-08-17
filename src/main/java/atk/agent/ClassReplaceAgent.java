package atk.agent;

import javassist.ClassPool;
import javassist.NotFoundException;

import java.lang.instrument.Instrumentation;
import java.time.Clock;

public class ClassReplaceAgent
{
    public static void premain( String agentArgs, Instrumentation inst )
    {
        AgentArguments agentArguments;
        ClassPool classPool;
        try
        {
            agentArguments = AgentArguments.from( agentArgs, Clock.systemUTC().instant() );
            classPool = createClassPool( agentArguments );
        }
        catch ( Throwable t )
        {
            System.out.println( "Failed to register agent " + t );
            return;
        }

        new ClassTransformer( agentArguments, classPool, inst );
    }

    private static ClassPool createClassPool( AgentArguments args ) throws NotFoundException
    {
        final ClassPool pool = new ClassPool( ClassPool.getDefault() );
        pool.appendClassPath( args.getOverrideJar().toString() );
        pool.childFirstLookup = true;
        return pool;
    }
}
