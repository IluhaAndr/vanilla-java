/*
 * Copyright (c) 2011 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vanilla.java.collections.impl;

import vanilla.java.collections.api.HugeList;
import vanilla.java.collections.api.HugeListIterator;
import vanilla.java.collections.api.Recycleable;
import vanilla.java.collections.api.impl.ByteBufferAllocator;
import vanilla.java.collections.api.impl.Copyable;
import vanilla.java.collections.api.impl.HugeElement;
import vanilla.java.collections.api.impl.HugePartition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

public abstract class AbstractHugeArrayList<E> extends AbstractHugeCollection<E> implements HugeList<E>, RandomAccess {
  protected final List<HugePartition> partitions = new ArrayList<HugePartition>();
  protected final ByteBufferAllocator allocator;
  private final Class<E> elementType;
  protected final List<E> pointerPool = new ArrayList<E>();
  private final List<HugeListIterator<E>> iteratorPool = new ArrayList<HugeListIterator<E>>();
  protected final List<E> implPool = new ArrayList<E>();
  private final List<SubList<E>> subListPool = new ArrayList<SubList<E>>();

  protected AbstractHugeArrayList(int partitionSize, Class<E> elementType, ByteBufferAllocator allocator) {
    super(elementType, allocator.sizeHolder());
    if (this.size.partitionSize() < 1)
      this.size.partitionSize(partitionSize);
    this.elementType = elementType;
    this.allocator = allocator;
  }

  @Override
  public E get(long index) {
    final int size = pointerPool.size();
    E e = size > 0 ? pointerPool.remove(size - 1) : createPointer();
    ((HugeElement) e).index(index);
    return e;
  }

  protected abstract E createPointer();

  @Override
  public HugeListIterator<E> listIterator(long start, long end) {
    final int size = iteratorPool.size();
    HugeListIterator<E> e = size > 0 ? iteratorPool.remove(size - 1) : createIterator();
    e.index(start - 1);
    e.end(end);
    return e;
  }

  protected abstract HugeListIterator<E> createIterator();

  @Override
  public void recycle() {
  }

  @Override
  public void recycle(Object recycleable) {
    if (recycleable instanceof VanillaHugeListIterator)
      iteratorPool.add((HugeListIterator<E>) recycleable);
    else if (recycleable instanceof SubList)
      subListPoolAdd((SubList<E>) recycleable);
  }

  @Override
  public E remove(long index) {
    final long size1 = longSize() - 1;
    if (index != size1) {
      E from = get(index + 1);
      E to = get(index);
      for (long i = index; i < size1; i++) {
        ((HugeElement) from).index(index + 1);
        ((HugeElement) to).index(index);
        ((Copyable<E>) to).copyFrom(from);
      }
    }
    final Copyable<E> e = (Copyable<E>) get(size1);
    setSize(size1);
    final E e2 = e.copyOf();
    ((Recycleable) e).recycle();
    return e2;
  }

  @Override
  public boolean remove(Object o) {
    throw new Error("Not implemented");
  }

  @Override
  public E set(long index, E element) {
    E e = get(index);
    E i = ((Copyable<E>) e).copyOf();
    ((Copyable<E>) e).copyFrom(element);
    return i;
  }

  public E acquireImpl() {
    final int size = implPool.size();
    return size > 0 ? implPool.remove(size - 1) : createImpl();
  }

  protected abstract E createImpl();

  @Override
  public HugeList<E> subList(long fromIndex, long toIndex) {
    final int size = subListPool.size();
    return size > 0 ? subListPool.remove(size - 1) : new SubList<E>(this, fromIndex, toIndex);
  }

  public int partitionSize() {
    return (int) this.size.partitionSize();
  }

  @Override
  protected void growCapacity(long capacity) {
    long partitions = (capacity + partitionSize() - 1) / partitionSize() + 1;
    try {
      while (this.partitions.size() < partitions)
        this.partitions.add(createPartition(this.partitions.size()));
    } catch (IOException e) {
      throw new IllegalStateException("Unable to grow collection", e);
    }
  }

  public HugePartition partitionFor(long index) {
    final int n = (int) (index / partitionSize());
    if (n >= partitions.size())
      growCapacity(index);
    return partitions.get(n);
  }

  protected abstract HugePartition createPartition(int partitionNumber) throws IOException;

  public void subListPoolAdd(SubList<E> es) {
    subListPool.add(es);
  }

  @Override
  public void flush() throws IOException {
    super.flush();
    for (HugePartition partition : partitions) {
      partition.flush();
    }
    allocator.flush();
  }

  @Override
  public void close() throws IOException {
    super.close();
    for (HugePartition partition : partitions) {
      partition.close();
    }
    allocator.close();
  }
}
