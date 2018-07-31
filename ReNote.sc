/*
        ReNote.sc
        Notes, Chords, Progressions and Scale.chordProgression for SuperCollider as part of Repetition.sc
*/

ReNote {
  classvar twelve, solfa;
  var <note;

  *new { | n = \c |
    if (n.isKindOf(Symbol).not) { Error("Please use Note(name.asSymbol) instead.").throw };
    ^super.new.init(n);
  }

  *initClass {
    // twelve = (\c: 0, \cs: 1, \db: 1, \d: 2, \ds:3, \eb: 3, \e: 4, \f:5, \fs:6, \gb: 6, \g:7, \gs:8, \ab: 8, \a:9, \as:10, \bb: 10, \b:11);
    twelve = (\c: 0, \cs: 1, \d: 2, \ds:3, \e: 4, \f:5, \fs:6, \g:7, \gs:8, \a:9, \as:10, \b:11);
    solfa = (\do: 0, \dos: 1, \re: 2, \res:3, \mi: 4, \fa:5, \fas:6, \sol:7, \sols:8, \la:9, \las:10, \si:11);
  }

  *theTwelve {
    ^twelve;
  }

  *names {
    ^twelve.keys.asArray;
  }

  init {
    |n|
    note = n;
  }

  degree {
    var deg = twelve.at(note.asString.toLower.asSymbol); // ugh
    if (deg.notNil) {
      ^deg;
    } {
      ^\rest; // failsafe
    }
  }

  abc {
    ^note;
  }

  solfege {
    ^solfa.findKeyForValue(this.degree.fold(0,11));
  }

  midi {
    |octave=5|
    ^this.degree+(12*octave);
  }

  freq {
    |octave=5|
    ^this.midi(octave).midicps;
  }

  circleOf5th {
    ^(this.degree + ((0..11) * 7 % 12)).collect(_.toABC).collect(ReNote(_));
  }

}

ReChord {
  classvar <chords, <progs;
  var <chord;

  *new { | ch = \one |
    if(ch.isKindOf(Symbol).not) { Error("Please use Chord(name.asSymbol) instead.").throw };
    ^super.new.init(ch);
  }

  *initClass {
    chords = (
      \one: [0],
      \maj: [0, 4, 7],
      \min: [0, 3, 7],
      \dim: [0, 3, 6],
      \aug: [0, 4, 8],
      \dim7: [0, 3, 6, 9],
      \five: [0, 7],
      \dom7: [0, 4, 7, 10],
      \maj7: [0, 4, 7, 11],
      \m7: [0, 3, 7, 10],
      \mMaj7: [0, 3, 7, 11],
      \sus4: [0, 5, 7],
      \sus2: [0, 2, 7],
      \six: [0, 4, 7, 9],
      \m6: [0, 3, 7, 9],
      \nine: [0, 4, 7, 10, 14],
      \m9: [0, 3, 7, 10, 14],
      \maj9: [0, 4, 7, 11, 14],
      \mMaj9: [0, 3, 7, 11, 14],
      \eleven: [0, 4, 7, 10, 14, 17],
      \m11: [0, 3, 7, 10, 14, 17],
      \maj11: [0, 4, 7, 11, 14, 17],
      \mMaj11: [0, 3, 7, 11, 14, 17],
      \thirteen: [0, 4, 7, 10, 14, 21],
      \m13: [0, 3, 7, 10, 14, 21],
      \maj13: [0, 4, 7, 11, 14, 21],
      \mMaj13: [0, 3, 7, 11, 14, 21],
      \add9: [0, 4, 7, 14],
      \madd9: [0, 3, 7, 14],
      \sixadd9: [0, 4, 7, 9, 14],
      \m6add9: [0, 3, 7, 9, 14],
      \add11: [0, 4, 7, 10, 17],
      \majAdd11: [0, 4, 7, 11, 17],
      \mAdd11: [0, 3, 7, 10, 17],
      \mMajAdd11: [0, 3, 7, 11, 17],
      \add13: [0, 4, 7, 10, 21],
      \majAdd13: [0, 4, 7, 11, 21],
      \mAdd13: [0, 3, 7, 10, 21],
      \mMajAdd13: [0, 3, 7, 11, 21],
      \sevenFlat5: [0, 4, 6, 10],
      \sevenSharp5: [0, 4, 8, 10],
      \sevenFlat9: [0, 4, 7, 10, 13],
      \sevenSharp9: [0, 4, 7, 10, 15],
      \sevenSharp5Flat9: [0, 4, 8, 10, 13],
      \m7Flat5: [0, 3, 6, 10],
      \m7dim: [0, 3, 6, 9],
      \m7Sharp5: [0, 3, 8, 10],
      \m7Flat9: [0, 3, 7, 10, 13],
      \nineSharp11: [0, 4, 7, 10, 14, 18],
      \nineFlat13: [0, 4, 7, 10, 14, 20],
      \sixSus4: [0, 5, 7, 9],
      \sevenSus4: [0, 5, 7, 10],
      \maj7Sus4: [0, 5, 7, 11],
      \nineSus4: [0, 5, 7, 10, 14],
      \maj9Sus4: [0, 5, 7, 11, 14]
    );
  }

  *names {
    ^chords.keys.asArray;
  }

  init {
    |ch|
    chord = ch;
    ^if (chords.at(ch).notNil) { chords.at(ch) } { [0] };
  }

}


