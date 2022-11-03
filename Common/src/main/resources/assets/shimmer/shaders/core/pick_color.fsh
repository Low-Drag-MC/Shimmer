#version 430

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform vec2 pickColorPosition = vec2(0.5, 0.5);
uniform vec2 ScreenSize;

layout(std430, binding = 0) buffer pickColorBuffer
{
    vec3 color;
};

void main(){
    fragColor = texture(DiffuseSampler, texCoord);
    if (distance(texCoord * ScreenSize, pickColorPosition * ScreenSize) <= 1.5){
        color = fragColor.rgb;
    }
}
