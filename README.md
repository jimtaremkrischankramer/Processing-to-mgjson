The toMgjson library provides a way to create .mgjson files which can be imported into After Effects. It lets you create multiple so called “Streams” of data which can be used in an After Effects Project, for example to be the source for a layers position, rotation, opacity, …

Although it was intended for and tested in Processing, it doesn’t rely on processing.core so it can be used (i think) in vanilla Java too.

This is an somewhat unfinished university project and the first library I’ve ever written, so please consider the pain you might encounter working with it.

Jim Kramer | KD HTW Berlin 2024 


## **Install for Processing**

Download this .zip file: [toMgjson.zip](https://github.com/user-attachments/files/16417032/toMgjson.zip).

Unzip it in your Processing libraries folder (probably /Users/username/Documents/Processing/libraries or something).

Done! (You might have to restart Processing)

## **new ToMgjson()**

Creates an instance of the of the “ToMgjson” class. The class takes five arguments, two of them are optional.

- **int** sampleCount → Amount of frames written to file.
- **String** path → Path to where the file will be created and file name
    - Must end in “../your-file-name.mgjson”.
- **String[]** displayNames → An array of names later displayed in the After Effects timeline.
- **int** digitsInteger (optional) → Amount of integer digits in the Data written to file (defaults to five).
    - example: float 12.3f
    - default: “00012.30”
    - digitsInteger = 3: “012.30”
- **int** digitsDecimal (optional) → Amount of decimal digits in the data written to file (defaults to two).
    - example: float 12.345f
    - default: “00012.34”
    - digitsInteger = 4: “00012.3450”

```java
import tomgjson.*;

ToMgjson mgjson;

void setup() {
	int sampleCount = 100;
	String path = "/Your/file/path/your-file-name.mgjson";
  String[] displayNames = {"x", "y"};
  // int digitsInteger = 3;
  // int digitsDecimal = 4;
  
  mgjson = new ToMgjson(sampleCount, path, displayNames);
  // mgjson = new ToMgjson(sampleCount, path, displayNames, digitsInteger, digitsDecimal);
}
```

## updateStreams()

gets called every Frame thats supposed to be written to file. The function takes two arguments.

- **int** currentFrame → The current frame number
    - Must start at 1 and increase by 1 every time the function is called.
- **Object[]** updateObjects → An array of objects with the values written to file.
    - The values must be int, float or double.
    - The array must be the same length of “displayNames”.
    - The array must be in corresponding order to “displayNames”.

```java
void draw() {
  Object [] updateObjects = {mouseX, mouseY};
  int currentFrame = frameCount;
  mgjson.updateStreams(currentFrame, updateObjects);
}
```

## Tips and Trick (for Processing and After Effects)

- Every frame passed to updateStreams() corresponds to one frame in After Effects, so it could be useful to set the Processing frame rate the same as in your After Effects composition with frameRate().
- The position value in the After Effects timeline can be right clicked on and split into x, y (and z).
- If you want to write data every frame, but don’t want to start with the first, you could do it like this:

```java
int startFrame = 50;
  int currentFrame = frameCount - startFrame;
  if(frameCount > startFrame){
    mgjson.updateStreams(currentFrame, updateObjects); 
  }
```
## Example

In this example we’ll track the mouse position relative to the Processing canvas and use that data in After Effects to move a layer.

This is the Processing script:

```java
import tomgjson.*;

ToMgjson mgjson;

void setup() {
	int sampleCount = 100; // set stream lenght to 100 frames
	String path = "/Users/jimkramer/Desktop/mousePos.mgjson"; // set path where file is created
  String[] displayNames = {"x", "y"}; // set names for streams in the After Effects timeline
  mgjson = new ToMgjson(sampleCount, path, displayNames); // create Mgjson class innstance

  frameRate(25);
  size(512, 512);
}

void draw() {
  Object [] updateObjects = {mouseX, mouseY}; // create an array of objects with the current mouse x and y position
  mgjson.updateStreams(frameCount, updateObjects); // update stream values
}
```

Import .mgjson file just like you would import footage and drag it on the timeline:

<img width="495" alt="import" src="https://github.com/user-attachments/assets/2a0947b7-48e3-4b31-92e8-69d4ee638218">

Split dimensions of the position value of the layer you want to move by right clicking:

<img width="526" alt="split" src="https://github.com/user-attachments/assets/2dcb778e-34ef-4214-b8ec-d13cff504345">

Link the x and y values of the layer to their respective values in the mgjson data:

![link](https://github.com/user-attachments/assets/32b366b0-39f3-4dc2-8c79-f9d4f486fab1)

Done!