ReProgression {
  classvar <progs;
  var <prog;

  *new { | prg = \eleven |
    if(prg.isKindOf(Symbol).not) { Error("Please use Progression(name.asSymbol) instead.").throw };
    ^super.new.init(prg);
  }

  *initClass {
    progs = (
      \eleven: [1,0,1,4],
      \elevenb: [1,4,1,0],
      \elevenc: [1,4,0],
      \elevend: [5,1,4,0],
      \sad: [0,3,4,4],
      \ballad: [0,0,3,5],
      \balladb: [0,3,5,4],
      \rockplus: [0,3,0,4],
      \rebel: [3,4,3],
      \nrg: [0,2,3,5],
      \fifties: [0,5,3,4],
      \creepy: [0,5,3,4],
      \creepyb: [0,5,1,4],
      \rock: [0,3,4],
      \gral: [0,3,4,0],
      \gralb: [0,3,1,4],
      \blues: [0,3,0,4,0],
      \pop: [0,4,5,3],
      \roll: [0,3,4,3],
      \unresolved: [3,0,3,4],
      \bluesplus: [0,0,0,0,3,3,0,0,4,3,0,0,],
    );
  }

  *names {
    ^progs.keys.asArray;
  }

  init {
    |prg|
    prog = prg;
    ^if (progs.at(prg).notNil) { progs.at(prg) } { [0] };
  }

}


+ Integer {
  toABC {
    ^ReNote.theTwelve.findKeyForValue(this.fold(0,11));
  }
}


+ String {
  toNote {
    ^ReNote(this.toLower.asSymbol);
  }
}

// SuperHelpers for ABC / Percussion notation parsing.

+ Scale {
  chords {
    var chords = [];
    var major = [\maj, \min, \min, \maj, \maj, \min, \dim];
    var minor = [\min, \dim, \maj, \min, \min, \maj, \maj];
    var harMinor = [\min, \dim, \aug, \min, \maj, \maj, \dim];
    var melMinor = [\min, \min, \aug, \maj, \maj, \dim, \dim];
    var dorian = [\min, \min, \maj, \maj, \min, \dim, \maj];
    var phrygian = [\min, \maj, \maj, \min, \dim, \maj, \min];
    var lydian = [\maj, \maj, \min, \dim, \maj, \min, \min];
    var mixolydian = [\maj, \min, \dim, \maj, \min, \min, \maj];
    var locrian = [\dim, \maj, \min, \min, \maj, \maj, \min];

    if (this.name == "Major") { chords = major; };
    if (this.name == "Natural Minor") { chords = minor; };
    if (this.name == "Harmonic Minor") { chords = harMinor; };
    if (this.name == "Melodic Minor") { chords = melMinor; };
    if (this.name == "Dorian") { chords = dorian; };
    if (this.name == "Phrygian") { chords = phrygian; };
    if (this.name == "Lydian") { chords = lydian; };
    if (this.name == "Mixolydian") { chords = mixolydian; };
    if (this.name == "Locrian") { chords = locrian; };

    ^(this.degrees + chords.collect(ReChord(_)));
  }

  chordProgression {
    |prog_name = \eleven|
    var progression = ReProgression(prog_name);
    var chords = this.chords;

    ^Array.fill(progression.size, { |i| chords[progression[i]] });
  }
}

+ SimpleNumber {

  abc {
    var note, semitone, octave, chromatic;
    chromatic = (\c: 0, \cs: 1, \d: 2, \ds:3, \e: 4, \f:5, \fs:6, \g:7, \gs:8, \a:9, \as:10, \b:11);
    note = (this + 0.5).asInt;
    semitone = note % 12;
    octave = (note / 12).asInteger;

    ^"%%".format(chromatic.findKeyForValue(semitone), octave);
  }

  percussion {
    var perc = (
      \bd: 36, // Bass Drum
      \sd: 38, // Snare Drum
      \lt: 45, // Low Tom
      \lc: 64, // Low Conga
      \mt: 48, // (high) Mid Tom
      \mc: 63, // Mid Conga
      \ht: 50, // High Tom
      \hc: 62, // High Conga
      \cl: 75, // CLaves
      \rs: 37, // RimShot
      \ma: 70, // MAracas
      \cp: 39, // hand ClaP
      \cb: 56, // CowBell
      \cy: 52, // CYmbal
      \oh: 46, // Open Hi-hat
      \ch: 42, // Closed Hi-hat
    );

    ^perc.findKeyForValue(this);
  }

}

