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

package edu.umn.biomedicus.sentence;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import edu.umn.biomedicus.acronym.AcronymExpansionsModel;
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.nlpengine.Aggregator;
import edu.umn.nlpengine.Artifact;
import edu.umn.nlpengine.Document;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import javax.annotation.Nullable;
import javax.inject.Inject;
import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.sentdetect.SentenceDetectorFactory;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.sentdetect.SentenceSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.StringList;
import opennlp.tools.util.TrainingParameters;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ONLPSentenceTrainer implements Aggregator {

  private static final Logger logger = LoggerFactory.getLogger(ONLPSentenceTrainer.class);

  private static final char[] EOS_CHARS = ".!?:\n\t".toCharArray();
  private static final String POISON = ">poison<";
  private final BlockingDeque<String> samplesQueue = new LinkedBlockingDeque<>();
  private final Dictionary abbrevs;
  private final Path outputPath;
  private final CountDownLatch modelTrained = new CountDownLatch(1);
  private final String documentName;

  @Nullable
  private SentenceModel sentenceModel = null;

  @Nullable
  private IOException ioException = null;

  @Inject
  ONLPSentenceTrainer(
      AcronymExpansionsModel acronymExpansionsModel,
      @ProcessorSetting("opennlp.sentence.trainerOutputDirectory") Path outputPath,
      @ProcessorSetting("documentName") String documentName
  ) {
    this.outputPath = outputPath;
    this.documentName = documentName;
    abbrevs = new Dictionary(true);
    for (String s : acronymExpansionsModel.getAcronyms()) {
      abbrevs.put(new StringList(s));
    }

    Thread thread = new Thread(() -> {
      try {
        SentenceSampleStream samples = new SentenceSampleStream(
            new ObjectStream<String>() {
              boolean done = false;

              @Nullable
              @Override
              public String read() throws IOException {
                try {
                  if (done) {
                    return null;
                  }
                  String sentenceSample = samplesQueue.take();
                  // comparing reference equality on purpose.
                  //noinspection StringEquality
                  if (sentenceSample == POISON) {
                    done = true;
                    return null;
                  }
                  return sentenceSample;
                } catch (InterruptedException e) {
                  throw new IOException(e);
                }
              }

              @Override
              public void reset() throws IOException,
                  UnsupportedOperationException {
                throw new UnsupportedOperationException();
              }

              @Override
              public void close() throws IOException {

              }
            });
        SentenceDetectorFactory sentenceDetectorFactory = new SentenceDetectorFactory("en",
            false, abbrevs, EOS_CHARS);
        TrainingParameters params = TrainingParameters.defaultParams();
        logger.info("Training sentence model.");
        sentenceModel = SentenceDetectorME.train("en", samples, sentenceDetectorFactory, params);
        logger.info("Finished training sentence model.");
        modelTrained.countDown();
      } catch (IOException e) {
        ioException = e;
      }
    });
    thread.start();
  }

  @Override
  public void done() {

    samplesQueue.add(POISON);
    try {
      modelTrained.await();
      if (ioException != null) {
        throw new RuntimeException(ioException);
      }

      if (sentenceModel == null) {
        throw new RuntimeException("Error training sentence model.");
      }

      OutputStream outputStream = Files.newOutputStream(outputPath.resolve("sentence.bin"), CREATE,
          TRUNCATE_EXISTING);
      sentenceModel.serialize(outputStream);
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted before model could be saved.");
    } catch (IOException e) {
      throw new RuntimeException("Failed to write out model.");
    }
  }

  @Override
  public void process(@NotNull Artifact artifact) {
    Document document = artifact.getDocuments().get(documentName);

    if (document == null) {
      throw new RuntimeException("No document with name: " + documentName);
    }

    String text = document.getText();

    for (Sentence sentence : document.labelIndex(Sentence.class)) {
      CharSequence sample = sentence.coveredString(text);
      samplesQueue.add(sample.toString());
    }
  }
}
