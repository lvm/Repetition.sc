/*
        RePatterns.sc
        Pattern classes for Repetition.sc
*/

/*Pkey : Pattern {
	var	<>key, <>repeats;

	*new { |key, repeats|
		^super.newCopyArgs(key, repeats)
	}

	storeArgs { ^[key, repeats] }

	asStream {
		var	keystream = key.asStream;
		// avoid creating a routine
		var stream = FuncStream({ |inevent| inevent !? { inevent[keystream.next(inevent)] } });
		^if(repeats.isNil) { stream } { stream.fin(repeats) }
	}

	embedInStream { |inval|
		var outval, keystream = key.asStream;
		repeats.value(inval).do {
			outval = inval[keystream.next(inval)];
			if(outval.isNil) { ^inval };
			inval = outval.yield;
		};
		^inval
	}
}*/

Pvol : ListPattern {
	var <>offset;
	*new { arg dict;
		^super.new(dict)
	}

  // asStream {
  //   var	keystream = key.asStream;
  //   // avoid creating a routine
  //   var stream = FuncStream({ |inevent| inevent !? { inevent[keystream.next(inevent)] } });
  //   ^if(repeats.isNil) { stream } { stream.fin(repeats) }
  // }

	embedInStream {  arg inval;
		var item, offsetValue;
		offsetValue = offset.value(inval);
		if (inval.eventAt('reverse') == true, {
			repeats.value(inval).do({ arg j;
				list.size.reverseDo({ arg i;
					item = list.wrapAt(i + offsetValue);
					inval = item.embedInStream(inval);
				});
			});
		},{
			repeats.value(inval).do({ arg j;
				list.size.do({ arg i;
					item = list.wrapAt(i + offsetValue);
					inval = item.embedInStream(inval);
				});
			});
		});
		^inval;
	}
	storeArgs { ^[ list, repeats, offset ] }
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

