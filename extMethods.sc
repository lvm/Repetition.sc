/*
        extMethods.sc
        External Methods that implement some of the behavior for Repetition.sc
*/

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

  asMIDINote {
    |typeOf, octave=5|
    var val = this;

    ^switch(typeOf,
      \perc, { val.asGMPerc },
      \degree, { Note(val).midi(octave) },
      \freq, { Note(val).freq(octave) },
      \int, { val.asInt },
      /*\fn, { var self = val; if (fn.notNil) { self = fn.value(val, pattern); }; self; },*/
      \chord, {
        var self = val.asString;
        var ch = Chord.names.reject{ |ch| self.findRegexp(ch.asString++"$").size == 0 }.pop;
        if ( ch.notNil ) {
          Note(self.replace(ch.asString, "").asSymbol).midi(octave) + Chord(ch) ;
        } {
          \rest;
        }
      }
    );
  }

  parseEvents {
    |typeOf=nil, amp=0.9, oct=5|

    var acc, size, dur, time, octave;
    var pattern = this.asString;
    var events = [];
    typeOf = typeOf.asSymbol;
    amp = amp.asFloat;
    oct = oct.asInt;

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
    // then we add the amplitude. if the `symbol` has `@`,  will sum `0.25`
    .collect(_.applyAmplitude(amp))
    // then we add the octave
    // if the `symbol` has `'`, will shift 1 octave up. if the 'symbol' has `,` will shift 1 octave down.
    .collect(_.applyOctave(oct))
    // now that everything is it's right place, we'll clean the symbols to
    // finally, convert each Event to a proper MIDI note based on `typeOf` and the `symbol`.
    .collect(_.applyMIDINote(typeOf))
    // and to be sure, we'll remove everything that's not an Event.
    .reject(_.isKindOf(Event).not)
    ;
  }

}

+ String {

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

  repetitionPattern {
    |typeOf=nil, amp=0.666, oct=5|
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
    .collect(_.parseEvents(typeOf, amp, oct))
    .pop
    ;
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

  // Repetition parsing shortcuts
  perc { |amp=0.9| ^this.repetitionPattern(\perc, amp, 0); }
  degree { |amp=0.7, oct=5| ^this.repetitionPattern(\degree, amp, oct); }
  chord { |amp=0.7, oct=5| ^this.repetitionPattern(\chord, amp, oct); }
  freq { |amp=0.7, oct=5| ^this.repetitionPattern(\freq, amp, oct); }
  int { |amp=0.7, oct=5| ^this.repetitionPattern(\int, amp, oct); }
  // fn{ |fn| ^this.repetitionPattern(\fn, 0, fn); }

  shuffle { ^this.split($ ).scramble.join(" "); }
}

// + Dictionary {
+ Event {
  // Where to send the Events.
  // midi
  midichannel { |chan| ^this.with([\type, \md, \chan, chan]); }
  // synths
  usingsynth { |synth| ^this.with([\type, \note, \instrument, synth]); }

  // Manipulates individual Events.
  with { |... args| ^this.merge(().putPairs(args.flat), {|a,b| b }); }
  plus { |... args| ^this.merge(().putPairs(args.flat), {|a,b| a+b }); }
  minus { |... args| ^this.merge(().putPairs(args.flat), {|a,b| a-b }); }
  mul { |... args| ^this.merge(().putPairs(args.flat), {|a,b| a*b }); }
  div { |... args| ^this.merge(().putPairs(args.flat), {|a,b| a/b }); }

  applyAmplitude {
    |amp=0.666|
    var symbol = this.at(\symbol), shift = 0;

    if (symbol.isKindOf(List)) {
      if (symbol.reject{ |x| x.asString.contains("@").not }.size.asBoolean) { shift = 0.25 }
    } {
      if (symbol.asString.contains("@")) { shift = 0.25 }
    };

    ^this
    .with(\amp, amp)
    .plus(\amp, shift)
    ;
  }

  applyOctave {
    |octave=5|
    var symbol = this.at(\symbol), shift = 0;

    if (symbol.isKindOf(List)) {
      if (symbol.reject{ |x| x.asString.contains(",").not }.size.asBoolean) { shift = -1 };
      if (symbol.reject{ |x| x.asString.contains("'").not }.size.asBoolean) { shift = 1 };
    } {
      if (symbol.asString.contains(",")) { shift = -1 };
      if (symbol.asString.contains("'")) { shift = 1 };
    };

    ^this
    .with(\octave, octave)
    .plus(\octave, shift)
    ;
  }

  applyMIDINote {
    |typeOf=nil|
    var symbol = this.at(\symbol), octave = this.at(\octave), midinote;

    if (typeOf.notNil) {
      if (symbol.isKindOf(Array)) {
        midinote = symbol
        .collect(_.maybeCleanUp).collect(_.asSymbol).collect(_.asMIDINote(typeOf, octave));
      } {
        midinote = symbol.maybeCleanUp.asSymbol.asMIDINote(typeOf, octave);
      }
      ;

      ^this
      .merge((midinote: midinote), {|a,b| b })
      ;
    } {
      ^nil;
    }
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

  // Scale.xxx.chords
  events {
    |octave=5|
    ^this.collect {
      |val|
      (accent: 0, dur: 1, octave: octave, midinote: val + (12 * octave))
    }
  }

  // midi channels
  ch { |channel=9| ^this.collect(_.midichannel(channel)).pseq; }
  ch1 { ^this.ch(1); }
  ch2 { ^this.ch(2); }
  ch3 { ^this.ch(3); }
  ch4 { ^this.ch(4); }
  ch5 { ^this.ch(5); }
  ch6 { ^this.ch(6); }
  ch7 { ^this.ch(7); }
  ch8 { ^this.ch(8); }
  ch9 { ^this.ch(9); }
  ch10 { ^this.ch(10); }
  ch11 { ^this.ch(11); }
  ch12 { ^this.ch(12); }
  ch13 { ^this.ch(13); }
  ch14 { ^this.ch(14); }
  ch15 { ^this.ch(15); }
  ch16 { ^this.ch(16); }

  //using synthdef
  synth { |synthdef=\default| ^this.collect(_.usingsynth(synthdef)).pseq; }

  // functions to apply over a List of Events
  mute { ^this.collect(_.with([\amp, 0])); }
  stretch { |n| ^this.collect(_.with([\stretch, n])); }
  fast { |n| ^this.stretch(1/n); }
  slow { |n| ^this.stretch(n); }
  freeze { |idx, times=2|
    var frozen = this.at(idx).dup(times);
    ^this.put(idx, frozen).flat;
  }

  with {
    |... args|
    args = args.flat;
    ^this.collect(_.with(args));
  }

  every {
    |times, callback|
    ^this
    .collect{
      |item, idx|
      if (idx % times == 0) {
        callback.(item);
      } {
        item
      }
    }
    .flat
    ;
  }

  stochastic {
    |chance, callback|
    ^this
    .collect{
      |item, idx|
      if (chance.coin) {
        callback.(item);
      } {
        item
      }
    }
    .flat
    ;
  }
  rarely { |callback| ^this.stochastic(0.25, callback); }
  sometimes { |callback| ^this.stochastic(0.5, callback); }
  regularly { |callback| ^this.stochastic(0.75, callback); }

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
