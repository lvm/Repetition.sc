/*
        Repetition

        (c)opyleft 2017 by Mauro <mauro@sdf.org>
        http://cyberpunk.com.ar/

        *Heavily* inspired by TidalCycles. Consider this a (tiny) dialect that implements some of its features.
*/

Repetition {
  classvar itself, <srv, <ps, <outmidi, <outmidicc, <samples;


  *new { |proxyspace|

    if(itself.isNil){
      itself = super.new;
      itself.start(proxyspace);
    }

    ^itself;
  }

  *initClass {
    samples = (); // Dictionary.new;
  }

  start { |proxyspace|
    ps = proxyspace;
    if (proxyspace.isNil, {
      srv = Server.default;
    }, {
      srv = proxyspace.server;
    });

    // "fake" hackish synthdef
    SynthDef(\r, {}).add;
    SynthDef(\rest, {}).add;

    if ("StageLimiter".classExists) { StageLimiter.activate; };
    "-> Repetition Loaded".postln;
  }

  initMIDI { |dev, port, latency|
    if (MIDIClient.initialized.not) {
      MIDIClient.init;
    };
    ^MIDIOut.newByName(dev, port).latency = (latency ?? Server.default.latency);
  }

  midiOut { |dev, port, latency|
    outmidi = this.initMIDI(dev, port, latency);
    ^outmidi;
  }

  midiOutCC { |dev, port, latency|
    outmidicc = this.initMIDI(dev, port, latency);
    ^outmidicc;
  }

  server {
    ^srv;
  }

  proxySpace {
    ^ps;
  }

}
