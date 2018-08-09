/*
        extMethods.sc
        External Methods that implement some of the behavior for Repetition.sc
*/

+ Routine {

  nextRP {
    |inval|
    var value = this.value(inval), tmp;

    // then we add the amplitude. if the `symbol` has `@`,  will sum `0.25`
    value = value.applyAccent(value.at(\amp) ?? 0.9);
    // if the `symbol` has `'`, will shift 1 octave up. if the 'symbol' has `,` will shift 1 octave down.
    // also we need to inject the typeOf to calculate later the octave we're on.
    value = value.applyOctave(value.at(\octave) ?? 5);
    // .collect(_.applyOctave(oct))
    // for convenience, let's add the "type of" Event we'll play
    // ie: percussion, chord, note
    value = value.applyTypeOf;
    // now that everything is it's right place, we'll clean the symbols to
    // finally, convert each Event to a proper MIDI note based on `typeOf` and the `symbol`.
    value = value.applyReNote;

    ^value;
  }

  rePlayer {
    |... args|
    ^Pchain(Prepetition(), Pbind(*args), RePevent(this));
  }

}

+ Object {

  nextNRP {
    |n, inval|
    ^Array.fill(n, { this.nextRP(inval) });
  }

}

+ String {

  parseRepetition {
    var regexp = "([\\w\\.\\|\\/\\'\\,!?@?\\+?(\\*\d+)? ]+)";

    ^this
    .asString
    .findRegexp(regexp)
    .collect(_[1])
    .collect(_.stripWhiteSpace)
    .collect(_.replace("/", ""))
    .uniq
    .collect(_.flat)
    .collect(_.asSymbol)
    .collect(_.maybeRepeat)
    .collect(_.maybeSplit)
    .collect(_.parseEvents)
    .pop
    ;
  }

  asRepetitionSequence {
    ^this
    .parseRepetition
    .pseq(inf)
    ;
  }

  asRepetitionStream {
    ^this
    .asRepetitionSequence
    .asStream
    ;
  }

  classExists {
    ^Class.allClasses.collect(_.asString).indexOfEqual(this).notNil;
  }

  // requires `Bjorklund` Quark.
  bjorklund {
    |k, n, rotate=0|
    var hit = Pseq(this.split($ ), inf).asStream;

    ^Bjorklund(k, n)
    .rotate(rotate)
    .collect { |p| if (p.asBoolean) { hit.next } { \r } }
    .join(" ")
    ;
  }

  maybeCleanUp {
    ^this
    .replace("@", "")
    .replace(",", "")
    .replace("'", "")
    ;
  }

  createSingleEvent {
    |dur=1|
    var item = this;

    if (item.contains("+")) {
      item = item.split($+);
      dur = (dur/item.size).dup(item.size);
    } {
      if (item.contains("|")) {
        item = item.split($|);
      }
    };

    if (dur.isKindOf(Array)) {
      ^dur.collect {
        |d, i|
        (dur: d, symbol: item[i]);
      }.flat;
    } {
      ^(dur: dur, symbol: item);
    }
  }

  /*
  Based on Steven Yi's Hex Beats.
  http://kunstmusik.com/2017/10/20/hex-beats/
  */
  hexbeat {
    // reject anything ouside hex valid numbers
    ^this.asList.reject{
      |chr|
      "0123456789abcdef".asList.indexOfEqual(chr).isNil;
    }
    .collect{
      |hex|
      // convert each character/number to a 4bits representation
      hex.asString.asList.collect{
        |h|
        h.digit.asBinaryDigits(4)
      };
    }.flat;
  }

  shuffle { ^this.split($ ).scramble.join(" "); }

  everyN {
    |times, callback|
    ^this
    .replace("/","").split($ ) // individual notes
    .reject { |x| x.size == 0 } // reject anything that's not a note
    .collect{
      |item, idx|
      if (idx % times == 0) {
        callback.(item);
      } {
        item
      }
    }
    .join(" ")
    ;
  }
  probability {
    |chance, callback|
    ^this
    .replace("/","").split($ ) // individual notes
    .reject { |x| x.size == 0 } // reject anything that's not a note
    .collect{
      |item, idx|
      if (chance.coin) {
        callback.(item);
      } {
        item
      }
    }
    .join(" ")
    ;
  }
  rarely { |callback| ^this.probability(0.25, callback); }
  sometimes { |callback| ^this.probability(0.5, callback); }
  regularly { |callback| ^this.probability(0.75, callback); }
  always { |callback| ^this.probability(1.0, callback);  }

}


