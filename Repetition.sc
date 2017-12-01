/*
        Repetition

        (c)opyleft 2017 by Mauro <mauro@sdf.org>
        http://cyberpunk.com.ar/

        *Heavily* inspired by TidalCycles. Consider this a (tiny) dialect that implements some of its features.
*/

Repetition {
  classvar itself;
  classvar <srv;
  classvar <ps;


  *new {
    |default_tempo=2, quant=1| // 120 BPM.

    if(itself.isNil){
      itself = super.new;
      itself.start(default_tempo, quant);
    }

    ^itself;
  }

  start {
    |default_tempo, quant|

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
    ps.quant = quant;

    "-> Repetition Loaded".postln;
  }

  initSuperDirt {
    var dirt;
    if ("StageLimiter".classExists) { StageLimiter.activate; };
    dirt = SuperDirt(2, srv);
    dirt.start(57120, (0!8));
    SuperDirt.default = dirt;

    ^dirt;
  }

  initMIDI {
    |dev, port, latency|
    var mout;
    if (MIDIClient.initialized.not) {
      MIDIClient.init;
    }
    ^MIDIOut.newByName(dev, port).latency = (latency ?? Server.default.latency);
  }

  loadSynths {
    // "fake" hackish synthdef
    SynthDef(\rest, { |out| Silent.ar(0); }).add;

    SynthDef(\bassy, {
      |out=0, amp=0.9, gate=1, lctf=1200, hctf=200, rq=0.5, pan=0, freq|
      var sig, env;
      amp = amp*0.9;
      // env = EnvGen.ar(Env.perc(attackTime: atk, releaseTime: rel, level: amp), timeScale: sus, doneAction: 2);
      env = EnvGen.kr(Env.asr, gate, doneAction: 2);
      sig = BHPF.ar(RLPF.ar(SawDPW.ar(freq,1), lctf, rq), freq:hctf);
      sig = sig * env;
      OffsetOut.ar(out, Pan2.ar(sig, pan, amp));
    }, metadata: (credit: "http://github.com/lvm/balc")).add;

    SynthDef(\fm, {
      |out, amp=0.9, gate=1, atk=0.001, sus=1, accel, carPartial=1, modPartial=1, index=3, mul=0.25, detune=0.1, pan=0, freq|
      var env, mod, car, sig;
      amp = amp * 0.9;
      // env = EnvGen.ar(Env.perc(atk, 0.999, 1, -3), timeScale: sus / 2, doneAction:2);
      env = EnvGen.kr(Env.asr, gate, doneAction: 2);
      mod = SinOsc.ar(freq * modPartial * Line.kr(1,1+accel, sus), 0, freq * index * LFNoise1.kr(5.reciprocal).abs);
      car = SinOsc.ar(([freq, freq+detune] * carPartial) + mod,	0, mul);
      sig = car * env;
      OffsetOut.ar(out, Pan2.ar(sig, pan, amp));
    }, metadata: (credit: "@munshkr")).add;

  }

  setupMIDI {
    var keys, instruments, instcc, noteon, noteoff;
    // MIDIIn.connectAll;
    this.loadSynths;

    keys = Array.newClear(128);
    instruments = Array.newClear(3);
    instcc = Array.newClear(3);

    instruments.put(0, \rest);
    instruments.put(1, \bassy);
    instruments.put(2, \fm);

    instcc.put(0, ());
    instcc.put(1, (\lctf: 86.midicps, \hctf: 55.midicps, \rq: [63.5].rangeMidi.pop));
    instcc.put(2, (\carPartial: [127].rangeMidi, \modPartial: [127].rangeMidi, \detune: [12.7].rangeMidi));

    // handle noteon
    MIDIdef.noteOn(\on, {
      |val, num, chan, src|
      var node, cc, args;

      if ( instruments.at(chan).notNil) {
        cc = instcc.at(chan);
        node = keys.at(num);
        if (node.notNil, {
          node.release;
          keys.put(num, nil);
        });

        if (chan == 1) {
          args = [\freq, num.midicps, \lctf, cc[\lctf], \hctf, cc[\hctf], \rq, cc[\rq]];
        };

        if (chan == 2) {
          args = [\freq, num.midicps, \carPartial, cc[\carPartial], \modPartial, cc[\modPartial], \detune, cc[\detune]];
        };

        node = Synth(instruments.at(chan), args);
        keys.put(num, node);
      }
    });

    // handle noteoff
    MIDIdef.noteOff(\off, {
      |val, num, chan, src|
      var node;
      node = keys.at(num);
      if (node.notNil, {
        node.release;
        keys.put(num, nil);
      });
    });

    // handle control messages
    // bassy
    // lctf, hctf, rq
    MIDIdef.cc(\ctrlbass1, { |val, num| instcc.at(1)[\lctf] = val.midicps; }, 20, 1);
    MIDIdef.cc(\ctrlbass2, { |val, num| instcc.at(1)[\hctf] = val.midicps; }, 21, 1);
    MIDIdef.cc(\ctrlbass2, { |val, num| instcc.at(1)[\rq] = [val].rangeMidi.pop.clip(0.05, 1) }, 22, 1);

    // fm
    // carPartial, modPartial, detune,
    MIDIdef.cc(\ctrlfm1, { |val, num| instcc.at(2)[\carPartial] = [val].rangeMidi; }, 23, 2);
    MIDIdef.cc(\ctrlfm2, { |val, num| instcc.at(2)[\modPartial] = [val].rangeMidi; }, 24, 2);
    MIDIdef.cc(\ctrlfm3, { |val, num| instcc.at(2)[\detune] = [val].rangeMidi; }, 26, 2);
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
        var to = evt[\to] ?? \midinote;
        var isPerc = false;
        // var isSynth = false;

        //if (evt[\octave].isNil) { }
        evt[\octave] = (evt[\octave] ?? 5) + evt[\oct].at(idx);

        if (evt[\stut].isNil) {
          evt[\stut] = 1;
        };

        if (evt[\cb].notNil) {
          current = current.applyCallback(evt[\cb], evt);
          isPerc = evt[\cb].asSymbol == \asPerc;
          // isSynth = evt[\cb].asSymbol == \asSynth;
        };

        if (evt[\type] == \dirt) {
          evt[\gain] = evt[\gain] ?? 0.9;
          evt[\gain] = evt[\gain] + evt[\accent].at(idx);
        } {
          evt[\amp] = evt[\amp] + evt[\accent].at(idx);
        };

        evt[to] = current + (if (((to.asSymbol == \midinote) || (to.asSymbol == \control) )&& (isPerc.asBoolean == false)) { 12*evt[\octave] } { 0 });
        evt[\dur] = evt[\time].at(idx);

/*
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
*/

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
        sym = [sym.asFloat].midiRange;
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
    // var regexp = "([\\w\\'\\,!?@?\\+?(\\*\d+)? ]+)";
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

}

+ Dictionary {

  asPbind {
    |dict|
    ^Pchain(Prepetition(), Pbind(*this.blend(dict).getPairs));
  }

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

}
