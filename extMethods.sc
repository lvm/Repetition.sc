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
      \sample, { val.asGMPerc },
      \degree, { ReNote(val).midi(0) },
      \freq, { ReNote(val).freq(0) },
      \int, { val.asInt },
      /*\fn, { var self = val; if (fn.notNil) { self = fn.value(val, pattern); }; self; },*/
      \chord, {
        var self = val.asString;
        var ch = ReChord.names.reject{ |ch| self.findRegexp(ch.asString++"$").size == 0 }.pop;
        if ( ch.notNil ) {
          ReNote(self.replace(ch.asString, "").asSymbol).midi(0) + ReChord(ch) ;
        } {
          \rest;
        }
      }
    );
  }

  parseEvents {
    |typeOf=nil, oct=5, amp=0.9|

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
    // also we need to inject the typeOf to calculate later the octave we're on.
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
    |typeOf=nil, oct=5, amp=0.9|
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
    .collect(_.parseEvents(typeOf, oct, amp))
    .pop
    ;
  }

  parseRepetition {
    |typeOf=\perc, octave=5|
    var pattern = this;
    typeOf = typeOf.asSymbol;
    if (typeOf == \perc ) { pattern = pattern.perc; };
    if (typeOf == \sample ) { pattern = pattern.perc; };
    if (typeOf == \degree ) { pattern = pattern.degree; };
    if (typeOf == \chord ) { pattern = pattern.chord; };
    if (typeOf == \int ) { pattern = pattern.int; };

    ^pattern.singleEvent.midinote;
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
  perc { |amp=0.9| ^this.repetitionPattern(\perc, 0, amp); }
  sample { |amp=0.9| ^this.repetitionPattern(\sample, 0, amp); }
  degree { |oct=5, amp=0.9| ^this.repetitionPattern(\degree, oct, amp); }
  chord { |oct=5, amp=0.9| ^this.repetitionPattern(\chord, oct, amp); }
  freq { |oct=5, amp=0.9| ^this.repetitionPattern(\freq, oct, amp); }
  int { |oct=5, amp=0.9| ^this.repetitionPattern(\int, oct, amp); }
  // fn{ |fn| ^this.repetitionPattern(\fn, 0, fn); }

  shuffle { ^this.split($ ).scramble.join(" "); }
}

