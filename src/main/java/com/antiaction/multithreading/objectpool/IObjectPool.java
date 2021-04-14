/*
 * Advanced ObjectPool manager interface.
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

/**
 * Advanced ObjectPool manager interface.
 *
 * @version 1.00
 * @author Nicholas Clarke <mayhem[at]antiaction[dot]com>
 */
public interface IObjectPool {

	/**
	 * Initialize object with specified properties.
	 * @param props init properties.
	 */
	public boolean init(HashMap props);

	public void setObjectMold(IPoolObject objectMold);

	public IPoolObject checkOut();

	public void checkIn(IPoolObject obj);

}
