/*
        ReRhythms.sc
        Rhythmic lib, part of Repetition.sc
*/


ReRhythms {
  classvar <ryt;

  *new {
    Library.put(\repetition, \rhythms, ryt);
  }

  *initClass {
    ryt = (
      jungle: (
        tempo: 0.666,
        pattern: (
          bd: [1, 0, 0, 0, 0, 1, 0, 0],
          sn: [0, 0, 1, 0, 0, 0, 1, 0],
          ch: [1, 1, 1, 1, 1, 1, 1, 1],
          oh: [0, 0, 0, 0, 0, 0, 1, 0]
        )
      ),
      amen: (
        tempo: 136/120/2,
        pattern: (
          bd: [1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0],
          sn: [0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1],
          ch: [1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0],
          oh: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0]
        )
      ),
      hiphop: (
        tempo: 0.666,
        pattern: (
          bd: [1, 0, 0, 0, 1, 1, 0, 0],
          sn: [0, 0, 1, 0, 0, 0, 1, 0],
          ch: [1, 1, 1, 1, 1, 1, 1, 1],
          oh: [0, 0, 0, 1, 0, 0, 0, 1]
        )
      ),
      funkypresident: (
        tempo: 105/120/2,
        pattern: (
          bd: [1, 0, 0, 1, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0],
          sn: [0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0],
          ch: [1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0],
          oh: [0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0]
        )
      ),
      superstition: (
        tempo: 98/120/2,
        pattern: (
          bd: [1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0],
          sn: [0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0],
          ch: [1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 1, 1],
        )
      ),
      expressyourself: (
        tempo: 94/120/2,
        pattern: (
          bd: [1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0],
          sn: [0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 1, 0, 0, 0],
          ch: [1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1]
        )
      )
    );
  }

  *get {
    |key|

    ^Library.at(\repetition, \rhythms).at(key.asSymbol);
  }

}


+ Repetition {

  loadRhythms {
    ^ReRhythms.new;
  }

}

+ Symbol {

  pattern {
    ^Library.at(\repetition, \rhythms).at(this)
    .pattern
    .collect{
      |pat key|
      pat.collect{ |i| if (i.asBoolean) { key.asSymbol } { \r }  }.join(" ");
    }
    .asArray
    .join("|")
    ;
  }

  tempo {
    ^Library.at(\repetition, \rhythms).at(this)
    .tempo;
  }

}