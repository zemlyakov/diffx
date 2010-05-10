/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at 
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.topologi.diffx.load.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.topologi.diffx.config.TextGranularity;
import com.topologi.diffx.config.WhiteSpaceProcessing;
import com.topologi.diffx.event.TextEvent;
import com.topologi.diffx.event.impl.SpaceEvent;
import com.topologi.diffx.event.impl.WordEvent;

/**
 * The tokeniser for characters events.
 * 
 * <p>This class is not synchronized.
 * 
 * @author Christophe Lauret
 * @version 10 May 2010
 */
public final class TokenizerByWord implements TextTokenizer {

  /**
   * Map characters to events in order to recycle events as they are created.
   */
  private Map<String, TextEvent> recycling = new HashMap<String, TextEvent>();  

  /**
   * Define the whitespace processing. 
   */
  private final WhiteSpaceProcessing whitespace;

  /**
   * Creates a new tokenizer.
   * 
   * @throws NullPointerException if the white space processing is not specified.
   */
  public TokenizerByWord(WhiteSpaceProcessing whitespace) {
    if (whitespace == null) throw new NullPointerException("the white space processing must be specified.");
    this.whitespace = whitespace;
  }

  /**
   * {@inheritDoc}
   */
  public List<TextEvent> tokenize(CharSequence seq) {
    if (seq == null) return null;
    if (seq.length() == 0) return Collections.emptyList();
    List<TextEvent> events = new ArrayList<TextEvent>(seq.length());

    Pattern p = Pattern.compile("\\s+");
    Matcher m = p.matcher(seq);
    int index = 0;

    // Add segments before each match found
    while (m.find()) {
      if (index != m.start()) {
        String word = seq.subSequence(index, m.start()).toString();
        events.add(getWordEvent(word));
      }
      String space = seq.subSequence(m.start(), m.end()).toString();
      events.add(getSpaceEvent(space));
      index = m.end();
    }

    // Add remaining word if any
    if (index != seq.length()) {
      String word = seq.subSequence(index, seq.length()).toString();
      events.add(getWordEvent(word));
    }

    return events;
  }

  /**
   * Always <code>TextGranularity.CHARACTER</code>.
   * 
   * {@inheritDoc}
   */
  public TextGranularity granurality() {
    return TextGranularity.WORD;
  }

  // Private helpers ------------------------------------------------------------------------------
  
  /**
   * Returns the word event corresponding to the specified characters.
   * 
   * @param word the characters of the word 
   * @return the corresponding word event
   */
  private TextEvent getWordEvent(String word) {
    TextEvent e = this.recycling.get(word);
    if (e == null) {
      e = new WordEvent(word);
      this.recycling.put(word, e);
    }
    return e;
  }

  /**
   * Returns the space event corresponding to the specified characters.
   * 
   * @param word the characters of the space 
   * @return the corresponding space event
   */
  private TextEvent getSpaceEvent(String space) {
    // we ignore white spaces so any will do
    if (this.whitespace == WhiteSpaceProcessing.IGNORE) return SpaceEvent.SINGLE_WHITESPACE;
    // preserve the actual white space used
    TextEvent e = this.recycling.get(space);
    if (e == null) {
      e = SpaceEvent.getInstance(space);
      this.recycling.put(space, e);
    }
    return e;
  }

}