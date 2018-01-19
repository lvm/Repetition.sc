/*
        ReMIDI.sc
        MIDI Stuff, part of the Repetition.sc project
*/

+ Repetition {

  eventTypes {
    |outmidi|

    Event.addEventType(\md, {
      |server|
      ~type = \midi;
      ~midiout = outmidi;
      ~chan = ~chan ?? 9;
      ~amp = ~amp ?? 0.9;
      currentEnvironment.play;
    });

    Event.addEventType(\cc, {
      |server|
      ~type = \midi;
      ~midicmd = \control;
      ~midiout = outmidi;
      ~ctlNum = ~ctlNum ?? 23;
      currentEnvironment.play;
    });
  }

  /*
  * This method will be deprecated soon, has a couple of interesting functions
  * but it's pretty pretty much useless since the same result could be achieved
  * by using synthdefs directly.
  *
  midiSynthDefs {

    var keys, instruments, ccinstruments, noteon, noteoff;
    var ccfuncs;
    var non, noff, ccr;

    ccfuncs = (
      \amp: {|vel| vel.linlin(0, 127, 0.001, 0.99); },
      \sus: {|vel| vel.linlin(0, 127, 0, 0.99); },
      \pan: {|vel| vel.linlin(0, 127, -1, 1); },
      \wave: {|vel| vel },
      \glitch: {|vel| vel },
      \reset: {|vel| vel },
      \fragment: {|vel| vel.linlin(0, 127, 0.01, 0.25); },
      \filter: {|vel| vel },
      \rq: {|vel| vel.linlin(0, 127, 0.01, 2); },
      \lctf: {|vel| vel.linlin(0, 127, 50, 22050); },
      \hctf: {|vel| vel.linlin(0, 127, 50, 22050); },
      \carPartial: {|vel| vel.linlin(0, 127, 0.01, 10); },
      \modPartial: {|vel| vel.linlin(0, 127, 0.01, 10); },
      \index: {|vel| vel },
      \mul: {|vel| vel.linlin(0, 127, 0.01, 0.9); },
      \atk: {|vel| vel.linlin(0, 127, 0.001, 0.5); },
      \detune: {|vel| vel.linlin(0, 127, 0.01, 1);},
      \delay: {|vel| vel.linlin(0, 127, 0, 2);},
      \delaydecay: {|vel| vel.linlin(0, 127, 0, 2);},
      \speed: {|vel|  var speed = vel.linlin(0, 127, 0, 10); if (speed < 1) { speed.asStringPrec(1).asFloat } { speed.round } },
      \mix: {|vel| vel.linlin(0, 127, 0.0, 0.99);},
      \room: {|vel| vel.linlin(0, 127, 0.0, 0.99);},
    );

    SynthDef(\r, {}).add;
    SynthDef(\rest, {}).add;

    SynthDef.new(\sample,
      {
        |
        out = 0, amp = 0.9, gate = 1, pan = 0.5,
        index = 0, speed = 1,
        glitch = 0, reset = 1, fragment = 0.1,
        filter = 0, lctf = 100, hctf = 1000,
        delay = 0, delaydecay = 0,
        mix = 0, room = 0,
        freq
        |
        var env, smp, waves, stutter;
        env = EnvGen.kr(Env.asr, gate, doneAction: 2);
        smp = PlayBuf.ar(2, index, (BufRateScale.ir(index) * speed));

        stutter = [smp, Stutter.ar(smp, reset, fragment)];
        smp = Select.ar(glitch, stutter);

        waves = [smp, RHPF.ar(smp, hctf), RLPF.ar(smp, lctf)];
        smp = Select.ar(filter, waves);

        smp = smp + CombL.ar(smp, delaytime: delay, maxdelaytime: 2, decaytime: delaydecay);
        smp = FreeVerb.ar(smp, mix:mix, room: room, mul:1);
        // smp = Mix(smp * amp) * 0.5;
        smp = Compander.ar(smp, smp, mul:0.8);
        Out.ar(out, Pan2.ar(smp, pan, amp))
    }).add;

    SynthDef(\sc303, {
      |out=0, gate=1, wave=0, lctf=100, hctf=1000, rq=0.5, sus=0.09, dec=1.0, amp=0.75, pan=0.5, freq|
      var  sig, env, filEnv, volEnv, waves;
      env = EnvGen.kr(Env.asr, gate, doneAction: 2);
      volEnv =  EnvGen .ar( Env .new([10e-10, 1, 1, 10e-10], [0.01, sus, dec],  \exp ), gate);
      filEnv =  EnvGen .ar( Env .new([10e-10, 1, 10e-10], [0.01, dec],  \exp ), gate);
      waves = [ Saw .ar(freq, volEnv),  Pulse .ar(freq, 0.5, volEnv)] * env;
      sig = RLPF .ar(  Select .ar(wave, waves), lctf + (filEnv * hctf), rq).dup * amp;
      Out.ar(out, Pan2.ar(sig, pan, amp));
    }, metadata:(credit: "based on http://sccode.org/1-4Wy")).add;

    SynthDef(\scfm, {
      |out, amp=0.9, gate=1, atk=0.001, sus=1, accel=0, carPartial=1, modPartial=1, index=3, mul=0.15, detune=0.1, pan=0, freq|
      var env, mod, car, sig;
      amp = amp * 0.5;
      env = EnvGen.kr(Env.asr, gate, doneAction: 2);
      mod = SinOsc.ar(freq * modPartial * Line.kr(1,1+accel, sus), 0, freq * index * LFNoise1.kr(5.reciprocal).abs);
      car = SinOsc.ar(([freq, freq+detune] * carPartial) + mod,	0, mul);
      sig = car * env;
      OffsetOut.ar(out, Pan2.ar(sig, pan, amp));
    }, metadata: (credit: "@munshkr")).add;

    keys = Array.newClear(128);
    instruments = Array.newClear(4);
    ccinstruments = Array.newClear(4);

    instruments.put(0, \rest);
    instruments.put(1, \sc303);
    instruments.put(2, \scfm);
    instruments.put(3, \sample);

    ccinstruments.put(0, ());
    // sc303
    ccinstruments.put(1, (
      // volume, cc7
      \amp: 0.9,
      // panning, cc10
      \pan: 0.5,
      // sustain, cc10
      \sus: 0.009,
      // wave, cc70
      \wave: 0,
      // resonance, cc71
      \rq: 0.5,
      // low ctf, cc74
      \lctf: 100,
      // hi ctfm cc75
      \hctf: 1000,
    )
    );
    // scfm
    ccinstruments.put(2, (
      // volume, cc7
      \amp: 0.9,
      // panning, cc10
      \pan: 0.5,
      // attack, cc73
      \atk: 0.001,
      // carrier partial, cc12
      \carPartial: 1,
      // modulator partial, cc13
      \modPartial: 1,
      // index, cc14
      \index: 3,
      // carrier signal mul, cc15
      \mul: 0.25,
      // carrier signal detune, cc16
      \detune: 0.1,
    ));
    // samples
    ccinstruments.put(3, (
      // volume, cc7
      \amp: 0.9,
      // panning, cc10
      \pan: 0.5,
      // index, cc14
      \index: 0,
      // wave, cc70
      \filter: 0,
      // low ctf, cc74
      \lctf: 100,
      // hi ctfm cc75
      \hctf: 1000,
      // glitch, cc85
      \glitch: 0,
      // glitch, cc86
      \reset: 1,
      // reset, cc87
      \fragment: 0.1,
      // echo, cc88
      \delay: 0,
      // decay, cc89
      \delaydecay: 0,
      // size, cc90
      \mix: 0,
      // speed, cc91
      \room: 0,
      // speed, cc95
      \speed: 1,
    ));

    // handle noteOn
    non = NoteOnResponder({
      |src, ch, note, vel|
      var node, args;

      if ( instruments.at(ch).notNil) {
        node = keys.at(note);
        if (node.notNil, {
          node.release;
          keys.put(note, nil);
        });
        args = [\freq, note.midicps] ++ ccinstruments.at(ch).getPairs();
        node = Synth(instruments.at(ch), args);
        keys.put(note, node);
      }
    },
    nil, // src
    nil, // chan
    nil, // note
    nil // vel
    );

    // handle noteOff
    noff = NoteOffResponder({
      |src, ch, note, vel|
      var node;
      node = keys.at(ch);
      if (node.notNil, {
        node.release;
        keys.put(note, nil);
      });
    },
    nil, // src
    nil, // chan
    nil, // note
    nil // vel
    );

    // handle CC
    ccr = CCResponder({
      |src, ch, num, value|
      var key = this.cc(num).asSymbol;
      var func = ccfuncs.at(key);
      ccinstruments.at(ch)[key] = func.(value);
    },
    nil, // src
    nil, // chan
    nil, // cc
    nil // val
    );

  }

  cc {
    |key=nil|
    var ret, cclist;

    cclist = (
      \pitch1: 1,
      \amp: 7,
      \pan: 10,
      \carPartial: 12,
      \modPartial: 13,
      \index: 14,
      \mul: 15,
      \lfo: 16,
      \sus: 69,
      \wave: 70,
      \filter: 70,
      \rq: 71,
      \atk: 73,
      \lctf: 74,
      \hctf: 75,
      \glitch: 85,
      \reset: 86,
      \fragment: 87,
      \delay: 88,
      \delaydecay: 89,
      \mix: 90,
      \room: 91,
      \detune: 94,
      \speed: 95,
    );

    if (key.isNil) {
      ret = cclist.keys();
    } {
      if (key.isInteger) {
        ret = cclist.findKeyForValue(key).asSymbol;
      } {
        ret = cclist[key];
      }
    }

    ^ret;
  }
  */

  on { |chan  note vel=127| mout.noteOn(chan, note: note, veloc:vel); }
  off { |chan| mout.allNotesOff(chan); }

}