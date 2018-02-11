# Repetition.sc

A set of tools to build a `SequenceableCollection of Events`.

## Installation

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

## Getting started

Once Repetition is properly installed and everything working correctly, type the following:

    (
    r = Repetition.new;
    m = r.initMIDI("Midi Through", "Midi Through Port-0");
    p = r.getProxySpace;

    r.midiEventTypes;  // setup repetition midi stuff
    ProxyMixer(p); // and a nice proxymixer too
    )


So, pretty much that's it.

## The *language*

The parser recognizes the following _symbols_:

* Parallel: `a|b`, plays symbols simultaneously. Kind of a Chord.
* Groups: `a+b`, plays symbols sharing the duration. If the duration is 1, each will have a duration of 1/2.
* Accent: `a@`, sums a quarter of the original amplitude to it. That is: amplitude/4.
* Shift octave up: `a'`, Current Octave +1.
* Shift octave down: `a,`, Current Octave -1.
* Repeat once: `a!`, Repeats the symbol once.
* Repeat N times: `a*N` (`N -> Int`), Repeats symbols `N` times.

All of them are "chainable". For example, a Pattern (ab)using every option available:

```
"a*3 b@+c' d@!, e+f".repetitionPattern;
-> [
  ( 'octave': 5, 'dur': 0.14285714285714, 'symbol': a, 'amp': 0.666 ),
  ( 'octave': 5, 'dur': 0.14285714285714, 'symbol': a, 'amp': 0.666 ),
  ( 'octave': 5, 'dur': 0.14285714285714, 'symbol': a, 'amp': 0.666 ),
  ( 'octave': 5, 'dur': 0.071428571428571, 'symbol': b@, 'amp': 0.916 ),
  ( 'octave': 6, 'dur': 0.071428571428571, 'symbol': c', 'amp': 0.666 ),
  ( 'octave': 4, 'dur': 0.14285714285714, 'symbol': d@,, 'amp': 0.916 ),
  ( 'octave': 4, 'dur': 0.14285714285714, 'symbol': d@,, 'amp': 0.916 ),
  ( 'octave': 5, 'dur': 0.071428571428571, 'symbol': e, 'amp': 0.666 ),
  ( 'octave': 5, 'dur': 0.071428571428571, 'symbol': f, 'amp': 0.666 )
]
```

```
(
~x =  "d e a d".degree(4, 0.25).synth(\default);
~x.play;
)
```

A more in-depth documentation is available in SCDoc format browseable through the SCIde Help Browser. If you feel anything is missing, don't hesitate to report an issue asking for clarification.


## Contributing

Bug reports and pull requests are welcome on GitHub at https://github.com/lvm/Repetition.sc. This project is intended to be a safe, welcoming space for collaboration, and contributors are expected to adhere to the [Contributor Covenant](http://contributor-covenant.org) code of conduct.

## LICENSE

See [LICENSE](LICENSE)
