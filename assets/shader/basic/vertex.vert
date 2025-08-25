#version 330 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aColor;
layout(location = 2) in vec3 aNormal;
layout(location = 3) in float aExposure;
layout(location = 4) in vec2 aTexCoord;

out vec3 fColor;
out float vExposure;
out vec3 fNormal;
out vec3 fragPos;
out vec2 fTexCoord;
out vec4 FragPosLightSpace;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform mat4 lightSpaceMatrix;   // FIX: war fälschlich vec4

void main()
{
    vec4 worldPos = model * vec4(aPos, 1.0);
    fragPos = worldPos.xyz;

    // richtige Normal-Transformation
    fNormal = mat3(transpose(inverse(model))) * aNormal;

    fTexCoord = aTexCoord;
    fColor = aColor;
    vExposure = aExposure;

    FragPosLightSpace = lightSpaceMatrix * worldPos;

    gl_Position = projection * view * worldPos;
}
