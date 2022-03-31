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
import java.time.Clock;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassTransformer implements ClassFileTransformer
{
    private final AgentArguments agentArguments;
    private final ClassPool classPool;
    private final Instrumentation instrumentation;
    private final AtomicInteger replacedClasses = new AtomicInteger();

    public ClassTransformer( AgentArguments agentArguments, ClassPool classPool, Instrumentation instrumentation )
    {
        this.agentArguments = agentArguments;
        this.classPool = classPool;
        this.instrumentation = instrumentation;
        instrumentation.addTransformer( this, true );
    }

    @Override
    public byte[] transform( ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                             byte[] classfileBuffer ) throws IllegalClassFormatException
    {
        if ( !agentArguments.canReplaceMoreClasses( replacedClasses.get() ) )
        {
            instrumentation.removeTransformer( this );
        }
        try
        {
            if ( agentArguments.matchPathSelector( className ) )
            {
                final String classPath = className.replace( "/", "." );
                final var transformedClass = replaceClass( className, classPath, classPool );
                replacedClasses.incrementAndGet();
                return transformedClass;
            }
            else
            {
                print( className, agentArguments );
                return ClassFileTransformer.super.transform( loader, className, classBeingRedefined, protectionDomain, classfileBuffer );
            }
        }
        catch ( Throwable ex )
        {
            print( "error " + ex, agentArguments );
            return ClassFileTransformer.super.transform( loader, className, classBeingRedefined, protectionDomain, classfileBuffer );
        }
    }

    private byte[] replaceClass( String classPath, String packageClassPath, ClassPool classPool )
            throws NotFoundException, IOException, CannotCompileException
    {
        print( "Tried to replace class " + classPath, agentArguments );
        CtClass cc = classPool.get( packageClassPath );
        print( "Successfully replaced class " + packageClassPath, agentArguments );
        var result = cc.toBytecode();
        cc.detach();
        return result;
    }

    private void print( String className, AgentArguments agentArguments )
    {

        try ( OutputStreamWriter writer = agentArguments.getLogFilePath().map( this::init )
                                                        .orElseGet( () -> new OutputStreamWriter( System.out ) ) )
        {
            writer.write( Clock.systemUTC().instant() + ":" + className + "\n" );
            writer.flush();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    private OutputStreamWriter init( Path path )
    {
        try
        {
            return new FileWriter( path.toFile(), true );
        }
        catch ( IOException e )
        {
            System.out.println( "Error to create file writer" + e.getMessage() );
            throw new RuntimeException( e );
        }
    }
}
