#version 330 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aColor;
layout(location = 2) in float aExposure;
layout(location = 3) in vec3 aNormal;

out vec3 fColor;
out vec4 v_normal;
out float exposure;
out vec3 fNormal;
out vec3 fragPos; // <-- neu

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main()
{
    vec4 worldPos = model * vec4(aPos, 1.0);
    gl_Position = projection * view * model * vec4(aPos, 1.0);
    
    fColor = aColor;
    exposure = aExposure;
    fNormal = aNormal;
    fragPos = worldPos.xyz; // <-- übergeben
    // v_normal = gl_
}
