#version 330 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aColor;
layout(location = 2) in vec3 aNormal;
layout(location = 3) in float aExposure;
layout(location = 4) in vec2 aTexCoord; 

out vec3 fColor;
out vec4 v_normal;
out float exposure;
out vec3 fNormal;
out vec3 fragPos; // <-- neu
out vec2 fTexCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform mat3 SunPos;

void main()
{
    vec4 worldPos = model * vec4(aPos, 1.0);
    gl_Position = projection * view * model * vec4(aPos, 1.0);
    
    fColor = aColor;
    fTexCoord = aTexCoord;
    exposure = aExposure;
    fNormal = aNormal;
    fragPos = worldPos.xyz; // <-- übergeben
    // v_normal = gl_
}
