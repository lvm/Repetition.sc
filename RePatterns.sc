/*
        RePatterns.sc
        Pattern classes for Repetition.sc
*/

Prepetition {
  *new {
    ^Prout({
      |evt|
      while { evt.notNil } {
        var revtStream = evt.rpEvent,
        revt = revtStream.nextRP(),
        octave = revt.octave ?? evt.octave
        ;

        // "defaults"
        evt[\stut] = evt.stut ?? 1;
        evt[\octave] = (octave ?? 5) + revt.shift;
        evt[\midinote] = revt.midinote + (if (revt.typeof != \percussion, { 12 * evt.octave }, { 0 }));

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

