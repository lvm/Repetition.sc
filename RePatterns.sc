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
    ^super.newCopyArgs(pattern, event ?? { Event.default });
	}
  currentpairs { ^pattern; }
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

        evt[\stut] = evt.stut ?? 1;
        // evt[\octave] = (evt.octave ?? 5) + evt.shift;
        // evt[\amp] = (evt.amp ?? 0.125) + evt.accent;
        // if (evt.midinote.isRest.not) {
        // evt[\midinote] = evt.midinote + (if (evt.typeof != \percussion, { 12 * evt.octave }, { 0 }));
        // };

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

Pnote {
  *new { |sequence, repeats=inf|
    ^Pseq(sequence.parseRepetition, repeats);
  }
}

Prnote {
  *new { |sequence, repeats=inf|
    ^Prand(sequence.parseRepetition, repeats);
  }
}

Pxnote {
  *new { |sequence, repeats=inf|
    ^Pxrand(sequence.parseRepetition, repeats);
  }
}

Psnote {
  *new { |sequence, repeats=inf|
    ^Pshuf(sequence.parseRepetition, repeats);
  }
}

Plsys {
  *new { |sequence, limit=100|
    ^Pseq(sequence.lsys(limit), inf);
  }
}

Pstruct : Pattern {
	var <>pattern, <>k= 3, <>n= 8, offset= 0, <>length= inf;
  *new { |pattern, k, n, offset= 0, length= inf|
		^super.newCopyArgs(pattern, k, n, offset, length);
	}
	storeArgs {^[pattern, k, n, offset, length]}
	embedInStream { |inval|
    var pStr = pattern.asStream;
		var kStr = k.asStream;
		var nStr = n.asStream;
    var oStr = offset.asStream;
		var pVal, kVal, nVal, oVal;
		length.value(inval).do{
			var outval, b;
      kVal = kStr.next(inval);
			nVal = nStr.next(inval);
			oVal = oStr.next(inval);
			if(kVal.notNil and:{nVal.notNil}, {
				b = Pseq(Bjorklund(kVal, nVal), 1, oVal).asStream;
				while({outval = b.next; outval.notNil}, {
          inval = (if (outval == 0) { \r } { pStr.next(inval) }).yield;
        });
			}, {
				inval = nil.yield;
			});
		};
		^inval;
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
