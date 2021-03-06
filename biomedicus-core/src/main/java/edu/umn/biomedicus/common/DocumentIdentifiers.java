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

package edu.umn.biomedicus.common;

/**
 * The default views that BioMedICUS uses.
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
public final class DocumentIdentifiers {

  public static final String ANALYSIS = "Analysis";

  /**
   * A view containing the original encoded data before decoding. An example is rtf before it is
   * parsed into plain text.
   */
  public static final String ORIGINAL_DOCUMENT = "OriginalDocument";

  /**
   * A view for storing gold standard evaluation data. Generally will have the same text as the
   * system view.
   */
  public static final String GOLD_STANDARD = "GoldStandard";

  /**
   * A view for the augmented form of the original document, for example RTF that has had
   * information written into it.
   */
  public static final String AUGMENTED = "Augmented";
}
