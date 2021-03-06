package com.github.ness.check;

import com.github.ness.NessPlayer;

/**
 * Instantiator of new checks
 * 
 * @author A248
 *
 * @param <C> the check type
 */
@FunctionalInterface
public interface CheckInstantiator<C extends Check> {

	/**
	 * Creates a new check with the associated factory and ness player. Most implementations
	 * will simply pass these arguments to a constructor.
	 * 
	 * @param factory the factory
	 * @param nessPlayer the ness player
	 * @return the check
	 */
	C newCheck(CheckFactory<C> factory, NessPlayer nessPlayer);
	
}
