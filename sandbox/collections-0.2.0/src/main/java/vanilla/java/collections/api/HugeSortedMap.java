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

package vanilla.java.collections.api;

import java.util.SortedMap;

public interface HugeSortedMap<K, V> extends SortedMap<K, V>, HugeMap<K, V> {
  @Override
  HugeSortedMap<K, V> subMap(K fromKey, K toKey);

  @Override
  HugeSortedMap<K, V> headMap(K toKey);

  @Override
  HugeSortedMap<K, V> tailMap(K fromKey);

  @Override
  HugeSet<Entry<K, V>> entrySet();
}
