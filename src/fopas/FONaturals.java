package fopas;

import java.util.Iterator;

import fopas.ElementImpl.FOInt;
import fopas.basics.FORelation;
import fopas.basics.FOSet;

// Infinite set for \mathbb{N}.
public class FONaturals implements FOSet<FOInt> {

	@Override
	public Iterator<FOInt> iterator() {
		// TODO Do this with Guava? Even streams?
		return null;
	}

	/***
	 * Returns -1 to signify infinity.
	 */
	@Override
	public int size() {
		return -1; // Ok this is weird, but why else use a negative number, I think I can keep the consistency here.
	}

	@Override
	public FOSet<FOInt> sacrificeForSubset(FORelation<FOInt> relation) {
		// TODO Auto-generated method stub
		return null;
	}

}
