/*
 * ReadWrite Semaphore, simultanious reads and exclusive writes.
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
 * 31-Dec-2000 : First implementation {
 * 02-Jan-2001 : }
 * 05-May-2001 : Changed package/structure.
 * 11-May-2001 : Added some comments.
 * 11-May-2001 : Change to IllegalStateException when lock not held.
 * 16-Sep-2001 : RWNode relocated as an inner class.
 *             : GPL'ed, ready for release.
 * 17-Nov-2001 : Packaged.
 * 04-Sep-2002 : Minor source clean.
 * 03-Oct-2005 : Package moved/renamed.
 *
 */

package com.antiaction.multithreading.concurrent;

import java.util.Vector;
import java.util.Enumeration;

/**
 * RWLock implements simultanious reads and exclusive writes.
 * Based on an implementation by Scott Oaks & Henry Wong.
 * Java Threads ISBN 1-56592-418-5.
 * <BR><BR>
 * This class allows multiple threads read access to data simultaniously.
 * Write access is only granted for one thread at a time, with no read access simultaniously.
 * Internal Vector used to coordinate access.
 * <BR><BR>
 * This should be a very robust implementation.
 * <BR><BR>
 * In depth coverage of the subject can be found in the afore mentioned book.
 *
 * @version 1.00
 * @author Nicholas Clarke <nclarke@diku.dk>
 */
public class RWLock {

	/** List of active locks. */
	private Vector waiters;

	/**
	 * Initialize object for use.
	 */
	public RWLock() {
		waiters = new Vector();
	}

	/**
	 * Return index of first thread requesting write access, <CODE>-1</CODE> is none is waiting.
	 * @return index of first thread requesting write access, <CODE>-1</CODE> is none is waiting.
	 */
	private int firstWriter() {
		Enumeration e;
		int i;
		for(i = 0, e = waiters.elements(); e.hasMoreElements(); i++) {
			RWNode node = (RWNode)e.nextElement();
			if ( node.state == RWNode.WRITER ) {
				return i;
			}
		}
		return Integer.MAX_VALUE;
	}

	/**
	 * Return index of node associated with supplied thread, or <CODE>-1</CODE> if none is found.
	 * @param t thread to which an index is requested.
	 * @return index of node associated with supplied thread, or <CODE>-1</CODE> if none is found.
	 */
	private int getIndex(Thread t) {
		Enumeration e;
		int i;
		for(i = 0, e = waiters.elements(); e.hasMoreElements(); i++) {
			RWNode node = (RWNode)e.nextElement();
			if ( node.t == t ) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Obtain a read lock, blocks until access is granted.
	 * @see #lockWrite()
	 * @see #unlock()
	 */
	public synchronized void lockRead() {
		RWNode node;
		Thread me = Thread.currentThread();
		int index = getIndex( me );
		if ( index == -1 ) {
			node = new RWNode( me, RWNode.READER );
			waiters.addElement( node );
		}
		else {
			node = (RWNode)waiters.elementAt( index );
		}
		while ( getIndex( me ) > firstWriter() ) {
			try {
				wait();
			}
			catch (Exception e) {}
		}
		++node.nAcquires;
	}

	/**
	 * Obtain a write lock, blocks until exclusive access can be granted.
	 * @see #lockRead()
	 * @see #unlock()
	 * @exception IllegalStateException if an attempt is made to upgrade a read lock to a write lock.
	 */
	public synchronized void lockWrite() {
		RWNode node;
		Thread me = Thread.currentThread();
		int index = getIndex( me );
		if ( index == -1 ) {
			node = new RWNode( me, RWNode.WRITER );
			waiters.addElement( node );
		}
		else {
			node = (RWNode)waiters.elementAt( index );
			if ( node.state == RWNode.READER ) {
				throw new IllegalStateException( "Upgrade lock" );
			}
		}
		while ( getIndex( me ) != 0 ) {
			try {
				wait();
			}
			catch (Exception e) {}
		}
		++node.nAcquires;
	}

	/**
	 * Decreases the nest count, if zero the lock is released.
	 * @see #lockRead()
	 * @see #lockWrite()
	 * @exception IllegalStateException if no lock is held.
	 */
	public synchronized void unlock() {
		RWNode node;
		Thread me = Thread.currentThread();
		int index = getIndex( me );
		if ( index != -1 ) {
			if ( index > firstWriter() ) {
				throw new IllegalStateException( "Lock not held" );
			}
			node = (RWNode)waiters.elementAt( index );
			--node.nAcquires;
			if ( node.nAcquires == 0 ) {
				waiters.removeElementAt( index );
				notifyAll();
			}
		}
		else {
			throw new IllegalStateException( "Lock not held" );
		}
	}

	/**
	 * Node allocated each active Thread using an RWLock object.
	 * Keeps track of Thread, state and its nest count.
	 */
	private class RWNode {

		/** Read lock type. */
		static final int READER = 0;
		/** Write lock type. */
		static final int WRITER = 1;

		/** Thread associated with the lock. */
		Thread t;
		/** Lock type. (Read/Write) */
		int state;
		/** Number of accumulated acquires. */
		int nAcquires;

		/**
		 * Initialize a lock object.
		 * @param t thread associated with lock.
		 * @param state lock type. (Read/Write)
		 */
		RWNode(Thread t, int state) {
			this.t = t;
			this.state = state;
			nAcquires = 0;
		}
	}

}
