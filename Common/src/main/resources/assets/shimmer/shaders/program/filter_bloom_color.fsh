#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D MainSampler;

in vec2 texCoord;

out vec4 fragColor;

void main(){
    fragColor = texture(DiffuseSampler, texCoord);
    if(texture(MainSampler, texCoord) != fragColor){
        fragColor = vec4(0.0);
    }
}
