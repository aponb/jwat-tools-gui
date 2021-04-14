/*
 * PoolObject interface.
 * Copyright (C) 2004  Nicholas Clarke
 *
 */

/*
 * History:
 *
 * 21-Jun-2004 : Initial implementation.
 *
 */

package com.antiaction.multithreading.objectpool;

/**
 * PoolObject interface.
 *
 * @version 1.00
 * @author Nicholas Clarke <mayhem[at]antiaction[dot]com>
 */
public interface IPoolObject extends Cloneable {

	public Object clone();

	public void setObjectPool(IObjectPool objectPool);

	public void checkIn();

}
