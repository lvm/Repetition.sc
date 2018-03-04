/*
        RePatterns.sc
        Pattern classes for Repetition.sc
*/

Prepetition {
  *new {
    ^Prout({
      |evt|
      var repetition = Repetition.new;
      while { evt.notNil } {

        // "defaults"
        evt[\stut] = evt.stut ?? 1;
        evt[\shift] = evt.shift ?? 0;
        evt[\octave] = (evt.octave ?? 5) + evt.shift;

        // correct midinote with octave and all.
        if (evt.typeof.asSymbol != \perc and: (evt.typeof.asSymbol != \sample)) {
          evt[\midinote] = evt.midinote + (12 * evt.octave);
        };
        // actual note calc.
        if (evt.typeof.asSymbol == \sample) {
          evt[\buf] = repetition.getBufnum( ((evt.kit ?? "808")++"_"++evt.midinote).asSymbol );
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

