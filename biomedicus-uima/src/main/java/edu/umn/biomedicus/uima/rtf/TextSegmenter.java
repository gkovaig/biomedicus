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

package edu.umn.biomedicus.uima.rtf;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses the table and paragraph information to create text segments.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class TextSegmenter extends CasAnnotator_ImplBase {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(TextSegmenter.class);

  private String documentName;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    documentName = ((String) aContext.getConfigParameterValue("documentName"));
  }

  @Override
  public void process(CAS aCAS) {
    LOGGER.trace("Segmenting rtf text.");
    CAS systemView = aCAS.getView(documentName);
    TextSegmentsBuilder textSegmentsBuilder = new TextSegmentsBuilder(systemView);

    TypeSystem typeSystem = systemView.getTypeSystem();

    textSegmentsBuilder.addAnnotations(typeSystem
        .getType("edu.umn.nlpengine.generated2_0.Paragraph"));
    textSegmentsBuilder.addAnnotations(typeSystem
        .getType("edu.umn.nlpengine.generated2_0.Row"));
    textSegmentsBuilder.addAnnotations(typeSystem
        .getType("edu.umn.nlpengine.generated2_0.Cell"));
    textSegmentsBuilder.addAnnotations(typeSystem
        .getType("edu.umn.nlpengine.generated2_0.NestedRow"));
    textSegmentsBuilder.addAnnotations(typeSystem
        .getType("edu.umn.nlpengine.generated2_0.NestedCell"));

    textSegmentsBuilder.buildInView();
  }
}
