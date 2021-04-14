/*
 * ThreadPool Interface.
 * Copyright (C) 2003  Nicholas Clarke
 *
 */

/*
 * History:
 *
 * 15-Feb-2002 : checkIn()/checkOut().
 * 16-Feb-2002 : attach()/detach().
 * 15-Dec-2002 : stop().
 * 21-Jun-2003 : Moved to antiaction.com package.
 * 19-Nov-2003 : Moved ThreadPool code/javadoc to separate interface.
 * 23-Nov-2003 : Altered methods and modified Javadoc.
 *
 */

package com.antiaction.multithreading.threadpool;

import java.util.Map;

/**
 * ThreadPool Interface.
 *
 * @version 1.00
 * @author Nicholas Clarke <mayhem[at]antiaction[dot]com>
 */
public interface IThreadPool {

	/**
	 * Initialize object with specified properties.
	 * @param props init properties.
	 */
	public boolean init(Map props);

	/**
	 * Sets the <CODE>Thread</CODE> mold for this ThreadPool, which is used either
	 * directly or for cloning. The worker object must be initialized and the
	 * clone implementation must re-init the clone to an identical state.
	 * @param _threadWorker Set the <CODE>Thread</CODE> mold for this broker.
	 */
	public void setThreadMold(IThreadWorker _threadWorker);

	/**
	 * Activate the broker.
	 */
	public boolean start();

	/**
	 * Notify thread pool that a new thread is running.
	 * @see #unregister()
	 */
	public void register();

	/**
	 * Notify thread pool that a thread has stopped.
	 * @see #register()
	 */
	public void unregister();

	/**
	 * Notify thread pool that a thread has gone inactive.
	 * @see #checkOut()
	 */
	public void checkIn();

	/**
	 * Notify thread bool that a thread has gone active.
	 * @see #checkIn()
	 */
	public void checkOut();

	/**
	 * Callback to let threads know they should shutdown. (random victims)
	 */
	public boolean stop();

}
