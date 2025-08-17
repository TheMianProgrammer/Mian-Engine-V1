#version 330 core
uniform float time;
uniform vec2 res;
uniform vec3 viewSource; // camera position in world space

in vec3 fColor;
in vec3 fNormal;        // normal per vertex
in vec3 fragPos;        // fragment position im world space
in float exposure;

out vec4 FragColor;

void main() {
    // Normale und Lichtvektor
    vec3 normal = normalize(fNormal);
    vec3 lightPos = vec3(5, 5, 5); // CPU-Lichtposition
    vec3 lightDir = normalize(lightPos - fragPos);

    // Ambient
    vec3 ambient = 0.1 * fColor;

    // Diffuse
    float diff = max(dot(normal, lightDir), 0.0);
    vec3 diffuse = diff * fColor;

    // Specular
    vec3 viewDir = normalize(viewSource - fragPos);
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);
    vec3 specular = spec * vec3(1.0); // white highlight

    // Combine
    vec3 color = (ambient + diffuse + specular) * exposure;

    FragColor = vec4(color, 1.0);
}
