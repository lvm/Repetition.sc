/*
        extMethods.sc
        External Methods that implement some of the behavior for Repetition.sc
*/

+ Symbol {

  // Requires `PercSymbol`;
  asPerc {
    ^this
    .collect {
      |item|
      if(item.isEmpty) {
        \rest;
      } {
        item;
      }
    }
    .collect {
      |item|
      if (item.isRest) {
        item;
      } {
        item.asGMPerc;
      }
    }
  }

  applyCallback {
    |cb evt|
    var sym = this;
    var chromatic = (\c: 0, \cs: 1, \db: 1, \d: 2, \ds:3, \eb: 3, \e: 4, \f:5, \fs:6, \gb: 6, \g:7, \gs:8, \ab: 8, \a:9, \as:10, \bb: 10, \b:11);
    var octave = evt[\octave];

    switch (cb.asSymbol,
      \asInt, {
        sym = sym.asInt;
      },
      // Requires `ChordProg`
      \asChord, {
        if (sym.asSymbol.isRest) {
          sym = \rest;
        } {
          sym = [sym].asChord(evt[\chord] ?? \maj).flat;
        }
      },
      \asSemitone, {
        var st = chromatic[sym.asSymbol];
        if (st.isNil) {
          sym = \rest;
        } {
          sym = st;
        }
      },
      \asFreq, {
        var st = chromatic[sym.asSymbol];
        sym = ((st ?? 0) + (12 * octave)).midicps;
      },
      \asCC, {
        sym = sym.asFloat.midirange;
      },
      // Requires `PercSymbol`
      \asPerc, {
        sym = [sym].asGMPerc;
      },
      \asFn, {
        if (evt[\fn].notNil) {
          sym = evt[\fn].value(sym, evt);
        }
      }
    );

    ^sym;
  }

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

  distributeInTime {
    var acc, size, dur, time, octave;
    var pattern = this.asString;

    pattern = pattern.split($ ).reject { |x| x.asString.size < 1 };
    size = pattern.size;
    dur = 1/size;
    pattern = pattern.collect(_.maybeSubdivide);
    octave = pattern.collect{ |sub| if (sub.isKindOf(String)) { sub.maybeShiftOctave; } { sub.collect(_.maybeShiftOctave) } };
    time = pattern.collect{ |sub| if (sub.isKindOf(String)) { dur; } { (dur/sub.size).dup(sub.size); } };
    acc = pattern.collect{ |sub| if (sub.isKindOf(String)) { sub.maybeAccent; } { sub.collect(_.maybeAccent) } };
    pattern = pattern.collect{ |sub| if (sub.isKindOf(String)) { sub.maybeCleanUp; } { sub.collect(_.maybeCleanUp) } };

    ^(
      accent: acc.flat,
      time: time.flat,
      oct: octave.flat,
      pattern: pattern.flat,
    )
  }

}

+ String {

  classExists {
    ^Class.allClasses.collect(_.asString).indexOfEqual(this).notNil;
  }

  // requires `Bjorklund` Quark.
  asBjorklund {
    |k, n, rotate=0|
    ^Bjorklund(k, n)
    .rotate(rotate)
    .collect { |p| if (p.asBoolean) { this.replace(" ", "+").asSymbol } { \r } }
    .flat.join(" ")
    ;
  }
  bj { |k n rot=0| ^this.asBjorklund(k, n, rot); }

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

  parseRepetitionPattern {
    var regexp = "([\\w\\.\\'\\,!?@?\\+?(\\*\d+)? ]+)";

    ^this
    .asString
    .findRegexp(regexp)
    .collect(_[1])
    .collect(_.stripWhiteSpace)
    .uniq
    .collect(_.flat)
    .collect(_.asSymbol)
    .collect(_.maybeRepeat)
    .collect(_.maybeSplit)
    .collect(_.distributeInTime)
    ;
  }

  asRepetition {
    |pbd|
    ^this.parseRepetitionPattern.at(0).asPbind(pbd);
  }
  rp { |pbd| ^this.asRepetition(pbd); }

  asGroupRepetition {
    |pbd rep=inf|
    ^Ppar(this.parseRepetitionPattern.collect{ |pat| pat.asPbind(pbd); }.asArray, rep);
  }
  grp { |pbd rep=inf| ^this.asGroupRepetition(pbd, rep); }

}

+ Dictionary {

  asPbind {
    |dict|
    var pbindcc, cc = dict[\cc], pchain = Pchain(Prepetition(), Pbind(*this.blend(dict).getPairs));

    if (cc.notNil) {
      pbindcc = cc
      .asDict.keys()
      .collect {
        |key|
        var ctrl = cc[cc.indexOfEqual(key)+1];
        Pbind(\type, \cc, \chan, dict[\chan], \dur, dict[\ccdur] ?? 1, \ctlNum, key, \control, ctrl);
      }
      .asArray
      ;

      ^Ppar(pbindcc ++ pchain.asArray);
    } {
      ^pchain;
    }

  }
  pb { |dict| ^this.asPbind(dict); }

}

+ Array {

  drawPattern {
    ^this.collect{ |each| each.pattern.collect{ |p| if (p.isRest) { "." } { "x" } }.join(" ") }.join("\n");
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

  asPseq {
    |rep = inf, offs = 0|
    ^Pseq(this, rep, offs);
  }
  ps { |rep=inf offs=0| ^this.asPseq(rep, offs) }

  asPshuf {
    |rep = inf|
    ^Pshuf(this, rep);
  }
  psh { |rep=inf| ^this.asPshuf(rep) }

  asPrand {
    |rep = inf|
    ^Prand(this, rep);
  }
  pr { |rep=inf| ^this.asPrand(rep) }

  asPxrand {
    |rep = inf|
    ^Pxrand(this, rep);
  }
  px { |rep=inf| ^this.asPxrand(rep) }

  asPwrand {
    |weights, rep = inf|
    weights = weights ?? [(1 / this.size) ! this.size].normalizeSum;
    ^Pwrand(this, weights, rep);
  }
  pw { |weights rep=inf| ^this.asPwrand(weights, rep) }


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