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

import vanilla.java.collections.api.impl.BCType;
import vanilla.java.collections.api.impl.FieldModel;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.CharBuffer;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Enumerated16FieldModel<T> implements FieldModel<T> {
  private final String fieldName;
  private final Map<T, Character> map = new LinkedHashMap<T, Character>();
  private final List<T> list = new ArrayList<T>();

  public Enumerated16FieldModel(String fieldName) {
    this.fieldName = fieldName;
    clear();
  }

  @Override
  public void setter(Method setter) {
    throw new Error("Not implemented");
  }

  @Override
  public void getter(Method getter) {
    throw new Error("Not implemented");
  }

  @Override
  public Method setter() {
    throw new Error("Not implemented");
  }

  @Override
  public Method getter() {
    throw new Error("Not implemented");
  }

  @Override
  public void clear() {
    map.clear();
    list.clear();
    map.put(null, (char) 0);
    list.add(null);
  }

  @Override
  public String fieldName() {
    return fieldName;
  }

  @Override
  public String titleFieldName() {
    throw new Error("Not implemented");
  }

  @Override
  public String bcStoreType() {
    throw new Error("Not implemented");
  }

  @Override
  public String bcLStoreType() {
    throw new Error("Not implemented");
  }

  @Override
  public String bcModelType() {
    throw new Error("Not implemented");
  }

  @Override
  public String bcLModelType() {
    throw new Error("Not implemented");
  }

  @Override
  public String bcFieldType() {
    throw new Error("Not implemented");
  }

  @Override
  public String bcLStoredType() {
    throw new Error("Not implemented");
  }

  @Override
  public String bcLFieldType() {
    throw new Error("Not implemented");
  }

  @Override
  public String bcLSetType() {
    throw new Error("Not implemented");
  }

  @Override
  public int bcFieldSize() {
    throw new Error("Not implemented");
  }

  @Override
  public BCType bcType() {
    throw new Error("Not implemented");
  }

  @Override
  public boolean virtualGetSet() {
    throw new Error("Not implemented");
  }

  @Override
  public boolean copySimpleValue() {
    throw new Error("Not implemented");
  }

  @Override
  public boolean isCallsNotEquals() {
    throw new Error("Not implemented");
  }

  @Override
  public boolean isCallsHashCode() {
    throw new Error("Not implemented");
  }

  @Override
  public boolean isBufferStore() {
    throw new Error("Not implemented");
  }

  @Override
  public boolean isCompacting() {
    throw new Error("Not implemented");
  }

  @Override
  public void flush() throws IOException {
    throw new Error("Not implemented");
  }

  public T get(CharBuffer buffer, int offset) {
    char ch = buffer.get(offset);
    try {
      return list.get(ch);
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalStateException("Object id " + (int) ch + " is not valid, must be less than " + list.size(), e);
    }
  }

  public void set(CharBuffer buffer, int offset, T id) {
    Character ch = map.get(id);
    if (ch == null) {
      final int size = list.size();
      if (size >= Character.MAX_VALUE)
        throw new IllegalStateException("Cannot enumerate more than " + Character.MAX_VALUE + " values in a partition.");
      list.add(id);
      ch = (char) size;
      map.put(id, ch);
    }
    buffer.put(offset, ch);
  }

  public void load(File dir, int partitionNumber) {
    if (dir == null) {
      clear();
      return;
    }
    try {
      ObjectInputStream ois = new ObjectInputStream(
                                                       new InflaterInputStream(new BufferedInputStream(new FileInputStream(fileFor(dir, partitionNumber)))));
      list.clear();
      list.addAll((Collection<T>) ois.readObject());
      ois.close();
      map.clear();
      for (int i = 0; i < list.size(); i++)
        map.put(list.get(i), (char) i);

    } catch (FileNotFoundException ignoed) {
      clear();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(e);
    }
  }

  private File fileFor(File dir, int partitionNumber) {
    return new File(dir, fieldName + "-model-" + partitionNumber);
  }

  public void save(File dir, int partitionNumber) {
    try {
      final File file = fileFor(dir, partitionNumber);
      if (list.size() <= 1) {
        file.delete();
        return;
      }
      ObjectOutputStream oos = new ObjectOutputStream(
                                                         new DeflaterOutputStream(new BufferedOutputStream(new FileOutputStream(file + ".tmp"))));
      oos.writeObject(list);
      oos.close();
      if (!file.exists() || file.delete())
        if (!new File(file + ".tmp").renameTo(file))
          throw new IllegalStateException("Unable to rename " + file + ".tmp");
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
