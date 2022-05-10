#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D HighLight;
uniform sampler2D BlurTexture1;
uniform sampler2D BlurTexture2;
uniform sampler2D BlurTexture3;
uniform sampler2D BlurTexture4;
uniform float BloomRadius;
uniform float BloomIntensive;
uniform float BloomBase;
uniform float BloomThresholdUp;
uniform float BloomThresholdDown;

in vec2 texCoord;
out vec4 fragColor;

float lerpBloomFactor(const in float factor) {
    float mirrorFactor = 1.2 - factor;
    return mix(factor, mirrorFactor, BloomRadius);
}

void main() {
    vec4 bloom = BloomIntensive * (lerpBloomFactor(1.) * texture(BlurTexture1, texCoord) +
    lerpBloomFactor(0.8) * texture(BlurTexture2, texCoord) +
    lerpBloomFactor(0.6) * texture(BlurTexture3, texCoord) +
    lerpBloomFactor(0.4) * texture(BlurTexture4, texCoord));

    vec4 background = texture(DiffuseSampler, texCoord);
    vec4 highLight = texture(HighLight, texCoord);
    background.rgb = background.rgb * (1 - highLight.a) + highLight.a * highLight.rgb;
    float max = max(background.b, max(background.r, background.g));
    float min = min(background.b, min(background.r, background.g));
    fragColor = vec4(background.rgb + bloom.rgb * ((1. - (max + min) / 2.) * (BloomThresholdUp - BloomThresholdDown) + BloomThresholdDown + BloomBase), 1.);
}