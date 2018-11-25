/*
extMethods.sc
External Methods that implement some of the behavior for Repetition.sc
*/

+ String {
  /*
  parseRepetition {
  var regexp = "\\[.*?\\]|\\{.*?\\}|[\\w\\.\\|\\/\\'\\,!?@?\\+?(\\*\d+)?]+";

  ^this
  .asString
  .findRegexp(regexp)
  .collect(_[1])
  .collect(_.stripWhiteSpace)
  .collect(_.replace("/", ""))
  .collect(_.flat)
  .collect(_.asSymbol)
  .collect(_.maybeRepeat)
  .collect(_.maybeSplit)
  .reject{ |x| x.asString.stripWhiteSpace.size < 1 }
  .flat
  ;
  }*/

  parseRepetition {
    // var regexp = "\\[.*?\\]|\\{.*?\\}|\\(.*?\\)|\\w+";
    var regexp = "\\[.*?\\]|\\{.*?\\}|\\(.*?\\)|[\\w+\\|?]+";

    // var regexp = "\\[.*?\\]|\\{.*?\\}|\\(.*?\\)|[\\w\\|\\'\\,!?(\\*\d+)?]+";
    // var regexp = "\\[.*?\\]|\\{.*?\\}|[\\w\\.\\|\\/\\'\\,!?@?\\+?(\\*\d+)?]+";

    ^this
    .asString
    .findRegexp(regexp)
    .collect(_[1])
    .collect(_.stripWhiteSpace)
    .collect(_.replace("/", ""))
    .collect(_.flat)
    .collect{ |str|
      var notes = str, typeof = \ind;
      if (str.contains("|")) { notes = str.split($|); typeof = \grp };
      if (str.contains("[")) { notes = str.replace("[","").replace("]","").split($ ); typeof = \seq };
      if (str.contains("{")) { notes = str.replace("{","").replace("}","").split($ ); typeof = \xrnd };
      if (str.contains("(")) { notes = str.replace("(","").replace(")","").split($ ); typeof = \grp };
      (notes: notes, typeof: typeof);
    }
    .collect{ |grp|
      var notes = grp.at(\notes), typeof = grp.at(\typeof);
      switch(typeof,
        \seq, { notes.collect(_.asReNote).pseq(1); },
        \xrnd, { notes.collect(_.asReNote).pxrand(1); },
        \grp, { notes.collect(_.asReNote); },
        \ind, { notes.asReNote; }
      );
    }

  }

  classExists {
    ^this.asSymbol.classExists;
    // ^Class.allClasses.collect(_.asString).indexOfEqual(this).notNil;
  }

  // requires `Bjorklund` Quark.
  bjorklund { |k=3, n=8, rotate=0|
    var hit = Pseq(this.split($ ), inf).asStream;
    if(k.isKindOf(SimpleNumber)) { k = [k]; };
    if(n.isKindOf(SimpleNumber)) { n = [n]; };
    if (n.size < k.size ) { n = n.dup(k.size).flat };

    ^Pbjorklund(Pseq(k, inf), Pseq(n, inf), inf)
    .asStream
    .nextN(n.sum)
    .rotate(rotate)
    .collect { |p| if (p.asBoolean) { hit.next } { \r } }
    .join(" ")
    ;
  }
  bj { |k=3,n=8,r=0| ^this.bjorklund(k, n, r) }

  maybeCleanUp {
    ^this
    .replace("@", "")
    .replace(",", "")
    .replace("'", "")
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

  shuffle { ^this.split($ ).scramble.join(" "); }

  everyN { |times, callback|
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
  probability { |chance, callback|
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

  binbeat { |notes|
    var stream = Pseq(notes.split($ ), inf).asStream;
    ^this
    .replace("/ ", "")
    .asList
    .reject{ |x| x.asString.stripWhiteSpace.isEmpty }
    .collect { |p| if (p.asString == "1") { stream.next.asString } { "r" } }
    .join(" ")
    ;
  }
  << { |notes| ^this.binbeat(notes); }

  tribeat { |notes|
    var stream = Pseq(notes.split($ ), inf).asStream;
    ^this
    .replace("/ ", "")
    .asList
    .reject{ |x| x.asString.stripWhiteSpace.isEmpty }
    .collect { |p| if (p.asString == "2") { [0,1].choose.asString } { p } }
    .collect { |p| if (p.asString == "1") { stream.next.asString } { "r" } }
    .join(" ")
    ;
  }
  <<< { |notes| ^this.tribeat(notes); }

  lsys { |limit=100|
    var start, each = ();
    this
    .split($|)
    .collect{ |e|
      var kv = e.split($:), dct;
      dct = ((kv[0].asSymbol): (kv[1].asList.asArray.collect(_.asSymbol)));
      each = each.merge(dct, {|a,b| b });
    }
    ;
    start = each.keys().pop()
    ;
    ^Prewrite(start, each, each.keys.size())
    .asStream
    .nextN(limit)
    .flat
    .reject{ |x| x.asString.isEmpty }
    ;
  }

  invert {
    ^this
    .replace("/ ", "")
    .replace("1", "x")
    .replace("0", "1")
    .replace("x", "0")
    ;
  }
  rev {
    ^this
    .replace("/ ", "")
    .split($ )
    .collect(_.stripWhiteSpace)
    .reject{ |x| x == "" }
    .reverse
    .join(" ")
    ;
  }

  // player short
  pbind { |... args|
    var midinote = this.parseRepetition
    ,stut_idx = args.atIdentityHash(\stut)
    ,oct_idx = args.atIdentityHash(\octave)
    ,stut = if (stut_idx > -1, { args.at(stut_idx+1) }, { 1 })
    ,oct = if (oct_idx > -1, { args.at(oct_idx+1) }, { 0 })
    ;
    midinote = if (args.atIdentityHash(\random) > -1, { midinote.pseq }, { midinote.pxrand });
    ^(args ++ [\midinote, Pstutter(stut, midinote + (12 * oct)), \type, \md])
    .playerProxy
    ;
  }

  console { |... args|
    this
    .parseRepetition
    .postln
    ;
    args
    .postln
    ;

    ^nil
  }

}

+ Symbol {

  classExists {
    ^this.asClass.notNil;
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

  maybeSplit { |sep|
    var item = this;

    if (item.isKindOf(String)) {
      item = item.split(sep);
    };

    ^item;
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

+ Event {

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

+ Array {

  playerProxy {
    // ^Pchain(Prepetition(), PbindProxy(*this));
    ^PbindProxy(*this);
  }


  applyIndex {
    ^this
    .collect{ |x, i| x.merge((index: i), {|a,b| b}); };
  }

}

+ SequenceableCollection {

  singleSequence { |... args|
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
  }

  uniq {
    var result = List.new;
    this.do{ |item|
      if (result.indexOfEqual(item).isNil) {
        result.add( item );
      }
    };
    ^result.asArray;
  }

  pseq { |rep = inf, offs = 0| ^Pseq(this, rep, offs); }
  pshuf { |rep = inf| ^Pshuf(this, rep); }
  pshufn { |rep = inf| if(\Pshufn.classExists, { ^Pshufn(this, rep) }, { this.pshuf(rep) }); }
  prand { |rep = inf| ^Prand(this, rep); }
  pxrand { |rep = inf| ^Pxrand(this, rep); }
  pwrand { |weights, rep = inf|
    weights = weights ?? [(1 / this.size) ! this.size].normalizeSum;
    ^Pwrand(this, weights, rep);
  }
  place { |rep = inf, offs = 0| ^Place(this, rep, offs); }
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

+ SimpleNumber {

  // 12 TET
  unison { ^this; }
  minor2nd { ^this * 1.059; }
  major2nd { ^this * 1.122; }
  minor3rd { ^this * 1.189; }
  major3rd { ^this * 1.259; }
  fourth { ^this * 1.334; }
  tritone { ^this * 1.414; }
  fifth { ^this * 1.498; }
  minor6th { ^this * 1.587; }
  major6th { ^this * 1.681; }
  minor7th { ^this * 1.781; }
  major7th { ^this * 1.887; }
  octaveUp { ^this * 2; }
  octaveDown { ^this / 2; }

}

// EXPERIMENTAL SPOOKY STUFF

+ Symbol {

  ndef { |aPbind|
    var self = Ndef(this);
    if (self.isNil) { self.proxyspace.quant = 8; };
    ^if(aPbind.isNil, { self }, { self[0] = aPbind });
  }
  cc { |aPbind| Ndef(this)[65535] = \cc -> aPbind; }

  pdef { |aPbind|
    var self = Pdef(this);
    ^if (aPbind.isNil, {
      self.clear;
    }, {
      if (self.isPlaying.not) { self.quant_(8).play; };
      if(aPbind.isNil, { self }, { Pdef(this, aPbind) });
    });
  }
  pbindef { |... args|
    ([this] ++ args.at(0)).postln;
    ^Pbindef(*([this] ++ args.at(0)));
  }
  <+ { |args| ^this.pbindef(args); }
  << { |aPbind| ^this.pdef(aPbind); }

  clear { ^this.pdef.clear; }
  >> { |meh| ^this.clear; }

  /*
  << { |aPbind|
  if (Ndef(this).isNil) { Ndef(this).proxyspace.quant = 8; };
  ^Ndef(this)[0] = aPbind
  ;
  }

  <+ { |aPbind|
  Ndef(this)[65535] = \cc -> aPbind;
  }

  >> { |meh|
  if (Ndef(this).notNil) { Ndef(this).clear; }
  }
  */

}
