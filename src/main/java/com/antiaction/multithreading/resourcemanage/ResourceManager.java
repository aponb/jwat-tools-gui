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
 *
 */

package com.antiaction.multithreading.resourcemanage;

/**
 * Advanced resource manager.
 *
 * @version 1.00
 * @author Nicholas Clarke <mayhem[at]antiaction[dot]com>
 */
public class ResourceManager implements Runnable {

	/*
	 * Internal state.
	 */

	/** Call-back for resource allocation and release. */
	protected IResourcePool resourcePool;

	/** Shutdown boolean. */
	protected boolean exit = false;

	/** Has thread been started. */
	protected boolean running = false;

	/** Array of idle objects in a linked list sorted by index=idlecountindex. */
	protected IdleObject[] idleObjects;

	/** First idle object in linked list. */
	protected IdleObject rootIdleObj = null;

	/*
	 * Settings.
	 */

	/** Minimum resources. */
	protected int min = 1;

	/** Idle threshold - minimum idle resources before more are spawned but below max. */
	protected int threshold = 1;

	/** Maximum resources. */
	protected int max = 1;

	/** Initial sample second interval. */
	protected int idleSampleSecsInit = 60 * 1;

	/** Sample second interval. */
	protected int idleSampleSecs = 60;

	/** Last modified idle threshold count timeout after which resources can be freed and still be within the idle threshold. */
	protected int lastTimeIdleTimeoutMillis = 1000 * 60 * 30;

	/*
	 * Resource pool state.
	 */

	/** Total amount of resources allocated. */
	protected int allocated = 0;

	/** Idle resources. */
	protected int idle = 0;

	/** Minimum idle number compared/set each time a worker goes busy. */
	protected int minIdle = 0;

