package processing.app.elements.sketch;


public interface ISketchEvents {
   // TODO: can I use SketchCode abstraction here?
   /**
    * Called upon new file successfully added to the sketch.
    * 
    * This is draft version of the method.
    * 
    * @param sc
    * @param sketch
    */
   public void onFileAdded(SketchCode sc, Sketch sketch);
}
