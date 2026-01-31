package sunneo.sdlmm.babylon3d;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON Scene Loader compatible with ref-sdlmm scene_json format.
 * Uses a simple JSON parser to avoid external dependencies.
 */
public class SceneLoader {
    
    public static final int SCENE_FORMAT_2D = 0;
    public static final int SCENE_FORMAT_3D = 1;

    /**
     * Load a 3D scene from a JSON file (compatible with ref-sdlmm format)
     */
    public static Scene3D loadScene3D(String filename) {
        try {
            String content = new String(Files.readAllBytes(Path.of(filename)), StandardCharsets.UTF_8);
            return parseScene3D(content, filename);
        } catch (IOException e) {
            System.err.println("Error reading scene file: " + filename + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Parse a 3D scene from JSON content
     */
    public static Scene3D parseScene3D(String jsonContent, String baseFilename) {
        JsonParser parser = new JsonParser(jsonContent);
        JsonObject root = parser.parseObject();
        
        if (root == null) {
            System.err.println("Error parsing JSON content");
            return null;
        }

        // Check format
        int format = root.getInt("format", -1);
        if (format != SCENE_FORMAT_3D) {
            System.err.println("Not a 3D scene (format=" + format + ")");
            return null;
        }

        int width = root.getInt("width", 800);
        int height = root.getInt("height", 600);
        
        Scene3D scene = new Scene3D(width, height);
        scene.backgroundColor = root.getInt("backgroundColor", 0);

        // Get base directory for relative file paths
        String baseDir = "";
        if (baseFilename != null) {
            File f = new File(baseFilename);
            if (f.getParent() != null) {
                baseDir = f.getParent() + File.separator;
            }
        }

        // Load camera
        JsonObject cameraObj = root.getObject("camera");
        if (cameraObj != null) {
            float[] pos = cameraObj.getFloatArray("position");
            if (pos != null && pos.length >= 3) {
                scene.camera.Position = new Vector3(pos[0], pos[1], pos[2]);
            }
            float[] target = cameraObj.getFloatArray("target");
            if (target != null && target.length >= 3) {
                scene.camera.Target = new Vector3(target[0], target[1], target[2]);
            }
        }

        // Load lights
        JsonArray lightsArray = root.getArray("lights");
        if (lightsArray != null) {
            for (int i = 0; i < lightsArray.size(); i++) {
                JsonObject lightObj = lightsArray.getObject(i);
                if (lightObj != null) {
                    Light3D light = new Light3D();
                    float[] pos = lightObj.getFloatArray("position");
                    if (pos != null && pos.length >= 3) {
                        light.position = new Vector3(pos[0], pos[1], pos[2]);
                    }
                    light.intensity = lightObj.getFloat("intensity", 1.0f);
                    light.color = lightObj.getInt("color", 0xFFFFFF);
                    scene.addLight(light);
                }
            }
        }

        // Load models
        JsonArray modelsArray = root.getArray("models");
        if (modelsArray != null) {
            for (int i = 0; i < modelsArray.size(); i++) {
                JsonObject modelObj = modelsArray.getObject(i);
                if (modelObj != null) {
                    Model3D model = new Model3D();
                    
                    model.modelFile = modelObj.getString("modelFile");
                    model.textureFile = modelObj.getString("textureFile");
                    
                    float[] pos = modelObj.getFloatArray("position");
                    if (pos != null && pos.length >= 3) {
                        model.position = new Vector3(pos[0], pos[1], pos[2]);
                    }
                    
                    float[] rot = modelObj.getFloatArray("rotation");
                    if (rot != null && rot.length >= 3) {
                        model.rotation = new Vector3(rot[0], rot[1], rot[2]);
                    }
                    
                    float[] scale = modelObj.getFloatArray("scale");
                    if (scale != null && scale.length >= 3) {
                        model.scale = new Vector3(scale[0], scale[1], scale[2]);
                    }

                    // Check for inline mesh data
                    JsonObject meshData = modelObj.getObject("mesh");
                    
                    // Load mesh from file or inline data
                    if (model.modelFile != null) {
                        String meshPath = baseDir + model.modelFile;
                        java.io.File meshFile = new java.io.File(meshPath);
                        if (!meshFile.exists()) {
                            System.err.println("Failed to load mesh: " + meshPath + " (file not found)");
                            continue;
                        }
                        model.mesh = Mesh.loadObj(meshPath);
                        if (model.mesh != null) {
                            model.mesh.Position = model.position;
                            model.mesh.Rotation = model.rotation;
                            System.out.println("Loaded mesh from: " + meshPath);
                        } else {
                            System.err.println("Failed to load mesh: " + meshPath + " (invalid OBJ format or parse error)");
                            continue;
                        }
                    } else if (meshData != null) {
                        // Load inline mesh
                        model.mesh = loadInlineMesh(meshData);
                        if (model.mesh != null) {
                            model.mesh.Position = model.position;
                            model.mesh.Rotation = model.rotation;
                            System.out.println("Loaded inline mesh");
                        } else {
                            System.err.println("Failed to load inline mesh (missing vertices or faces data)");
                            continue;
                        }
                    } else {
                        // No mesh file or inline data - create a default cube
                        model.mesh = Mesh.createCube();
                        model.mesh.Position = model.position;
                        model.mesh.Rotation = model.rotation;
                        System.out.println("Using default cube mesh");
                    }

                    // Load texture
                    if (model.textureFile != null && model.mesh != null) {
                        String texPath = baseDir + model.textureFile;
                        java.io.File texFile = new java.io.File(texPath);
                        if (!texFile.exists()) {
                            System.err.println("Failed to load texture: " + texPath + " (file not found)");
                        } else {
                            Texture tex = Texture.load(texPath);
                            if (tex != null) {
                                model.mesh.texture = tex;
                                System.out.println("Loaded texture from: " + texPath);
                            } else {
                                System.err.println("Failed to load texture: " + texPath + " (unsupported format or read error)");
                            }
                        }
                    }

                    scene.addModel(model);
                }
            }
        }

        System.out.println("Scene loaded: " + width + "x" + height + 
            ", " + scene.lights.size() + " lights, " + scene.models.size() + " models");
        return scene;
    }

    /**
     * Load inline mesh from JSON data
     */
    private static Mesh loadInlineMesh(JsonObject meshData) {
        JsonArray vertices = meshData.getArray("vertices");
        JsonArray faces = meshData.getArray("faces");
        
        if (vertices == null || faces == null) {
            return null;
        }

        int vertexCount = vertices.size();
        int faceCount = faces.size();
        
        if (vertexCount == 0 || faceCount == 0) {
            return null;
        }

        Mesh mesh = new Mesh("inline", vertexCount, faceCount);

        // Load vertices
        for (int i = 0; i < vertexCount; i++) {
            JsonObject vertexObj = vertices.getObject(i);
            if (vertexObj != null) {
                float[] coords = vertexObj.getFloatArray("coordinates");
                if (coords != null && coords.length >= 3) {
                    mesh.Vertices[i].Coordinates = new Vector3(coords[0], coords[1], coords[2]);
                }
                
                float[] normal = vertexObj.getFloatArray("normal");
                if (normal != null && normal.length >= 3) {
                    mesh.Vertices[i].Normal = new Vector3(normal[0], normal[1], normal[2]);
                } else {
                    mesh.Vertices[i].Normal = new Vector3(0, 1, 0);
                }
                
                float[] texCoord = vertexObj.getFloatArray("texCoord");
                if (texCoord != null && texCoord.length >= 2) {
                    mesh.Vertices[i].TextureCoordinates = new Vector3(texCoord[0], texCoord[1], 0);
                }
            }
        }

        // Load faces
        for (int i = 0; i < faceCount; i++) {
            int[] faceIndices = faces.getIntArray(i);
            if (faceIndices != null && faceIndices.length >= 3) {
                mesh.faces[i] = new Face(faceIndices[0], faceIndices[1], faceIndices[2]);
            }
        }

        return mesh;
    }

    /**
     * Detect scene format from JSON file
     */
    public static int getSceneFormat(String filename) {
        try {
            String content = new String(Files.readAllBytes(Path.of(filename)), StandardCharsets.UTF_8);
            JsonParser parser = new JsonParser(content);
            JsonObject root = parser.parseObject();
            if (root != null) {
                return root.getInt("format", -1);
            }
        } catch (Exception e) {
            System.err.println("Error reading scene format: " + e.getMessage());
        }
        return -1;
    }

    // ============================================================
    // Simple JSON Parser (no external dependencies)
    // ============================================================

    private static class JsonParser {
        private final String json;
        private int pos = 0;

        public JsonParser(String json) {
            this.json = json;
        }

        private void skipWhitespace() {
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
                pos++;
            }
        }

        public JsonObject parseObject() {
            skipWhitespace();
            if (pos >= json.length() || json.charAt(pos) != '{') {
                return null;
            }
            pos++; // skip '{'
            
            JsonObject obj = new JsonObject();
            skipWhitespace();
            
            if (pos < json.length() && json.charAt(pos) == '}') {
                pos++; // skip '}'
                return obj;
            }

            while (pos < json.length()) {
                skipWhitespace();
                
                // Parse key
                String key = parseString();
                if (key == null) break;
                
                skipWhitespace();
                if (pos >= json.length() || json.charAt(pos) != ':') break;
                pos++; // skip ':'
                
                skipWhitespace();
                
                // Parse value
                Object value = parseValue();
                obj.put(key, value);
                
                skipWhitespace();
                if (pos >= json.length()) break;
                
                char c = json.charAt(pos);
                if (c == '}') {
                    pos++;
                    break;
                } else if (c == ',') {
                    pos++;
                }
            }
            
            return obj;
        }

        private JsonArray parseArray() {
            skipWhitespace();
            if (pos >= json.length() || json.charAt(pos) != '[') {
                return null;
            }
            pos++; // skip '['
            
            JsonArray arr = new JsonArray();
            skipWhitespace();
            
            if (pos < json.length() && json.charAt(pos) == ']') {
                pos++; // skip ']'
                return arr;
            }

            while (pos < json.length()) {
                skipWhitespace();
                Object value = parseValue();
                arr.add(value);
                
                skipWhitespace();
                if (pos >= json.length()) break;
                
                char c = json.charAt(pos);
                if (c == ']') {
                    pos++;
                    break;
                } else if (c == ',') {
                    pos++;
                }
            }
            
            return arr;
        }

        private Object parseValue() {
            skipWhitespace();
            if (pos >= json.length()) return null;
            
            char c = json.charAt(pos);
            
            if (c == '{') {
                return parseObject();
            } else if (c == '[') {
                return parseArray();
            } else if (c == '"') {
                return parseString();
            } else if (c == 't' || c == 'f') {
                return parseBoolean();
            } else if (c == 'n') {
                return parseNull();
            } else if (c == '-' || Character.isDigit(c)) {
                return parseNumber();
            }
            
            return null;
        }

        private String parseString() {
            skipWhitespace();
            if (pos >= json.length() || json.charAt(pos) != '"') {
                return null;
            }
            pos++; // skip opening quote
            
            StringBuilder sb = new StringBuilder();
            while (pos < json.length()) {
                char c = json.charAt(pos);
                if (c == '"') {
                    pos++;
                    return sb.toString();
                } else if (c == '\\' && pos + 1 < json.length()) {
                    pos++;
                    char next = json.charAt(pos);
                    switch (next) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        default: sb.append(next);
                    }
                    pos++;
                } else {
                    sb.append(c);
                    pos++;
                }
            }
            
            return sb.toString();
        }

        private Number parseNumber() {
            skipWhitespace();
            int start = pos;
            
            if (pos < json.length() && json.charAt(pos) == '-') {
                pos++;
            }
            
            while (pos < json.length() && Character.isDigit(json.charAt(pos))) {
                pos++;
            }
            
            boolean isFloat = false;
            if (pos < json.length() && json.charAt(pos) == '.') {
                isFloat = true;
                pos++;
                while (pos < json.length() && Character.isDigit(json.charAt(pos))) {
                    pos++;
                }
            }
            
            if (pos < json.length() && (json.charAt(pos) == 'e' || json.charAt(pos) == 'E')) {
                isFloat = true;
                pos++;
                if (pos < json.length() && (json.charAt(pos) == '+' || json.charAt(pos) == '-')) {
                    pos++;
                }
                while (pos < json.length() && Character.isDigit(json.charAt(pos))) {
                    pos++;
                }
            }
            
            String numStr = json.substring(start, pos);
            try {
                if (isFloat) {
                    return Double.parseDouble(numStr);
                } else {
                    return Long.parseLong(numStr);
                }
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        private Boolean parseBoolean() {
            if (json.regionMatches(pos, "true", 0, 4)) {
                pos += 4;
                return true;
            } else if (json.regionMatches(pos, "false", 0, 5)) {
                pos += 5;
                return false;
            }
            return null;
        }

        private Object parseNull() {
            if (json.regionMatches(pos, "null", 0, 4)) {
                pos += 4;
                return null;
            }
            return null;
        }
    }

    public static class JsonObject {
        private final java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();

        public void put(String key, Object value) {
            map.put(key, value);
        }

        public Object get(String key) {
            return map.get(key);
        }

        public String getString(String key) {
            Object v = map.get(key);
            return v instanceof String ? (String) v : null;
        }

        public int getInt(String key, int defaultValue) {
            Object v = map.get(key);
            if (v instanceof Number) {
                return ((Number) v).intValue();
            }
            return defaultValue;
        }

        public float getFloat(String key, float defaultValue) {
            Object v = map.get(key);
            if (v instanceof Number) {
                return ((Number) v).floatValue();
            }
            return defaultValue;
        }

        public JsonObject getObject(String key) {
            Object v = map.get(key);
            return v instanceof JsonObject ? (JsonObject) v : null;
        }

        public JsonArray getArray(String key) {
            Object v = map.get(key);
            return v instanceof JsonArray ? (JsonArray) v : null;
        }

        public float[] getFloatArray(String key) {
            JsonArray arr = getArray(key);
            if (arr == null) return null;
            float[] result = new float[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                Object v = arr.get(i);
                if (v instanceof Number) {
                    result[i] = ((Number) v).floatValue();
                }
            }
            return result;
        }
    }

    public static class JsonArray {
        private final List<Object> list = new ArrayList<>();

        public void add(Object value) {
            list.add(value);
        }

        public Object get(int index) {
            return index >= 0 && index < list.size() ? list.get(index) : null;
        }

        public int size() {
            return list.size();
        }

        public JsonObject getObject(int index) {
            Object v = get(index);
            return v instanceof JsonObject ? (JsonObject) v : null;
        }

        public int[] getIntArray(int index) {
            Object v = get(index);
            if (v instanceof JsonArray) {
                JsonArray arr = (JsonArray) v;
                int[] result = new int[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    Object item = arr.get(i);
                    if (item instanceof Number) {
                        result[i] = ((Number) item).intValue();
                    }
                }
                return result;
            }
            return null;
        }
    }
}
