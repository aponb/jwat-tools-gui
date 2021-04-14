/*
 * ObjectBroker Interface.
 * Copyright (C) 2001, 2002, 2003  Nicholas Clarke
 *
 */

/*
 * History:
 *
 * 28-Jan-2002 : First implementation.
 * 15-Dec-2002 : remove(long).
 * 21-Jun-2003 : Moved to antiaction.com package.
 * 10-Jul-2003 : Added properties to init().
 * 19-Nov-2003 : Moved ThreadPool code/javadoc to separate interface.
 *
 */

package com.antiaction.multithreading.objectbroker;

import java.util.Map;

/**
 * ObjectBroker Interface.
 *
 * @version 1.00
 * @author Nicholas Clarke <mayhem[at]antiaction[dot]com>
 */
public interface IObjectBroker {

	/**
	 * Initialize object with specified properties.
	 * @param props init properties.
	 */
	public boolean init(Map props);

	/**
	 * Insert an object in the broker queue, until it is removed.
	 * @param obj an object to be stored in the broker until it is removed.
	 */
	public void insert(Object obj);

	/**
	 * Returns an object from the queue or blocks until one arrives.
	 * @return object from the queue to be processed.
	 * @see #remove(long)
	 */
	public Object remove();

	/**
	 * Returns an object from the queue or blocks until one arrives
	 * or the call is timed out.
	 * @return object from the queue to be processed.
	 * @see #remove()
	 */
	public Object remove(long timeout);

}
