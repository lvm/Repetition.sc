/*
        ReMIDI.sc
        MIDI Stuff, part of the Repetition.sc project
*/

+ Repetition {

  midiEventTypes {
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
      ~chan = ~chan ?? 9;
      ~ctlNum = ~ctlNum ?? 23;
      currentEnvironment.play;
    });

    Event.addEventType(\prog, {
      |server|
      ~type = \midi;
      ~midicmd = \program;
      ~midiout = outmidi;
      ~chan = ~chan ?? 9;
      ~ctlNum = ~ctlNum ?? 10;
      currentEnvironment.play;
    });
  }
}
