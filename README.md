# Repetition.sc

*Heavily* inspired by TidalCycles. Consider this a (tiny) dialect that implements some of its features.

## Quick intro

In this little intro I'll go about how to setup Supercollider to get `Repetition.sc` working as smoothly as possible, there might be a couple of rough edges still and stuff that don't work properly (I hope not), so be patient and report an issue if that's the case :-)

### Installation

Simply open a new document on your SC IDE and type:

    Quarks.install("https://github.com/lvm/Repetition.sc");

This should print something like this in the Post window:

    Installing Repetition
    Installing PercSymbol
    PercSymbol installed
    Installing FluidSynth
    FluidSynth installed
    Installing Bjorklund
    Bjorklund installed
    Repetition installed
    -> Quark: Repetition[0.1]


Notice: `FluidSynth` is not really a **dependency** and is useful only on GNU/Linux or macOS, but doesn't do any harm to have it installed.
There's a repo where I try to maintain most of the classes/stuff I regularly use with lots of goodies called [SuperUtilities](https://github.com/lvm/SuperUtilities/), once again, not a dependency either but brings to the table a couple useful helpers such as:

* `ChordProg.sc`, a class with Chords, Progressions and some useful music theory stuff.
* `Aconnect.sc`, an `aconnect` front end (GNU/Linux only).
* `Tiny.sc`, a class to handle/autocomplete snippets.
* `MidiEvents.sc`, Event types for MIDIOut patterns. Provides types `\md` and `\cc`.
* `Tidal.sc`, a (really basic) `TidalCycles` interface.
* `Pswing`, wrote by @[triss](https://github.com/triss/LiveCollider/blob/dev/patterns/classes/Pswing.sc) and lifted from Pattern Guide Cookbook 08: Swing.
* `Pbindenmayer`, "Merge" a Pbind with a Prewrite.
* Various methods:
  * `.midiRange`, converts 0..1 to 0..127
  * `.hexBeat`, based on [Steven Yi's Hex Beats](http://kunstmusik.com/2017/10/20/hex-beats/).

To install this, simply clone the repository to your `Platform.userExtensionDir`.

### Getting started

Once Repetition is properly installed and everything working correctly, in a _blank document_ type the following:

    // Create a Repetition instance, which will automatically boot the SuperCollider server + ProxySpace
    r = Repetition.new;
    // Init MIDIClient if not running, and create MIDIOut instance which is assigned to `m`
    m = r.initMIDI("Midi Through", "Midi Through Port-0");
    MIDIIn.connectAll;
    r.midiEventTypes;  // create the custom midi event types
    ProxyMixer(r.getProxySpace); // and a nice ProxyMixer


So, pretty much that's it.

## The *language*

So far, i've implemented only these possibilities:

* Polyrhythms: `"a | b"`
* Groups: `"a+b"`
* Accents: `"a@"`
* Shift octave up: `"a'"`
* Shift octave down: `"a,"`
* Repetition: `"a!"`
* Multiplication: "`a*N`" (`N` -> `Int`)

Of course, all of this is "chainable".
A more in-depth documentation is available in SCDoc formatm, browseable through the SCIde Help Browser.

### Quick example

```
"bd*3 | hq@+sn rm@! cp@".parseRepetitionPattern;
-> [
    ( 'pattern': [ bd, bd, bd ],
      'accent': [ 0, 0, 0 ],
      'time': [ 0.33333333333333, 0.33333333333333, 0.33333333333333 ],
      'oct': [ 0, 0, 0 ],
    ),
    ( 'pattern': [ hq, sn, rm, rm, cp ],
      'accent': [ 0.25, 0, 0.25, 0.25, 0.25 ],
      'time': [ 0.125, 0.125, 0.25, 0.25, 0.25 ],
      'oct': [ 0, 0, 0, 0, 0 ],
    )
   ]
```

```
(
~x = "0 0+3 7".asRepetition(\tempo, 60/60, \type, \md, \chan, 2, \amp, 0.75);
)
```

Is equivalent to:

```
(
var notes = "0 0+3 7".parseRepetitionPattern.pop;
~x = Pbind(
  \tempo, 60/60,
  \type, \md,
  \amp, Pseq(notes.amp, inf) + 0.75,
  \dur, Pseq(notes.time, inf),
  \midinote, Pseq(notes.pattern.collect(_.asInt), inf) + 60,
  \sustain, Pkey(\dur),
  \chan, 2,
);
)
```

When in doubt remember: Each pattern is parsed and finally converted to a `Pbind`, so whatever you normally do, you'll be able to do it using this small DSL.


## Contributing

Bug reports and pull requests are welcome on GitHub at https://github.com/lvm/Repetition.sc. This project is intended to be a safe, welcoming space for collaboration, and contributors are expected to adhere to the [Contributor Covenant](http://contributor-covenant.org) code of conduct.

## LICENSE

See [LICENSE](LICENSE)
