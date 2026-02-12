package sunneo.sdlmm.babylon3d;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mesh class for Babylon3D engine
 */
public class Mesh {
    public String name;
    public Vertex[] Vertices;
    public Face[] faces;
    public int faceCount;
    public int verticesCount;
    public Vector3 Rotation;
    public Vector3 Position;
    public Texture texture;

    public Mesh(String name, int verticesCount, int facesCount) {
        this.name = name;
        this.verticesCount = verticesCount;
        this.faceCount = facesCount;
        this.Vertices = new Vertex[verticesCount];
        this.faces = new Face[facesCount];
        
        for (int i = 0; i < verticesCount; i++) {
            Vertices[i] = new Vertex();
        }
        for (int i = 0; i < facesCount; i++) {
            faces[i] = new Face();
        }
        
        this.Rotation = Vector3.zero();
        this.Position = Vector3.zero();
        this.texture = new Texture();
    }

    /**
     * Load mesh from OBJ file
     */
    public static Mesh loadObj(String filename) {
        List<Vector3> positions = new ArrayList<>();
        List<Vector3> normals = new ArrayList<>();
        List<Vector3> texCoords = new ArrayList<>();
        List<int[]> faceData = new ArrayList<>(); // Each face: [v1, vt1, vn1, v2, vt2, vn2, v3, vt3, vn3]

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");
                if (parts.length == 0) continue;

                switch (parts[0]) {
                    case "v":
                        if (parts.length >= 4) {
                            positions.add(new Vector3(
                                Float.parseFloat(parts[1]),
                                Float.parseFloat(parts[2]),
                                Float.parseFloat(parts[3])
                            ));
                        }
                        break;
                    case "vn":
                        if (parts.length >= 4) {
                            normals.add(new Vector3(
                                Float.parseFloat(parts[1]),
                                Float.parseFloat(parts[2]),
                                Float.parseFloat(parts[3])
                            ));
                        }
                        break;
                    case "vt":
                        if (parts.length >= 3) {
                            texCoords.add(new Vector3(
                                Float.parseFloat(parts[1]),
                                Float.parseFloat(parts[2]),
                                0
                            ));
                        }
                        break;
                    case "f":
                        if (parts.length >= 4) {
                            int[] face = new int[9];
                            for (int i = 0; i < 3; i++) {
                                String[] indices = parts[i + 1].split("/");
                                face[i * 3] = Integer.parseInt(indices[0]) - 1; // vertex index
                                face[i * 3 + 1] = indices.length > 1 && !indices[1].isEmpty() 
                                    ? Integer.parseInt(indices[1]) - 1 : -1; // texture index
                                face[i * 3 + 2] = indices.length > 2 
                                    ? Integer.parseInt(indices[2]) - 1 : -1; // normal index
                            }
                            faceData.add(face);
                        }
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading OBJ file: " + filename + " - " + e.getMessage());
            return null;
        }

        if (positions.isEmpty() || faceData.isEmpty()) {
            System.err.println("Invalid OBJ file: " + filename);
            return null;
        }

        // Create mesh with unique vertices per face vertex
        int vertexCount = faceData.size() * 3;
        Mesh mesh = new Mesh("OBJ_" + filename, vertexCount, faceData.size());

        int vertexIndex = 0;
        for (int i = 0; i < faceData.size(); i++) {
            int[] face = faceData.get(i);
            
            for (int j = 0; j < 3; j++) {
                int posIdx = face[j * 3];
                int texIdx = face[j * 3 + 1];
                int normIdx = face[j * 3 + 2];

                Vertex v = mesh.Vertices[vertexIndex];
                v.Coordinates = positions.get(posIdx).copy();
                
                if (normIdx >= 0 && normIdx < normals.size()) {
                    v.Normal = normals.get(normIdx).copy();
                } else {
                    v.Normal = new Vector3(0, 1, 0);
                }
                
                if (texIdx >= 0 && texIdx < texCoords.size()) {
                    v.TextureCoordinates = texCoords.get(texIdx).copy();
                } else {
                    v.TextureCoordinates = Vector3.zero();
                }
                
                v.WorldCoordinates = Vector3.zero();
                
                if (j == 0) mesh.faces[i].A = vertexIndex;
                else if (j == 1) mesh.faces[i].B = vertexIndex;
                else mesh.faces[i].C = vertexIndex;
                
                vertexIndex++;
            }
        }

        return mesh;
    }

