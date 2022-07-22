#version 150

const float multiConstant = 1.000;


in vec3 Position;
in vec2 UV;

uniform mat4 modelViewMat;
uniform mat4 ProjMat;
uniform vec3 normal;
uniform float iTime;
uniform float Ratio;

out vec3 worldPosition;
out vec2 uv;

float rand(float x){
    return fract(sin(x)*100000.0);
}


float noise_offset(float samplerValue){
    float i = floor(samplerValue  * multiConstant);  // 整数（i 代表 integer）
    float f = fract(samplerValue * multiConstant);  // 小数（f 代表 fraction）
    return (mix(rand(i), rand(i + 1.), smoothstep(0.,1.,f))) *2. - 1.;
}

void main() {
//    vec3 offset = normalize(normal) * noise_offset(uv.x * Ratio * 160 + iTime) / 2.;
    gl_Position = ProjMat * modelViewMat * vec4(Position, 1.0);
    uv = UV;
}
