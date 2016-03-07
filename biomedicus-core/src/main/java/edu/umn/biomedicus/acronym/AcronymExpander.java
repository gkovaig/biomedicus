package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.model.simple.SimpleToken;
import edu.umn.biomedicus.model.text.Document;
import edu.umn.biomedicus.model.text.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Will normalize/expand/disambiguate any acronyms or abbreviations that have been tagged by AcronymDetector.
 * Needs an AcronymModel to do this
 *
 * @author Greg Finley
 * @since 1.5.0
 */
@Singleton
public class AcronymExpander {

    private static final Logger LOGGER = LogManager.getLogger(AcronymModel.class);

    private final AcronymModel model;

    @Inject
    public AcronymExpander(AcronymModel model) {
        this.model = model;
    }

    /**
     * Go through all tagged acronyms in a document and fill in their long forms according to an AcronymModel
     *
     * @param document a tokenized Document
     */
    public void expandAcronyms(Document document) {
        LOGGER.info("Expanding acronyms and abbreviations");

        List<Token> allTokens = new ArrayList<>();
        for (Token token : document.getTokens()) {
            allTokens.add(token);
        }

        Token prevToken = null;
        Token prevPrevToken = null;

        for (Token token : document.getTokens()) {

            if (token.isAcronym() && hasLetters(token)) {
                boolean solved = false;

                // First see if these two or three tokens form an obvious abbreviation
                if (prevToken != null && prevToken.isAcronym()) {
                    if (prevPrevToken != null && prevPrevToken.isAcronym()) {
                        Token threeWordToken = new SimpleToken(document.getText(), prevPrevToken.getBegin(), token.getEnd());
                        if (model.hasAcronym(threeWordToken)) {
                            String sense = model.findBestSense(allTokens, threeWordToken);
                            token.setLongForm(sense);
                            prevToken.setLongForm(sense);
                            prevPrevToken.setLongForm(sense);
                            solved = true;
                        }
                    }
                    if (!solved) {
                        Token twoWordToken = new SimpleToken(document.getText(), prevToken.getBegin(), token.getEnd());
                        if (model.hasAcronym(twoWordToken)) {
                            String sense = model.findBestSense(allTokens, twoWordToken);
                            token.setLongForm(sense);
                            prevToken.setLongForm(sense);
                            solved = true;
                        }
                    }
                }

                if (!solved) {
                    token.setLongForm(model.findBestSense(allTokens, token));
                }

                // If the sense of the 'acronym' is really just an English word, revoke its acronym status
                String longForm = token.getLongForm();
                if (longForm != null && longForm.toLowerCase().equals(token.getText().toLowerCase())) {
                    token.setIsAcronym(false);
                    token.setLongForm(null);
                }
            }
            prevPrevToken = prevToken;
            prevToken = token;
        }
    }

    /**
     * @param token a Token to check
     * @return true if the token has any letters
     */
    private boolean hasLetters(Token token) {
        return !token.getText().toLowerCase().matches("[^a-z]*");
    }
}
