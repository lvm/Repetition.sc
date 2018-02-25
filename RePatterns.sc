/*
        RePatterns.sc
        Pattern classes for Repetition.sc
*/

Prepetition {
  *new {
    ^Prout({
      |evt|
      while { evt.notNil } {
        // "defaults"
        evt[\stut] = evt.stut ?? 1;
        evt[\shift] = evt.shift ?? 0;
        evt[\octave] = (evt.octave ?? 5) + evt.shift;

        // actual note calc.
        if (evt[\typeof].asSymbol != \perc) {
          evt[\midinote] = evt.midinote + (12 * evt.octave);
        };

        evt = evt.yield;
      }
    }).stutter(Pkey(\stut));
  }
}

Linda {
  *new {
    |lsystem basepattern|
    var lsys = lsystem.asStream;
    var lindenmayer = Prout({
      |evt|
       while { evt.notNil } {
        evt[\type] = \md;
        evt[\amp] = evt[\amp] ?? 0.9;
        evt[\stut] = evt[\stut] ?? 1;
        evt[\plus] = evt[\plus] ?? 0;
        evt[\midinote] = [lsys.next].asGMPerc.flat + evt[\plus];
        evt = evt.yield;
      }
    }).stutter(Pkey(\stut));

    ^Pchain(lindenmayer, basepattern);
  }
}

