/*
        ReNote.sc
        Notes and Chords for SuperCollider as part of Repetition.sc
*/

Note {
  classvar <twelve;
  var <note;

	*new { | n = \c |
    if (n.isKindOf(Symbol).not) { Error("Please use Note(name.asSymbol) instead.").throw };
    ^super.new.init(n);
	}

  *initClass {
    twelve = (\c: 0, \cs: 1, \db: 1, \d: 2, \ds:3, \eb: 3, \e: 4, \f:5, \fs:6, \gb: 6, \g:7, \gs:8, \ab: 8, \a:9, \as:10, \bb: 10, \b:11);
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

  midi {
    |octave=5|
    ^this.degree+(12*octave);
  }

  freq {
    |octave=5|
    ^this.midi(octave).midicps;
  }

}

Chord {
  classvar <chords;
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


+ Integer {
  toABC {
    ^Note.theTwelve.findKeyForValue(this.fold(0,11));
  }
}


+ String {
  toNote {
    ^Note(this.toLower.asSymbol);
  }
}
