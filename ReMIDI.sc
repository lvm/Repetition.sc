/*
        ReMIDI.sc
        MIDI Stuff, part of the Repetition.sc project
*/

+ Repetition {

  cc {
    |key|
    var cc_n = (
      // named
      amp: 7,
      pan: 10,
      // generic
      cc0: 0,
      cc1: 1,
      cc2: 2,
      cc3: 3,
      cc4: 4,
      cc5: 5,
      cc6: 6,
      cc7: 7,
      cc8: 8,
      cc9: 9,
      cc10: 10,
      cc11: 11,
      cc12: 12,
      cc13: 13,
      cc14: 14,
      cc15: 15,
      cc16: 16,
      cc17: 17,
      cc18: 18,
      cc19: 19,
      cc20: 20,
      cc21: 21,
      cc22: 22,
      cc23: 23,
      cc24: 24,
      cc25: 25,
      cc26: 26,
      cc27: 27,
      cc28: 28,
      cc29: 29,
      cc30: 30,
      cc31: 31,
      cc32: 32,
      cc33: 33,
      cc34: 34,
      cc35: 35,
      cc36: 36,
      cc37: 37,
      cc38: 38,
      cc39: 39,
      cc40: 40,
      cc41: 41,
      cc42: 42,
      cc43: 43,
      cc44: 44,
      cc45: 45,
      cc46: 46,
      cc47: 47,
      cc48: 48,
      cc49: 49,
      cc50: 50,
      cc51: 51,
      cc52: 52,
      cc53: 53,
      cc54: 54,
      cc55: 55,
      cc56: 56,
      cc57: 57,
      cc58: 58,
      cc59: 59,
      cc60: 60,
      cc61: 61,
      cc62: 62,
      cc63: 63,
      cc64: 64,
      cc65: 65,
      cc66: 66,
      cc67: 67,
      cc68: 68,
      cc69: 69,
      cc70: 70,
      cc71: 71,
      cc72: 72,
      cc73: 73,
      cc74: 74,
      cc75: 75,
      cc76: 76,
      cc77: 77,
      cc78: 78,
      cc79: 79,
      cc80: 80,
      cc81: 81,
      cc82: 82,
      cc83: 83,
      cc84: 84,
      cc85: 85,
      cc86: 86,
      cc87: 87,
      cc88: 88,
      cc89: 89,
      cc90: 90,
      cc91: 91,
      cc92: 92,
      cc93: 93,
      cc94: 94,
      cc95: 95,
      cc96: 96,
      cc97: 97,
      cc98: 98,
      cc99: 99,
      cc100: 100,
      cc101: 101,
      cc102: 102,
      cc103: 103,
      cc104: 104,
      cc105: 105,
      cc106: 106,
      cc107: 107,
      cc108: 108,
      cc109: 109,
      cc110: 110,
      cc111: 111,
      cc112: 112,
      cc113: 113,
      cc114: 114,
      cc115: 115,
      cc116: 116,
      cc117: 117,
      cc118: 118,
      cc119: 119,
      cc120: 120,
      cc121: 121,
      cc122: 122,
      cc123: 123,
      cc124: 124,
      cc125: 125,
      cc126: 126,
      cc127: 127,
    );
    var n;

    if( cc_n.at(key).notNil,
      { n = cc_n.at(key) },
      { n = nil }
    );

    ^n;
  }

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

    Event.addEventType(\panic, {
      |server|
      ~type = \midi;
      ~midicmd = \allNotesOff;
      ~midiout = outmidi;
      ~chan = ~chan ?? 9;
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
