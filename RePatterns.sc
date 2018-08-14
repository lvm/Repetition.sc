/*
        RePatterns.sc
        Pattern classes for Repetition.sc
*/

PifRest : Pattern {
  var	<>key, <>iftrue, <>iffalse, <>default;
  *new { |condition, iftrue, iffalse, default|
    ^super.newCopyArgs(condition, iftrue, iffalse, default)
  }
  storeArgs { ^[key, iftrue, iffalse,default] }
  asStream {
    var	trueStream = iftrue.asStream,
    falseStream = iffalse.asStream;

    ^FuncStream({ |inval|
      var test;
      if((test = (inval.at(key) == \rest).next(inval)).isNil) {
        nil
      } {
        if(test) {
          trueStream.next(inval) ? default
        } {
          falseStream.next(inval) ? default
        };
      };
    }, {
      trueStream.reset;
      falseStream.reset;
    })
  }
}

Peach : Pattern {
  var <>dict, <>default;
  *new { arg dict, default;
    ^super.newCopyArgs(dict, default ?? 0.5);
  }
  storeArgs { ^[dict,default ] }
  asStream {
    ^FuncStream({ |inval|
      var test;

      if((test = (dict.at(inval.symbol.asSymbol)).next(inval)).isNil) {
        default;
      } {
        dict.at(inval.symbol.asSymbol);
      };
    }, {});
  }
}

RePevent : Pattern {
	var <>pattern, <>event;

	*new { arg pattern, event;
		^super.newCopyArgs(pattern, event ?? { Event.default });
	}
	storeArgs { ^[pattern, event] }
	embedInStream { arg inval;
		var outval;
		var stream = pattern.asStream;
		loop {
			outval = stream.nextRP(event);
			if (outval.isNil) { ^inval };
			inval = outval.yield
		}
	}
}

Prepetition {
  *new {
    ^Prout({
      |evt|
      while { evt.notNil } {

        // "defaults"
        evt[\stut] = evt.stut ?? 1;
        evt[\octave] = (evt.octave ?? 5) + evt.shift;
        evt[\amp] = (evt.amp ?? 0.125) + evt.accent;
        evt[\midinote] = evt.midinote + (if (evt.typeof != \percussion, { 12 * evt.octave }, { 0 }));

        // i'm lazy and don't want to specify an Event type if i already defined a (midi) channel.
        if (evt.chan.notNil && evt.type.isNil) { evt[\type] = \md; };

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

