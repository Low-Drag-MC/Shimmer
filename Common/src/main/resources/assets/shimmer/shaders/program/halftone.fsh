#version 150

// "deadly_halftones, KilaBash"
// by Julien Vergnaud @duvengar-2018
// License Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
//////////////////////////////////////////////////////////////////////////////////////

uniform sampler2D DiffuseSampler;
uniform sampler2D Background;
uniform vec2 OutSize;
uniform float iTime;

in vec2 texCoord;

out vec4 fragColor;

float rem(vec2 iR) {
    float slices = floor(iR.y / 320.);
    if(slices < 1.){
        return 4.;
    }
    else if(slices == 1.){
        return 6.;
    }
    else if(slices == 2.){
        return 8.;
    }
    else if(slices >= 3.){
        return 10.;
    }
    else if(slices >= 4.){
        return 12.;
    }
    return 0.;
}

/////////////////////////////////////////////
// hash2 taken from Dave Hoskins
// https://www.shadertoy.com/view/4djSRW
/////////////////////////////////////////////

float hash2(vec2 p)
{
    vec3 p3  = fract(vec3(p.xyx) * .2831);
    p3 += dot(p3, p3.yzx + 19.19);
    return fract((p3.x + p3.y) * p3.z);
}

/////////////////////////////////////////////
//                 NOISE 3D
// 3D noise and fbm function by Inigo Quilez
/////////////////////////////////////////////

mat3 m = mat3( .00,  .80,  .60,
-.80,  .36, -.48,
-.60, -.48,  .64 );

float hash( float n )
{
    float h =  fract(sin(n) * 4121.15393);

    return  h + .444;
}

float noise( in vec3 x )
{
    vec3 p = floor(x);
    vec3 f = fract(x);

    f = f * f * (3.0 - 2.0 * f );

    float n = p.x + p.y * 157.0 + 113.0 * p.z;

    return mix(mix(mix( hash(n + 00.00), hash(n + 1.000), f.x),
    mix( hash(n + 157.0), hash(n + 158.0), f.x), f.y),
    mix(mix( hash(n + 113.0), hash(n + 114.0), f.x),
    mix( hash(n + 270.0), hash(n + 271.0), f.x), f.y), f.z);
}

void main(){
    if (length(texture(DiffuseSampler, texCoord).rgb) < 0.001) {
        fragColor = vec4( texture(Background, texCoord).rgb, 1.0 );
        return;
    }
    // glitch offset
    vec2 V  = 1. - 2. * texCoord;
    vec2 U = texCoord * OutSize;
    float _remp = rem(OutSize);

    // colorize
    float r = texture(DiffuseSampler,  texCoord).x;
    float g = texture(DiffuseSampler,  texCoord).x;
    float b = texture(DiffuseSampler,  texCoord).x;
    fragColor = vec4(.0,.1,.1,1.);

    fragColor += .06 * hash2(iTime + V * vec2(1462.439, 297.185));  // animated grain (hash2 function in common tab)
    fragColor += vec4(r, g, b, 1.);

    U = mod(U, _remp);
    fragColor *= .4+sign(smoothstep(.99, 1., U.y));
    fragColor *= 0.8;
}
