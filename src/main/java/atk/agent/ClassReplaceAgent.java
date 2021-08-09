package atk.agent;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.UUID;

public class ClassReplaceAgent
{
    private final static String fileName = "agent-" + UUID.randomUUID() + ".txt";

    public static void premain( String agentArgs, Instrumentation inst )
    {
        AgentArguments agentArguments;
        ClassPool classPool;
        try
        {
            agentArguments = AgentArguments.from( agentArgs );
            classPool = createClassPool( agentArguments );
        }
        catch ( Throwable t )
        {
            System.out.println( "Failed to register agent " + t );
            return;
        }

        inst.addTransformer( new ClassFileTransformer()
        {

            @Override
            public byte[] transform( ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                     byte[] classfileBuffer ) throws IllegalClassFormatException
            {
                try
                {
                    if ( agentArguments.matchPathSelector( className ) )
                    {
                        final String classPath = className.replace( "/", "." );
                        return replaceClass( className, classPath, classPool );
                    }
                }
                catch ( Throwable ex )
                {
                    print( "error " + ex, agentArguments );
                    return ClassFileTransformer.super.transform( loader, className, classBeingRedefined, protectionDomain, classfileBuffer );
                }

                print( className, agentArguments );
                return ClassFileTransformer.super.transform( loader, className, classBeingRedefined, protectionDomain, classfileBuffer );
            }

            private byte[] replaceClass( String classPath, String packageClassPath, ClassPool classPool )
                    throws NotFoundException, IOException, CannotCompileException
            {
                print( "Tried to replace class " + classPath, agentArguments );
                CtClass cc = classPool.get( packageClassPath );
                print( "Successfully replaced class " + packageClassPath, agentArguments );
                return cc.toBytecode();
            }
        } );
    }

    private static ClassPool createClassPool( AgentArguments args ) throws NotFoundException
    {
        final ClassPool pool = new ClassPool( ClassPool.getDefault() );
        pool.appendClassPath( args.getOverrideJar().toString() );
        pool.childFirstLookup = true;
        return pool;
    }

    private static void print( String className, AgentArguments agentArguments )
    {
        OutputStreamWriter writer = agentArguments.getLogsDir().map( ClassReplaceAgent::init )
                                                  .orElseGet( () -> new OutputStreamWriter( System.out ) );
        try
        {
            writer.write( className + "\n" );
            writer.flush();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    private static OutputStreamWriter init( Path path )
    {
        try
        {
            return new FileWriter( path.resolve( fileName ).toFile(), true );
        }
        catch ( IOException e )
        {
            System.out.println( "Error to create file writer" + e.getMessage() );
            throw new RuntimeException( e );
        }
    }
}
