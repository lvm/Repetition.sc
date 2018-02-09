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

  createEvent {
    |cb, oct=5, fn=nil|

    var acc, size, dur, time, octave;
    var pattern = this.asString;
    var events = [];
    oct = oct.asInt;
    cb = cb.asSymbol;

    pattern = pattern.split($ ).reject { |x| x.asString.size < 1 };
    size = pattern.size;
    dur = 1/size;
    pattern = pattern.collect(_.maybeSubdivide);
    octave = pattern.collect{ |sub| if (sub.isKindOf(String)) { sub.maybeShiftOctave; } { sub.collect(_.maybeShiftOctave) } }.flat;
    time = pattern.collect{ |sub| if (sub.isKindOf(String)) { dur; } { (dur/sub.size).dup(sub.size); } }.flat;
    acc = pattern.collect{ |sub| if (sub.isKindOf(String)) { sub.maybeAccent; } { sub.collect(_.maybeAccent) } }.flat;
    pattern = pattern.collect{ |sub| if (sub.isKindOf(String)) { sub.maybeCleanUp; } { sub.collect(_.maybeCleanUp) } }.flat;

    pattern.collect {
      |val, idx|
      var evt_oct = octave[idx] + oct;
      val = switch(cb,
        \perc, { val.asGMPerc },
        \degree, { Note(val).midi(evt_oct) },
        \freq, { Note(val).freq(evt_oct) },
        \int, { val.asInt },
        \fn, {
          var self = val;
          if (fn.notNil) { self = fn.value(val, pattern); };
          self;
        },
        \chord, {
          var self = val.asString;
          var ch = Chord.names.reject{ |ch| self.findRegexp(ch.asString++"$").size == 0 }.pop;
          if ( ch.notNil ) {
            Note(self.replace(ch.asString, "").asSymbol).midi(evt_oct) + Chord(ch) ;
          } {
            \rest;
          }
        }
      );

      events = events.add( (accent: acc[idx], dur: time[idx], octave: evt_oct, midinote: val) )
    };

    ^events;
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
    .asSymbol;
  }

  maybeAccent {
    ^if (this.contains("@")) { 0.25 } { 0 }
  }

  maybeShiftOctave {
    var oct = 0;
    if (this.contains(",")) { oct = -1 };
    if (this.contains("'")) { oct = 1 };

    ^oct;
  }

  maybeSubdivide {
    var item = this;

    if (item.contains("+")) {
      item = item.split($+);
    };

    ^item;
  }

  repetitionPattern {
    |cb, oct=5, fn|
    var regexp = "([\\w\\.\\/\\'\\,!?@?\\+?(\\*\d+)? ]+)";

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
    .collect(_.createEvent(cb, oct, fn))
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
  perc { ^this.repetitionPattern(\perc, 0); }
  degree { |oct=5| ^this.repetitionPattern(\degree, oct); }
  freq { |oct=5| ^this.repetitionPattern(\freq, oct); }
  int { |oct=5| ^this.repetitionPattern(\int, oct); }
  chord { |oct=5| ^this.repetitionPattern(\chord, oct); }
  fn{ |fn| ^this.repetitionPattern(\fn, 0, fn); }

}

+ Dictionary {

  // midi
  midichannel { |chan| ^this.blend( ( type:\md, chan:chan) ); }

  // synths
  usingsynth { |synth| ^this.blend( ( type:\note, instrument:synth) ); }

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


}

+ Array {

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

  // functions to apply over a pattern
  mute { ^this.collect(_.merge((amp: 0), { 0 })); }
  stretch { |n| ^this.collect(_.blend( (stretch: n) ) ); }
  fast { |n| ^this.stretch(1/n); }
  slow { |n| ^this.stretch(n); }
  freeze { |idx, times=2|
    var frozen = this.at(idx).dup(times);
    ^this.put(idx, frozen).flat;
  }

  duration { |time=0.25| ^this.collect(_.merge((dur: time), { |a, b| b })); }

  with {
    |... args|
    var dct = ();
    dct.putPairs(args);
    ^this.collect(_.blend(dct) );
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
