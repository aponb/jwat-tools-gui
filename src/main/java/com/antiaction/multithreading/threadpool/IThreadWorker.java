/*
 * Worker Interface.
 * Copyright (C) 2001, 2002  Nicholas Clarke
 *
 */

/*
 * History:
 *
 * 28-Dec-2001 : First implementation.
 * 29-Dec-2001 : Javadoc.
 * 28-Jan-2002 : Renamed interface to IWorker making it generic.
 * 14-Dec-2002 : Added setExit().
 * 21-Jun-2003 : Moved to antiaction.com package.
 * 21-Nov-2003 : Split from IWorker interface.
 * 23-Nov-2003 : Method cleanup and Javadoc.
 *
 */

package com.antiaction.multithreading.threadpool;

/**
 * Worker Interface.
 *
 * @version 1.00
 * @author Nicholas Clarke <mayhem[at]antiaction[dot]com>
 */
public interface IThreadWorker extends Cloneable, Runnable {

	/**
	 * Assign a <CODE>ThreadPool</CODE> to this thread.
	 * @param _threadPool Associated <CODE>ThreadPool</CODE> to this thread.
	 */
	public void setThreadPool(IThreadPool _threadPool);

 	/**
 	 * Overrides Cloneable.
 	 * @return a clone of this instance.
 	 * @exception OutOfMemoryError if there is not enough memory.
 	 * @see java.lang.Cloneable
 	 */
 	public Object clone();
}
