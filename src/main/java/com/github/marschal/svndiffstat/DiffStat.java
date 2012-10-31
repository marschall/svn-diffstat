package com.github.marschal.svndiffstat;

final class DiffStat {

  private int added;
  private int removed;

  DiffStat(int added, int removed) {
    this.added = added;
    this.removed = removed;
  }

  @Override
  public String toString() {
    return this.added + " insertions(+), " + this.removed + " deletions(-)";
  }

  void add(DiffStat other) {
    this.added += other.added;
    this.removed += other.removed;
  }

  int added() {
    return this.added;
  }

  int removed() {
    return this.removed;
  }

  int delta() {
    return this.added - this.removed;
  }
  
  @Override
  public int hashCode() {
    return this.added ^ this.removed;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof DiffStat)) {
      return false;
    }
    DiffStat other = (DiffStat) obj;
    return this.added == other.added
        && this.removed == other.removed;
  }

}
