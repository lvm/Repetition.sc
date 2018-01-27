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
        var current = evt[\pattern].at(idx).asSymbol;
        var to = evt[\to] ?? \midinote;
        var isPerc = false;

        // "defaults"
        evt[\stut] = evt[\stut] ?? 1;
        evt[\dur] = evt[\time].at(idx);
        evt[\octave] = (evt[\octave] ?? 5) + evt[\oct].at(idx);

        // callbacks
        if (evt[\cb].notNil) {
          current = current.applyCallback(evt[\cb], evt);
          isPerc = evt[\cb].asSymbol == \asPerc;
        };

        // amplitude / gain
        if (evt[\type] == \dirt) {
          evt[\gain] = evt[\gain] ?? 0.9;
          evt[\gain] = evt[\gain] + evt[\accent].at(idx);
        } {
          evt[\amp] = evt[\amp] + evt[\accent].at(idx);
        };

        // where to send the current evt
        evt[to] = current + (if (((to.asSymbol == \midinote) || (to.asSymbol == \control)) && (isPerc.asBoolean == false)) { 12*evt[\octave] } { 0 });

        // pattern playing order
        evt[\sort] = evt[\sort] ?? \normal;
        if (evt[\sort].asSymbol == \rand) { idx = len.rand.clip(0, len) };
        if (evt[\sort].asSymbol == \rev) {
          if (idx-1 > -1) {
            idx = idx - 1;
          } {
            idx = len-1;
          };
        };
        if (evt[\sort].asSymbol == \normal) {
          if (idx+1 < len) {
            idx = idx + 1;
          } {
            idx = 0;
          };
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
        evt[\amp] = 0.9;
        evt[\stut] = evt[\stut] ?? 1;
        evt[\plus] = evt[\plus] ?? 0;
        evt[\midinote] = [lsys.next].asGMPerc.flat + evt[\plus];
        evt = evt.yield;
      }
    }).stutter(Pkey(\stut));

    ^Pchain(lindenmayer, basepattern);
  }
}

