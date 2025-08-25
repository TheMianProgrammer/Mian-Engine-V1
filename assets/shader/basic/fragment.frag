#version 330 core

uniform float sunPower;
uniform float fSpecular;
uniform float exposure;
uniform vec3 viewSource;
uniform vec3 SunPos;
uniform vec2 shadowMapSize;

uniform sampler2D texture1;
uniform sampler2D shadowMap;

in vec3 fNormal;        
in vec3 fragPos;        
in vec2 fTexCoord;
in vec4 FragPosLightSpace;

out vec4 FragColor;

float ShadowCalculation(vec4 fragPosLightSpace, vec3 normal, vec3 lightDir)
{
    // homogene -> NDC -> [0,1]
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;

    // outside of shadow map -> no shadow
    if(projCoords.x < 0.0 || projCoords.x > 1.0 ||
       projCoords.y < 0.0 || projCoords.y > 1.0)
        return 0.0;

    float currentDepth = projCoords.z;

    // bias depending on angle between normal and light (reduces acne)
    float bias = max(0.005 * (1.0 - dot(normal, lightDir)), 0.0005);

    // PCF (3x3)
    float shadow = 0.0;
    vec2 texelSize = 1.0 / shadowMapSize;
    for(int x = -1; x <= 1; ++x) {
        for(int y = -1; y <= 1; ++y) {
            float pcfDepth = texture(shadowMap, projCoords.xy + vec2(x, y) * texelSize).r;
            if(currentDepth - bias > pcfDepth) shadow += 1.0;
        }
    }
    shadow /= 9.0;

    return shadow;
}

void main() {
    vec3 normal = normalize(fNormal);
    vec3 lightDir = normalize(SunPos - fragPos);
    float shadow = ShadowCalculation(FragPosLightSpace, normal, lightDir);

    vec3 albedo = vec3(texture(texture1, fTexCoord));

    vec3 ambient = 0.1 * albedo;

    float diff = max(dot(normal, lightDir), 0.0);
    vec3 diffuse = diff * albedo * sunPower;

    vec3 viewDir = normalize(viewSource - fragPos);
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), fSpecular);
    vec3 specular = spec * vec3(1.0);

    vec3 hdrColor = ambient + diffuse + specular;
    vec3 mapped = vec3(1.0) - exp(-hdrColor * exposure);
    mapped = pow(mapped, vec3(1.0/2.2));

    vec3 shadowMapped = mapped * (1.0 - shadow);

    FragColor = vec4(shadowMapped, 1.0);
}
