package jburg.burg;

import java.lang.reflect.Method;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import java.util.jar.JarFile;

import antlr.collections.AST;
import static jburg.parser.JBurgTokenTypes.*;

@SuppressWarnings("rawtypes")
public class JBurgUtilities
{
    /**
     *   Traverse the given collection (and any subcollections) 
     *   and apply the specified method to all elements.
     *   This applyToAll variant assumes that the method takes an Object parameter.
     */
	public static void applyToAll(
		Object actor,
	    java.util.AbstractCollection collection,
        String strMethodName) throws Exception
    {
        //  Assume the method has an Object parameter.
        applyToAll(actor, collection, strMethodName, Object.class);
    }

    /**
     *   Traverse the given collection (and any subcollections) 
	 *   and apply the specified method to all elements.
     *   @param strMethodName the method to apply.
     *   @param cParamType the class of the method's one parameter.
     */
    public static void applyToAll( Object actor, java.util.AbstractCollection collection,
        String strMethodName, Class cParmType) throws Exception
    {
        Method m = actor.getClass().getDeclaredMethod(strMethodName, new Class[] {cParmType });
        applyToAll(actor, collection, m);
    }

    /**
     *  Traverse the given collection (and any subcollections)
	 *  and apply the specified method to all elements.
     */
    private static void applyToAll( Object actor, java.util.AbstractCollection collection, Method m)
        throws Exception
    {
        for ( Object o: collection ) {
            if (o instanceof java.util.AbstractCollection) {
                applyToAll( actor, (java.util.AbstractCollection) o, m);
            } else {
                Object[] arglist = new Object[1];
                arglist[0] = o;
                m.invoke(actor, arglist);
            }
        }
    }

	/**
	 *   @return all implementations of the given interface class that
	 *     are in the same URI.
	 */
	public static List<Class> getInterfaceImpls( Class interfaceClass )
	{
		List<Class> candidates = Collections.<Class>emptyList();

		//  Starting point for the search is the EmitLang class' location.
		URL location = interfaceClass.getProtectionDomain().getCodeSource().getLocation();

		try {
			if(location.toString().endsWith(".jar")) {
				candidates = searchJar(new URI(location.toString()), interfaceClass);
			} else {
				//  A filesystem-based build, search the directory.
				URI iLoc = new URI(
					location.toString() +
					interfaceClass.getPackage().getName().replace('.','/') + '/'
				);
				candidates = searchPath(iLoc, interfaceClass);
			}
		} catch(URISyntaxException e) {
			System.err.println("Unable to load interface implementations of " + interfaceClass.getName() );
			System.err.println(e);
		}

		return candidates;
	}
	
	/**
	 *  Search a URI for candidate classes that implement an interface.
	 *  @param location -- the URI to search, usually a directory.
	 *  @param iface -- the interface class that successful candidates implement.
	 *  @return a List of Class objects that implement the interface.
	 */
	private static List<Class> searchPath(URI location, Class iface) {
		File floc;
		List<Class> retv = new ArrayList<Class>();
		
		floc = new File( location );
		
		if(!floc.isDirectory())
			return retv;
		
		String[] fls = floc.list();
		for(int i=0; i<fls.length; ++i) {
			if(fls[i].endsWith(".class") && fls[i].indexOf('$') < 0) {
				try {
					Class cl = Class.forName(iface.getPackage().getName()+"."
											 +fls[i].substring(0, fls[i].length()-6));
					Class[] ifs = cl.getInterfaces();
					boolean bIfOk = false;
					for(int j=0; j<ifs.length; ++j)
						if(ifs[j].equals(iface))
							bIfOk = true;
					if(bIfOk && !cl.isInterface())
						retv.add(cl);
				} catch(ClassNotFoundException e) {};
			}
		}
		return retv;
	}
	
	/**
	 *  Search a jarfile for candidate classes that implement an interface.
	 *  @param location -- the jarfile to search.
	 *  @param iface -- the interface class that successful candidates implement.
	 */
	private static List<Class> searchJar(URI location, Class iface)
	{
		JarFile jf;
		List<Class> retv = new ArrayList<Class>();
		
		try {
			jf = new JarFile( new File( location ) );
		} catch(IOException e) { System.out.println(e); return retv; }
		
		String pkg = iface.getPackage().getName().replace('.','/');
		
		for(Enumeration walker=jf.entries(); walker.hasMoreElements(); ) {
			// check each name for being a ".class", no "$" & starting with the iface's package name
			String cname = walker.nextElement().toString();
			if(cname.startsWith(pkg) && cname.endsWith(".class") && cname.indexOf('$')<0) {
				try {
					Class cl = Class.forName(cname.substring(0,cname.length()-6).replace('/','.'));
					Class[] ifs = cl.getInterfaces();
					boolean bIfOk = false;
					for(int j=0; j<ifs.length; ++j)
						if(ifs[j].equals(iface))
							bIfOk = true;
					if(bIfOk && !cl.isInterface())
						retv.add(cl);
				} catch(ClassNotFoundException e) {
					System.err.println("Couldn't resolve class : "+cname.substring(0,cname.length()-6).replace('/','.'));
					System.err.println(e);
				}
			}
		}
		return retv;		
	}

	/**
	 *  @return the text of the parent AST's code block, which is represented as a BLOCK token.
	 */
	public static String getCodeBlock( AST parent )
	{
		return getASTByType(parent, BLOCK).getText();
	}

    /**
     *  @return the first child of the parent AST with the specified node type.
     */
    public static AST getASTByType(AST parent, int node_type)
    {
        for ( AST current = parent.getFirstChild(); current != null; current = current.getNextSibling() )
        {
            if ( current.getType() == node_type )
                return current;
        }

        throw new IllegalStateException ( "AST " + parent.toStringTree() + " has no child of node type " + node_type + "." );
    }

    /**
     *  @return true if the parent AST has a child of the specified node type.
     */
    public static boolean hasASTOfType(AST parent, int node_type)
    {
        if ( parent == null )
            return false;
        for ( AST current = parent.getFirstChild(); current != null; current = current.getNextSibling() )
        {
            if ( current.getType() == node_type )
                return true;
        }

        return false;
    }

    /**
     *  Get the payload of an identifier, with error checking.
     *  @return the identifier's text.
     */
    public static String getIdentifierText(AST p)
    {
		if ( p.getType() != IDENTIFIER ) {
			throw new IllegalStateException ( "Expected IDENTIFIER, found " + p.toStringTree() );
		}

        return p.getText();
    }
    
}
