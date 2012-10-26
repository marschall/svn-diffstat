package com.github.marschal.svndiffstat;

final class DiffStat {
	
	final int added;
	final int removed;
	
	DiffStat(int added, int removed) {
		this.added = added;
		this.removed = removed;
	}
	
	@Override
	public String toString() {
		return this.added + " insertions(+), " + this.removed + " deletions(-)";
	}

}