// + Dictionary {
+ Event {
  // Parsing / Building Repetition Events.
  applyAmplitude {
    |amp=0.9|
    var symbol = this.at(\symbol), shift = 0;

    if (symbol.isKindOf(List)) {
      if (symbol.reject{ |x| x.asString.contains("@").not }.size.asBoolean) { shift = amp/4 }
    } {
      if (symbol.asString.contains("@")) { shift = amp/4 }
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
    }
    ;
    ^this
    .with(\octave, octave)
    .plus(\shift, shift)
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

      if (typeOf.asSymbol == \freq) {
        ^this
        .merge((freq: midinote, typeof: typeOf), {|a,b| b })
      } {
        ^this
        .merge((midinote: midinote, typeof: typeOf), {|a,b| b })
      }
      ;
    } {
      ^nil;
    }
  }

  // Where to send the Events.
  // MIDI
  usingMIDI { |chan| ^this.with(\type, \md, \chan, chan); }
  // Synthdefs
  usingSynthdef { |synth| ^this.with(\type, \note, \instrument, synth); }

  // Manipulates individual Events.
  with { |... args| ^this.merge(().putPairs(args.flat), {|a,b| b }); }
  // pbind { |... args| ^this.merge(().putPairs(args.flat), {|a,b| b }); }
  plus { |... args| ^this.merge(().putPairs(args.flat), {|a,b| a+b }); }
  minus { |... args| ^this.merge(().putPairs(args.flat), {|a,b| a-b }); }
  mul { |... args| ^this.merge(().putPairs(args.flat), {|a,b| a*b }); }
  div { |... args| ^this.merge(().putPairs(args.flat), {|a,b| a/b }); }
  mute { ^this.with([\amp, 0]); }
  stretch { |n| ^this.with([\stretch, n]); }
  fast { |n| ^this.stretch(1/n); }
  slow { |n| ^this.stretch(n); }
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

  // Scale.xxx
  asEvents {
    |amp=0.9, octave=5|
    ^this.collect {
      |val|
      (octave: octave, dur: 1, amp: amp, midinote: val + (12 * octave))
    }
  }

  // midi channels
  midich {
    |channel=9, rep=inf|
    ^this
    .collect(_.usingMIDI(channel))
    .pseq(rep);
  }

  //using synthdef
  synthdef {
    |synthdef=\default, rep=inf|
    ^this
    .collect(_.usingSynthdef(synthdef))
    .pseq(rep);
  }

  // lazy shortcuts
  ch1 { |rep=inf| ^this.midich(1, rep); }
  ch2 { |rep=inf| ^this.midich(2, rep); }
  ch3 { |rep=inf| ^this.midich(3, rep); }
  ch4 { |rep=inf| ^this.midich(4, rep); }
  ch5 { |rep=inf| ^this.midich(5, rep); }
  ch6 { |rep=inf| ^this.midich(6, rep); }
  ch7 { |rep=inf| ^this.midich(7, rep); }
  ch8 { |rep=inf| ^this.midich(8, rep); }
  ch9 { |rep=inf| ^this.midich(9, rep); }
  ch10 { |rep=inf| ^this.midich(10, rep); }
  ch11 { |rep=inf| ^this.midich(11, rep); }
  ch12 { |rep=inf| ^this.midich(12, rep); }
  ch13 { |rep=inf| ^this.midich(13, rep); }
  ch14 { |rep=inf| ^this.midich(14, rep); }
  ch15 { |rep=inf| ^this.midich(15, rep); }
  ch16 { |rep=inf| ^this.midich(16, rep); }


  // functions to apply over a List of Events
  mute { ^this.collect(_.mute); }
  stretch { |n| ^this.collect(_.stretch(n)); }
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

  everyN {
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

  probability {
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
  rarely { |callback| ^this.probability(0.25, callback); }
  sometimes { |callback| ^this.probability(0.5, callback); }
  regularly { |callback| ^this.probability(0.75, callback); }
  always { |callback| ^this.probability(1.0, callback);  }
  // an alias, in order to be "constant". see String.shuffle
  shuffle { ^this.scramble; }

  // sequences work better with single notes -> `"c".degree`.
  arpeggio {
    |sq|
    ^this.collect{
      |evt|
      var seqEvts = evt.dup(sq.size);
      sq.collect {
        |s, i|
        seqEvts[i][\midinote] = seqEvts[i][\midinote] + s;
        seqEvts[i];
      };
    }
    .flat
    ;
  }


  // sequence aliases:
  dim { ^this.arpeggio([0,3,0,6]); }
  tritone { ^this.arpeggio([0,6,5,-3]); }
  fifth { ^this.arpeggio([0,4,0,8]); }
  oneUp { ^this.arpeggio([0,12,0]); }
  oneDown { ^this.arpeggio([0,-12,0]); }


  // "Join" the whole SequenceCollection of Events into a single Event -> Pbind
  singleEvent {
    |... args|
    var evt = ();
    this
    .collect {
      |e,i|
      e.keys.collect {
        |key|
        if (evt.keys.asList.indexOfEqual(key).isNil)
        { evt[key] = List.new; };
        evt[key].add(e.at(key));
      }
    }
    ;
    ^evt.collect{
      |v, k|
      v = v.asArray;
      if (v.flat.uniq.size == 1) { v.pop; } { v; }
    }
    ;
  }

  pbind {
    |... args|
    var evt = this.singleEvent;
    evt = evt.collect{
      |v, k|
      v = v.asArray;
      if (v.flat.uniq.size == 1) { v.pop; } { v.pseq; }
    }
    ;
    ^Pchain(Prepetition(), Pbind(*(evt.getPairs ++ args.flat)));
  }
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
