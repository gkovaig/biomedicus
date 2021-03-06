/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.tnt;

import edu.umn.biomedicus.common.tuples.Pair;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public interface KnownWordsDataStore {

  @Nullable
  Double getProbability(String word, PartOfSpeech candidate);

  List<PartOfSpeech> getCandidates(String word);

  boolean isKnown(String word);

  void addAllProbabilities(Map<Pair<PartOfSpeech, String>, Double> lexicalProbabilities);

  void write();
}