    /**
     * Create a simple cube mesh
     */
    public static Mesh createCube() {
        Mesh cubeMesh = new Mesh("Cube", 24, 12);

        // Front face (Z = 1)
        cubeMesh.Vertices[0].Coordinates = new Vector3(-1, 1, 1);
        cubeMesh.Vertices[1].Coordinates = new Vector3(1, 1, 1);
        cubeMesh.Vertices[2].Coordinates = new Vector3(-1, -1, 1);
        cubeMesh.Vertices[3].Coordinates = new Vector3(1, -1, 1);

        // Back face (Z = -1)
        cubeMesh.Vertices[4].Coordinates = new Vector3(1, 1, -1);
        cubeMesh.Vertices[5].Coordinates = new Vector3(-1, 1, -1);
        cubeMesh.Vertices[6].Coordinates = new Vector3(1, -1, -1);
        cubeMesh.Vertices[7].Coordinates = new Vector3(-1, -1, -1);

        // Top face (Y = 1)
        cubeMesh.Vertices[8].Coordinates = new Vector3(-1, 1, -1);
        cubeMesh.Vertices[9].Coordinates = new Vector3(1, 1, -1);
        cubeMesh.Vertices[10].Coordinates = new Vector3(-1, 1, 1);
        cubeMesh.Vertices[11].Coordinates = new Vector3(1, 1, 1);

        // Bottom face (Y = -1)
        cubeMesh.Vertices[12].Coordinates = new Vector3(-1, -1, 1);
        cubeMesh.Vertices[13].Coordinates = new Vector3(1, -1, 1);
        cubeMesh.Vertices[14].Coordinates = new Vector3(-1, -1, -1);
        cubeMesh.Vertices[15].Coordinates = new Vector3(1, -1, -1);

        // Left face (X = -1)
        cubeMesh.Vertices[16].Coordinates = new Vector3(-1, 1, -1);
        cubeMesh.Vertices[17].Coordinates = new Vector3(-1, 1, 1);
        cubeMesh.Vertices[18].Coordinates = new Vector3(-1, -1, -1);
        cubeMesh.Vertices[19].Coordinates = new Vector3(-1, -1, 1);

        // Right face (X = 1)
        cubeMesh.Vertices[20].Coordinates = new Vector3(1, 1, 1);
        cubeMesh.Vertices[21].Coordinates = new Vector3(1, 1, -1);
        cubeMesh.Vertices[22].Coordinates = new Vector3(1, -1, 1);
        cubeMesh.Vertices[23].Coordinates = new Vector3(1, -1, -1);

        // Set normals for each face
        for (int i = 0; i < 4; i++) cubeMesh.Vertices[i].Normal = new Vector3(0, 0, 1);
        for (int i = 4; i < 8; i++) cubeMesh.Vertices[i].Normal = new Vector3(0, 0, -1);
        for (int i = 8; i < 12; i++) cubeMesh.Vertices[i].Normal = new Vector3(0, 1, 0);
        for (int i = 12; i < 16; i++) cubeMesh.Vertices[i].Normal = new Vector3(0, -1, 0);
        for (int i = 16; i < 20; i++) cubeMesh.Vertices[i].Normal = new Vector3(-1, 0, 0);
        for (int i = 20; i < 24; i++) cubeMesh.Vertices[i].Normal = new Vector3(1, 0, 0);

        // Initialize WorldCoordinates
        for (int i = 0; i < 24; i++) {
            cubeMesh.Vertices[i].WorldCoordinates = Vector3.zero();
        }

        // Set texture coordinates
        for (int i = 0; i < 6; i++) {
            int base = i * 4;
            cubeMesh.Vertices[base + 0].TextureCoordinates = new Vector3(0, 0, 0);
            cubeMesh.Vertices[base + 1].TextureCoordinates = new Vector3(1, 0, 0);
            cubeMesh.Vertices[base + 2].TextureCoordinates = new Vector3(0, 1, 0);
            cubeMesh.Vertices[base + 3].TextureCoordinates = new Vector3(1, 1, 0);
        }

        // Define the 12 faces (2 triangles per side)
        cubeMesh.faces[0] = new Face(0, 1, 2);
        cubeMesh.faces[1] = new Face(1, 3, 2);
        cubeMesh.faces[2] = new Face(4, 5, 6);
        cubeMesh.faces[3] = new Face(5, 7, 6);
        cubeMesh.faces[4] = new Face(8, 9, 10);
        cubeMesh.faces[5] = new Face(9, 11, 10);
        cubeMesh.faces[6] = new Face(12, 13, 14);
        cubeMesh.faces[7] = new Face(13, 15, 14);
        cubeMesh.faces[8] = new Face(16, 17, 18);
        cubeMesh.faces[9] = new Face(17, 19, 18);
        cubeMesh.faces[10] = new Face(20, 21, 22);
        cubeMesh.faces[11] = new Face(21, 23, 22);

        // Set initial position
        cubeMesh.Position = new Vector3(0, 0, 10);
        cubeMesh.Rotation = Vector3.zero();

        return cubeMesh;
    }

