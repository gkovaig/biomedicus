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

package edu.umn.biomedicus.concepts;

import edu.umn.nlpengine.TextRange;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 *
 */
public class SuiCuiTui implements Serializable {

  private static final Pattern PATTERN = Pattern.compile("S([\\d]{7})C([\\d]{7})T([\\d]{3})");

  private final SUI sui;

  private final CUI cui;

  private final TUI tui;

  public SuiCuiTui(SUI sui, CUI cui, TUI tui) {
    this.sui = sui;
    this.cui = cui;
    this.tui = tui;
  }

  public SuiCuiTui(byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    sui = new SUI(buffer.getInt());
    cui = new CUI(buffer.getInt());
    tui = new TUI(buffer.getInt());
  }

  public static SuiCuiTui fromString(String string) {
    Matcher matcher = PATTERN.matcher(string);
    if (matcher.find()) {
      SUI sui = new SUI(Integer.parseInt(matcher.group(1)));
      CUI cui = new CUI(Integer.parseInt(matcher.group(2)));
      TUI tui = new TUI(Integer.parseInt(matcher.group(3)));

      return new SuiCuiTui(sui, cui, tui);
    } else {
      throw new IllegalArgumentException("String does not match SuiCuiTui");
    }
  }

  public SUI sui() {
    return sui;
  }

  public CUI cui() {
    return cui;
  }

  public TUI tui() {
    return tui;
  }

  public DictionaryConcept toConcept(TextRange label, double confidence) {
    return new DictionaryConcept(label, cui.toString(), "UMLS", tui.toString(), confidence);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SuiCuiTui suiCuiTui = (SuiCuiTui) o;

    if (!sui.equals(suiCuiTui.sui)) {
      return false;
    }
    if (!cui.equals(suiCuiTui.cui)) {
      return false;
    }
    return tui.equals(suiCuiTui.tui);

  }

  @Override
  public int hashCode() {
    int result = sui.hashCode();
    result = 31 * result + cui.hashCode();
    result = 31 * result + tui.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return sui.toString() + cui.toString() + tui.toString();
  }

  byte[] getBytes() {
    return ByteBuffer.allocate(BYTES_LENGTH).putInt(sui.identifier()).putInt(cui.identifier())
        .putInt(tui.identifier()).array();
  }

  public static final int BYTES_LENGTH = 12;
}
