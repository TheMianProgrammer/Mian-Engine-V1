#version 330 core

uniform float sunPower;      // Lichtintensität
uniform float fSpecular;     // Specular exponent
uniform float exposure;      // HDR exposure
uniform vec3 viewSource;     // Kamera-Position
uniform vec3 SunPos;         // Lichtposition

uniform sampler2D texture1;

in vec3 fNormal;        
in vec3 fragPos;        
in vec2 fTexCoord;

out vec4 FragColor;

void main() {
    vec3 normal = normalize(fNormal);
    vec3 albedo = vec3(texture(texture1, fTexCoord));

    // --- Lighting ---
    vec3 lightDir = normalize(SunPos - fragPos);

    // Ambient (immer minimal hell)
    vec3 ambient = 0.1 * albedo;

    // Diffuse
    float diff = max(dot(normal, lightDir), 0.0);
    vec3 diffuse = diff * albedo * sunPower;

    // Specular (Phong)
    vec3 viewDir = normalize(viewSource - fragPos);
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), fSpecular);
    vec3 specular = spec * vec3(1.0);

    // --- Combine HDR ---
    vec3 hdrColor = ambient + diffuse + specular;

    // --- Apply exposure ---
    vec3 mapped = vec3(1.0) - exp(-hdrColor * exposure);

    // Gamma correction
    mapped = pow(mapped, vec3(1.0/2.2));

    FragColor = vec4(mapped, 1.0);
}
