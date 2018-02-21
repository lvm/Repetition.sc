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
  classvar <outmidi;


  *new {
    |proxyspace|

    if(itself.isNil){
      itself = super.new;
      itself.start(proxyspace);
    }

    ^itself;
  }

  start {
    |proxyspace|
    ps = proxyspace;
    srv = proxyspace.server;

    // "fake" hackish synthdef
    SynthDef(\r, {}).add;
    SynthDef(\rest, {}).add;

    if ("StageLimiter".classExists) { StageLimiter.activate; };
    "-> Repetition Loaded".postln;
  }

  initMIDI {
    |dev, port, latency|
    if (MIDIClient.initialized.not) {
      MIDIClient.init;
    };
    outmidi = MIDIOut.newByName(dev, port).latency = (latency ?? Server.default.latency);
    ^outmidi;
  }

  server {
    ^srv;
  }

  proxySpace {
    ^ps;
  }

}
