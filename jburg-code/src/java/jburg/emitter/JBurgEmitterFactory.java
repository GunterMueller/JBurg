package jburg.emitter;

import java.util.List;

import jburg.burg.JBurgUtilities;
import jburg.burg.Logger;

/**
 *  The JBurgEmitterFactory searches for an emitter that can
 *  emit the specified language using i-nodes of the given class.
 *  @author Nick Brereton -- original implementation.
 *  @author Tom Harwood -- maintenance, split out of JBurgGenerator into a factory.
 *  
 */
@SuppressWarnings("rawtypes")
public class JBurgEmitterFactory
{
	/**
	 * Find a suitable EmitLang interface class to deal with the language we want to emit
	 * @param language the target language.
	 * @param iNodeClass the intput i-node class.
	 * @return vector of classes
	 */
	public static EmitLang getEmitter( String langname, Logger log )
	{
        if(langname==null || langname.length()==0)
        {
			log.warning("No Language specified, assuming Language Java.\n");
			return new EmitJava();
		}

		List<Class> candidates = JBurgUtilities.getInterfaceImpls(EmitLang.class);

		for(Class cl: candidates) {

			try {
				//  Skip emitters that cannot be instantiated; these include
				//  abstract superclasses and the BURG's own emitter for 
				//  its pattern-matching rules, which needs special setup
				//  and would malfunction if it were ever selected this way.
				if ( isEmitterSuperclass(cl) ) {
					continue;
				}
				
				try {
					EmitLang el = (EmitLang) JBurgEmitterFactory.class.getClassLoader().loadClass(cl.getName()).newInstance();

					if ( el.accept(langname) ) {
						log.info("Using Language Adapter %s\n", el.getClass().getName());
						return el;
					}
				} catch(InstantiationException e) {
					log.warning("Unable to instantiate possible language class: " + e.getMessage());
				} catch(IllegalAccessException e) {
					log.warning("IllegalAccessException: " + e.getMessage());
				}
			}  catch(ClassNotFoundException e) {
				log.warning("Class does not appear to exist in CLASSPATH: " + e.getMessage());
			}
		}
		
		return null;
	}

	private static boolean isEmitterSuperclass(Class cl)
	{
		return 
			cl.getName().equals("jburg.emitter.DelegatingEmitter")
			||
			cl.getName().equals("jburg.emitter.EmitINodeASTTargetJava")
			;
	}
}
