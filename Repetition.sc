/*
        Repetition

        (c)opyleft 2017 by Mauro <mauro@sdf.org>
        http://cyberpunk.com.ar/

        *Heavily* inspired by TidalCycles. Consider this a (tiny) dialect that implements some of its features.

        So far, i've implemented only these possibilities:
        * Polyrhythms: "a | b"
        * Groups: "a+b"
        * Accents: "a@"
        * Repetition: "a!"
        * Multiplication: "a*N" (N -> Int)

        All of this is "chainable".
*/

Repetition {
  classvar itself;
  classvar <srv;
  classvar <ps;


  *new {
    |default_tempo=2| // 120 BPM.

    if(itself.isNil){
      itself = super.new;
      itself.start(default_tempo);
    }

    ^itself;
  }

  start {
    |default_tempo|

		srv = Server.default;
    srv.options.numBuffers = 1024 * 64;
    srv.options.memSize = 8192 * 16;
    srv.options.maxNodes = 1024 * 32;
    srv.options.sampleRate = 44100;
    srv.options.numOutputBusChannels = 2;
    srv.options.numInputBusChannels = 2;
    srv.boot();

    //start proxyspace
    ps = ProxySpace.push(srv);
    ps.makeTempoClock;
    ps.clock.tempo = default_tempo;

    "-> Repetition Loaded".postln;
  }

  initSuperDirt {
    var dirt;
    if ("StageLimiter".classExists) { StageLimiter.activate; };
    dirt = SuperDirt(2, srv);
    dirt.start(57120, (0!8));
    SuperDirt.default = dirt;

    // "fake" hackish synthdef for SuperDirt
    SynthDef(\rest, { |out| Silent.ar(0); }).add;

    ^dirt;
  }

  initMIDI {
    |dev, port|
    var mout;
    if (MIDIClient.initialized.not) {
      MIDIClient.init;
    }
    ^MIDIOut.newByName(dev, port);
  }

}


Prepetition {
  *new {
    ^Prout({
      |evt|
      var idx = 0;
      var len = evt[\pattern].size;

      while { evt.notNil } {
        var current = evt[\pattern].at(idx).asSymbol;
        var isPerc = false;
        var isSynth = false;

        if (evt[\cb].notNil) {
          current = current.applyCallback(evt[\cb], evt);
          isPerc = evt[\cb].asSymbol == \asPerc;
          isSynth = evt[\cb].asSymbol == \asSynth;
        };

        if (evt[\octave].isNil) {
          evt[\octave] = 5;
        };

        if (evt[\stut].isNil) {
          evt[\stut] = 1;
        };

        evt[\dur] = evt[\time].at(idx);
        evt[\amp] = evt[\amp] + evt[\accent].at(idx);

        if ((evt[\type].asSymbol == \midi) || (evt[\type].asSymbol == \md)) {
          evt[\midinote] = current + (if(isPerc.asBoolean == false) { 12*evt[\octave] } { 0 });
        } {
          if (evt[\type].asSymbol == \dirt) {
            if (isSynth.asBoolean) {
              evt[\s] = current;
            } {
              evt[\n] = current;
            }
          } {
            evt[\note] = current;
          }
        };

        if (idx+1 < len) {
          idx = idx + 1;
        } {
          idx = 0;
        };

        evt = evt.yield;
      }
    }).stutter(Pkey(\stut));
  }
}


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
      // Requires `PercSymbol`
      \asPerc, {
        sym = [sym].asGMPerc;
      },
      \asFn, {
        if (evt[\fn].notNil) {
          sym = evt[\fn].value(sym);
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
    var acc, size, dur, time;
    var pattern = this.asString;

    pattern = pattern.split($ ).reject { |x| x.asString.size < 1 };
    size = pattern.size;
    dur = 1/size;
    pattern = pattern.collect(_.maybeSubdivide);
    time = pattern.collect{ |sub| if (sub.isKindOf(String)) { dur; } { (dur/sub.size).dup(sub.size); } };
    acc = pattern.collect{ |sub| if (sub.isKindOf(String)) { sub.maybeAccent; } { sub.collect(_.maybeAccent) } };
    pattern = pattern.collect{ |sub| if (sub.isKindOf(String)) { sub.maybeCleanUp; } { sub.collect(_.maybeCleanUp) } };

    ^(
      accent: acc.flat,
      time: time.flat,
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

  maybeCleanUp {
    ^this.replace("@", "").asSymbol;
  }

  maybeAccent {
    ^if (this.contains("@")) { 0.25 } { 0 }
  }

  maybeSubdivide {
    var item = this;

    if (item.contains("+")) {
      item = item.split($+);
    };

    ^item;
  }

  parseRepetitionPattern {
    var regexp = "([\\w!?@?\\+?(\\*\d+)? ]+)";

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

}

+ Dictionary {

  asPbind {
    |dict|
    ^Pchain(Prepetition(), Pbind(*this.blend(dict).getPairs));
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

}
