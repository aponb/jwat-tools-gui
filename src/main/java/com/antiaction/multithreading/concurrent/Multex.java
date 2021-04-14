/*
 * Multex Semaphore, resource restricted access.
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
 * 27-Dec-2000 : First implementation {
 * 28-Dec-2000 : }
 * 05-May-2001 : Changed package/structure.
 * 11-May-2001 : Added some comments.
 * 16-Sep-2001 : GPL'ed, ready for release.
 * 17-Nov-2001 : Packaged.
 * 15-Feb-2002 : Catch InterruptedException and not Exception.
 * 15-Feb-2002 : Added AttemptSemaphore(long).
 * 15-Feb-2002 : IllegalArgumentException if timeout negative.
 * 04-Sep-2002 : Minor source clean.
 * 14-Dec-2002 : Renamed AttemptSemaphore(long) -> obtainSemaphore(long).
 * 03-Oct-2005 : Package moved/renamed.
 *
 */

package com.antiaction.multithreading.concurrent;

/**
 * Basic counter semaphore, allows for controlled multithreaded access to data.
 * Losely based on a mutex implementation by Scott Oaks & Henry Wong.
 * Java Threads ISBN 1-56592-418-5.
 * <BR><BR>
 * This allows for the management of resources. Several threads can be admitted
 * access to the lock at the same time. For each released resource one can be
 * obtained.
 * <BR><BR>
 * This should be a very robust implementation.
 * <BR><BR>
 * In depth coverage of the subject can be found in the afore mentioned book.
 *
 * @version 1.00
 * @author Nicholas Clarke <nclarke@diku.dk>
 */
public class Multex {

	/** Number of resources available. */
	private int openSlots = 0;

	/**
	 * Initialize closed multex semaphore.
	 * @see #Multex(int)
	 */
	public Multex() {
		this( 0 );
	}

	/**
	 * Initialize multex semaphore with specified initial open slots.
	 * @param cnt initial open slots.
	 * @exception IllegalArgumentException in case a negative argument is passed.
	 */
	public Multex(int cnt) {
        if ( cnt < 0 ) {
			throw new IllegalArgumentException( "Cannot instantiate Multex with negative slot count!" );
		}
		openSlots = cnt;
	}

	/**
	 * This call returns when the current thread has obtained a free resource.
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
	 * This call returns when the current thread has obtained a free resource,
	 * timeout occurs or an InterruptedException was thrown.
	 * @param timeout the maximum time to wait in milliseconds.
	 * @throws IllegalArgumentException if the value of timeout is negative.
	 * @see #obtainSemaphore()
	 * @see #attemptSemaphore()
	 * @see #releaseSemaphore()
	 */
	public synchronized boolean obtainSemaphore(long timeout) {
		if ( timeout < 0 ) {
			throw new IllegalArgumentException( "Multex: attempSemaphore() with negative timeout!" );
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
	 * Attempt to obtain a free resource, returns either way.
	 * @return boolean indicating whether the attempt was successful or not.
	 * @see #obtainSemaphore()
	 * @see #obtainSemaphore(long)
	 * @see #releaseSemaphore()
	 */
	public synchronized boolean attemptSemaphore() {
		if ( openSlots > 0 ) {
			--openSlots;
			return true;
		}
		return false;
	}

	/**
	 * Release a resource, increases the amount of open slots.
	 * @see #obtainSemaphore()
	 * @see #obtainSemaphore(long)
	 * @see #attemptSemaphore()
	 */
	public synchronized void releaseSemaphore() {
			if ( openSlots == 0 ) {
				notify();
			}
			++openSlots;
	}

}
