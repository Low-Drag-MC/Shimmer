#version 150

/**
 * Dot screen shader
 * based on glfx.js sepia shader
 * https://github.com/evanw/glfx.js
 */

uniform sampler2D DiffuseSampler;
uniform sampler2D Background;
uniform vec2 OutSize;
uniform vec2 center;
uniform float angle;
uniform float scale;

in vec2 texCoord;

out vec4 fragColor;

float pattern() {
    float s = sin( angle ), c = cos( angle );
    vec2 tex = texCoord * OutSize - center;
    vec2 point = vec2( c * tex.x - s * tex.y, s * tex.x + c * tex.y ) * scale;
    return ( sin( point.x ) * sin( point.y ) ) * 4.0;
}

void main(){
    if (length(texture(DiffuseSampler, texCoord).rgb) < 0.001) {
        fragColor = vec4( texture(Background, texCoord).rgb, 1.0 );
        return;
    }

    vec4 color = texture( DiffuseSampler, texCoord );
    float average = ( color.r + color.g + color.b ) / 3.0;
    fragColor = vec4( vec3( average * 10.0 - 5.0 + pattern() ), 1. );

}
