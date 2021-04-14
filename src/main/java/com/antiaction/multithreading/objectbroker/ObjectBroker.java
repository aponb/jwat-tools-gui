/*
 * ObjectBroker.
 * Copyright (C) 2001, 2002, 2003  Nicholas Clarke
 *
 */

/*
 * History:
 *
 * 27-Dec-2001 : First implementation.
 * 28-Dec-2001 : Renamed to Broker.java.
 *             : Renamed methods to insert and remove.
 *             : Fixed bug in remove, always returned null.
 * 26-Jan-2002 : Javadoc.
 * 28-Jan-2002 : Dispatcher use changed to IWorker making it generic.
 *               Implements IBroker interface.
 * 06-Feb-2002 : Threshold field and set method added.
 * 14-Feb-2002 : Added ThreadGroups.
 * 15-Feb-2002 : Redefined threshold.
 *             : Added synchronized checkIn/Out.
 *             : Spawner Thread implemented.
 * 16-Feb-2002 : activeCount() changed to field.
 *             : attach()/detach() methods added.
 * 12-Aug-2002 : Added spawn counter instead of waiting for attaching threads.
 * 13-Aug-2002 : Removed spawn counter and added a list of worker objects and threads.
 * 14-Dec-2002 : Added exit boolean.
 * 15-Dec-2002 : remove(long).
 * 15-Dec-2002 : stop().
 * 16-Dec-2002 : Idle management.
 * 17-Dec-2002 : Idle management.
 * 18-Dec-2002 : Idle management debugging.
 * 21-Jun-2003 : Moved to antiaction.com package.
 * 10-Jul-2003 : Added properties to init().
 * 19-Nov-2003 : Moved ThreadPool code to separate class.
 *
 */

package com.antiaction.multithreading.objectbroker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.antiaction.multithreading.concurrent.Multex;
import com.antiaction.multithreading.concurrent.Mutex;

/**
 * ObjectBroker.
 *
 * @version 1.00
 * @author Nicholas Clarke <mayhem[at]antiaction[dot]com>
 */
public class ObjectBroker implements IObjectBroker {

	/** Mutually exclusive objectList Semaphore. */
	protected Mutex taskMutex;

	/** Resource Semaphore, a resource amounts to an object in the objectList. */
	protected Multex tasksSem;

	/** List of objects for the workers. */
	protected List tasks;

	/**
	 * Instantiate an empty broker.
	 */
	public ObjectBroker() {
		taskMutex = new Mutex( 0 );
		tasksSem = new Multex( 0 );
		tasks = new ArrayList();
	}

	/* Javadoc Inherited. */
	public boolean init(Map props) {
		return true;
	}

	/* Javadoc Inherited. */
	public void insert(Object obj) {
		taskMutex.obtainSemaphore();
		tasks.add( obj );
		tasksSem.releaseSemaphore();
		taskMutex.releaseSemaphore();
	}

	/* Javadoc Inherited. */
	public Object remove() {
		Object task = null;
		tasksSem.obtainSemaphore();
		taskMutex.obtainSemaphore();
		if ( !tasks.isEmpty() ) {
			task = tasks.remove( 0 );
		}
		taskMutex.releaseSemaphore();
		return task;
	}

	/* Javadoc Inherited. */
	public Object remove(long timeout) {
		Object task = null;
		tasksSem.obtainSemaphore( timeout );
		taskMutex.obtainSemaphore();
		if ( !tasks.isEmpty() ) {
			task = tasks.remove( 0 );
		}
		taskMutex.releaseSemaphore();
		return task;
	}

}
