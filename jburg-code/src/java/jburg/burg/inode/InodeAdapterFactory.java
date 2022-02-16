package jburg.burg.inode;

import jburg.burg.JBurgUtilities;
import jburg.burg.Logger;

import java.util.List;

public class InodeAdapterFactory
{
	@SuppressWarnings("rawtypes")
	public static InodeAdapter getAdapter(String inodeTypeName, Logger logger)
	{
		for(Class cl: JBurgUtilities.getInterfaceImpls(InodeAdapter.class)) {

				try {
					InodeAdapter el = (InodeAdapter)ClassLoader.getSystemClassLoader().loadClass(cl.getName()).newInstance();

					if(el.accept(inodeTypeName )) {
						logger.info("Using I-Node Adapter %s ", el.getClass());

						return el;
					}
				} catch(InstantiationException e) {
                    logger.debug("InstantiationException, class %s: %s", cl, e);
				} catch(IllegalAccessException e) {
                    logger.debug("IllegalAccessException, class %s: %s", cl, e);
				} catch(ClassNotFoundException e) {
                    logger.debug("ClassNotFoundException, class %s: %s", cl, e);
			}
		}
		return null;
	}
}
