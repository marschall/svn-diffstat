package com.github.marschal.svndiffstat;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;

final class FullSet<E> extends AbstractSet<E> {

  @Override
  public boolean contains(Object o) {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    return o instanceof FullSet;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public Iterator<E> iterator() {
    return Collections.emptyIterator();
  }
  
  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public int size() {
    return -1;
  }

}
