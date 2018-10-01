/*
        RePatterns.sc
        Pattern classes for Repetition.sc
*/

PifRest : Pattern {
  var	<>key, <>iftrue, <>iffalse, <>default;
  *new { |key, iftrue, iffalse, default|
    ^super.newCopyArgs(key, iftrue, iffalse, default)
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

PifEqual : Pattern {
  var	<>key, <>condition, <>iftrue, <>iffalse, <>default;
  *new { |key, condition, iftrue, iffalse, default|
    ^super.newCopyArgs(key, condition, iftrue, iffalse, default)
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
  var <>key, <>dict, <>default;
  *new { arg key, dict, default;
    ^super.newCopyArgs(key, dict, default ?? 0.5);
  }
  storeArgs { ^[key,dict,default ] }
  asStream {
    ^FuncStream({ |inval|
      var test;

      if((test = (dict.at(inval.at(key).asSymbol)).next(inval)).isNil) {
        default;
      } {
        dict.at(inval.at(key).asSymbol);
      };
    }, {});
  }
}

RePevent : Pattern {
	var <>pattern, <>event;

	*new { arg pattern, event;
    // ^super.newCopyArgs(pattern, event ?? { Event.default });
    ^super.newCopyArgs(pattern, event ?? { (chan: 9, amp: 0.9, dur: 1/4) });
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
    ^Prout({ |evt|
      while { evt.notNil } {

        // "defaults"
        evt[\stut] = evt.stut ?? 1;
        evt[\octave] = (evt.octave ?? 5) + evt.shift;
        evt[\amp] = (evt.amp ?? 0.125) + evt.accent;
        evt[\midinote] = evt.midinote + (if (evt.typeof != \percussion, { 12 * evt.octave }, { 0 }));

        // if both are defined, i'll discard them.
        if (evt.fast.notNil && evt.slow.isNil) { evt[\stretch] = 1/evt.fast; };
        if (evt.fast.isNil && evt.slow.notNil) { evt[\stretch] = evt.slow; };

        // i'm lazy and don't want to specify an Event type if i already defined a (midi) channel.
        if (evt.chan.notNil && evt.type.isNil) { evt[\type] = \md; };

        evt = evt.yield;
      }
    }).stutter(Pkey(\stut));
  }
}

Linda {
  *new { |lsystem basepattern|
    var lsys = lsystem.asStream;
    var lindenmayer = Prout({ |evt|
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


+ Pbind {

  fx { |... args| ^Pfx(this, *args); }

}

+ Pchain {

  fx { |... args| ^Pfx(this, *args); }

}

+ Pfx {

  fx { |... args| ^Pfx(this, *args); }

}
