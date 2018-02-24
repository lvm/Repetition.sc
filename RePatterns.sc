/*
        RePatterns.sc
        Pattern classes for Repetition.sc
*/

Prepetition {
  *new {
    ^Prout({
      |evt|
      var idx = 0;
      var len = evt[\pattern].size;

      while { evt.notNil } {
        // "defaults"
        evt[\stut] = evt[\stut] ?? 1;
        // where to send the current evt
        // evt[to] = current + (if (((to.asSymbol == \midinote) || (to.asSymbol == \control)) && (isPerc.asBoolean == false)) { 12*evt[\octave] } { 0 });

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