    /**
     * Create a sphere mesh with specified subdivisions
     * @param radius Radius of the sphere
     * @param segments Number of horizontal segments (longitude)
     * @param rings Number of vertical rings (latitude)
     * @return Sphere mesh
     */
    public static Mesh createSphere(float radius, int segments, int rings) {
        // Calculate vertex and face counts
        int vertexCount = (rings + 1) * (segments + 1);
        int faceCount = rings * segments * 2;
        
        Mesh sphereMesh = new Mesh("Sphere", vertexCount, faceCount);
        
        int vertexIndex = 0;
        
        // Generate vertices
        for (int ring = 0; ring <= rings; ring++) {
            float phi = (float)(Math.PI * ring) / rings;
            float y = radius * (float)Math.cos(phi);
            float ringRadius = radius * (float)Math.sin(phi);
            
            for (int seg = 0; seg <= segments; seg++) {
                float theta = (float)(2.0 * Math.PI * seg) / segments;
                float x = ringRadius * (float)Math.cos(theta);
                float z = ringRadius * (float)Math.sin(theta);
                
                Vertex v = sphereMesh.Vertices[vertexIndex];
                v.Coordinates = new Vector3(x, y, z);
                
                // Normal is normalized position for a sphere centered at origin
                Vector3 normal = new Vector3(x, y, z);
                normal = normal.normalize();
                v.Normal = normal;
                
                // Texture coordinates
                float u = (float)seg / segments;
                float vCoord = (float)ring / rings;
                v.TextureCoordinates = new Vector3(u, vCoord, 0);
                
                v.WorldCoordinates = Vector3.zero();
                
                vertexIndex++;
            }
        }
        
        // Generate faces
        int faceIndex = 0;
        for (int ring = 0; ring < rings; ring++) {
            for (int seg = 0; seg < segments; seg++) {
                int current = ring * (segments + 1) + seg;
                int next = current + segments + 1;
                
                // First triangle
                sphereMesh.faces[faceIndex] = new Face(current, next, current + 1);
                faceIndex++;
                
                // Second triangle
                sphereMesh.faces[faceIndex] = new Face(current + 1, next, next + 1);
                faceIndex++;
            }
        }
        
        // Set initial position
        sphereMesh.Position = new Vector3(0, 0, 10);
        sphereMesh.Rotation = Vector3.zero();
        
        return sphereMesh;
    }

    /**
     * Create a sphere mesh with default subdivisions
     * @param radius Radius of the sphere
     * @return Sphere mesh with 16 segments and 16 rings
     */
    public static Mesh createSphere(float radius) {
        return createSphere(radius, 16, 16);
    }
}
