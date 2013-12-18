This is an attempt to refactor PDE to reduce coupling and improve modularity
==========
I have always wanted to participate in an open source project, and Processing seemed to be a good match. However once I have started reading PDE code, I realized that it seems to be very monolithic. Almost all classes are coupled with `Base.java` or `Editor.java`.  This basicaly disallows reusability of the components,  but what's worse, that means that it is impossible to test for example `EditorToolbar.java` component without runnign the whole PDE. 

Or for example `Sketch.java` doesn't really follow SRP (Single Responsibility Principle) because it contains states for files, states for 

There're a lot of global states and public member variables which are used across different classes.

Also there's a bunch of 'logical coupling' which is the worst type of coupling, where two classes assume each others functionality.

Also there's bunch of 'baked' in variables.

`Preferences.java` has three responsiblities: showing GUI, representing data in memory and persisting data.


There's an ongoing talk on forums and github issue comments about getting more people involved into development of Processing, but taking into account current lack of modularity and high levels of coupling, which result in exponential growth of complexity of the project, the learning curve of 2-3 month to be able to understand the current structure of PDE I believe is a great obstacle. 

Hopefully refactoring may help.


Dimitry Kireyenkov
18 December 2013





Processing
==========

This is the official source code for the [Processing](http://processing.org) Development Environment (PDE), 
the “core” and the libraries that are included with the [download](http://processing.org/download). 

If you have found a bug in the Processing software, you can file it here under the [“issues” tab](https://github.com/processing/processing/issues). 
If it relates to the [JavaScript](http://processingjs.org) version, please use [their issue tracker](https://processing-js.lighthouseapp.com/).
All Android-related development has moved to its own repository [here](https://github.com/processing/processing-android), 
so issues with Android Mode, or with the Android version of the core library should be posted there instead.

The issues list has been imported from Google Code, so there are many spurious references 
amongst them since the numbering changed. Basically, any time you see references to 
changes made by [processing-bugs](https://github.com/processing-bugs), it may be somewhat suspect.
Over time this will clean itself up as bugs are fixed and new issues are added from within Github.
Help speed this process along by helping us!

The [processing-web](https://github.com/processing/processing-web/) repository 
contains reference, examples, and the site. 
(Please use that link to file issues regarding the web site, the examples, or the reference.)

The instructions for building the source [are here](https://github.com/processing/processing/wiki/Build-Instructions), 
although they [need an update](https://github.com/processing/processing/issues/1629).

Someday we'll also write code style guidelines, fix all these bugs, 
throw together hundreds of unit tests, and solve the Israeli-Palestinian conflict. 

But in the meantime, I ask for your patience, 
[participation](https://github.com/processing/processing/wiki/Project-List), 
and [patches](https://github.com/processing/processing/pulls).

Ben Fry, 3 February 2013  
Last updated 21 April 2013