+ String {

  midi {
    |octave=5|
    var whites = (\c: 0, \d: 2, \e: 4, \f:5, \g:7, \a:9, \b:11);
    var note = this.toLower;
    var tone = whites.at(note.at(0).asSymbol);

    if ("[a-g]".matchRegexp(note.at(0).asString).not) {
      ^nil;
    };

    if ("[0-9]".matchRegexp(note.reverse.at(0).asString)) {
      octave = note.reverse.at(0);
      note = note.replace(octave, "");
    };
    if (octave.isInteger.not) { octave = octave.digit; };

    if (".(b|s)".matchRegexp(note)) {
      if ("s$".matchRegexp(note)) {
        tone = tone + 1;
      };
      if ("b$".matchRegexp(note)) {
        tone = tone - 1;
      }
    };

    ^(tone + (12 * octave));
  }

  percussion {
    var perc = (
      \bd: 36, // Bass Drum
      \sd: 38, // Snare Drum
      \lt: 45, // Low Tom
      \lc: 64, // Low Conga
      \mt: 48, // (high) Mid Tom
      \mc: 63, // Mid Conga
      \ht: 50, // High Tom
      \hc: 62, // High Conga
      \cl: 75, // CLaves
      \rs: 37, // RimShot
      \ma: 70, // MAracas
      \cp: 39, // hand ClaP
      \cb: 56, // CowBell
      \cy: 52, // CYmbal
      \oh: 46, // Open Hi-hat
      \ch: 42, // Closed Hi-hat
    );

    ^perc.at(this.asSymbol);
  }

  chord {
    |octave=5|
    var chords = (
      \one: [0],
      \maj: [0, 4, 7],
      \min: [0, 3, 7],
      \dim: [0, 3, 6],
      \aug: [0, 4, 8],
      \dim7: [0, 3, 6, 9],
      \five: [0, 7],
      \dom7: [0, 4, 7, 10],
      \maj7: [0, 4, 7, 11],
      \m7: [0, 3, 7, 10],
      \mMaj7: [0, 3, 7, 11],
      \sus4: [0, 5, 7],
      \sus2: [0, 2, 7],
      \six: [0, 4, 7, 9],
      \m6: [0, 3, 7, 9],
      \nine: [0, 4, 7, 10, 14],
      \m9: [0, 3, 7, 10, 14],
      \maj9: [0, 4, 7, 11, 14],
      \mMaj9: [0, 3, 7, 11, 14],
      \eleven: [0, 4, 7, 10, 14, 17],
      \m11: [0, 3, 7, 10, 14, 17],
      \maj11: [0, 4, 7, 11, 14, 17],
      \mMaj11: [0, 3, 7, 11, 14, 17],
      \thirteen: [0, 4, 7, 10, 14, 21],
      \m13: [0, 3, 7, 10, 14, 21],
      \maj13: [0, 4, 7, 11, 14, 21],
      \mMaj13: [0, 3, 7, 11, 14, 21],
      \add9: [0, 4, 7, 14],
      \madd9: [0, 3, 7, 14],
      \sixadd9: [0, 4, 7, 9, 14],
      \m6add9: [0, 3, 7, 9, 14],
      \add11: [0, 4, 7, 10, 17],
      \majAdd11: [0, 4, 7, 11, 17],
      \mAdd11: [0, 3, 7, 10, 17],
      \mMajAdd11: [0, 3, 7, 11, 17],
      \add13: [0, 4, 7, 10, 21],
      \majAdd13: [0, 4, 7, 11, 21],
      \mAdd13: [0, 3, 7, 10, 21],
      \mMajAdd13: [0, 3, 7, 11, 21],
      \sevenFlat5: [0, 4, 6, 10],
      \sevenSharp5: [0, 4, 8, 10],
      \sevenFlat9: [0, 4, 7, 10, 13],
      \sevenSharp9: [0, 4, 7, 10, 15],
      \sevenSharp5Flat9: [0, 4, 8, 10, 13],
      \m7Flat5: [0, 3, 6, 10],
      \m7dim: [0, 3, 6, 9],
      \m7Sharp5: [0, 3, 8, 10],
      \m7Flat9: [0, 3, 7, 10, 13],
      \nineSharp11: [0, 4, 7, 10, 14, 18],
      \nineFlat13: [0, 4, 7, 10, 14, 20],
      \sixSus4: [0, 5, 7, 9],
      \sevenSus4: [0, 5, 7, 10],
      \maj7Sus4: [0, 5, 7, 11],
      \nineSus4: [0, 5, 7, 10, 14],
      \maj9Sus4: [0, 5, 7, 11, 14]
    );
    var regexp = "(%)".format(chords.keys().asArray.collect(_.asString).collect(_.toLower).join("|"));
    var chord = this.toLower, ch, note, notes;
    if (regexp.matchRegexp(chord)) {
      ch = chords.keys.reject{ |ch| chord.findRegexp(ch.asString++"$").size == 0 }.pop;
      note = chord.replace(ch.asString, "");
      notes = (note.midi(octave) + chords.at(ch));
    };

    ^notes;
  }

}

+ Symbol {

  midi {
    |octave=5|
    ^this.asString.midi(octave);
  }

  percussion {
    ^this.asString.percussion;
  }

  chord {
    |octave=5|
    ^this.asString.chord(octave);
  }

}

+ SequenceableCollection {

  abc {
    ^this.collect(_.abc);
  }

  percussion {
    ^this.collect(_.percussion);
  }

  midi {
    ^this.collect(_.percussion);
  }

}
