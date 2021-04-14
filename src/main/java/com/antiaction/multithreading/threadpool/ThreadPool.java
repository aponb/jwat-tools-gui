/*
 * Advanced ThreadPool manager.
 * Copyright (C) 2001, 2002, 2003, 2010  Nicholas Clarke
 *
 */

/*
 * History:
 *
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
 * 15-Dec-2002 : stop().
 * 16-Dec-2002 : Idle management.
 * 17-Dec-2002 : Idle management.
 * 18-Dec-2002 : Idle management debugging.
 * 21-Jun-2003 : Moved to antiaction.com package.
 * 10-Jul-2003 : Added properties to init().
 * 19-Nov-2003 : Moved ThreadPool code to separate class.
 * 23-Nov-2003 : IWorker interface renamed to IThreadWorker.
 * 23-Nov-2003 : Modified against new interfaces.
 * 30-Aug-2010 : Separated monitoring thread from pool.
 * 31-Aug-2010 : Renamed several variables and refactored some methods.
 * 06-Sep-2010 : Added check pool for dead threads and various other enhancements.
 * 27-Jun-2011 : Refactored init and commented the variables.
 *
 */

package com.antiaction.multithreading.threadpool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.antiaction.multithreading.resourcemanage.IResourcePool;
import com.antiaction.multithreading.resourcemanage.ResourceManager;

/**
 * Advanced ThreadPool which is used together with a <code>ResourceManager</code>.
 * This class manages a pool of threads by starting/stopping and monitoring them.
 * The number of active threads at any given time is managed by the <code>ResourceManager</code>.
 *
 * @version 1.00
 * @author Nicholas Clarke <mayhem[at]antiaction[dot]com>
 */
public class ThreadPool implements IThreadPool, IResourcePool {

	/*
	 * Thread.
	 */

	/** Shutdown boolean. */
	protected boolean exit = false;

	/** Has thread been started. */
	protected boolean running = false;

	/** Resource manager monitoring instance. */
	protected ResourceManager resourceManager;

	/** Resource manager monitoring thread. */
	protected Thread resourceManagerThread;

	/*
	 * Threadpool configuration.
	 */

	/** Original thread, used for cloning. */
	protected IThreadWorker threadWorker;

	/** Minimum resources allocated. */
	protected int min = 1;

	/** Minimum idle resources allocated. */
	protected int minIdle = 1;

	/** Maximum resources allocated. */
	protected int max = 1;

	/*
	 * Internal state.
	 */

	/** ThreadPool object. */
	protected IThreadPool threadPool;

	/** ThreadGroup. */
	protected ThreadGroup threadGroup;

	/** Total amount of thread allocated. */
	protected int allocated_threads = 0;

	/** Idle thread. */
	protected int idle_threads = 0;

	/** Threads to stop. */
	protected int overflowing_threads = 0;

	/** Worker Threads. */
	protected int registered_threads = 0;

	/** Working Threads. */
	protected int busy_threads = 0;

	protected List threadList;

	protected Set idleSet;

	protected Set busySet;

	public ThreadPool() {
		resourceManager = new ResourceManager( this );
		threadGroup = new ThreadGroup( "ThreadPool" );
		threadPool = this;
		threadList = new ArrayList();
		idleSet = new HashSet();
		busySet = new HashSet();
	}

	/* Javadoc Inherited. */
	public boolean init(Map props) {
		min = getMapInt( props, "min", 1 );
		minIdle = getMapInt( props, "min-idle", getMapInt( props, "threshold", 1 ) );
		max = getMapInt( props, "max", 1 );

		return true;
	}

	private static int getMapInt(Map map, String name, int defaultValue) {
		int value = defaultValue;
		if ( map != null && name != null && name.length() > 0 && map.containsKey( name ) ) {
			try {
				value = Integer.parseInt( (String)map.get( name ) );
			}
			catch (NumberFormatException e) {
			}
		}
		return value;
	}

