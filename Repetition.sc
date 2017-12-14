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

    // "fake" hackish synthdef
    SynthDef(\r, {}).add;
    SynthDef(\rest, {}).add;

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

}