	/**
	 * Construct a resource manager for the designated resource pool.
	 * @param resourcePool resource pool to manage.
	 */
	public ResourceManager(IResourcePool resourcePool) {
		this.resourcePool = resourcePool;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getIdleSampleSecsInit() {
		return idleSampleSecsInit;
	}

	public void setIdleSampleSecsInit(int idleSampleSecsInit) {
		this.idleSampleSecsInit = idleSampleSecsInit;
	}

	public int getIdleSampleSecs() {
		return idleSampleSecs;
	}

	public void setIdleSampleSecs(int idleSampleSecs) {
		this.idleSampleSecs = idleSampleSecs;
	}

	public int getLastTimeIdleTimeoutMillis() {
		return lastTimeIdleTimeoutMillis;
	}

	public void setLastTimeIdleTimeoutMillis(int lastModifiedTimeout) {
		this.lastTimeIdleTimeoutMillis = lastModifiedTimeout;
	}

	public void update(int allocated, int idle) {
		synchronized ( this ) {
			this.allocated = allocated;
			this.idle = idle;
			if ( idle < minIdle ) {
				minIdle = idle;
			}
		}
	}

	public void stop() {
		if ( running ) {
			exit = true;
		}
	}

	public void run() {
		running = true;

		int n;

		long ctm = 0;

		int sampleSeconds = idleSampleSecsInit;
		int localMinIdle = 0;

		/*
		 * Idle monitoring objects.
		 */

		idleObjects = new IdleObject[ max + 1 ];
		IdleObject prevIdleObj = null;
		IdleObject idleObj = null;
		IdleObject nextIdleObj = null;

		for ( int i=0; i<=max; ++i ) {
			idleObj = new IdleObject( i );
			idleObjects[ i ] = idleObj;
			if ( i > 0 ) {
				prevIdleObj.next = idleObj;
				idleObj.prev = prevIdleObj;
			}
			prevIdleObj = idleObj;
		}

		rootIdleObj = idleObjects[ 0 ];

		/*
		 * Monitor loop.
		 */

		while ( running ) {
			// debug
			//System.out.println( workerThreads + " - " + workingThreads + " - " + idleThreads );
			//System.out.println( workerList.size() );

			resourcePool.check_pool();

			if ( !exit ) {
				if ( ( allocated < max ) && ( idle < threshold ) ) {
					/*
					 * Allocate.
					 */
					n = threshold - idle;
					if ( ( allocated + n > max ) ) {
						n = max - allocated;
					}
					resourcePool.allocate( n );
				}
				else {
					/*
					 * New min idle.
					 */
					if ( sampleSeconds < 0 ) {
						sampleSeconds = idleSampleSecs;
						synchronized( this ) {
							localMinIdle = minIdle;
							minIdle = idle;
						}
						// debug
						//System.out.println( workers + " - " + localMinIdle);

						/*
						 * Last modified.
						 */

						ctm = System.currentTimeMillis();
						idleObjects[ localMinIdle ].lastTimeIdleMillis = ctm;

						/*
						 * Remove old idleObjects.
						 */
						idleObj = rootIdleObj;

						while ( idleObj != null ) {
							if ( ( ctm - idleObj.lastTimeIdleMillis ) > ( lastTimeIdleTimeoutMillis ) ) {
								prevIdleObj = idleObj.prev;
								nextIdleObj = idleObj.next;
								if ( nextIdleObj != null ) {
									nextIdleObj.prev = prevIdleObj;
								}
								if ( prevIdleObj != null ) {
									prevIdleObj.next = nextIdleObj;
								}
								else {
									rootIdleObj = nextIdleObj;
								}
								idleObj.prev = null;
								idleObj.next = null;
								idleObj = nextIdleObj;
							}
							else {
								idleObj = idleObj.next;
							}
						}
						/*
						 * Insert new IdleObject.
						 */
						idleObj = idleObjects[ localMinIdle ];
						if ( rootIdleObj == null ) {
							rootIdleObj = idleObj;
						}
						else if ( ( idleObj.prev == null ) && ( idleObj.next == null ) && ( idleObj != rootIdleObj ) ) {
							prevIdleObj = null;
							nextIdleObj = rootIdleObj;

							boolean b = true;
							/*
							 * Compare.
							 */
							while ( b ) {
								if ( idleObj.index < nextIdleObj.index ) {
									b = false;
								}
								else {
									prevIdleObj = nextIdleObj;
									nextIdleObj = nextIdleObj.next;
									if ( nextIdleObj == null ) {
										b = false;
									}
								}
							}
							/*
							 * Insert.
							 */
							if ( prevIdleObj == null ) {
								rootIdleObj = idleObj;
							}
							else {
								prevIdleObj.next = idleObj;
								idleObj.prev = prevIdleObj;
							}
							if ( nextIdleObj != null ) {
								idleObj.next = nextIdleObj;
								nextIdleObj.prev = idleObj;
							}
						}
						/*
						 * Print.
						 */
						/*
						idleObj = rootIdleObj;
						while ( idleObj != null ) {
							// debug
							System.out.println( idleObj.index);
							idleObj = idleObj.next;
						}
						*/
						/*
						 * Release.
						 */
						int postWorkers;
						if ( ( rootIdleObj != null ) && ( rootIdleObj.index > threshold ) && ( allocated > min ) ) {
							postWorkers = allocated - ( rootIdleObj.index - threshold );
							if ( postWorkers < min ) {
								postWorkers = min;
							}
							n = allocated - postWorkers;
							resourcePool.release( n );

							// debug
							//System.out.println( "Stopping: " + stop );
						}
					}
				}
			}
			else {
				if ( idle > 0 ) {
					resourcePool.release( idle );
				}
				if ( allocated == 0 ) {
					running = false;
				}
			}

			try {
				Thread.sleep( 1000 );
				--sampleSeconds;
			}
			catch (InterruptedException e) {
			}
		}
	}

	/*
	public void validate() {
		IdleObject prevIdleObj = null;
		IdleObject idleObj = rootIdleObj;

		while ( idleObj != null ) {
			// debug
			System.out.println( idleObj );
			if ( idleObj.prev == idleObj ) {
				System.out.println( "error: prev" );
				System.exit( 1 );
			}
			if ( idleObj.next == idleObj ) {
				System.out.println( "error: next" );
				System.exit( 1 );
			}
			prevIdleObj = idleObj;
			idleObj = idleObj.next;
		}
	}
	*/

	static class IdleObject {

		int index;
		long lastTimeIdleMillis;

		IdleObject prev = null;
		IdleObject next = null;

		IdleObject(int index) {
			this.index = index;
		}

	}

}
