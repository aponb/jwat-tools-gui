/*
 * Barrier Semaphore, control of flow.
 * Copyright (C) 2007  Nicholas Clarke
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
 * 06-Feb-2007 : First implementation.
 *
 */

package com.antiaction.multithreading.concurrent;

/**
 * Basic barrier semaphore, allows for controlled control of flow.
 * Losely based on a barrier implementation by Scott Oaks & Henry Wong.
 * Java Threads ISBN 1-56592-418-5.
 * <BR><BR>
 * This allows for the management of resources. Several threads can be forced 
 * to wait until all are finished.
 * <BR><BR>
 * This should be a very robust implementation.
 * <BR><BR>
 * In depth coverage of the subject can be found in the afore mentioned book.
 *
 * @version 1.00
 * @author Nicholas Clarke <nclarke@diku.dk>
 */
public class Barrier {

	/** Barrier count to wait for. */
	private int cnt = 0;

	/**
	 * Init barrier with count to wait for.
	 * @param cnt barrier count.
	 */
	public Barrier(int cnt) {
        if ( cnt < 0 ) {
			throw new IllegalArgumentException( "Cannot instantiate Barrier with negative count!" );
		}
		this.cnt = cnt;
	}

	/**
	 * Decrease barrier count and check if the rest are also finished.
	 */
	public synchronized void waitForRest() {
		--cnt;
		waitForAll();
	}

	/**
	 * Check to see if the barrier count is zero and the barrier is finished.
	 */
	public synchronized void waitForAll() {
		if ( cnt <= 0 ) {
			notifyAll();
			return;
		}
		while ( cnt > 0 ) {
			try {
				wait();
			}
			catch (InterruptedException e) {
			}
		}
	}

}
