package cse.buffalo.objparser;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.ByteOrder;
public class ObjParser {
    File objFile = null;
    private final static String OBJ_VERTEX_TEXTURE = "vt";
    private final static String OBJ_VERTEX_NORMAL = "vn";
    private final static String OBJ_VERTEX = "v";
    private final static String OBJ_FACE = "f";
    private final static String OBJ_MTLLIB = "mtllib";
    private final static String OBJ_USEMTL = "usemtl";
    private final static String MTL_NEWMTL = "newmtl";
    private final static String MTL_KD = "Kd";
    public ArrayList<Float> vertex= new ArrayList<Float>();
    public ArrayList<Float> normals= new ArrayList<Float>();
    public ArrayList<Float> vertexTexture= new ArrayList<Float>();
    //For aligning
    public ArrayList<List<Float>> texturealign= new ArrayList<List<Float>>();
    public ArrayList<List<Float>> vertexalign= new ArrayList<List<Float>>();
    public ArrayList<List<Float>> normalalign= new ArrayList<List<Float>>();
    public ArrayList<Integer> indices= new ArrayList<Integer>();
    //color per vertex
    public ArrayList<Float> VertexColor= new ArrayList<Float>();
    //current color values
    float kd1=1,kd2=0,kd3=0;
    //color map
    Map<String,float[]> colorMap= new HashMap<>();
    public ObjParser(String filename)  {
            parseObjFile(filename);
    }
    private void parseObjFile(String objFilename){
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        objFile = new File(objFilename);
        try {
            fileReader = new FileReader(objFile);
            bufferedReader = new BufferedReader(fileReader);
            String line = null;
            while (true) {
                line = bufferedReader.readLine();
                if (null == line) {
                    break;
                }
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                if (line.startsWith("#")) // comment
                {
                    continue;
                }else if (line.startsWith(OBJ_VERTEX_TEXTURE)) {
                    processVertexTexture(line);
                    //
                } else if (line.startsWith(OBJ_VERTEX_NORMAL)) {
                    processVertexNormals(line);
                }
                else if (line.startsWith(OBJ_VERTEX)) {
                    processVertex(line);
                } else if (line.startsWith(OBJ_FACE)) {
                    processFace(line);
                }else if (line.startsWith(OBJ_USEMTL)) {
                    processUseMaterial(line);
                } else if (line.startsWith(OBJ_MTLLIB)) {
                    processMaterialLib(line);
                }
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    private void processVertex(String line) {
        float[] vertexs = StringUtils.parseFloatList(3, line, OBJ_VERTEX.length());
        List<Float> m= new ArrayList<>();
        m.add(vertexs[0]);
        m.add(vertexs[1]);
        m.add(vertexs[2]);
        vertexalign.add(m);
    }
    private void processFace(String line) {
        line = line.substring(OBJ_FACE.length()).trim();
        int[] faces = StringUtils.parseListVerticeNTuples(line, 3);
        String parsedList = "";
        int loopi = 0;
        while (loopi < faces.length) {
            parsedList = parsedList + "( "+faces[loopi] + " / "+faces[loopi+1] + " / "+faces[loopi+2] + " ) ";
            indices.add(faces[loopi]-1);
            List<Float> m=vertexalign.get(faces[loopi]-1);
            if(m!=null){
                vertex.add(m.get(0));
                vertex.add(m.get(1));
                vertex.add(m.get(2));
            }

            List<Float> temp=texturealign.get(faces[loopi+1]-1);
            if(temp!=null){
                vertexTexture.add(temp.get(0));
                vertexTexture.add(temp.get(1));
            }

             List<Float> ns=normalalign.get(faces[loopi+2]-1);
            if(ns!=null){
                normals.add(ns.get(0));
                normals.add(ns.get(1));
                normals.add(ns.get(2));
            }
            //now add color for the vertex
            VertexColor.add(kd1);
            VertexColor.add(kd2);
            VertexColor.add(kd3);
            VertexColor.add(1f);
            loopi+=3;
        }
    }
    private void processVertexTexture(String line) {
        float[] values = StringUtils.parseFloatList(2, line, OBJ_VERTEX_TEXTURE.length());
        List<Float> m= new ArrayList<>();
        m.add(values[0]);
        m.add(values[1]);
        texturealign.add(m);
    }
    private void processVertexNormals(String line){
        float[] normals= StringUtils.parseFloatList(3, line, OBJ_VERTEX_NORMAL.length());
        List<Float> m= new ArrayList<>();
        m.add(normals[0]);
        m.add(normals[1]);
        m.add(normals[2]);
        normalalign.add(m);
    }

    private void processUseMaterial(String line) {
        String matname=line.substring(OBJ_USEMTL.length()).trim();
        float[] colors=colorMap.get(matname);
        kd1=colors[0];
        kd2=colors[1];
        kd3=colors[2];
    }

    //reading materials file
    private void processMaterialLib(String line) throws FileNotFoundException, IOException {
        String[] matlibnames = StringUtils.parseWhitespaceList(line.substring(OBJ_MTLLIB.length()).trim());

        if (null != matlibnames) {
            for (int loopi = 0; loopi < matlibnames.length; loopi++) {
                try {
                    parseMtlFile(matlibnames[loopi]);
                } catch (FileNotFoundException e) {
                    Log.d("Error","can not find file");
                    e.printStackTrace();
                }
            }
        }
    }

    private void parseMtlFile(String mtlFilename) throws FileNotFoundException, IOException {
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        File mtlFile = new File(objFile.getParent(), mtlFilename);
        fileReader = new FileReader(mtlFile);
        bufferedReader = new BufferedReader(fileReader);
        String line = null;
        String CurrMatlName="";
        while (true) {
            line = bufferedReader.readLine();
            if (null == line) {
                break;
            }
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }

            if (line.startsWith("#")) // comment
            {
                continue;
            } else if (line.startsWith(MTL_NEWMTL)) {
                CurrMatlName=  processNewmtl(line);
            } else if (line.startsWith(MTL_KD)) {
                processReflectivityTransmissivity(MTL_KD, line,CurrMatlName);
            }
        }
        bufferedReader.close();
    }

    private String processNewmtl(String line) {
        line = line.substring(MTL_NEWMTL.length()).trim();
        return line;
    }
    private void processReflectivityTransmissivity(String fieldName, String line,String matlname) {
        String[] tokens = StringUtils.parseWhitespaceList(line.substring(fieldName.length()));
        if(tokens==null){
            Log.d("MtlFile","Token Returned Null");
        }
        else if(tokens.length<=0){
            Log.d("MtlFile","Token Returned size is zero");
        }
        else{
            float[] rgb= new float[3];
            rgb[0]=Float.parseFloat(tokens[0]);
            rgb[1]=Float.parseFloat(tokens[1]);
            rgb[2]=Float.parseFloat(tokens[2]);
            colorMap.put(matlname,rgb);
        }
    }
    public FloatBuffer getVertexBuffer(){
        float[] vertexs= new float[vertex.size()];
        for(int i=0;i<vertex.size();i++) vertexs[i]=vertex.get(i)*8;
        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(vertexs.length * 7)
                                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vertexs).position(0);
        return vertexBuffer;
    }
    public FloatBuffer getVertexTextureBuffer(){
        float[] vertextext= new float[vertexTexture.size()];
        for(int i=0;i<vertexTexture.size();i++) vertextext[i]=vertexTexture.get(i);
        FloatBuffer VertexTextureBuffer = ByteBuffer.allocateDirect(vertextext.length * 7)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        VertexTextureBuffer.put(vertextext).position(0);
        return VertexTextureBuffer;
    }
    public FloatBuffer getNormalBuffer(){
        Float[] vertexNormals= new Float[normals.size()];
        vertexNormals= normals.toArray(vertexNormals);
        FloatBuffer VertexNormalBuffer = ByteBuffer.allocateDirect(vertexNormals.length * 7)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        return VertexNormalBuffer;
    }
    public IntBuffer getIndexBuffer(){
        int[] faceIndices= new int[indices.size()];
        for(int i=0;i<indices.size();i++) faceIndices[i]=i;
        IntBuffer IndexBuffer = ByteBuffer.allocateDirect(faceIndices.length * 4)
                .order(ByteOrder.nativeOrder()).asIntBuffer();
        IndexBuffer.put(faceIndices).position(0);
        return IndexBuffer;
    }
    public FloatBuffer getColorBuffer(){
        float[] colors= new float[VertexColor.size()];
        for(int i=0;i<VertexColor.size();i++) colors[i]=VertexColor.get(i);
        FloatBuffer ColorBuffer = ByteBuffer.allocateDirect(colors.length * 7)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        ColorBuffer.put(colors).position(0);
        return ColorBuffer;
    }

    public ArrayList<Float> getVertexs(){
        return vertex;
    }
    public ArrayList<Float> getVertexTex(){
        return vertexTexture;
    }
    public ArrayList<Float> getNormals(){
        return normals;
    }
    public ArrayList<Integer> getFaces(){
        return indices;
    }
    public ArrayList<Float> getVertexColor(){
        return VertexColor;
    }
}
