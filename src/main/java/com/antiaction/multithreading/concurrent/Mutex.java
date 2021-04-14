/*
 * Mutex Semaphore, exclusive access.
 * Copyright (C) 2001, 2002  Nicholas Clarke
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

/*
 * History:
 *
 * 26-Dec-2000 : First implementation {
 * 27-Dec-2000 : }
 * 05-May-2001 : Changed package/structure.
 * 09-May-2001 : Throws IllegalArgumentException if release is attempted and the lock is not owned.
 * 11-May-2001 : Changed to IllegalStateException, makes more sense.
 * 11-May-2001 : Added some comments.
 * 16-Sep-2001 : GPL'ed, ready for release.
 * 17-Nov-2001 : Packaged.
 * 14-Feb-2002 : Catch InterruptedException and not Exception.
 * 14-Feb-2002 : Added AttemptSemaphore(long) and isOwner().
 * 15-Feb-2002 : IllegalArgumentException if timeout negative.
 * 04-Sep-2002 : Minor source clean.
 * 14-Dec-2002 : Renamed AttemptSemaphore(long) -> obtainSemaphore(long).
 * 03-Oct-2005 : Package moved/renamed.
 *
 */

package com.antiaction.multithreading.concurrent;

/**
 * Mutex semaphore, restricts access to one thread at a time.
 * Based on an implementation by Scott Oaks & Henry Wong.
 * Java Threads ISBN 1-56592-418-5.
 * <BR><BR>
 * Basicly this class allows for the maintenance of a so called critical area.
 * Access is restricted to one thread at at time.
 * <BR><BR>
 * This should be a very robust implementation.
 * <BR><BR>
 * In depth coverage of the subject can be found in the afore mentioned book.
 *
 * @version 1.01
 * @author Nicholas Clarke <nclarke@diku.dk>
 */
public class Mutex {

	/** Reference to the thread currently holding the lock. */
	private Thread lockThread = null;
	/** Nested lock count. */
	private int lockCount = 0;

	/**
	 * Initialize open mutex semaphore.
	 * @see #Mutex(int)
	 */
	public Mutex() {
		this(0);
	}

	/**
	 * Initialize mutex semaphore.with state. State depends on the argument.
	 * @param cnt initial nest count.
	 * @exception IllegalArgumentException in case a negative argument is passed.
	 */
	public Mutex(int cnt) {
		if ( cnt == 0 ) {
			lockThread = null;
			lockCount = 0;
		}
		else if ( cnt > 0 ) {
			lockThread = Thread.currentThread();
			lockCount = cnt;
		}
		else {
			throw new IllegalArgumentException("Cannot instantiate Mutex with negative lock count!");
		}
	}

	/**
	 * This call returns when the current thread has obtained ownership of the lock.
	 * @see #obtainSemaphore(long)
	 * @see #attemptSemaphore()
	 * @see #releaseSemaphore()
	 */
	public synchronized void obtainSemaphore() {
		while ( attemptSemaphore() == false ) {
			try {
				wait();
			}
			catch (InterruptedException e) {
			}
		}
	}

	/**
	 * This call returns when the current thread has obtained ownership of the lock,
	 * timeout occurs or an InterruptedException was thrown.
	 * @param timeout the maximum time to wait in milliseconds.
	 * @throws IllegalArgumentException if the value of timeout is negative.
	 * @see #obtainSemaphore()
	 * @see #attemptSemaphore()
	 * @see #releaseSemaphore()
	 */
	public synchronized boolean obtainSemaphore(long timeout) {
		if ( timeout < 0 ) {
			throw new IllegalArgumentException( "Mutex: attempSemaphore() with negative timeout!" );
		}
		if ( attemptSemaphore() == false ) {
			try {
				wait( timeout );
				return attemptSemaphore();
			}
			catch (InterruptedException e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Attempt to obtain ownership of the lock, returns either way.
	 * @return boolean indicating whether the attempt was successful or not.
	 * @see #obtainSemaphore()
	 * @see #obtainSemaphore(long)
	 * @see #releaseSemaphore()
	 */
	public synchronized boolean attemptSemaphore() {
		if ( lockThread == null ) {
			lockThread = Thread.currentThread();
			lockCount = 1;
			return true;
		}
		if ( lockThread == Thread.currentThread() ) {
			++lockCount;
			return true;
		}
		return false;
	}

	/**
	 * Release ownership of the lock in case the nest count is zero.
	 * @see #obtainSemaphore()
	 * @see #obtainSemaphore(long)
	 * @see #attemptSemaphore()
	 * @exception IllegalStateException in case an attempt is made to release a lock that is not owned.
	 */
	public synchronized void releaseSemaphore() {
		if ( getOwner() == Thread.currentThread() ) {
			--lockCount;
			if ( lockCount == 0 ) {
				lockThread = null;
				notify();
			}
		}
		else {
			throw new IllegalStateException("Lock not held");
		}
	}

	/**
	 * Returns the thread currently owning this lock.
	 * @return thread currently owning this lock.
	 */
	public synchronized Thread getOwner() {
		return lockThread;
	}

	/**
	 * Returns boolean indicating whether the current Thread owns the lock.
	 * @return boolean indicating whether the current Thread owns the lock.
	 */
	public synchronized boolean isOwner() {
		return ( Thread.currentThread() == lockThread );
	}

}