	public boolean start() {
		if ( threadWorker == null ) {
			throw new IllegalStateException( "Missing worker!" );
		}
		if ( !running ) {
			resourceManager.setMin( min );
			resourceManager.setThreshold( minIdle );
			resourceManager.setMax( max );

			resourceManagerThread = new Thread( threadGroup, resourceManager );
			resourceManagerThread.start();

			running = true;

			System.out.println( "ThreadPool initialized." );
			System.out.println( " min: " + resourceManager.getMin() + " - threshold: " + resourceManager.getThreshold() + " - max: " + resourceManager.getMax() );
		}

		return running;
	}

	/* Javadoc Inherited. */
	public void setThreadMold(IThreadWorker _threadWorker) {
		threadWorker = _threadWorker;
		threadWorker.setThreadPool( this );
	}

	/* Javadoc Inherited. */
	public void register() {
		synchronized ( this ) {
			if ( busySet.add( Thread.currentThread() ) ) {
				++registered_threads;
				++busy_threads;
			}
		}
	}

	/* Javadoc Inherited. */
	public void unregister() {
		synchronized ( this ) {
			if ( busySet.remove( Thread.currentThread() ) ) {
				--registered_threads;
				--busy_threads;
			}
		}
	}

	/* Javadoc Inherited. */
	public void checkIn() {
		synchronized ( this ) {
			if ( busySet.remove( Thread.currentThread() ) ) {
				--busy_threads;
			}
			if ( idleSet.add( Thread.currentThread() ) ) {
				++idle_threads;
			}
			resourceManager.update( allocated_threads, idle_threads );
		}
	}

	/* Javadoc Inherited. */
	public void checkOut() {
		synchronized ( this ) {
			if ( busySet.add( Thread.currentThread() ) ) {
				++busy_threads;
			}
			if ( idleSet.remove( Thread.currentThread() ) ) {
				--idle_threads;
			}
			resourceManager.update( allocated_threads, idle_threads );
		}
	}

	/* Javadoc Inherited. */
	public boolean stop() {
		synchronized ( this ) {
			if ( overflowing_threads <= 0 ) {
				return false;
			}
			else {
				--overflowing_threads;
				return true;
			}
		}
	}

	/**
	 * Start a given number of threads.
	 * @param n number of threads to start
	 */
	public void allocate(int n) {
		IThreadWorker w;
		Thread t;
		for( int i=0; i<n; ++i ) {
			w = (IThreadWorker)threadWorker.clone();
			w.setThreadPool( threadPool );
			t = new Thread( threadGroup, w );
			//t.setPriority( 1 );
			t.start();

			synchronized ( this ) {
				threadList.add( t );
				++allocated_threads;
				resourceManager.update( allocated_threads, idle_threads );
			}

			//workerList.add( new WorkerEntry( w, t ) );

			// debug
			//System.out.println( "Worker spawned." );
		}
	}

	/**
	 * Set the number of threads that should be stopped when convenient.
	 * @param n number of threads that should be stopped when convenient
	 */
	public void release(int n) {
		synchronized ( this ) {
			overflowing_threads += n;
			allocated_threads -= n;
			resourceManager.update( allocated_threads, idle_threads );
		}
	}

	/** Work list for alive check. */
	protected List checkList = new ArrayList();

	/**
	 * Perform Thread liveness check.
	 */
	public void check_pool() {
		Thread t;
		synchronized ( this ) {
			checkList.addAll( threadList );
		}
		int i = 0;
		while ( i<checkList.size() ) {
			t = (Thread)checkList.get( i );
			if ( !t.isAlive() ) {
				synchronized ( this ) {
					if ( busySet.remove( t ) ) {
						--allocated_threads;
						--registered_threads;
						--busy_threads;
					}
					else if ( idleSet.remove( t ) ) {
						--allocated_threads;
						--registered_threads;
						--idle_threads;
					}
					threadList.remove( t );
				}

				// debug
				System.out.println( "Dead thread: " + t );
			}
			++i;
		}
		checkList.clear();
		resourceManager.update( allocated_threads, idle_threads );
	}

	/*
	class WorkerEntry {

		public final IThreadWorker worker;
		public final Thread thread;
		public final long initTime;

		public WorkerEntry(IThreadWorker _worker, Thread _thread) {
			worker = _worker;
			thread = _thread;
			initTime = System.currentTimeMillis();
		}

	}
	*/

}