+ Symbol {

  maybeRepeat {
    var item = this.asString;

    if (item.contains("!")) {
      item = item.split($ ).collect{
        |i|
        if (i.contains("!")) {
          i.replace("!", "").dup.join(" ")
        } {
          i
        }
      }.join(" ")
    };

    if (item.contains("*")) {
      item = item.split($ ).collect{
        |i|
        if (i.contains("*")) {
          i = i.split($*);
          i[0].dup(i[1].asInt).join(" ");
        } {
          i
        }
      }.join(" ")
    };

    ^item.asSymbol;
  }

  maybeSplit {
    |sep|
    var item = this;

    if (item.isKindOf(String)) {
      item = item.split(sep);
    };

    ^item;
  }

  whichTypeOf {
    var symbol = this, midinote = \rest, typeof = \percussion; // fix me: what about Integers?

    if (symbol.isRest,
      { typeof = \rest; },
      {
        if (symbol.percussion.notNil,
          { typeof = \percussion; },
          {
            if (symbol.chord.notNil,
              { typeof = \chord; },
              {
                if (symbol.midi.notNil)
                { typeof = \note; }
            });
        });
      });

    ^typeof;
  }

  asReNote {
    var symbol = this, midinote = \rest;

    if (symbol.percussion.notNil,
      { midinote = symbol.percussion; },
      {
        if (symbol.chord.notNil,
          { midinote = symbol.chord(0); },
          {
            if (symbol.midi.notNil)
            { midinote = symbol.midi(0); }
          });
      });

    ^midinote;
  }

  parseEvents {
    var acc, size, dur, time, octave;
    var pattern = this.asString;
    var events = [];

    /*
    Basic Steps:
    1. remove unwanted stuff
    2. measure the List size
    3. calculate the duration
    */
    pattern = pattern.split($ ).reject { |x| x.asString.size < 1 };
    size = pattern.size;
    dur = 1/size;

    // Then we can start building our `List of Events`:
    ^pattern
    // based on the `symbol`, we'll create the Events with each duration.
    .collect(_.createSingleEvent(dur))
    // since we don't need to calculate the individual duration anymore, the `LoE` needs to be flattened to be able to apply functions over each Event.
    .flat
    // and to be sure, we'll remove everything that's not an Event.
    .reject(_.isKindOf(Event).not)
    ;
  }

}

+ Event {

  applyAccent {
    |gain=0.9|
    var symbol = this.at(\symbol), accent = 0;

    if (symbol.isKindOf(List) || symbol.isKindOf(Array)) {
      if (symbol.reject{ |x| x.asString.contains("@").not }.size.asBoolean) { accent = 0.25 }
    } {
      if (symbol.asString.contains("@")) { accent = 0.25 }
    };

    ^this
    .merge((accent: accent), {|a,b| b })
    ;
  }

  applyOctave {
    var symbol = this.at(\symbol), shift = 0;

    if (symbol.isKindOf(List) || symbol.isKindOf(Array)) {
      if (symbol.reject{ |x| x.asString.contains(",").not }.size.asBoolean) { shift = -1 };
      if (symbol.reject{ |x| x.asString.contains("'").not }.size.asBoolean) { shift = 1 };
    } {
      if (symbol.asString.contains(",")) { shift = -1 };
      if (symbol.asString.contains("'")) { shift = 1 };
    }
    ;

    ^this
    .merge((shift: shift), {|a,b| b })
    ;
  }

  applyTypeOf {
    var symbol = this.at(\symbol), typeof;

    if (symbol.isKindOf(Array)) {
      // in this particular case, i'm only interested in obtaining just one typeof
      // it might happen, but having a `chord|perc-note` doesn't make much sense.
      typeof = symbol.collect(_.maybeCleanUp).collect(_.asSymbol).collect(_.whichTypeOf).uniq.pop;
    } {
      typeof = symbol.maybeCleanUp.asSymbol.whichTypeOf;
    }
    ;

    ^this
    .merge((typeof: typeof), {|a,b| b })
    ;
  }

  applyReNote {
    var symbol = this.at(\symbol), midinote;

    if (symbol.isKindOf(Array)) {
      midinote = symbol.collect(_.maybeCleanUp).collect(_.asSymbol).collect(_.asReNote).uniq;
    } {
      midinote = symbol.maybeCleanUp.asSymbol.asReNote;
    }
    ;

    ^this
    .merge((midinote: midinote), {|a,b| b })
    ;
  }

}

+ SequenceableCollection {

  uniq {
    var result = List.new;
    this.do{
      |item|
      if (result.indexOfEqual(item).isNil) {
        result.add( item );
      }
    };
    ^result.asArray;
  }

  pseq {
    |rep = inf, offs = 0|
    ^Pseq(this, rep, offs);
  }

  pshuf {
    |rep = inf|
    ^Pshuf(this, rep);
  }

  prand {
    |rep = inf|
    ^Prand(this, rep);
  }

  pxrand {
    |rep = inf|
    ^Pxrand(this, rep);
  }

  pwrand {
    |weights, rep = inf|
    weights = weights ?? [(1 / this.size) ! this.size].normalizeSum;
    ^Pwrand(this, weights, rep);
  }

  place {
    |rep = inf, offs = 0|
    ^Place(this, rep, offs);
  }

  // alias because String.shuffle
  shuffle { ^this.scramble; }
}

+ Float {
  midirange {
    ^(127 * this).round;
  }
}

+ Integer {
  rangemidi {
    ^(this/127).asStringPrec(2);
  }
}
