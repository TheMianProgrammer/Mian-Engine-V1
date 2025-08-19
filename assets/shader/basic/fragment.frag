#version 330 core
uniform float time;
uniform float sunPower;
uniform float fSpecular;
uniform vec2 res;
uniform vec3 viewSource; // camera position in world space
uniform vec3 SunPos;

uniform sampler2D texture1;

in vec3 fColor;
in vec3 fNormal;        // normal per vertex
in vec3 fragPos;        // fragment position im world space
in vec2 fTexCoord;
in float exposure;

out vec4 FragColor;

void main() {
    // Normale und Lichtvektor
    vec3 normal = normalize(fNormal);
    vec3 lightDir = normalize(SunPos - fragPos);

    // Ambient
    vec3 ambient = 0.4F * vec3(texture(texture1, fTexCoord));

    // Diffuse
    float diff = max(dot(normal, lightDir), 0.0);
    vec3 diffuse = diff * vec3(texture(texture1, fTexCoord)) * (0.01F * sunPower);

    // Specular
    vec3 viewDir = normalize(viewSource - fragPos);
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), fSpecular);
    vec3 specular = spec * vec3(1.0); // white highlight

    // Combine
    vec3 color = (ambient + diffuse + specular) * exposure;

    // simple tonemapping
    color = color / (color + vec3(1.0));

    FragColor = vec4(color, 1.0);
}
