/*
 * Advanced ObjectPool manager.
 * Copyright (C) 2004  Nicholas Clarke
 *
 */

/*
 * History:
 *
 * 24-Jan-2004 : Initial implementation.
 *
 */

package com.antiaction.multithreading.objectpool;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Advanced ObjectPool manager.
 *
 * @version 1.00
 * @author Nicholas Clarke <mayhem[at]antiaction[dot]com>
 */
public class ObjectPool implements IObjectPool, Runnable {

	/** Object Mold. */
	private IPoolObject objectMold;

	/**  Objects checked out. */
	private int inUse = 0;

	/** Objects that are not in use. */
	private int unUsed = 0;

	/** Unused objects collection. */
	private List objects = new ArrayList();

	/* Javadoc Inherited. */
	public boolean init(HashMap props) {
		Thread t = new Thread( this );
		t.start();
		return true;
	}

	public void setObjectMold(IPoolObject objectMold) {
		this.objectMold = objectMold;
	}

	/* Javadoc Inherited. */
	public synchronized IPoolObject checkOut() {
		IPoolObject obj = null;
		if ( !objects.isEmpty() ) {
			--unUsed;
			++inUse;
			obj = (IPoolObject)objects.remove( objects.size() - 1 );
		}
		else {
			++inUse;
			obj = (IPoolObject)objectMold.clone();
		}
		return obj;
	}

	/* Javadoc Inherited. */
	public synchronized void checkIn(IPoolObject obj) {
		objects.add( obj );
		--inUse;
		++unUsed;
	}

	// TODO thread
	public void run() {
		while ( true ) {
			try {
				Thread.sleep( 1000 );
			}
			catch (InterruptedException e) {
			}

			// debug
			//System.out.println( inUse + " " + unUsed );
		}
	}

}
