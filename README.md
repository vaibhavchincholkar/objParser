# objParser

ObjParser reads the wavefront .obj file and .mtl file associated with it and returns OpenGL buffers which can then directly used to render object.

# How to add objParser to your Android Studio Project

Go to

File -> new -> New Moudle -> select import .JAR/.AAR package 

Click next

Give the file path 

and click Finish

above steps will include the objParser in your project but its not a dependency yet 

To do that

File -> project Strucutre -> dependencies -> click on App-> click on + -> module dependencies and select objParser-> finish.


it will sync gradle build and you are ready to use ObjParser.

# usage

You need to store object file and mtl file in the same directory. Also you need to give storage permission to access the the directory path.

```
  ObjParser parse= new ObjParser("/storage/emulated/path/file.obj");
  FloatBuffer mColorBuffer= parse.getColorBuffer();
  FloatBuffer mVertexBuffer = parse.getVertexBuffer();
  FloatBuffer mTextureBuffer =parse.getVertexTextureBuffer();
  IntBuffer mIndexBuffer = parse.getIndexBuffer();
 
```
 give above buffer to OpenGL and you should see your object.

Thats easy :-D
Enjoy.
