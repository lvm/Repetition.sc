/*
ReKit.sc
Drum Kits as part of Repetition.sc
*/

+ Repetition {

  loadKit {
    |path|
    var key_name;
    if (path.isFolder.not) {
      Error("Is the folder correct?").throw
    } {
      PathName(path).filesDo {
        |file|
        if (file.fileName.split($.)[1] == "wav") {
          key_name = file.fileName.toLower.replace(".wav");
          // file.folderName.postln;
          // samples.add(key_name.asSymbol -> Buffer.read(this.server, file.fullPath));
          samples.add(key_name.asSymbol -> Buffer.readChannel(this.server, file.fullPath, channels:0!2));
        }
      }
    }
  }

  listSamples { ^samples; }

  getBufnum { |key| ^samples[key.asSymbol]; }

}
