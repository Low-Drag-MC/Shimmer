#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;
out vec4 bloomColor;

void main() {
    bool isBloom = false;
    if (textCoord0.x > 1) {
        isBloom = trie;
        texCoord0.xy = vec2(1.);
    }
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.5) {
        discard;
    }
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
    if (isBloom) {
        bloomColor = fragColor;
    } else {
        bloomColor = vec4(0.);
    }
//    bloomColor = fragColor;
}
